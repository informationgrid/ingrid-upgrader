Upgrade Server
==============

This software is part of the InGrid software package. The upgrade server 

- scans a distribution directory for changed ingrid components
- delivers a RSS feed with information about all ingrid components

The RSS feed is the base for the Upgrade Client that is part of the InGrid Portals Administration GUI.


Requirements
-------------

- a running InGrid Software System

Installation
------------

Download from https://dev.informationgrid.eu/ingrid-distributions/ingrid-upgrader/
 
or

build from source with `mvn package assembly:single`.

Execute

```
java -jar ingrid-upgrader-x.x.x-installer.jar
```

and follow the install instructions.

Obtain further information at https://dev.informationgrid.eu/


Contribute
----------

- Issue Tracker: https://github.com/informationgrid/ingrid-upgrader/issues
- Source Code: https://github.com/informationgrid/ingrid-upgrader
 
### Set up eclipse project

```
mvn eclipse:eclipse
```

and import project into eclipse.


Support
-------

If you are having issues, please let us know: info@informationgrid.eu

License
-------

The project is licensed under the EUPL license.
