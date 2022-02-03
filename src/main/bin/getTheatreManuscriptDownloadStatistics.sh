#!/usr/bin/env bash

#Run this as
# ./getMetadataStatistics.sh [~/tomcat/logs/pdf-service-downloads.log] [http://localhost:8211/pdf-service] [output.csv]
SCRIPT_DIR=$(dirname "$(readlink -f -- ${BASH_SOURCE[0]})")

jq --version > /dev/null || {
   >&2 echo "This script requires jq ( https://stedolan.github.io/jq/ ) to function. Please install and rerun the script"
   exit 2;
}
curl --version > /dev/null || {
   >&2 echo "This script requires curl to function. Please install and rerun the script"
   exit 2;
}



#The log file to parse
LOGFILE=${1:-~/tomcat/logs/pdf-service-downloads.log}

#What server to use
export SERVER=${2:-http://localhost:8211/pdf-service}

#Where to output. Default to stdout
export OUTPUT=${3:-/dev/stdout}

#These columns should be excluded from the output
excludeColumns='alternativeTitle,keywords,publicationDate,size,udgavebetegnelse,volume'
exclusions=$(echo "del(.[].${excludeColumns})" |  sed 's/,/)|del(.[]./g')

#Temp file to collect errors for the run
export errorLog=$(mktemp --suffix=.log)
trap '{ rm -f -- "$errorLog"; }' EXIT


#As I cannot add comments inside this pipe, they go here
# cat + cut: cat the log file and remove anything but the filename. To adapt this to an Apache Log, simply change this step
# sort + uniq: Sort and count unique files
# xargs: For each unique file, query the server to get the metadata
# grep+sed: Fix the downloadcount and filename in the json output
# grep apronType: Restrict to only list downloads of files with apronType C (teatermanuskripts under copyright)
# jq -s: Parse json lines to json array
# jq exclusions: Remove unnessesary fields from each record
# jq -r: Magic to turn json array into CSV
cat "$LOGFILE" | \
  cut -d' ' -f10- | \
  sort  | \
  uniq -c | \
  sort -h  | \
  xargs -r -n2 bash -c \
    'curl --fail --silent --show-error -w "£,\"downloads\":$0, \"file\":\"$1\"}\n" "'"$SERVER"'/api/getPdfMetadata/$1" 2>>"'$errorLog'" || echo -e "curl failed for $1\n" >> "'"$errorLog"'"'  | \
  grep -v '^£' |\
  sed 's/}£//'  | \
  grep '"apronType":"C"' |\
  jq -s '.' - | \
  jq "$exclusions" | \
  jq -r '(map(keys) | add | unique) as $cols | map(. as $row | $cols | map($row[.])) as $rows | $cols, $rows[] | @csv' - > "$OUTPUT"

if [[ -s "$errorLog" ]]; then
  >&2 echo ""
  >&2 echo "Encountered problems with some files, which means that they are excluded from the output:"
  >&2 echo ""
  >&2 cat "$errorLog"
  >&2 echo "This is most likely because the barcodes no longer exist in ALMA."
  >&2 echo "While this script only output download statistics for Teater Manuscripts, it cannot determine which files are teater manuscripts without being able to look up the files in ALMA."
  >&2 echo "So while the errors most likely will not affect the final output, you will have to investigate it yourself."
  >&2 echo ""
  >&2 echo "If the barcode of a record was changed (in ALMA) follow this procedure to fix the error"
  >&2 echo "1. Find the original pdf file and open it to read the title and author directly"
  >&2 echo "2. In ALMA, search for this title and author to find the new Bib record"
  >&2 echo "3. From the Bib record in ALMA, find the new barcode"
  >&2 echo "4. In the input logfile ('$LOGFILE'), perform search/replace to change all references to the old barcode to the new barcode"
  >&2 echo "5. Run this script again on the updated logfile. The error should now be gone"
  exit 1
fi
