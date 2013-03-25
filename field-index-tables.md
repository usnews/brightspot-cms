---
layout: default
title: Custom Field Index Tables
id: customFieldIndexTables
section: documentation
---

<div markdown="1" class="span12">

Sometimes it is necessary for Dari to access data in database tables other than the standard Record tables. For instance, if an external 
process frequently inserts or updates data directly in your Dari database, and you want to access the fields in that table without 
copying the data into Dari objects, here is how you would do it.

First, the database table you wish to access:

{% highlight java %}
CREATE TABLE TeamStatistics (
    id BINARY(16) NOT NULL PRIMARY KEY,
    symbolId INT NOT NULL,
    wins INT NOT NULL,
    losses INT NOT NULL,
    ranking INT NOT NULL
);
{% endhighlight %}

<div class="alert alert-block">
    <strong>Performance Tip:</strong>
    <p>If you are going to query on these fields, create the 
appropriate indexes on the columns. Dari does not enforce this!
</p>
</div>


Note that id must be populated with a Dari Record ID. This can be done in your external process by selecting it from one of the Record index 
tables using an alternate identifier you know to be unique, such as the team's name.

Also note the presence of the symbolId column. It should be set to the symbolId of the *first* field in the table, in this case, "wins". This can be retrieved at load time from the Symbol table.

Next, your Dari Record definition:

{% highlight java %}
    public class Team extends Record {
        @Indexed(unique=true)
        private String name;

        @SqlDatabase.FieldIndexTable(value="TeamStatistics", readOnly=true, sameColumnNames=true, source=true)
        @Indexed(extraFields={"losses", "ranking"})
        private int wins;
        private int losses;
        private int ranking;

        // Getters and Setters

    }
{% endhighlight %}

This is introducing a lot at once, so let's go through this one at a time:
{% highlight java %}
@SqlDatabase.FieldIndexTable(value="TeamStatistics", ...
{% endhighlight %}

This is the name of your custom table. 

{% highlight java %}readOnly=true{% endhighlight %}

This tells Dari NOT to insert or update the values in your custom table. If you have set `readOnly=true`, when you `save()` your Record the values will NOT be persisted to the database, and you will be responsible for updating the tables directly.  Otherwise, Dari will handle saving the values to the database when you `save()`, as usual.

{% highlight java %}sameColumnNames=true{% endhighlight %}

When we created our table, the column names we used are the same as the field names in the Record. Otherwise, Dari will look for columns named "value", "value2", "value3", in the order that the field names are listed in the extraFields parameter to `@Indexed`. 

{% highlight java %}source=true){% endhighlight %}

This tells Dari NOT to look for the values of these columns in the JSON object. Instead, it will query the TeamStatistics table and populate the field values from that table. Otherwise, Dari will use your Custom Field Index Table only when you use one of its fields as a predicate in your query.

{% highlight java %}@Indexed(extraFields={"losses", "ranking"}){% endhighlight %}
    
The annotation is on the "wins" field, so this is simply telling Dari that these other fields are in the same FieldIndexTable.


</div>

