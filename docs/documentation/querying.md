---
layout: default
title: Querying
id: querying
section: documentation
---

<div markdown="1" class="span8">

Through Dari, Brightspot provides a database-abstraction API that lets you retrieve 
content. Queries are represented by instances of the Dari Query class. This
class should look very familiar to anybody that has used SQL before.

If you have Dari already installed, you can use the Code Tool to test out these
queries.

### The **FROM** clause

The simplest query is to select all records of a given type:


{% highlight java %}
List<Author> authors = Query.from(Author.class).selectAll();
{% endhighlight %}

This will return all instances of the `Author` class.

Inheritance also works with the `FROM` clause by querying from a base
class. It is possible to build an activity feed, for example:

{% highlight java %}
public class Activity extends Content {
    @Index private Date activityDate;
    @Index private User user;
}

public class Checkin extends Activity { ... }
public class Comment extends Activity { ... }
public class ReadArticle extends Activity { ... }
public class PostedArticle extends Activity { ... }
{% endhighlight %}

Given this class hierarchy you can query for user's activity by querying
from the `Activity` class. This will also retrieve any
records that are subclasses of `Activity`.

{% highlight java %}
PaginatedResult<Activity> results = Query.from(Activity.class).
        where("user = ?", user).
        sortDescending("activityDate").
        select(0, 10);
{% endhighlight %}

### The **LIMIT** clause

Dari supports limiting the number of results returned.

{% highlight java %}
PaginatedResult<Article> articles = Query.from(Article.class).
        sortAscending("title").
        select(1000, 10);
List<Article> items = articles.getItems();
{% endhighlight %}

This will start at offset 1000 and return the next 10 instances of `Article`. The
object returned from a limit query is a `PaginatedResult`. This is a pagination
helper class that provides efficient methods for building pagination, such as
`hasNext()` and `getNextOffset()`.

### The **WHERE** clause

The `WHERE` method allows you to filter which object instances that are returned.
In order to use a field in a `WHERE` clause it must have the @Index annotation.

{% highlight java %}
Author author = Query.from(Author.class).where("name = 'John Smith'").first();
{% endhighlight %}

This will return the first instance of `Author` with the name 'John Smith'.

Logical operations `not, or, and` are supported. For a full list of supported predicates see the [Predicate section](predicate.html).

{% highlight java %}
List<Author> authors = Query.from(Author.class).
        where("name = 'John Smith' or name = 'Jane Doe'").
        selectAll();
{% endhighlight %}

The `Query` class follows the builder pattern so this query can also be written as:

{% highlight java %}
List<Author> authors = Query.from(Author.class).
        where("name = 'John Smith'").
        and("name = 'Jane Doe'").
        selectAll();
{% endhighlight %}




### Querying Relationships


Dari supports querying relationships using path notation (i.e. field/subfield)
in `WHERE` clauses. A common use case is finding all articles by a particular
author. We'll use the following models to demonstrate how to use path notation.

{% highlight java %}
public class Article extends Content {
    @Index private Author author;
    private String title;
    private String body;

    // Getters and Setters...
}

public class Author extends Content {
    private String firstName;
    @Index private String lastName;
    @Index private String email;

    // Getters and Setters...
}
{% endhighlight %}

There are two ways to can find articles by a specific author. Query for the author first and then query for articles by that
author.

{% highlight java %}
Author author = Query.from(Author.class).where("email = 'john.smith@psddev.com'");
List<Articles> = Query.from(Article.class).where("author = ?", author);
{% endhighlight %}

However, it's easier and more efficient to do this in a single query
using path notation.


{% highlight java %}
List<Articles> = Query.from(Article.class).where("author/email = 'john.smith@psddev.com'");
{% endhighlight %}


### Bind variables


In the previous section `?` was used in the `WHERE` clause when specifying the author. Dari supports bind
variables in query strings using `?` for placeholders.


{% highlight java %}
String authorName = "John Smith";
Author author = Query.from(Author.class).
        where("name = ?", authorName).
        first();
{% endhighlight %}


Placeholders can be basic types like `String` or `Integer` but they can also be
Lists or other Dari objects. This allows for `IN` style queries.


{% highlight java %}
List<String> names = new ArrayList<String>();
names.add("John Smith");
names.add("Jane Doe");
List<Author> authors = Query.from(Author.class).
        where("name = ?", names).
        selectAll();
{% endhighlight %}


### The **ORDER BY** clause


