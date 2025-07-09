# Developer documentation

This project is build from the [java webapp template](https://sbprojects.statsbiblioteket.dk/stash/projects/ARK/repos/pdf-service-template/browse)
from the Royal Danish Library.

The information in this document is aimed at developers that are not proficient in the java webapp template, Jetty, 
Tomcat deployment or OpenAPI.

## Initial use

### build fist
After a fresh checkout or after the `openapi.yaml` specification has changed, the `api` and the `model` files 
must be (re)generated. This is done by calling 
```
mvn package
```

### setup test data local

run

```
./setupJettyTestData.sh
```

It will create test folders in your user home dir and download examples pdf's

### add alma.apikey.pdfserviece to pdf-service-local.yaml

For some reason jetty dont use .m2/settings.xml profiles, so we have to hardcode it

the file is here "conf/pdf-service-local.yaml"

### startting Jetty
Jetty is a servlet container (like Tomcat) that is often used for testing during development.
Jetty is enabled, so testing the webservice can be done by running
Start a Jetty web server with the application:
```
mvn jetty:run
```

The Swagger-UI is available at <http://localhost:8080/pdf-service/api/api-docs?url=openapi.json>
which is the location that <http://localhost:8080/pdf-service/api/> will redirect to.

### running examples

just open these links in af browser

http://localhost:8080/pdf-service/api/getPdf/130007257539-color.pdf

http://localhost:8080/pdf-service/api/getPdf/130008805998-color.pdf

http://localhost:8080/pdf-service/api/getPdf/622264bf-9743-456b-8b73-52880cdac715_0001-color.pdf

## java webapp template structure

Configuration of the project is handled with [YAML](https://en.wikipedia.org/wiki/YAML). It is split into multiple parts:
 
 * `behaviour` which contains setup for thread pools, limits for arguments etc. This is controlled by the developers.
 * `environment` which contains server names, userIDs, passwords etc. This is controlled by operations.
 * `local` which contains temporary developer overrides. This is controlled by the individual developer.

During development the configurations are located in the `conf`-folder as `conf/pdf-service-behaviour.yaml`,  
`conf/pdf-service-environmant.yaml` and `conf/pdf-service-local.yaml`. 
When the project is started using Jetty, these are the configurations that are used.

Access to the configuration is through the static class at `src/main/java/dk.kb.pdfservice/config/ServiceConfig.java`.

**Note**: The environment configuration typically contains sensitive information. Do not put it in open code
repositories. To guard against this, `conf/pdf-service-environment.yaml` is added to `.gitignore`. 

## Jetty



This project can be started with `mvn jetty:run`, which will expose a webserver with the implemented service at port 8080.
If it is started in debug mode from an IDE (normally IntelliJ IDEA), breakpoints and all the usual debug functionality
will be available.

## Tomcat

Tomcat is the default servlet container for the Royal Danish Library and as deployment to Tomcat must be tested before
delivering the project to Operations. As of 2021, Tomcat 9 is used for Java 11 applications.

A [WAR](https://en.wikipedia.org/wiki/WAR_(file_format))-file is generated with `mvn package` and can be deployed
directly into Tomcat, although this will log to `catalina.out` and use the developer configuration YAML.

Deployment on a shared server or a developer machine is done by

* Creating and adjusting production specific copies of `conf/<application-ID>.yaml` and `conf/ocp/logback.xml`
  in the designated folder on the server. The folder will probably be `$HOME/services/conf`, 
  `$HOME/services/application-id/` or `$HOME/conf/`.
* Adjusting the the paths to `docBase`, `<application-ID>.yaml` and `logback.xml` in `conf/ocp/<application-ID>.xml`
  and symlinking (`ln -s`) the file `conf/ocp/<application-ID>.xml` to the Tomcat folder `conf/Catalina/localhost/`
                                  
Deployment on a production server at the Royal Danish Library is normally done by Operations and is normally quite
similar to the procedure for test- and developer-machines.

## A full web application

For smaller projects or standalone web applications, it can be useful to bundle the user interface with the API 
implementation: Files and folders added to the `src/main/webapp/` folder are served under 
[http://localhost:8080/<application-ID>/](http://localhost:8080/<application-ID>/).

While it is possible to use [JSP](https://en.wikipedia.org/wiki/Jakarta_Server_Pages), as the sample 
[index.jsp](./src/main/webapp/index.jsp) shows, this is considered legacy technology.
With an [API first](http://apievangelist.com/2020/03/09/what-is-api-first/)-approach, the web application
will typically be static files and JavaScript.

## OpenAPI 1.3 (aka Swagger)

[OpenAPI 1.3](https://swagger.io/specification/) generates interfaces and skeleton code for webservices.
It also generates online documentation, which includes sample calls and easy testing of the endpoints.

Everything is defined centrally in the file [src/main/openapi/openapi.yaml](src/main/openapi/openapi.yaml).
IntelliJ IDEA has a plugin for editing OpenAPI files that provides a semi-live preview of the generated GUI and
the online [Swagger Editor](https://editor.swagger.io/) can be used by copy-pasting the content of `openapi.yaml`.


The interfaces and models generated from the OpenAPI definition are stored in `target/generated-sources/`.
They are recreated on each `mvn package`.

Skeleton classes are added to `/src/main/java/${project.package}/api/impl/` but only if they are not already present. 
A reference to the classes must be added manually to `/src/main/java/${project.package}/webservice/Application` or its equivalent.

A common pattern during initial definition of the `openapi.yaml` is to delay implementation and recreate the skeleton
implementation files on each build. This can be done by setting `generateOperationBody` in the `pom.xml` to `true`.

**Tip:** If the `openapi.yaml` is changed a lot during later development of the application, it might be better to have 
`<generateOperationBody>true</generateOperationBody>` in `pom.xml` and add the implementation code to manually created
classed (initially copied from the OpenAPI-generated skeleton impl classes). When changes to `openapi.yaml` results in
changed skeleton implementation classes, the changes can be manually ported to the real implementation classes.

**Note:** The classes in `/src/main/java/${project.package}api/impl/` will be instantiated for each REST-call.
Persistence between calls must be handled as statics or outside of the classes.

### OpenAPI and exceptions

When an API end point shall return anything else than the default response (HTTP response code 200),
this is done by throwing an exception.

See how we map exceptions to responsecodes in [ServiceExceptionMapper](./src/main/java/dk/kb/webservice/ServiceExceptionMapper.java) 

See [ServiceException](./src/main/java/dk/kb/webservice/exception/ServiceException.java) and its specializations for samples.

### Mustache templates

The templates in [src/main/templates/](./src/main/templates/) overrides the default Swagger templates.
They are needed in order to provide functionality needed by the Royal Danish Library, e.g. delivering a streaming
response while announcing a well-defined structure in the Swagger UI.

Normally you do not need to touch the Mustache-files.

### Tests

The project contains "normal" unittest plus PdfTitlePageCleanerTest class. The latter depends on specific test files
being present at the local file system, why those tests have the @Disabled annotation.  
(At least) in case of problems with apron cleaning, copy the relevant documents from the server, and 
run those tests also.


## Release procedure

1. Review that the `version` in `pom.xml` is fitting. `pdf-service` uses
[Semantic Versioning](https://semver.org/spec/v2.0.0.html): The typical release
will bump the `MINOR` version and set `PATCH` to 0. Keep the `-SNAPSHOT`-part as
the Maven release plugin handles that detail.   
1. Ensure that [CHANGELOG.md](CHANGELOG.md) is up to date. `git log` is your friend. 
Ensure that the about-to-be-released version is noted in the changelog entry
1. Ensure all local changes are committed and pushed.
1. Ensure that your local `.m2/settings.xml` has a current `sbforge-nexus`-setup
(contact Kim Christensen @kb or another Maven-wrangler for help)
1. Follow the instructions on
[Guide to using the release plugin](https://maven.apache.org/guides/mini/guide-releasing.html)
which boils down to
   * Run `mvn clean release:prepare`
   * Check that everything went well, then run `mvn clean release:perform`
   * Run `git push`   
   If anything goes wrong during release, rollback and delete tags using something like
   `mvn release:rollback ; git tag -d pdf-service-1.4.2 ; git push --delete origin pdf-service-1.4.2`
