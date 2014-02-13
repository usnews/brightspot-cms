---
layout: default
title: Creating Tasks
id: creating-tasks
section: documentation
---
<div markdown="1" class="span12">

Tasks, such as scheduled imports, counts, feed generation or syndication can be created using the Dari Task class.

The task tool, found at _debug/task shows all background tasks being implemented on the server. New tasks that are created show up within the interface, including Database Manager tasks carried out through the `_debug/db-manager`tool.

Creating a new task requires extending the Task class, found within `dari.util`. Alternately a simple way to implement a new task is:

<div class="highlight">{% highlight java %}private Task taskName = new Task(null, "Name to show in Task Tool") { 
       protected void doTask() { 
           **TASK**
       } 
    };

{% endhighlight %}</div>

### Starting Task

The Dari `_debug` dashboard provides a Custom Tools section, where scripts for starting tasks can be placed. Create a `_debug` directory under `/WEB-INF/` in your project and place any custom `.jsp` files within it.

Alternatively, by extending `AbstractFilter` tasks begin when the application starts. The example shown below runs a task that checks the CMS to see if there are more than 5000 comments stored. 


<div class="highlight">{% highlight java %}public class CommentCount extends AbstractFilter {
 
        private Task updateCommentCountTask = new Task(null, "Update Comment Count Task") {
            protected void doTask() {
          
             if (Query.from(Comment.class).count() > 5000) {
             // Logic to perform archive of oldest comments
            }
           }
        };
 
        protected void doInit() {
 
            updateCommentCountTask.schedule(10, 60);
        }
    }
{% endhighlight %}</div>

### Example - Import Task

A good use of the Task tool is the management of importing content. The following example looks at importing a csv file with name using Task, and saving the objects as Authors.

#### Task Class

<div class="highlight">{% highlight java %}package com.psddev.brightspot.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.psddev.cms.db.Draft;
import com.psddev.cms.db.Schedule;
import com.psddev.dari.db.Query;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.Task;
import com.psddev.brightspot.Author;

public class AuthorImporter extends Task {

    private InputStream input;

    public AuthorImporter(InputStream input) {
        this.input = input;
    }

    @Override
    protected void doTask() throws Exception {

        BufferedReader in = new BufferedReader(new InputStreamReader(input));

        String line = null;

        while((line = in.readLine()) != null) {
            String[] fields = line.split(",");

            String name = fields[0];

            Author author = new Author();
            author.setName(name);
            author.save();

        }     
    }

}
{% endhighlight %}</div>

#### Upload User Interface

In order to provide a means by which the csv file can be uploaded, a page was built in the _debug custom tools section - where the uploaded file could be selected. Add the jsp to WEB-INF/_debug directory:

<div class="highlight">{% highlight jsp %}

<%@page import="org.apache.commons.fileupload.FileItem"%>
<%@page import="org.apache.commons.fileupload.DiskFileUpload"%>
<%@page import="
    org.apache.commons.fileupload.servlet.ServletFileUpload,
    java.util.*,
    java.io.*,
    com.psddev.brightspot.*,
    com.psddev.brightspot.utils.AuthorImporter
"%>
<%

boolean isMultipart = ServletFileUpload.isMultipartContent(request);
if (isMultipart) {
    DiskFileUpload fu = new DiskFileUpload();

    List fileItems = fu.parseRequest(request);
    Iterator itr = fileItems.iterator();

    while(itr.hasNext()) {
        FileItem fi = (FileItem)itr.next();
        if(!fi.isFormField()) {
            AuthorImporter importer = 
                    new AuthorImporter(fi.getInputStream());
            importer.start();
        }
    }
}

%>

<!DOCTYPE html>
<html>
    <head>
        <title>Author Ingestion Task</title>
        <link href="/static/css/bootstrap.css" rel="stylesheet" type="text/css">
    </head>
    <body>
        <div class="container">
            <div class="page-header">
                <h2>Author Ingestion Task</h2>
            </div>
            <div class="well">
            <h5>Upload a .csv file with your authors</h5>
                <form class="form-horizontal" method="post" enctype="multipart/form-data">

                    <div class="control-group">
                        <label class="control-label" for="data">File</label><br>
                        <div class="controls">
                            <input type="file" name="data" />
                        </div>
                    </div>
                    <br><br>
                    <div class="control-group">
                        <div class="controls">
                        <input type="submit" class="btn btn-primary" name="action" value="Import">
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </body>
</html>
{% endhighlight %}</div>

![](http://docs.brightspot.s3.amazonaws.com/task-custom-tool.png)