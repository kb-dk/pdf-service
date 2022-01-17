


På https://sbprojects.statsbiblioteket.dk/display/ALMAWIKI/DOD+workflow+i+Alma
har vi denne beskrivelse


    Bogen sendes videre til digitalisering:
    Besked sendes til bruger med link til fil
    Bestillingen slettes i Alma uden at sende besked til låner
    
    Den nye elektroniske post oprettes med digital fil som electronic portfolio under electronic collection DOD (Digitaliseret udgave).
    
    Der indsættes følgende i den elektroniske post:
        999 $$a med indhold ‘DigiXXXXXX’
        999 $$a med kode EDOD
        506 0_ $f Unrestricted online access $2 star (open access indikator i Primo)
        595 $$s med MMS ID på den fysiske post
        856 felter med URL i $$u samt 'Link til elektronisk udgave' i $$z (pga Danbib eksport)

    Der indsættes følgende i den digitaliserede fysiske post:
        999 $$a med kode DOD
        595 $$a med MMS ID på den elektroniske post
        999 $$a digitaliseret

    Denne kode er i Primo normaliseret til at give klarteksten 'Materialet er digitaliseret - brug digital version' i det korte format:
