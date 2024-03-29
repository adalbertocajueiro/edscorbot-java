# Getting Started
This repository contains the Web API for manipulating register, authentication, users and trajectories of the EDScorbot project. The service is implemented as a Spring Boot/Java microservice. 

### Tools Information
This project has been developed using the following tools:
* Java version 18.0.1.1 (Oracle JDK)
* Apache Maven version 3.8.5
* Spring Boot version 3.0.5
* H2 Database version 2.1.214
* Visual Studio Code version 1.77.1 with extensions: Extension Pack for Java and Maven for Java.  

### Project structure
* After downloading and extracting the project, the folder `edscorbot-java` is the root folder of a Spring Boot project with maven, wich have a specific folder/files structure
* We have disabled the automatic generation of the API and use the file `src/main/resources/static/escorbot_service_webapi.yaml` as the openapi documentation/specification of the microservice.
* [The synchronous API](https://app.swaggerhub.com/apis-docs/ADALBERTOCAJUEIRO_1/ed-scorbot-service_api/1.0.0) is available at SwaggerHub.

### Installing and running
* You can try to use your own versions of Java and Maven. If it does not work we suggest you to install the above versions
* Download de project and unzip it
* Open the project in Visual Studio Code (vscode). It might be possible vscode offers other extensions to be installed. Just accept it.
* Open the file application.properties `src/main/resources/application.properties` and check/adjust the values.
* Pay attention on the database file name. It is associated to the property `spring.datasource.url`. Please adjust its value to point to a file in your file system. In our example, the value is `jdbc:h2:file:~/data/edscorbot` where `~/data/edscorbot` is the database file. In this case the folder `~/data` must exist. If the file `edscorbot` does not exist, it will be created at startup.
* Run the class EdscorbotApplication.java and the server must start. Make sure that port informed in `application.properties` is not in use. This step is more user friendly if executed from the vscode editor, as it presents options for Run and Debug above the `main` method.
* Open the URL http://localhost:PORT in your browser and enjoy the swagger interface. Will can use any browser to see the swagger interface.
* You will be able to test routes only with browsers like Insomnia and Postman, that allow to select the HTTP method. You can test the route `/api/signup` to register a new user or the routes `/api/authenticate` with valid credentials to get the token to be used in all other routes.
* There is a super user (`root/edscorbot`). Use it carefully!
* Open the URL http://localhost:PORT/h2-console in your browser to access the database tool (click in `Test Connection` and afterwards in `Connect`) to access the database. You can also do some manipulation directly on the database. However, this is not suggested as the saved objects can cause side effects in the application if they do not have the suitable format (mainly trajectories).


### Reference Documentation
For further reference, please consider the following items:
* [EDScorbot Github Project](https://github.com/RTC-research-group/Py-EDScorbotTool) - the main Github project with details about the entire project and the low level code to control the robotic arm
* [EDScorbot Documentation](https://py-edscorbottool.readthedocs.io/en/latest/) - documentation about the entire project
* [The synchronous API specification](https://app.swaggerhub.com/apis-docs/ADALBERTOCAJUEIRO_1/ed-scorbot-service_api/1.0.0) followed by this microservice.
* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/3.0.2/maven-plugin/reference/html/)

### Guides
The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)

