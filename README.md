# License

Copyright (C) 2016 IOLITE GmbH, All rights reserved.

See [LICENSE](LICENSE) for license details.

# Introduction

This readme gives an overview of how to get started with IOLITE development.

There are two main ways to extend IOLITE. You can implement:
1. Apps - software modules installed into IOLITE and receive access to the home
  environment via a set of APIs. In their nature IOLITE apps are very similar to
  smartphone apps. You implement them to provide end-users with a new use case /
  functionality in their smart environments.
2. Drivers - software modules responsible for communication with devices /
  sensors / services in the smart environment. Examples drivers are Philips hue
  driver, EIB/KNX driver, OpenWeatherMap driver. You implement drivers if you
  want IOLITE to communicate with a new device, protocol or webservice.

This project provides all resources necessary for app and driver development.
  
# Prerequisites

To work with IOLITE you will need:
* JDK 8.0 or higher version
* [Maven](https://maven.apache.org) build tool. Please note that user credentials needed to access the Maven repository will be provided separately.

# Resources of this package
This section lists all resources you need to work with IOLITE.

## IOLITE Binary
The IOLITE binary is the [iolite.jar](iolite.jar).

Before the binary is executed, please copy the `/.iolite` directory into your
<user-home> directory. The `/.iolite` directory provided in this ZIP holds example
drivers, an app and an example logging configuration file.

To run IOLITE, open a console and execute the following command:
`java -jar iolite.jar`

Please note that IOLITE starts a webserver on port 80 per default. On some
systems this may be impossible. A custom port number can be configured in the
`IOLITERuntime.xml`. More on this in FAQ below.

After the of start IOLITE, you can access the Home Control Center user interface
via a browser under the address:
`http://localhost/`

(if you reconfigure the port number, the address is `http://localhost:<port>/`)

Please note that the runtime does not have a default user. Upon the first run, you will
be prompted to create a new user account.

## Example Driver
The provided `/.iolite` folder contains an example driver that installs a few
virtual devices to IOLITE.

For a source code example of a driver, please check the `/.iolite/drivers/diagnostics-driver.jar`.  

## Example App (with source)
The provided `/.iolite` folder contains an example application under
`/.iolite/apps/example-app.jar`.

The JAR file **contains source files**, so it can be used as a coding example.

The app does not feature any particular functionality other than testing IOLITE
APIs. It can be installed via the HCC.
1. open the HCC user interface in your browser
2. Login
3. Go to _IOLITE Apps_ (bottom navigation bar)
4. Go to _Store_ (top navigation bar)
5. You should see _Example App_ element available. Click on the _arrow down_ icon to install the app.
6. Go to _Installed_ (top navigation bar)
7. Click on the _Example App_ icon to open its user interface. The user interface contains some developer logs from the APIs.

## Documents
This ZIP package contains several documents about IOLITE in the `/docs` folder:
* `IOLITE App Development Guide.pdf` - provides all necessary information about
  IOLITE app development. Check it for further details.
* `IOLITE Driver Development Guide.pdf` - provides all necessary information
  about IOLITE driver development. Check it for further details.
* `IOLITE Home Control Center User Guide.pdf` - overview of the Home Control
  Center user interface and features.
* `iolite-driver-api Profile Reference.pdf`- holds the reference of all basic
  property types and profiles defined by IOLITE. It describes the details of the device
  types and their properties. Check it to see what properties to use in apps / drivers
  and what their exepcted values are.

## Example Maven configuration
The `/maven/settings.xml` file shows the Maven repository configuration to use with IOLITE.

Please note that the `username` and `password` fields **need to be replaced** with the
configuration provided to you by the IOLITE team.

## Example App pom
The `pom.xml` is an example Maven configuration for an IOLITE App. You can use
it as a starting point for your `pom.xml`. Please make sure to fill out the
meta-data (e.g. artifactId) with information matching your app.

# FAQ

* How to change the port number of IOLITE web server?
    
    Open the `<user-home>/.iolite/IOLITERuntime.xml` file and change the value of the `http.port` property of the `<http>` service node (~line 140). You should also change the `announce.dnssd.port` property value of the `<discovery>` service node.
  
* How to reset IOLITE, e.g. if something goes wrong?
    
    Delete the `<user-home>/.iolite/FileStorageService` directory.
  
* How to change the logging configuration?
    
    Modify the `<user-home>/.iolite/logback.xml`.
  
* How to change IOLITE's time zone?
   
    Open the `<user-home>/.iolite/IOLITERuntime.xml` file and change the value of the `timezone.id.default` property of the `<settings>` service node (~line 21). Please note that when no default time zone is configured, the platform's default is used.