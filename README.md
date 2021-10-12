# pdf-service

<http://localhost:8080/pdf-service/api/getPdf?barcode=130022786122>

<http://localhost:8080/pdf-service/api/getPdf?barcode=130018854342>

<http://localhost:8080/pdf-service/api/getPdf?barcode=130018852943>

<http://localhost:8080/pdf-service/api/getPdf?barcode=115808025307>

**Webservice to produce a pdf-file from an existing pdf-fil with added dynamic content.**

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
