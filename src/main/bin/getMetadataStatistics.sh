#!/usr/bin/env bash

#Run this as
# ./getMetadataStatistics.sh [~/tomcat/logs/pdf-service-downloads.log] [http://localhost:8211/pdf-service/] [output.csv]
SCRIPT_DIR=$(dirname "$(readlink -f -- ${BASH_SOURCE[0]})")

LOGFILE=${1:-~/tomcat/logs/pdf-service-downloads.log}

export SERVER=${2:-http://localhost:8211/pdf-service}

export OUTPUT=${3:-/dev/stdout}

excludeColumns='alternativeTitle,keywords,primoLink,publicationDate,size,udgavebetegnelse,volume'
exclusions=$(echo "del(.[].${excludeColumns})" |  sed 's/,/)|del(.[]./g')


export errorLog=$(mktemp --suffix=.log)

cat "$LOGFILE" | \
  cut -d' ' -f10- | \
  sort  | \
  uniq -c | \
  sort -h  | \
  xargs -r -n2 bash -c \
    'curl --fail --silent --show-error -w "£,\"downloads\":$0, \"file\":\"$1\"}\n" "'"$SERVER"'/api/getPdfMetadata/$1" 2>>"'$errorLog'" || echo -e "curl failed for $1\n" >> "'"$errorLog"'"'  | \
  grep -v '^£' |\
  sed 's/}£//'  | \
  jq -s '.' - | \
  jq "$exclusions" | \
  jq -r '(map(keys) | add | unique) as $cols | map(. as $row | $cols | map($row[.])) as $rows | $cols, $rows[] | @csv' - > "$OUTPUT"

cat "$errorLog" > /dev/stderr
rm  "$errorLog"
