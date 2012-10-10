---
layout: default
title: Installation
id: documentation
section: documentation
---

## Installation

Brightspot CMS is built on top of the [Dari Framework](http://dariframework.org). This install will create your new Brightspot CMS application.

Installing Brightspot CMS requires five main steps. We will walk through these in more detail below.

- Create Database
- Install Application Server
- Install Solr DB
- Use Maven to run archetype embed Brightspot CMS
- Start Application Server


### Create Database

**MySQL 5.5.x**

If you don't already have MySQL [download](http://dev.mysql.com/downloads/mysql) and [install](http://dev.mysql.com/doc/refman/5.5/en/installing.html).

Start MySQL on the default port 3306 and create a new database. Load the [Dari Database Schema file](https://github.com/perfectsense/dari/tree/master/etc/mysql) into your newly created database.

Note: Brightspot CMS (Dari) also supports PostgreSQL, Oracle and MongoDB. The schema files for these databases can be found in the Dari Github repo, [here](https://github.com/perfectsense/dari/tree/master/etc)


### Create Application Server

**Tomcat 6.x**

[Download](http://tomcat.apache.org/download-60.cgi) Tomcat and unzip to a directory now referred to as $TOMCAT_HOME. 

Note: For Windows users install the generic Tomcat version, NOT the Windows Version. This is required for the on-the-fly code compilation (Reloader Tool) to function correctly.

**Glassfish**

Brightspot CMS (Dari) can also be run on Glassfish.

### Install Solr Database

**Tomcat Solr Install**

[Download](http://www.apache.org/dyn/closer.cgi/lucene/solr) Solr 3.6.0 and unzip to a directory now referred to as $SOLR_DIST.

Copy solr.war into the Tomcat webapps directory:

`cp $SOLR_DIST/example/webapps/solr.war $TOMCAT_HOME/webapps`

Copy Solr's DB directory into $TOMCAT_HOME:

`cp -r $SOLR_DIST/example/solr $TOMCAT_HOME`

Replace the default Solr `schema.xml` and `solrconfig.xml` files with Dari configurations downloaded from the Dari repository - [here](https://github.com/perfectsense/dari/tree/master/etc/solr)

`cp solrconfig.xml $TOMCAT_HOME/solr/conf/solrconfig.xml`

`cp schema.xml $TOMCAT_HOME/solr/conf/schema.xml`

Place the [MySQL Connector jar](http://dev.mysql.com/downloads/connector/j/) file in the Tomcat `lib` directory. 

`cp mysql-connector-java-5.1.18-bin.jar $TOMCAT_HOME/lib`

Create a media directory on the Tomcat server where the CMS can store uploaded files:

`mkdir $TOMCAT_HOME/webapps/media`


### Configure context.xml

This is an example `context.xml` file. Replace the default file found in $TOMCAT_HOME/conf/context.xml. 

	<?xml version='1.0' encoding='utf-8'?>
	<Context allowLinking="true" crossContext="true">

	<WatchedResource>WEB-INF/web.xml</WatchedResource>

	<!-- CMS Settings -->
	<Environment name="cms/tool/isAutoCreateUser" override="true" type="java.lang.Boolean" value="true" />
	<Environment name="cms/fe/isJspErrorEmbedded" type="java.lang.Boolean" value="true" override="false" />
	
	<!-- Create Production Environment for CMS -->
	<!-- <Environment name="PRODUCTION" override="false" type="java.lang.Boolean" value="true" /> -->

	<!-- Add authentication for _debug tool -->
	<!-- <Environment name="dari/debugRealm" type="java.lang.String" value="DIRECT HOST NAME" override="false" />
	<Environment name="dari/debugUsername" type="java.lang.String" value="USERNAME" override="false" />
	<Environment name="dari/debugPassword" type="java.lang.String" value="PASSWORD" override="false" /> -->

	<Environment name="dari/defaultDatabase" type="java.lang.String" value="$DB_NAME" override="false" />
	<Environment name="dari/database/$DB_NAME/class" override="false" type="java.lang.String" value="com.psddev.dari.db.AggregateDatabase" />
	<Environment name="dari/database/$DB_NAME/defaultDelegate" override="false" type="java.lang.String" value="sql" />
	<Environment name="dari/database/$DB_NAME/delegate/sql/class" override="false" type="java.lang.String" value="com.psddev.dari.db.SqlDatabase" />

	<Resource name="dari/database/$DB_NAME/delegate/sql/dataSource" auth="Container" driverClassName="com.mysql.jdbc.Driver" logAbandoned="true" 	maxActive="100" maxIdle="30" maxWait="10000" type="javax.sql.DataSource" removeAbandoned="true" removeAbandonedTimeout="60" username="$DB_USER" 	password="$DB_PASS" url="jdbc:mysql://localhost:3306/$DB_NAME" testOnBorrow="true" validationQuery="SELECT 1"/>

	<Environment name="solr/home" override="false" type="java.lang.String" value="$TOMCAT_HOME/solr" />
	<Environment name="dari/database/$DB_NAME/delegate/solr/groups" override="false" type="java.lang.String" value="-* +cms.content.searchable" />
	<Environment name="dari/database/$DB_NAME/delegate/solr/class" override="false" type="java.lang.String" value="com.psddev.dari.db.SolrDatabase" />
	<Environment name="dari/database/$DB_NAME/delegate/solr/serverUrl" override="false" type="java.lang.String" value="http://localhost:$TOMCAT_PORT/solr" />

	<Environment name="dari/defaultStorage" type="java.lang.String" value="local" override="false" />
	<Environment name="dari/storage/local/class" override="false" type="java.lang.String" value="com.psddev.dari.util.LocalStorageItem" />
	<Environment name="dari/storage/local/rootPath" override="false" type="java.lang.String" value="$TOMCAT_HOME/webapps/media" />
	<Environment name="dari/storage/local/baseUrl" override="false" type="java.lang.String" value="http://localhost:$TOMCAT_PORT/media" />

	<Environment name="cookieSecret" type="java.lang.String" value="Deem2oenoot1Ree5veinu8" override="true" />

	</Context>


The new file will need to be configured. Replace the values outlined below with your own.

`$DB_NAME` - the name of the MySQL database you created.

`$DB_USER` - the username to login to your MySQL database

`$DB_PASS` - the password to login to your MySQL database

`$TOMCAT_HOME` - The directory where tomcat is installed.

`$TOMCAT_PORT` - The port at which tomcat will run. Default is 8080. You can find this value in $TOMCAT_HOME/conf/server.xml by looking for: "&lt;Connector port="
    
### Maven

[Download](http://maven.apache.org/download.html) and install Maven.

You will need to create a Maven project in which we will embed the CMS application. Find pom.xml and web.xml guidelines below.

**pom.xml** 

   Dari Parent
   
    <parent>
    <groupId>com.psddev</groupId>
    <artifactId>dari-parent</artifactId>
    <version>1.9-SNAPSHOT</version>
    </parent>


CMS Tool UI

     <plugin>
         <groupId>org.apache.maven.plugins</groupId>
         <artifactId>maven-war-plugin</artifactId>
            <configuration>
              <overlays>
                 <overlay>
                   <groupId>com.psddev</groupId>
                   <artifactId>cms-tool-ui</artifactId>
                   <targetPath>cms</targetPath>
                   <excludes/>
                 </overlay>
              </overlays>
            </configuration>
      </plugin>

  
Dari Utils and DB  
  

            
            <!-- Brightspot CMS -->
        <dependency>
            <groupId>com.psddev</groupId>
            <artifactId>dari-util</artifactId>
            <version>2.0-SNAPSHOT</version>
        </dependency>

        <dependency>
            <groupId>com.psddev</groupId>
            <artifactId>dari-db</artifactId>
            <version>2.0-SNAPSHOT</version>
        </dependency>

        <dependency>
            <groupId>com.psddev</groupId>
            <artifactId>cms-db</artifactId>
            <version>2.0-SNAPSHOT</version>
        </dependency>

        <dependency>
            <groupId>com.psddev</groupId>
            <artifactId>cms-tool-ui</artifactId>
            <version>2.0-SNAPSHOT</version>
            <type>war</type>
        </dependency>

Solr

        <dependency>
            <groupId>org.apache.solr</groupId>
            <artifactId>solr-solrj</artifactId>
            <version>3.6.0</version>
        </dependency>

Maven Repo
       
    <repositories>
        <repository>
            <id>public.psddev</id>
            <url>http://public.psddev.com/maven</url>
            <snapshots>
                <updatePolicy>always</updatePolicy>
            </snapshots>
        </repository>
    </repositories>

    <pluginRepositories>
        <pluginRepository>
            <id>public.psddev</id>
            <url>http://public.psddev.com/maven</url>
            <snapshots>
                <updatePolicy>always</updatePolicy>
            </snapshots>
        </pluginRepository>
    </pluginRepositories>



**web.xml** 


    <!-- Filters -->

    <filter>
        <filter-name>Utf8Filter</filter-name>
        <filter-class>com.psddev.dari.util.Utf8Filter</filter-class>
    </filter>
    <filter-mapping>
        <filter-name>Utf8Filter</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>

    <filter>
        <filter-name>HeaderResponseFilter</filter-name>
        <filter-class>com.psddev.dari.util.HeaderResponseFilter</filter-class>
    </filter>
    <filter-mapping>
        <filter-name>HeaderResponseFilter</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>

    <filter>
        <filter-name>LogCaptureFilter</filter-name>
        <filter-class>com.psddev.dari.util.LogCaptureFilter</filter-class>
    </filter>
    <filter-mapping>
        <filter-name>LogCaptureFilter</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>
    
    <filter>
        <filter-name>DebugFilter</filter-name>
        <filter-class>com.psddev.dari.util.DebugFilter</filter-class>
    </filter>
    <filter-mapping>
        <filter-name>DebugFilter</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>
  
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
    <filter>
        <filter-name>ToolFilter</filter-name>
        <filter-class>com.psddev.cms.tool.ToolFilter</filter-class>
    </filter>
    <filter-mapping>
        <filter-name>ToolFilter</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>
    </web-app>

### Create Project

MYARTIFACTID will be your project name and MYGROUP will be the directory in which your Java classes (objects) will be placed. 

Once your required folder structure is in place run a `mvn clean install` within your `MYARTIFACTID` folder. *You can confirm you are in the correct location if you can see your pom.xml file.*

A war file will now be created in the `target` directory. The CMS application will be embedded into your project: MYARTIFACTID -> target -> MYARTIFACTID -> CMS

Next step is to create a symbolic link pointing from your Tomcat `ROOT` to your project. This only needs to be created once. The contents found within the default Apache ROOT in Tomcat must be removed before creating this new link to ROOT. This symbolic link allows you to run your application locally with updates seen instantly on your local host.

`ln -s path/to/your/MYARTIFACTID/src/main/webapp path/to/your/tomcat/webapps/ROOT`

Note - on a Windows machine, create the Symbolic link with:

`mklink /d "C:\tomcat\webapps\ROOT" "C:\ProjectName\src\main\webapp"`

Once the symbolic link is in place run a `mvn war:inplace`.

`cd path/to/your/MYARTIFACTID/`

`mvn war:inplace`

### Start Tomcat

`./bin/startup.sh`

### Access your CMS

The first time that you access your CMS you will need to perform a `_debug/init`

<http://localhost:8080/_debug/init>

The function of the `_debug/init` is to update the CMS application that is embedded within your project.

<img src="http://docs.brightspot.s3.amazonaws.com/init.png"/>

### Login

Once installed, and assuming the default port is being used, you can access the CMS application at: <http://localhost:8080/cms>

![](http://docs.brightspot.s3.amazonaws.com/login.png)

You will be brought to the default login screen (See above). First time access is unrestricted. Enter any username and password to login. This username and password will automatically be saved in the CMS, with a Tool User account created. (Admin -> Users & Roles)

This automatic user creation can be turned off. 

To do this simply access `$TOMCAT_HOME/conf/context.xml` and change `value="true"` to `value="false"`

    <Environment name="cms/tool/isAutoCreateUser" override="false" type="java.lang.Boolean" value="false" />