Results can be ordered using `sortAscending` and `sortDescending`  Both of these methods take the name of
the field to sort. The field being sorted must have the `@Indexed`
annotation.


{% highlight java %}
List<Author> authors = Query.from(Author.class).sortAscending("name");
{% endhighlight %}


### The **GROUP BY** clause


Using the `groupBy` method allows queries to return items in
groupings, based on associations.  The example below returns a count
of articles grouped by the tags associated with each.

The following example shows how Group By works, using Articles that
contain the Tag field that can be grouped by.


{% highlight java %}
public class Article extends Content {
    private Tag tag;
    private String author;

    // Getters and Setters
}
{% endhighlight %}


Now the groupBy query:


{% highlight java %}
List<Grouping<Article>> groupings = Query.from(Article.class).groupBy("tag")

for (Grouping grouping : groupings) {
    Tag tag = (Tag) grouping.getKeys().get(0);
    long count = grouping.getCount();
}
{% endhighlight %}


It is possible to retrieve the items that make up a grouping by using
the `createItemsQuery` method on the returned `Grouping` objects. This
method will return a `Query` object.


{% highlight java %}
List<Grouping<Article>> groupings = Query.from(Article.class).groupBy("tag");

for (Grouping grouping : groupings) {
    Tag tag = (Tag) grouping.getKeys().get(0);
    List<Article> articles = grouping.createItemsQuery().selectAll();
}
{% endhighlight %}


Grouping by more than one item, for example, a Tag, and Author is
possible as well.


{% highlight java %}
List<Grouping<Article>> groupings = Query.from(Article.class).groupBy("tag" , "author");

for (Grouping grouping : groupings) {
    Tag tag = (Tag) grouping.getKeys().get(0);
    Author author = (Author) grouping.getKeys().get(1);
    long count = grouping.getCount();
}
{% endhighlight %}


Sort the count using the `_count`:


{% highlight java %}
List<Grouping<Article>> groupings = Query.from(Article.class).sortAscending("_count").groupBy("tag");

for (Grouping grouping : groupings) {
    Tag tag = (Tag) grouping.getKeys().get(0);
    List<Article> articles = grouping.createItemsQuery().getSorters().clear().SelectAll();
}
{% endhighlight %}

![GroupBy](http://docs.brightspot.s3.amazonaws.com/groupBy-code-preview.png)

### Query Tool


When used in a J2EE web project Dari provides a query tool that supports the Dari query
syntax. This tool can be found at `/_debug/query`.


![Query Tool](http://www.dariframework.org/img/query.png)

### Spatial Queries

Dari supports spatial queries on MySQL, PostgreSQL and Solr. To use Dari's
spatial features define a field of type
`com.psddev.dari.db.Location` on the model you want to do spatial lookups.
This type is a container for latitude and longitude values. This field should be
indexed using the `@Index` annotation.

For example:


{% highlight java %}
public class Venue {
    private String name;
    @Index private Location location;

    // Getters and Setters
}
{% endhighlight %}


To find all venues within a 10 mile radius of Reston Town Center in
Reston, VA we would issue the following query:


{% highlight java %}
double degrees = Region.milesToDegrees(10);
PaginatedResult<Venue> venues = Query.from(Venue.class).
        where("location = ?", Region.sphericalCircle(38.95854, -77.35815, degrees));
{% endhighlight %}


Sorting venues by closest works as well:


{% highlight java %}
double degrees = Region.milesToDegrees(10);
PaginatedResult<Venue> venues = Query.from(Venue.class).
        where("location = ?", Region.sphericalCircle(38.95854, -77.35815, degrees))).
        sortClosest("location", new Location(38.95854, -77.35815));
{% endhighlight %}


<div class="alert alert-block">
    <strong>Performance Tip:</strong>
    <p>When using <code>sortClosest</code> you should limit the results to be inside
    a given distance with a <code>WHERE</code> clause. This will speed up your query.
    </p>
</div>

### The **FIELDS** clause

Specific fields from within a Dari object can be returned for a given object. Other fields will return null when you access them. The use of the `.fields` clause requires a (MySQL Plugin)[https://github.com/perfectsense/dari/tree/master/mysql] to be installed.

{% highlight java %}
Author author = Query.from(Author.class).fields("name", "age").selectAll();
{% endhighlight %}

</div>

<div class="span4 dari-docs-sidebar">
<div markdown="1" style="position:scroll;" class="well sidebar-nav">


* auto-gen TOC:
{:toc}

</div>
</div>
