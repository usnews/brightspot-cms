---
layout: default
title: Dari Developer Tools
id: dari-developer-tools
---

## Dari Developer Tools


Brightspot CMS is built on top of the [Dari Framework](http://dariframework.org), an open source Java Framework also created by [Perfect Sense Digital](http://perfectsensedigital.com/products/dari). Full documentation for Dari is available [here](http://www.dariframework.org/documentation.html).

By leveraging Dari, a full set of developer tools are available within your Brightspot project. This section will look at each tool in detail. Please see the [Debugging Section](/brightspot-cms/debugging.html) for more information on the Contextual Debug Tool also available through Dari.

### Build Tool

The `build` tool gives you access to the build history for your application, showing commits, and other information. You can configure external services, for example GitHub, Hudson and JIRA, allowing developers to reference bugs they have fixed, and code they have changed.

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

**Code Tool**

The Code Tool provides you with a playground leveraging the on-the-fly code compiling from Dari. Here you can see instant results for your Java code in your browser. 

Here you can run queries against your project data, for example below you can see returning an Author object, where all fields, and `getters` and `setters` are outlined.

<a href="javascript:;" ><img src="http://docs.brightspot.s3.amazonaws.com/code_tool_return.png"/></a>

An example of a query, here we find a Blog Post where the Author's first name is John.

<a href="javascript:;" ><img src="http://docs.brightspot.s3.amazonaws.com/code_tool_return_query.png"/></a>

As well as running queries, existing objects from your project can be accessed from the drop down and edited. When running locally, any changes saved will be made to your source.

<a href="javascript:;" ><img src="http://docs.brightspot.s3.amazonaws.com/code_tool_modify_objects.png"/></a>

You can also select the New Class option in the drop down to create an entirely new object. Here we are creating a new Category object.

<a href="javascript:;" ><img src="http://docs.brightspot.s3.amazonaws.com/code_tool_create_objects.png"/></a>

**DB-Bulk Tool**

The DB-Bulk Tool is predominately used to re-index content. The `@Indexed` annotation is often added retrospectively, to already existing objects. In order to update content that you now want to index, you can use the db-bulk tool. You can re-index on a single type, or all types. The status of this process once started, can be seen in the Task Tool.

When importing an updated, or new database Solr will also need to be indexed, which can be done from here.

<a href="javascript:;" ><img src="http://docs.brightspot.s3.amazonaws.com/db-bulk-tool.png"/></a>

The Copy tool, also seen here, is used to move CDN Storage items from one database to another. New Databases should be specified in the `context.xml`.

**Database Schema**

A unique view of your data model is available through the Dari Schema tool. All content types within your project are listed. Simply select the type you want to view, or type to find from the list.

The schema outlines the model, showing all fields and associated content types. Here we can see our Blog object with associated Author and Category objects.

<a href="javascript:;"><img src="http://docs.brightspot.s3.amazonaws.com/db-schema-tool.png"/></a>

When working on your local machine the objects listed are click-able, opening the Code Tool directly so changes can be made.
