---
layout: default
title: FAQ
id: faq
---

## Frequently Asked Questions


### Creating a Production Environment

The following should be added to your context.xml file to create a Production Environment. This hides any front end errors.

    <Environment name="PRODUCTION" override="false" type="java.lang.Boolean" value="true" />

**Locking CMS Access**

The "isAutoCreateUser" parameter must be set to false to lock the CMS. It is, by default, true, so needs to be changed in the context.xml for a production environment.

    <Environment name="cms/tool/isAutoCreateUser" override="false" type="java.lang.Boolean" value="false" /> 

**Hiding .jsp errors**

    <Environment name="cms/db/isJspErrorEmbedded" type="java.lang.Boolean" value="true" override="false" />

**Adding Password to _debug and ?_debug=true**

There are three parameters that must be set in the Tomcat context.xml file to add a password to the Debug tool, so that it is not accessible in production.

    <Environment name="dari/debugRealm" type="java.lang.String" value="DIRECT HOST NAME" override="false" />
    <Environment name="dari/debugUsername" type="java.lang.String" value="USERNAME" override="false" />
    <Environment name="dari/debugPassword" type="java.lang.String" value="PASSWORD" override="false" />

### Solrj Version Error

`Invalid version (expected 2, but 1) or the data in not in 'javabin' format (java.lang.RuntimeException)`

The error above is due to a Solr version issue. Make sure that the version of Solr you have deployed matches that outlined in your `pom.xml`. To fix, define an explicit version of the solrj to match your Solr version.

Within your Maven Project `pom.xml` add/update the following:

        <dependency>
            <groupId>org.apache.solr</groupId>
            <artifactId>solr-solrj</artifactId>
            <version>3.6.0</version>
        </dependency>

Rebuild your war and deploy to see the fix.

### Build Errors

**Failed to execute goal org.codehaus.mojo**

	[ERROR] Failed to execute goal org.codehaus.mojo:exec-maven-plugin:1.2.1:java
	(default) on project petfinder: Execution default of goal
	org.codehaus.mojo:exec-maven-plugin 1.2.1:java failed: Plugin
	org.codehaus.mojo:exec-maven-plugin:1.2.1 or one of its dependencies could
	not be resolved
	
The build error above is produced when the following Plugin snippet is not included in your project pom.xml. Add and then rebuild to fix.
	
	 <pluginRepositories>
        <pluginRepository>
            <id>public.psddev</id>
            <url>http://public.psddev.com/maven</url>
            <snapshots>
                <updatePolicy>always</updatePolicy>
            </snapshots>
        </pluginRepository>
    </pluginRepositories>
    
    
### Reloader not working

The majority of changes made to classes are auto-compiled and a simple refresh of the CMS will update the objects, fields and UI.

For annotations added to objects, sometimes the reloader fails to detect the change. On these occasions, a manual reload can be triggered, by adding `?_reload=true` to the URL within Brightspot.

<img class="smaller" src="http://docs.brightspot.s3.amazonaws.com/reload_true.png" alt="" />

If after a CMS refresh the changes made are not showing perform the following steps:

1) Stop Tomcat - `bin/shutdown.sh`
2) Run mvn clean install
3) Start Tomcat - `bin/startup.sh`
4) Run Init - http://localhost:8080/_debug/init

Access the CMS after the reset to see changes update.

### Updating your Database

The following steps outline how to re-index your Solr database once a new SQL database has been added to your Brightspot application.

**Step 1 - Adding the Database**

If you are using MySQL perform a [SQL Dump](http://dev.mysql.com/doc/refman/5.1/en/mysqldump.html) and save as a `.sql` file. If this database was already being used in another Brightspot / Dari application it already contains the Dari Schema, therefore the most seamless method to changing the database is:

- Drop old database
- Create new with same name (This means your context.xml can remain unchanged)
- Source your SQL Dump for the new database.

**Step 2 - Re-indexing Solr Database**

Start your Tomcat and access `/_debug` - we are going to use the DB-Bulk tool to re-index the Solr DB.

Select from `SQL` and to `Solr` to index Solr using the new SQL DB. Check `Delete Before Copy`.

<img  src="http://docs.brightspot.s3.amazonaws.com/re-index-start.png"/>

A Task will now start automatically in the Task tool - click on the link now provided to see the progress

<img  src="http://docs.brightspot.s3.amazonaws.com/re-index-link.png"/>

You can refresh your Task tool until you see all items indexed.

<img  src="http://docs.brightspot.s3.amazonaws.com/re-index-task.png"/>