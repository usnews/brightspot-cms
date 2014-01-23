---
layout: default
title: Development Installation
id: devInstallation
section: documentation
---

<div markdown="1" class="span12">


Brightspot CMS is built on top of the [Dari Framework](http://dariframework.org). This install will create your new Brightspot CMS application, setting up a local instance with a local SQL and Solr database.

Installing Brightspot CMS requires five main steps. This installation will walk through these in more detail.

- **Step 1.** Create Database
- **Step 2.** Install Application Server
- **Step 3.** Install Solr Database
- **Step 4.** Create Brightspot CMS project using Maven
- **Step 5.** Run project in Application Server

This installation uses the latest stable release, Brightspot 2.2.

## Create Database

**MySQL 5.6.x**

If you don't already have MySQL [download](http://dev.mysql.com/downloads/mysql) and [install](http://dev.mysql.com/doc/refman/5.6/en/installing.html).

Start MySQL on the default port 3306 and create a new database. *Note, if you are using Brightspot 2.1 or earlier, you will need to load the [Dari Database Schema file](https://github.com/perfectsense/dari/tree/master/db/src/main/resources/mysql) into your newly created database.*


Note: Brightspot CMS (Dari) also supports PostgreSQL, Oracle and MongoDB. The schema files for these databases can be found in the Dari Github repo, [here](https://github.com/perfectsense/dari/tree/master/db/src/main/resources)


## Create Application Server

**Tomcat 7.x**

[Download](http://tomcat.apache.org/download-70.cgi) Tomcat and unzip to a directory now referred to as $TOMCAT_HOME. 

Note: For Windows users install the generic Tomcat version, NOT the Windows Version. This is required for the on-the-fly code compilation (Reloader Tool) to function correctly.

**Glassfish**

Brightspot CMS (Dari) can also be run on Glassfish.

## Install Solr Database

**Tomcat Solr Install**

[Download](http://archive.apache.org/dist/lucene/solr/) Solr 3.6.2 and unzip to a directory now referred to as $SOLR_DIST.

Copy solr.war into the Tomcat webapps directory:

<div class="highlight">{% highlight java %}
cp $SOLR_DIST/example/webapps/solr.war $TOMCAT_HOME/webapps
{% endhighlight %}</div>

Copy Solr's DB directory into $TOMCAT_HOME:

<div class="highlight">{% highlight java %}
cp -r $SOLR_DIST/example/solr $TOMCAT_HOME
{% endhighlight %}</div>

Replace the default Solr `schema.xml` and `solrconfig.xml` files with Dari configurations downloaded from the Dari repository - [here](https://github.com/perfectsense/dari/tree/master/etc/solr) - *Note, Make sure the filenames remain `solrconfig.xml` and `schema.xml`*

<div class="highlight">{% highlight java %}
cp solrconfig.xml $TOMCAT_HOME/solr/conf/solrconfig.xml
{% endhighlight %}</div>

<div class="highlight">{% highlight java %}
cp schema.xml $TOMCAT_HOME/solr/conf/schema.xml
{% endhighlight %}</div>

Place the [MySQL Connector jar](http://dev.mysql.com/downloads/connector/j/) file in the Tomcat `lib` directory. 

<div class="highlight">{% highlight java %}
cp mysql-connector-java-5.1.18-bin.jar $TOMCAT_HOME/lib
{% endhighlight %}</div>

Create a media directory on the Tomcat server where Brightspot can store uploaded files:

<div class="highlight">{% highlight java %}
mkdir $TOMCAT_HOME/webapps/media
{% endhighlight %}</div>

## Configure context.xml

This is an example `context.xml` file. Replace the default file found in $TOMCAT_HOME/conf/context.xml. 

<div id="input" contenteditable>
<div class="highlight">{% highlight java %}
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

<Resource name="dari/database/$DB_NAME/delegate/sql/dataSource" auth="Container" driverClassName="com.mysql.jdbc.Driver" logAbandoned="true" maxActive="100" maxIdle="30" maxWait="10000" type="javax.sql.DataSource" removeAbandoned="true" removeAbandonedTimeout="60" username="$DB_USER" password="$DB_PASS" url="jdbc:mysql://localhost:3306/$DB_NAME" testOnBorrow="true" validationQuery="SELECT 1"/>

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
{% endhighlight %}</div></div>

The new file will need to be configured. Replace the values outlined below with your own. Note, the snippet above is editable.

`$DB_NAME` - the name of the MySQL database you created.

`$DB_USER` - the username to login to your MySQL database

`$DB_PASS` - the password to login to your MySQL database

`$TOMCAT_HOME` - The directory where tomcat is installed.

`$TOMCAT_PORT` - The port at which tomcat will run. Default is 8080. You can find this value in $TOMCAT_HOME/conf/server.xml by looking for: "&lt;Connector port="

    
## Maven

OS X comes with Maven 3 built in. Run `mvn -version` to see your Maven Version number. If you do not have Maven, [download](http://maven.apache.org/download.html) and install. You will need to create your Brightspot project using Maven. *Note, the latest stable release of Brightspot is 2.2. You can, however, use the 2.3-SNAPSHOT in your archetype. This will build against the latest version of Brightspot*

Run the following Archetype to create the project structure. 

<div class="highlight">{% highlight java %}
mvn archetype:generate -B \
   -DarchetypeRepository=http://public.psddev.com/maven \
   -DarchetypeGroupId=com.psddev \
   -DarchetypeArtifactId=cms-app-archetype \
   -DarchetypeVersion=2.2-SNAPSHOT \
   -DgroupId=yourGroupID \
   -DartifactId=yourProject
{% endhighlight %}</div> 
    	
*Note, the GroupID and Project name must not contain spaces or hyphens. The path to the directory must also not container any spaces*

Once your project has been created access your new pom.xml and add the following dependency, for Solr.

<div class="highlight">{% highlight xml %}
<dependency>
    <groupId>org.apache.solr</groupId>
    <artifactId>solr-solrj</artifactId>
    <version>3.6.2</version>
</dependency>
{% endhighlight %}</div>

## Embed CMS

Once your required folder structure is in place run a `mvn clean install` within  `yourProject` folder. *You can confirm you are in the correct location if you can see your pom.xml file.*

A war file will now be created in the `target` directory. The CMS application will be embedded into your project.

Next step is to copy your new war file to `$TOMCAT_HOME/webapps` - rename this to be `ROOT.war`. *Note, the default Apache ROOT directory must be removed.*


## Start Tomcat

`./bin/startup.sh`

If you are seeing the following error - `The BASEDIR environment variable is not defined correctly. This environment variable is needed to run this program` you will need to remove restrictions on the permissions of your /bin/*.sh files.

From $TOMCAT_HOME run the following command `chmod +x bin/*.sh`


## Login

Once installed, and assuming the default port is being used, you can access the CMS application at: <http://localhost:8080/cms>

![](http://docs.brightspot.s3.amazonaws.com/login.png)

You will be brought to the default login screen (See above). First time access is unrestricted. Enter any username and password to login. This username and password will automatically be saved in the CMS, with a Tool User account created. (Admin -> Users & Roles)

This automatic user creation can be turned off. 

To do this simply access `$TOMCAT_HOME/conf/context.xml` and change `value="true"` to `value="false"`

<div class="highlight">{% highlight java %}
<Environment name="cms/tool/isAutoCreateUser" override="false" type="java.lang.Boolean" value="false" />
{% endhighlight %}</div>

The default project contains an `index.jsp` file within your webapp directory, remove this once you have confirmed install to begin your first project.

#### Java Heap Size

You may need to configure the memory allocation for Tomcat. If you see errors regarding Java Heap Size add the following to your catalina.sh file, found at $TOMCAT_HOME/bin/catalina.sh. This can be added directly above the # OS specific support config.

    # ----- Adding more Memory
    CATALINA_OPTS="-Xmx1024m -XX:MaxPermSize=256M -Djava.awt.headless=true "