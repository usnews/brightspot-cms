---
layout: default
title: Release Control
id: release-control
section: documentation
---

<div markdown="1" class="span12">

As the Brightspot development team continues to build out new features on the platform, stable releases are available to run. 

Access the Brightspot repository here to begin [http://nexus.public.psddev.com/content/repositories/public/com/psddev/cms/](http://nexus.public.psddev.com/content/repositories/public/com/psddev/cms/).

The Snapshot versions are versions in continuous development so for a more stable version, use the most recent version number excluding the SNAPSHOT extension. 

![brightspot-version](http://docs.brightspot.s3.amazonaws.com/brightspot-version.png)

Click the version you would like to run. Copy the version number without the .pom extension and in your pom.xml file update the version number in the parent tag and all dependencies referencing Brightspot. 

![pom](http://docs.brightspot.s3.amazonaws.com/pom.png)

Now acess the Dari repository here to grab the stable release [http://nexus.public.psddev.com/content/repositories/public/com/psddev/dari/](http://nexus.public.psddev.com/content/repositories/public/com/psddev/dari/). 

Select the most recent version number excluding the SNAPSHOT extension and copy the version number in to Dari dependencies in your pom.xml file. 

Save the pom.xml file and run `mvn clean install`. Restart Tomcat and you should be running a stable release of Brightspot.  

</div>