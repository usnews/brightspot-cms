---
layout: default
title: Microdata
id: microdata
section: documentation
---

<div markdown="1" class="span12">

## Overview

Web pages have an underlying context that the user understands when they access the pages, however search engines have a very limited understanding of what the content is on the page being crawled.

By adding additional tags to the HTML of your web pages—tags that give it content, you can help the search engines and other applications better understand your content and display it in a useful, relevant way. Microdata is a set of tags, introduced with HTML5, that allows you to do this.

## Implementing

Start by finding a content type within your project that you would like to give a search engine more context to. In this example we have chosen our Article object:

{% highlight java %}
public class Article extends Content implements Directory.Item {

    private String headline;
    private Author author;
    private ReferentialText body;
	
    // Getters and Setters
}
{% endhighlight %}

Access the [schema.org](http://schema.org/docs/full.html) site to find the matching schema instructions for your chosen type. We found the [Article](http://schema.org/Article) schema.

Match the available tags with the fields that exist on your object. We have highlighted the following:

{% highlight html %}
<div itemscope itemtype="http://schema.org/Article">

<span itemprop="name">Article Name</span> or <span itemprop="headline"> Article Headline</span>

<span itemprop="author">Author Name</span>

<span itemprop="articleBody">Body Text</span>
</div>
{% endhighlight %}

**Add to your JSP**

Once you have determined the relevant tags that you can add, place them into your jsp:

{% highlight jsp %}
<div itemscope itemtype="http://schema.org/Article">
    <h1><span itemprop="headline"><cms:render value="${content.headline}"/></span></h1>
    <h5>Written by: <span itemprop="author"><c:out value="${content.author.name}"/></span></h5>
    <span itemprop="articleBody"><cms:render value="${content.body}" /></span>
</div>
{% endhighlight %}

## View Output

To see the output of the tags, and check the content being highlighted for the search engine, access a page which you have implemented the tags on, and add `?_format=json` to the URL.

![](http://docs.brightspot.s3.amazonaws.com/microdata-format.png)