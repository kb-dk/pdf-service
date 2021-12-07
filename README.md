# pdf-service

<http://localhost:8080/pdf-service/api/getPdf/130022786122.pdf>

<http://localhost:8080/pdf-service/api/getPdf/130018854342.pdf>

<http://localhost:8080/pdf-service/api/getPdf/130018852943.pdf>


Gamle filer: 
<http://www5.kb.dk/e-mat/dod/115808025291_bw.pdf>
<http://www5.kb.dk/e-mat/dod/115808025307_bw.pdf>

<http://localhost:8080/pdf-service/api/getPdf/115808025307_bw.pdf>



**Webservice to produce a pdf-file from an existing pdf-fil with added dynamic content.**




Men der er nogen problemer med katalogiseringen i ALMA
115808025307 er en stregkode for en fysisk bog
Denne bog er en del af bib post 99122905335805763
Men den elektroniske udgave kom fra bib post 995529632805761

Developed and maintained by the Royal Danish Library.

## Requirements

* Maven 3                                  
* Java 11

## Build & run

Build with
``` 
mvn package
```

Test the webservice with
```
mvn jetty:run
```

The default port is 8080 and the default Hello World service can be accessed at
<http://localhost:8080/pdf-service/api/hello>

The Swagger-UI is available at <http://localhost:8080/pdf-service/api/api-docs?url=openapi.json>
which is the location that <http://localhost:8080/pdf-service/api/> will redirect to.

See the file [DEVELOPER.md](DEVELOPER.md) for developer specific details and how to deploy to tomcat.
