---
layout: default
title: SEO
id: SEO
section: documentation
---

<div markdown="1" class="span12">


All content that is assigned a URL appears within the CMS above a global SEO widget (Found at the bottom of the content edit page). This widget, when populated, provides SEO Title, Description and Keywords that overwrite the defaults from the content itself.

Within the respective .jsp files these fields must be chosen specifically, so as to appear on the front end.

By clicking on Content Tools link (Wrench) found to the right of the Publish Widget, a view of the SEO fields can be seen in code.

	"cms.seo.title" : "Our New Title",
	"cms.seo.description" : "This description is very different from the default",
	"cms.seo.keywords" : [ "Added", "Are", "Here", "Keywords", "Shown" ],
	"cms.directory.pathsMode" : "MANUAL",
	"cms.directory.paths" : [ "8b48aee0-42d1-11e1-9309-12313d23e8f7/seotest" ],
	"cms.directory.pathTypes" : {
	"8b48aee0-42d1-11e1-9309-12313d23e8f7/seotest" : "PERMALINK"

A typical implementation would be to test to see if an SEO Title has been added. If not, the standard object title can be used instead.

	<title>Perfect Sense Digital<c:if test="${!empty seo.title}" >: <c:out value="${seo.title}" /></c:if></title>
	
The SEO Widget in the CMS provides lots of added robots.txt tools to hide and noindex your content.

![](http://docs.brightspot.s3.amazonaws.com/seo-widget-2.3.png)

### Microdata

Web pages have an underlying context that the user understands when they access the pages, however search engines have a very limited understanding of what the content is on the page being crawled.

By adding additional tags to the HTML of your web page's tags that give it content, you can help the search engines and other applications better understand your content and display it in a useful, relevant way. Microdata is a set of tags, introduced with HTML5, that allows you to do this.

**Implementing**

Start by finding a content type within your project that you would like to give a search engine more context to. In this example we have chosen our Article object:

	public class Article extends Content implements Directory.Item {

    	private String headline;
    	private Author author;
    	private ReferentialText body;
	
    	// Getters and Setters
	}

Access the [schema.org](http://schema.org/docs/full.html) site to find the matching schema instructions for your chosen type. We found the [Article](http://schema.org/Article) schema.

Match the available tags with the fields that exist on your object. We have highlighted the following:

	<div itemscope itemtype="http://schema.org/Article">

	<span itemprop="name">Article Name</span> or <span itemprop="headline"> Article Headline</span>

	<span itemprop="author">Author Name</span>

	<span itemprop="articleBody">Body Text</span>
	</div>

**Add to your JSP**

Once you have determined the relevant tags that you can add, place them into your jsp:

	<div itemscope itemtype="http://schema.org/Article">
    	<h1><span itemprop="headline"><cms:render value="${content.headline}"/></span></h1>
    	<h5>Written by: <span itemprop="author"><c:out value="${content.author.name}"/></span></h5>
    	<span itemprop="articleBody"><cms:render value="${content.body}" /></span>
	</div>

**View Output**

To see the output of the tags, and check the content being highlighted for the search engine, access a page which you have implemented the tags on, and add `?_format=json` to the URL.

</div>