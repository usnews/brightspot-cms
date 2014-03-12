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

<div class="highlight">{% highlight java %}
"cms.seo.title" : "Our New Title",
"cms.seo.description" : "This description is very different from the default",
"cms.seo.keywords" : [ "Added", "Are", "Here", "Keywords", "Shown" ],
"cms.directory.pathsMode" : "MANUAL",
"cms.directory.paths" : [ "8b48aee0-42d1-11e1-9309-12313d23e8f7/seotest" ],
"cms.directory.pathTypes" : {
"8b48aee0-42d1-11e1-9309-12313d23e8f7/seotest" : "PERMALINK"
{% endhighlight %}</div>

A typical implementation would be to test to see if an SEO Title has been added. If not, the standard object title can be used instead.

<div class="highlight">{% highlight java %}
<title>Perfect Sense Digital<c:if test="${!empty seo.title}" >: <c:out value="${seo.title}" /></c:if></title>
{% endhighlight %}</div>

The SEO Widget in the CMS provides lots of added robots.txt tools to hide and noindex your content.

![](http://docs.brightspot.s3.amazonaws.com/seo-widget-2.3.png)