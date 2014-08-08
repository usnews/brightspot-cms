---
layout: default
title: Upgrading Brightspot
id: upgrading-brightspot
section: documentation
---

<div markdown="1" class="span12">

Brightspot CMS and the Dari Framework are open source projects. The latest code can be found on GitHub. [Brightspot](http://github.com/perfectsense/brightspot-cms) and [Dari](http://github.com/perfectsense/dari) repositories.

The latest version of Brightspot is always developed via a SNAPSHOT build, allowing anyone to build against it using the public Maven download. To build against a stable version of Brightspot update the application pom file to reference a stable build version. Replacee the dependencies in the application pom file.

    <dependency>
         <groupId>com.psddev</groupId>
         <artifactId>cms-db</artifactId>
         <version>2.X</version>
    </dependency>

If moving from an older version of Brightspot and Dari to a newer version, various upgrade steps are typically required. This document outlines the process for upgrading Brightspot and Dari, as well as documenting known changes needed between specific versions.

If issues arise during an upgrade use the [issue tracker](https://github.com/perfectsense/brightspot-cms/issues) within GitHub.


### Upgrade Application

To upgrade Brightspot to a newer version start by changing the vesions of the dependencies found within the application pom file. Replace the Parent and the artifact versions for Dari and the CMS. The latest version of Brightspot is built as a SNAPSHOT. Determine the latest using the public Maven site, found [here](http://nexus.public.psddev.com/content/repositories/public/com/psddev/cms/) or the GitHub Repo. Stable releases can also be used, and are also located within the public Maven repo. SNAPSHOT builds will be updated regularly with new features, and running `mvn clean install` will inherit these changes automatically as the application builds.

	<parent>
        <groupId>com.psddev</groupId>
        <artifactId>dari-parent</artifactId>
        <version>2.X-SNAPSHOT</version>
    </parent>

        <dependency>
            <groupId>com.psddev</groupId>
            <artifactId>cms-db</artifactId>
            <version>2.X-SNAPSHOT</version>
        </dependency>

        <dependency>
            <groupId>com.psddev</groupId>
            <artifactId>cms-tool-ui</artifactId>
            <version>2.X-SNAPSHOT</version>
            <type>war</type>
        </dependency>

        <dependency>
            <groupId>com.psddev</groupId>
            <artifactId>dari-db</artifactId>
            <version>2.X-SNAPSHOT</version>
        </dependency>

        <dependency>
            <groupId>com.psddev</groupId>
            <artifactId>dari-util</artifactId>
            <version>2.X-SNAPSHOT</version>
        </dependency>

        <dependency>
            <groupId>com.psddev</groupId>
            <artifactId>dari-reloader-tomcat6</artifactId>
            <version>2.X-SNAPSHOT</version>
            <type>war</type>
        </dependency>


Once the application pom file has been updated, run `mvn clean install` to build against the newest version of Brightspot and Dari.

<div class="alert alert-block">
    <strong>Note</strong>
    <p>As well as the pom.xml, the web.xml in the application is sometimes updated. Check the specific version upgrade notes, to determine if this is the case.
    </p>
</div>


### Debugging Upgrade

If there are issues with the application upgrading, the maven build will fail, and throw errors. Brightspot and Dari deprecate, rather than remove methods between versions. Any issues that arise through the build process will need to be resolved before the upgraded war file can be created.


**2.3 Upgrade App FAQ**

To see deprecated classes, methods or interfaces for 2.3, as well as relacements, view the [deprecated list](http://www.brightspotcms.com/javadocs/deprecated-list.html) using the 2.3 JavDocs

The web.xml file for 2.3 changed from past versions. Use the below structure for the filters, adding any existing custom filters where noted.

    <?xml version="1.0" encoding="UTF-8"?>
    <web-app
	        xmlns="http://java.sun.com/xml/ns/javaee"
	        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	        xsi:schemaLocation="http://java.sun.com/xml/ns/javaee
	        http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd"
	        version="2.5">
	
	    <display-name>com.psddev: projectName</display-name>
	
	    <filter>
	        <filter-name>ApplicationFilter</filter-name>
	        <filter-class>com.psddev.dari.db.ApplicationFilter</filter-class>
	    </filter>
	    <filter-mapping>
	        <filter-name>ApplicationFilter</filter-name>
	        <url-pattern>/*</url-pattern>
	        <dispatcher>ERROR</dispatcher>
	        <dispatcher>FORWARD</dispatcher>
	        <dispatcher>INCLUDE</dispatcher>
	        <dispatcher>REQUEST</dispatcher>
	    </filter-mapping>
	
	    <!-- BEGIN CUSTOM FILTERS -->
	
	    <!-- END CUSTOM FILTERS -->
	
	    <filter>
	        <filter-name>PageFilter</filter-name>
	        <filter-class>com.psddev.cms.db.PageFilter</filter-class>
	    </filter>
	    <filter-mapping>
	        <filter-name>PageFilter</filter-name>
	        <url-pattern>/*</url-pattern>
	        <dispatcher>ERROR</dispatcher>
	        <dispatcher>FORWARD</dispatcher>
	        <dispatcher>INCLUDE</dispatcher>
	        <dispatcher>REQUEST</dispatcher>
	    </filter-mapping>
  
    </web-app>


### Upgrading Stack

An upgrade to the latest Brightspot and Dari version often depends on a newer version of the software in use. This can include Java, Tomcat, MySQL, Solr or Maven.

| Brightspot Version  | MySQL | Solr  | Apache | Java | Tomcat
| :------------- :    |:-----:|:-----:| :-----:|:----:|:-----:|
| 2.0                 |    5.6| 3.6.0 |  2.2   | 6    | 6     |
| 2.1                 |    5.6| 3.6.2 |  2.2   | 7    | 7     |
| 2.2                 |    5.6| 3.6.2 |  2.2   | 7    | 7     |
| 2.3                 |    5.6| 3.6.2 |  2.2   | 7    | 7     |
| 2.4                 |    5.6| 3.6.2 |  2.2   | 7    | 7 (8) |


### Upgrading Databases


**Brightspot 2.4 Database Upgrade Details**

When upgrading to Brightspot/Dari 2.4, start by checking the database schema that is currently used on the application being upgraded. This can be done by accesing the Dari SQL tool `/_debug/db-sql`, and running `show tables;` 

The schema files used in Dari are located in the GitHub repo, found [here](https://github.com/perfectsense/dari/tree/master/etc/mysql/changes).

Version 2.4 uses Schema 12.

When upgrading from an earlier schema, use the upgrade sql files to change the schema. Use 10 - 11 and then 11 - 12 for example.

In version 2.3 of Dari a new table `RecordRegion` was added. This added the ability for geographic regions to be indexed. When upgrading to a version of Brightspot that requires `RecordRegion` run the change sql file found in the Dari GitHub [repo](https://github.com/perfectsense/dari/tree/master/etc/mysql/changes).


**Brightspot 2.3 Database Upgrade Details**

When upgrading to Brightspot/Dari 2.3, start by checking the database schema that is currently used on the application being upgraded. This can be done by accesing the Dari SQL tool `/_debug/db-sql`, and running `show tables;` 

The schema files used in Dari are located in the GitHub repo, found [here](https://github.com/perfectsense/dari/tree/master/etc/mysql/changes).

Version 2.3 uses Schema 12.

When upgrading from an earlier schema, use the upgrade sql files to change the schema. Use 10 - 11 and then 11 - 12 for example.

In version 2.3 of Dari a new table `RecordRegion` was added. This added the ability for geographic regions to be indexed. When upgrading to a version of Brightspot that requires `RecordRegion` run the change sql file found in the Dari GitHub [repo](https://github.com/perfectsense/dari/tree/master/etc/mysql/changes).

**Brightspot 2.2 Database Upgrade Details**

When upgrading to Brightspot/Dari 2.2, start by checking the database schema that is currently used on the application being upgraded. This can be done by accesing the Dari SQL tool `/_debug/db-sql`, and running `show tables;` 

The schema files used in Dari are located in the GitHub repo, found [here](https://github.com/perfectsense/dari/tree/master/etc/mysql/changes).

Version 2.2 uses Schema 11.

When upgrading from an earlier schema, use the upgrade sql files to change the schema. Use 9 - 11 for example.

In version 2.1 of Dari an update was made to `RecordString2` to allow lower cases. It became `RecordString3`
