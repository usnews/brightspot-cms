---
layout: default
title: Search
id: search
section: documentation
---

<div markdown="1" class="span8">

Brightspot CMS uses [Lucene Solr](http://lucene.apache.org/solr/) to provide full text search capabilities.

Within the CMS itself, the global search is powered by Solr. The Admin panel for Solr can be reached on your application at [http://localhost:8080/solr/admin/](http://localhost:8080/solr/admin/) - assuming you are running on port 8080.

## CMS Tool Search

![](http://docs.brightspot.s3.amazonaws.com/search-tool-close.png)


Out of the box, filtering and advanced searches are enabled within the CMS tool, for editors, however there are a number of ways in which developers can help make the search tool even more useful.

Each of the dashboard widgets that allow filtering allow customization, as well as the object types themselves in the global search. Annotations placed on target fields or content types can help make the experience even easier for the editor.

#### Search Filters

To have a property within a class as a default filter, add the `@Indexed` filter.

#### Color Search

For images, a color picker can be added to the search tool, so they can be searched by color. Each image is saved with the dominant hex values. These are then used to drive the search. To enable start by implementing `ColorImage` on your Image class.

![](http://docs.brightspot.s3.amazonaws.com/color-search-tool.png)


<div class="highlight">{% highlight java %}public class Image extends Content implements ColorImage {% endhighlight %}</div>

You also need to add a `getColorImage` method for the `StorageItem` type.

<div class="highlight">{% highlight java %}public StorageItem getColorImage() {
	return file;
} {% endhighlight %}</div>

This will allow all newly added images to be searched by color. If you want to enable for existing images, ReSave them.

#### Global Filter

By default, the sitemap and search widgets can be used to filter by object type, (and the URL path, for sitemap specifically). A global custom filter can be added, however, by adding the annotation `@ToolUi.GlobalFilter` to a content type. The example below shows how you can help editors filter projects by client. This was done with the following addition:

<div class="highlight">{% highlight java %}
@Indexed
@ToolUi.GlobalFilter
public class Client extends Content {
{% endhighlight %}</div>

![](http://docs.brightspot.s3.amazonaws.com/sitemap_custom_filter.png)

#### Global Search

The `@ToolUi.GlobalFilter` annotation also adds filters to global search:

![](http://docs.brightspot.s3.amazonaws.com/search_custom_filter.png)

#### Search Filter

A field can be called out as being added as a default filter for a particular object type not only by adding `@Indexed`, but also by adding the annotation `@ToolUi.Filterable`. This will show up on the global search widget, but not the sitemap, as `@ToolUi.GlobalFilter` would.

#### Sort Filter

Another annotation, `@ToolUi.Sortable` can be added to a field, allowing the search results to be sorted by that field.


## Querying with Solr

The full text search capabilities of Solr can be used with a query, by changing the syntax from a SQL to Solr query.

**Search with SQL**

<div class="highlight">{% highlight java %}
return Query.from(Author.class).where("firstName = 'Alex'").selectAll();
{% endhighlight %}</div>

> This will return us all the Author objects where Alex is the first name.

	
**Search with Solr**

<div class="highlight">{% highlight java %}
return Query.from(Author.class).where("firstName matches 'Alex'").selectAll();
{% endhighlight %}</div>	

> This will now match the string, using Solr, and return us all Author objects where the first name contains Alex, for example Alexander.


**Search API**

## Using Search

As well as powering the search within the CMS application, Solr can also be used for your website search. This section will outline how to enable these capabilities, and how to customize them.


**Search API**

The Search API within Brightspot gives you the ability to describe how a search should behave based on the user input. It works in conjunction with the Query API. You can try the basic search, seen below, in your Code Tool. Add your own object names, and a relevant search term to see how it performs.

<div class="highlight">{% highlight java %}
Search search = new Search(); // com.psddev.cms.db.Search
search.addTypes(Article.class, Blog.class);
return search.toQuery("Top Java CMS Platforms").select(0, 5);
{% endhighlight %}</div>	

**Boosted Search**

In Brightspot you can boost items where the search query matches the representative labels for each of the types included. This boosted search uses the Relevancy standards present in Solr, which are explained here, on the [Solr Wiki](http://wiki.apache.org/solr/SolrRelevancyFAQ#How_can_I_increase_the_score_for_specific_documents). 

The default behavior in Brightspot is for the first `String` field in any object to become the label field. This can be programmed to be any field with the annotation `@LabelFields` (See below) 

<div class="highlight">{% highlight java %}
@LabelFields({"headline"})
public class Article extends Content {

	private String internalName;
	@Indexed private String headline;

}
{% endhighlight %}</div>

If you want to boost the designated label field in an object, it must be `@Indexed`. The new boosted search for the headline field in the Article will include `search.boostLabels(5.0);`

<div class="highlight">{% highlight java %}
Search search = new Search();
search.addTypes(Article.class, Blog.class);
search.boostLabels(5.0);
return search.toQuery("Top Java CMS Platforms").select(0, 5);
{% endhighlight %}</div>

You can also manually chose the field that is to be boosted, without controlling `@LabelFields`

<div class="highlight">{% highlight java %}
search.boostFields(5.0, Article.class, "headline");
search.boostFields(5.0, Blog.class, "title");
{% endhighlight %}</div>

If you want to broaden the search you can add a keyword within a particular class. This allows certain keywords to be associated directly with objects. For example, below the search term "CMS" has been defined, therefore any user input matching that term will automatically boost references found in the News class.

<div class="highlight">{% highlight java %}
search.addTypeKeywords(1.5, News.class, "CMS");
{% endhighlight %}</div>

You can filter out words that are not adding to search, for example add 'top' as a stop word so that it isn't required to be in the text of the object:

<div class="highlight">{% highlight java %}
search.addStopWords("top");
{% endhighlight %}</div>


**Saving Searches**

The Search class extends Record, so it can be saved to the database. This can provide editors the ability to tweak the rules even after a site is in production. In your code, you can use those like so:

<div class="highlight">{% highlight java %}
Search.named("topPlatforms").toQuery("Top Java CMS Platforms").select(0, 5);
{% endhighlight %}</div>

</div>
<div class="span4 dari-docs-sidebar">
<div markdown="1" style="position:scroll;" class="well sidebar-nav">


* auto-gen TOC:
{:toc}

</div>
</div>
