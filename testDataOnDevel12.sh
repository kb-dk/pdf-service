#!/usr/bin/env bash

SCRIPT_DIR=$(dirname "$(readlink -f -- ${BASH_SOURCE[0]})")

mkdir -p data
chmod 777 data
mkdir -p cache
mkdir -p temp

scp dodpdfsv@devel12.statsbiblioteket.dk:/data1/e-mat/dod/130024645461-color.pdf data
scp dodpdfsv@devel12.statsbiblioteket.dk:/data1/e-mat/dod/130008805998-color.pdf data
scp dodpdfsv@devel12.statsbiblioteket.dk:/data1/e-mat/dod/130023745268-color.pdf data
scp dodpdfsv@devel12.statsbiblioteket.dk:/data1/e-mat/dod/130007257539-color.pdf data
scp dodpdfsv@devel12.statsbiblioteket.dk:/data1/e-mat/dod/130022785800-color.pdf data
scp dodpdfsv@devel12.statsbiblioteket.dk:/data1/e-mat/dod/622264bf-9743-456b-8b73-52880cdac715_0001-color.pdf data

exit

scp -3 develro@webext-10.kb.dk:/data1/e-mat/dod/130023138892*.pdf dodpdfsv@devel12.statsbiblioteket.dk:data1/e-mat/dod/
scp -3 develro@webext-10.kb.dk:/data1/e-mat/dod/130023745268*.pdf dodpdfsv@devel12.statsbiblioteket.dk:data1/e-mat/dod/
scp -3 develro@webext-10.kb.dk:/data1/e-mat/dod/130007257539*.pdf dodpdfsv@devel12.statsbiblioteket.dk:data1/e-mat/dod/
scp -3 develro@webext-10.kb.dk:/data1/e-mat/dod/130022785800*.pdf dodpdfsv@devel12.statsbiblioteket.dk:data1/e-mat/dod/
