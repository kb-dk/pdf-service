dod-pdf-service
==================

Tilhørende projekt <https://sbprojects.statsbiblioteket.dk/display/DK/Projektblanket+KULA-170>

Se mere om hvad dette er på
* <https://sbprojects.statsbiblioteket.dk/display/SSYS/Overdragelse+af+DOD+PDF+Service+til+applikationsdrift>



Andre relevante links
-----------------------

* Record Types <https://sbprojects.statsbiblioteket.dk/display/DK/Record+Typer>

* Test Eksempler <https://sbprojects.statsbiblioteket.dk/display/DK/Testeksempler>
    * This lokalt brug kan man hente originalerne (med samme navne) fra `https://www.kb.dk/e-mat/dod/` eller `develro@webext-10.kb.dk:/data1/e-mat/dod/`
    
* Marc21 felter og deres anvendelse <https://sbprojects.statsbiblioteket.dk/pages/viewpage.action?pageId=103877561>



Hvorfor:
--------

Ideen er at lave en service, der skal stå i stedet for <https://www.kb.dk/e-mat/dod/>

<https://www.kb.dk/e-mat/dod/> serverer filerne direkte som de ligger på disk.

Denne service, til modsætning, vil indsætte en licens-betinget 'forklæde'-side udfra de nyeste informationer om værket i ALMA bibliotekssystemet.

Den service jeg har lavet kan

1. Karakterisere værkets licens-regler og begrænsninger
2. Fjerne eventuelle eksisterende forklæde-sider
3. Indsætte en moderne og korrekt forklæde med copyright informationer
4. Indsætte en side-footer med en copyright advarsel, hvis påkrævet af licensen


Miljøer og brugere
------------------
Denne service er blevet tildelt portrange 8210-8219

Port anvendelser:
* tomcatShutdownPort: 8210
* tomcatHttpPort: 8211
* tomcatDebugPort: 8219

Devel:
* `dodpdfsv@devel12.statsbiblioteket.dk`
* <http://devel12.statsbiblioteket.dk:8211/pdf-service/api/getPdf/115808025307_bw.pdf>

Stage:
* `dodpdfsv@alma-ssys-stage01.statsbiblioteket.dk`
* <http://alma-ssys-stage01.statsbiblioteket.dk:8211/pdf-service/api/getPdf/115808025307_bw.pdf>

Prod:
* `dodpdfsv@alma-ssys-prod01.statsbiblioteket.dk`
* <http://alma-ssys-prod01.statsbiblioteket.dk:8211/pdf-service/api/getPdf/115808025307_bw.pdf>
* <https://www.kb.dk/e-mat/dod/115808025307_bw.pdf>



Anvendelse:
-----------
Den tager pdf på helt samme måde som e-mat, dvs.

<http://www5.kb.dk/e-mat/dod/115808025291_bw.pdf>
->
<http://localhost:8080/pdf-service/api/getPdf/115808025307_bw.pdf>

I stedet for at genskrive hele config filen her, og risikere at ændringer smutter, vil jeg bede jer læse `conf/pdf-service-behaviour.yaml`

#### fra TGC:

webext-10 er prod host for adgang til e-mat/dod. Al prod adgang til filerne fra www5 mm. ender så vidt jeg ved der (vha proxy), så den skal ikke peges andre steder hen. For at rokke så lidt med båden som muligt, så er min plan at webext-10 kommer til at køre din applikation i produktion da den allerede har adgang til filerne. Vi retter så eksisterende vhost på webext-10 til så /e-mat/dod går igennem din applikation i stedet for direkte til disk. Al anden traffik igennem webext-10 vil være uændret.


Behov:
------
Den benytter sig af tre ting

1. Den læser pdf filerne lokalt, så serveren skal have et mount hvor den kan læse
   `webext-10.kb.dk:/data1/e-mat/dod`
    ```yaml
    pdfService:
      #  Where original PDFs are read from
      PDFsource:
        - "/data1/e-mat/dod"
    ```

2. Omkring 1 GB RAM
 
3. Et temp storage til at unloade PDF filer fra memory (for at begrænse mængden af nødvendigt Heap Space).  
    ```yaml
    pdfService:
      temp:
        #  Where temporary files are stored when we unload them from memopry
        folder: "/home/dodpdfsv/temp/"
        #This is the maximum memory we will use for PDFs. If we need more memory, we will use temp files in the folder above
        # This allows us to open the 2+ GB pdf, without having 4+GB Memory just for this
        memoryForPDFs: 800 MB 
        # You can use KB, MB, GB, TB, PB, EB. You can use fractional numbers, like 4.5MB. Space between number and unit is optional
    ```

4. Et cache storage hvor den kan lægge producerede PDFer.
    ```yaml
    pdfService:
      cache:
        #  Where generated PDFs are stored
        cacheFolder: "/home/dodpdfsv/cache/"
        # If copyrighted PDF is older than this age, request ALMA data and create it again
        # This ensures that updates to ALMA will tage effect (after a while)
        # Note that if this age is SHORTER than the alma.cache_timeout, the cached ALMA record
        # will be used, which defeats the purpose....
        maxAgeOfCachedPdfs:
          value: 24
          unit: HOURS
          # unit can be one of NANOS, MICROS, MILLIS, SECONDS, MINUTES, HOURS, HALF_DAYS
    ```
    Bemærk, servicen vil IKKE selv slette filerne efter sig. Så sæt en Temp Watcher på denne mappe eller afsæt nok plads til hele DOD arkivet (2+ TB)

5. En ALMA api key med rettigheder til

   * <https://developers.exlibrisgroup.com/alma/apis/bibs/>
     * Readonly. Til at slå copyright informationer op for PDF filen

   * <https://developers.exlibrisgroup.com/alma/apis/conf/>
     * Readonly. Til at logge om den kører mod Sandbox eller Prod

    Denne service KAN fungere med ALMA sandbox

6. En Primo URL
    
    Servicen indsætter et link til Primo på forklædet. Dette bliver konfigureret udfra
    
    ```yaml
    pdfService:
    
     primo:
          #    host: "https://soeg.kbdk" # For production
          host: "https://kbdk-kgl-psb.primo.exlibrisgroup.com" #Important, do not have double / between host and path
          path: "/discovery/fulldisplay?docid=alma" #The mmsID is appended here
          postfix: "&context=U&vid=45KBDK_KGL:KGL&lang=da"
    ```

### Test Urls

Se <https://sbprojects.statsbiblioteket.dk/display/DK/Testeksempler>


