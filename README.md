# Getting Started
This repository contains the Web API for manipulating login, users and trajectories 
of the ED Scorbot project. The service is implemented in Spring Boot and is intented
to provide end-points to the fron-end, so that users can access the robotic arm as an intuitive service on the Web. 

### Tools Information
This project has been developed using the following tools:
* Java version 18.0.1.1
* Apache Maven version 3.8.5
* Spring Boot version 3.0.2
* Visual Studio Code version 1.74.3 with extensions: Extension Pack for Java and Maven for Java.  

### Install instructions
* You can try to use your own versions of Java and Maven. If it does not work we advice to install the above versions
* Download de project and unzip it
* Open the project in Visual Studio Code (vscode). It might be possible vscode offers other extensions to be installed. Just accept it.
* Run the class EdscorbotApplication.java and the server must start. Make sure that port 8080 is not in used. This step is more user friendly if executed from the vscode editor, as it presents options for Run and Debug above the `main` method.
* Open the URL http://localhost:8080 in your browser and enjoy the swagger interface 

### Project structure
* After downloading and extracting the project, the folder `edscorbot-java` is the root folder of a Spring Boot project with maven, wich have a specific folder/files structure
* We have disabled the automatic generation of the API and use the file `src/main/resources/static/escorbot_service_webapi.yaml` as the openapi documentation about the routes.


### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/3.0.2/maven-plugin/reference/html/)
* [Ed Scorbot Python] (https://github.com/RTC-research-group/Py-EDScorbotTool) - the Github projectcontaining the library (real implementation) of elementary/low level functions to access the robotic arm
* [Ed Scorbot Documentation] (https://py-edscorbottool.readthedocs.io/en/latest/) - the user documentation/guide of the ED Scorbot tools (GUI, command line and detailed configurations). 

### Guides
The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)

