#!/bin/bash

# This script create test folders to use when running pdf-service local
# and download test examples

USER="dodpdfsv"
EXTERNAL_PDF_FOLDER="devel12.statsbiblioteket.dk:/data1/e-mat/dod/"
LOCAL_TEST_FOLDER=$HOME/jetty-test/
LOCAL_PDF_FOLDER=$LOCAL_TEST_FOLDER"data"

echo "user: $USER"
echo "external url: $EXTERNAL_PDF_FOLDER"
echo "local dir: $LOCAL_PDF_FOLDER"

mkdir -p "$LOCAL_PDF_FOLDER"
mkdir -p "$LOCAL_TEST_FOLDER"/cache
mkdir -p "$LOCAL_TEST_FOLDER"/temp

scp $USER@$EXTERNAL_PDF_FOLDER/130024645461-color.pdf "$LOCAL_PDF_FOLDER"
scp $USER@$EXTERNAL_PDF_FOLDER/130008805998-color.pdf "$LOCAL_PDF_FOLDER"
scp $USER@$EXTERNAL_PDF_FOLDER/130023745268-color.pdf "$LOCAL_PDF_FOLDER"
scp $USER@$EXTERNAL_PDF_FOLDER/130007257539-color.pdf "$LOCAL_PDF_FOLDER"
scp $USER@$EXTERNAL_PDF_FOLDER/130022785800-color.pdf "$LOCAL_PDF_FOLDER"
scp $USER@$EXTERNAL_PDF_FOLDER/622264bf-9743-456b-8b73-52880cdac715_0001-color.pdf "$LOCAL_PDF_FOLDER"

exit


# scp dodpdfsv@devel12.statsbiblioteket.dk:/data1/e-mat/dod/130024645461-color.pdf  /home/XXX/jetty-test/data/