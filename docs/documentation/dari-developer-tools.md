---
layout: default
title: Dari Developer Tools
id: dari-developer-tools
---

## Dari Developer Tools


Brightspot CMS is built on top of the [Dari Framework](http://dariframework.org), an open source Java Framework also created by [Perfect Sense Digital](http://perfectsensedigital.com/products/dari). Full documentation for Dari is available [here](http://www.dariframework.org/documentation.html).

By leveraging Dari, a full set of developer tools are available within your Brightspot project. This section will look at each tool in detail. Please see the [Debugging Section](/debugging.html) for more information on the Contextual Debug Tool also available through Dari.

### Build Tool

The Build tool gives you access to the build history for your application, showing commits, and other information. You can configure external services, for example GitHub, Hudson and JIRA, allowing developers to reference bugs they have fixed, and code they have changed.

**Build Tool Configuration:**

Within your Maven project POM.xml file, add the following to configure the build tool:

    <issueManagement>
        <system>JIRA</system>
        <url>JIRA_URL_HERE</url>
    </issueManagement>

    <scm>
        <connection>scm:git:ssh://git@github.com/GITHUB_NAME_HERE/REPO_NAME_HERE.git</connection>
        <developerConnection>scm:git:ssh://git@github.com/GITHUB_NAME_HERE/REPO_NAME_HERE.git</developerConnection>
        <url>https://github.com/GITHUB_NAME_HERE/REPO_NAME_HERE</url>
    </scm>

### Code Tool

The Code Tool provides you with a playground leveraging the on-the-fly code compiling from Dari. Here you can see instant results for your Java code in your browser. 

Here you can run queries against your project data, for example below you can see returning an Author object, where all fields, and `getters` and `setters` are outlined.

<a href="javascript:;" ><img src="http://docs.brightspot.s3.amazonaws.com/code_tool_return.png"/></a>

An example of a query, here we find a Blog Post where the Author's first name is John.

<a href="javascript:;" ><img src="http://docs.brightspot.s3.amazonaws.com/code_tool_return_query.png"/></a>

As well as running queries, existing objects from your project can be accessed from the drop down and edited. When running locally, any changes saved will be made to your source.

<a href="javascript:;" ><img src="http://docs.brightspot.s3.amazonaws.com/code_tool_modify_objects.png"/></a>

You can also select the New Class option in the drop down to create an entirely new object. Here we are creating a new Category object.

<a href="javascript:;" ><img src="http://docs.brightspot.s3.amazonaws.com/code_tool_create_objects.png"/></a>

### DB-Bulk Tool

The DB-Bulk Tool is predominately used to re-index content. The `@Indexed` annotation is often added retrospectively, to already existing objects. In order to update content that you now want to index, you can use the db-bulk tool. You can re-index on a single type, or all types. The status of this process once started, can be seen in the Task Tool.

When importing an updated, or new database Solr will also need to be indexed, which can be done from here.

<a href="javascript:;" ><img src="http://docs.brightspot.s3.amazonaws.com/db-bulk-tool.png"/></a>

The Copy tool, also seen here, is used to copy data from one database to another. For example, SQL to Solr. New databases can be added in the `context.xml` configurations.

### Database Schema

A unique view of your data model is available through the Dari Schema tool. All content types within your project are listed. Simply select the type you want to view, or type to find from the list.

The schema outlines the model, showing all fields and associated content types. Here we can see our Blog object with associated Author and Category objects.

<a href="javascript:;"><img src="http://docs.brightspot.s3.amazonaws.com/db-schema-tool.png"/></a>

When working on your local machine the objects listed are click-able, opening the Code Tool directly so changes can be made.

### SQL / Solr Databases

The `db-solr` and `db-sql` tools provide direct access to the respective databases.

Direct queries against Solr indexing can be run from within the `db-solr` tool. Standard Solr syntax is used.

For the `db-sql` tool access to the SQL tables is provided, again, normal SQL syntax can be used.

<a href="javascript:;"><img src="http://docs.brightspot.s3.amazonaws.com/sql-query.png"/></a>

### Database Storage

CDN Storage for resources are referenced within this `db-storage` tool. It allows the bulk movement of `StorageItem` objects. If naming conventions dictate a new name is defined on the objects, relative to the new location, check the `Save Object` option. *Note: this is results in a slower process.*

<a href="javascript:;"><img src="http://docs.brightspot.s3.amazonaws.com/db-storage.png"/></a>

### Web Database

The web database feature allows data from other instances of Brightspot to be ingested quickly and easily. It does not require the object to exist in the instance being copied to. It is utilized from within the Code Tool.

From within the code tool:       

    WebDatabase web = new WebDatabase();
    web.setRemoteUrl(http://localhost:8080/_debug/db-web);
    return Query.from(Article.class).where.("title startsWith foo").using(web).select(0,10);

Moving objects from one database to the other:

    WebDatabase web = new WebDatabase();
    web.setRemoteUrl("http://your.site.com/_debug/db-web");
    Record record = Query.from(Article.class).where("_id = xxxxxx-xxxx-xxx-xxxx-xxxxxxxxxx").using(web).first();
    record.getState().setDatabase(Database.Static.getDefault());
    record.save();
    return record;

### Query Tool

![](http://docs.brightspot.s3.amazonaws.com/query-tool-basic.png)

From here you can query single, aggregated SQL and Solr or multiple databases.

The `All Types` drop-down allows you to specify the content you would like to query within. In the example below, running the query against the `Author` content type shows us the records stored.

A specific ID can be provided for querying directly, and in the right side drop down a single database can be chosen, as well as specifying the sort order, and which field to sort from. Standard operators can be used.

Example Syntax:

    title matches "This is the" <!-- Matches part of text -->
    title ^= "This" <!-- Starts with -->
    title = "This is the Title" <!-- Exact String -->

Also, search within an object. Example, an Article with an image. Return Article types:

    image/caption matches "This is the caption"

For results, clicking into each result provides a detailed look at each object, in JSON format.


You can return results for a specific field within an object. In the example screen grab above we have specified the `twitterHandle` and the `lastName`.

Clicking on a result shows the `JSON`, `Raw JSON` and `Fielded` view that allows control of the object content.

![](http://docs.brightspot.s3.amazonaws.com/query-tool-fielded.png)


### Settings

The Settings section of the _debug tool gives the developer an overall view of their application, with information taken from the JVM, server, Tomcat,`context.xml`, and the application, `web.xml` and `pom.xml`. It is not editable from within this view.

### Stats

The status tool provides insight into the performance of Dari when running inside a servlet container. It provides information on SQL and Solr Throughput and Latency as well as JSP includes, averages and HTTP Response Throughput and Latency.

![](http://docs.brightspot.s3.amazonaws.com/stats-tool.png)

### Task Tool

The Task Tool shows all background tasks being implemented on the server. New tasks that are created show up within the interface, including Database Manager tasks carried out through the `_debug/db-managertool`.

![](http://docs.brightspot.s3.amazonaws.com/task-tool.png)

