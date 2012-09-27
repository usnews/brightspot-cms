---
layout: default
title: Importing new Database
id: importing-new-db
---

## Importing a new Database

The following steps outline how to re-index your solr database once a new SQL database has been added to your BrightSpot application.

### Step 1 - Adding DB

If you are using MySQL perform a simple [SQL Dump](http://dev.mysql.com/doc/refman/5.1/en/mysqldump.html) and save as a `.sql` file. If this database was already being used in another BrightSpot / Dari application it already contains the Dari Schema, therefore the most seamless method to changing the database is:

<img  src="http://docs.brightspot.s3.amazonaws.com/import-db.png"/>

- Drop old database
- Create new with same name (This means your context.xml can remain unchanged)
- Source your SQL Dump for the new database.

### Step 2 - Re-indexing Solr DB

Start your Tomcat and access `/_debug` - we are going to use the DB-Bulk tool to re-index the Solr DB.

Select from `SQL` and to `Solr` to index Solr using the new SQL DB. Check `Delete Before Copy`.

<img  src="http://docs.brightspot.s3.amazonaws.com/re-index-start.png"/>

A Task will now start automatically in the Task tool - click on the link now provided to see the progress

<img  src="http://docs.brightspot.s3.amazonaws.com/re-index-link.png"/>

You can refresh your Task tool until you see all items indexed.

<img  src="http://docs.brightspot.s3.amazonaws.com/re-index-task.png"/>