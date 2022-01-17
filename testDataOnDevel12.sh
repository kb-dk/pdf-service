#!/usr/bin/env bash

SCRIPT_DIR=$(dirname "$(readlink -f -- ${BASH_SOURCE[0]})")


scp develro@webext-10.kb.dk:/data1/e-mat/dod/130023138892*.pdf $SCRIPT_DIR/data/
scp develro@webext-10.kb.dk:/data1/e-mat/dod/130023745268*.pdf $SCRIPT_DIR/data/
scp develro@webext-10.kb.dk:/data1/e-mat/dod/130007257539*.pdf $SCRIPT_DIR/data/
scp develro@webext-10.kb.dk:/data1/e-mat/dod/130022785800*.pdf $SCRIPT_DIR/data/

exit

scp -3 develro@webext-10.kb.dk:/data1/e-mat/dod/130023138892*.pdf dodpdfsv@devel12.statsbiblioteket.dk:data1/e-mat/dod/
scp -3 develro@webext-10.kb.dk:/data1/e-mat/dod/130023745268*.pdf dodpdfsv@devel12.statsbiblioteket.dk:data1/e-mat/dod/
scp -3 develro@webext-10.kb.dk:/data1/e-mat/dod/130007257539*.pdf dodpdfsv@devel12.statsbiblioteket.dk:data1/e-mat/dod/
scp -3 develro@webext-10.kb.dk:/data1/e-mat/dod/130022785800*.pdf dodpdfsv@devel12.statsbiblioteket.dk:data1/e-mat/dod/
