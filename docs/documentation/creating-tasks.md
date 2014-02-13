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

**Starting Task**

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