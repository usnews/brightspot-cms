---
layout: default
title: Search
id: search
section: search
---

## Search

Brightspot CMS uses [Lucene Solr](http://lucene.apache.org/solr/) to provide full text search capabilities.

Within the CMS itself, the global search is powered by Solr. The Admin panel for Solr can be reached on your application at `http://localhost:8080/solr/admin/` - assuming you are running on port 8080.


### Using Search

As well as powering the search within the CMS application, Solr can also be used for your website search. In this section we will outline how to enable these capabilities, and how to customize them.


**Search API**

The Search API within Brightspot gives you the ability to describe how a search should behave based on the user input. It works in conjunction with the Query API. You can try the basic search, seen below, in your Code Tool. Add your own object names, and a relevant search term to see how it performs.

    Search search = new Search(); // com.psddev.cms.db.Search
	search.addTypes(Article.class, Blog.class);
	return search.toQuery("Top Java CMS Platforms").select(0, 5);
	
**Boosted Search**

In Brightspot we can boost items where the search query matches the representative labels for each of the types included. This boosted search uses the Relevancy standards present in Solr, which are explained here, on the [Solr Wiki](http://wiki.apache.org/solr/SolrRelevancyFAQ#How_can_I_increase_the_score_for_specific_documents). 

The default behavior in Brightspot is for the first `String` field in any object to become the label field. This can be programmed to be any field with the annotation `@LabelFields` (See below) 

	@LabelFields({"headline"})
	public class Article extends Content {

    	private String internalName;
		@Indexed private String headline;

	}


If we want to boost the designated label field in an object, it must be `@Indexed`. Our new boosted search for the headline field in our Article will include `search.boostLabels(5.0);`
	
	Search search = new Search();
	search.addTypes(Article.class, Blog.class);
    search.boostLabels(5.0);
	return search.toQuery("Top Java CMS Platforms").select(0, 5);


You can also manually chose the field that is to be boosted, without controlling `@LabelFields`

	search.boostFields(5.0, Article.class, "headline");
	search.boostFields(5.0, Blog.class, "title");


If we want to broaden our search we can add a keyword within a particular class. This allows certain keywords to be associated directly with objects. For example, below we have defined the search term "CMS", therefore any user input matching that term will automatically boost references found in the News class.

	search.addTypeKeywords(1.5, News.class, "CMS");

We can filter out words that are not adding to our search, for example we can add 'top' as a stop word so that it isn't required to be in the text of the object:

	search.addStopWords("top");



**Saving Searches**

The Search class extends Record, so it can be saved to the database. This can provide editors the ability to tweak the rules even after a site is in production. In your code, you can use those like so:

	Search.named("topPlatforms").toQuery("Top Java CMS Platforms").select(0, 5);

