TODO lookup client from pdfs to author/title info





Generate the list of available barcodes for DOD
------------------------------------------------
```
[develro@webext-10 /data1/e-mat/dod]$ find -type f -name '*.pdf' | xargs -r -i basename {} .pdf | grep -o '^[^_-]\+' | sort -u > ~/dodBarcodes.list
```

Get this list locally
----------------------
```
scp develro@webext-10.kb.dk:dodBarcodes.list ~/DODMetadata/
```

Fetch ALMA metadata for each barcode
-------------------------------------
```
cat dodBarcodes.list  | ./getMarcData.sh 
```

where getMarcData.sh is

```
xargs -P4 -r -IBARCODE bash -c "curl \
	--header 'Authorization:apikey l8xx...b1c1' \
	--include \
	--no-progress-meter \
	'https://api-eu.hosted.exlibrisgroup.com/almaws/v1/items/?item_barcode=BARCODE' \
| grep '^Location:' \
| cut -d'/' -f5 \
| xargs -r -iMMSID curl \
	--header 'Authorization:apikey l8xx...b1c1' \
	--no-progress-meter \
	'https://api-eu.hosted.exlibrisgroup.com/almaws/v1/bibs/MMSID' \
| xmllint --format - > BARCODE.barcode "
```

Wait a while as the process will try to fetch the marc data for ALL the records. 





Extract the 999a, 997a and 260c fields from the marc21 contents
--------------------------------------------------------------
```
export FIELD=999
export CODE=a
find . -type f -name '*.barcode' ! -empty | xargs -r -i bash -c "echo -n '{} '; xmllint --xpath '/bib/record/datafield[@tag=\"$FIELD\"]/subfield[@code=\"$CODE\"]/text()' '{}' | sort -u | tr '\n' '\t'; echo ''"> "$FIELD$CODE.list"

export FIELD=997
export CODE=a
find . -type f -name '*.barcode' ! -empty | xargs -r -i bash -c "echo -n '{} '; xmllint --xpath '/bib/record/datafield[@tag=\"$FIELD\"]/subfield[@code=\"$CODE\"]/text()' '{}' | sort -u | tr '\n' '\t'; echo ''"> "$FIELD$CODE.list"

export FIELD=260
export CODE=c
find . -type f -name '*.barcode' ! -empty | xargs -r -i bash -c "echo -n '{} '; xmllint --xpath '/bib/record/datafield[@tag=\"$FIELD\"]/subfield[@code=\"$CODE\"]/text()' '{}' | sort -u | tr '\n' '\t'; echo ''"> "$FIELD$CODE.list"
```

Analysis
========================================


Records missing 260c
-----------------------

```
grep 'barcode\s$' 260c.list 
```


Records missing 997a
-----------------------

```
grep 'barcode\s$' 997a.list 
```


Records missing 999a
-----------------------

```
grep 'barcode\s$' 999a.list 
```


Records missing both 997a and 999a
-----------------------

```
grep 'barcode\s$' 999a.list  | xargs -r -i grep {} 997a.list | grep 'barcode\s$'
```



Records without 997a=MUS and missing 260c
-----------------------

```
grep 'barcode\s$' 260c.list | xargs -r -i grep {} 997a.list | grep -v MUS  
```


