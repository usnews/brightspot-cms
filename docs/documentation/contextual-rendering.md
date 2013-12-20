---
layout: default
title: Contextual Rendering
id: contextualRendering
section: documentation
---
<div markdown="1" class="span12">

## Overview

Often an object will be used in multiple places throughout a website, with different rules around how it is displayed for each location. Brightspot provides the ability to render an object differently, based on context. This is available for Brightspot 2.2.


## Rendering Paths

Start by changing the `@Renderer.Path` annotation on the object to `@Renderer.Paths`. This allows multiple paths to be defined. The `value` is the default rendering of the object. Set the `context` for other rendering locations. The example below provides a new jsp for when the article object is used as a module.

{% highlight java %}@Renderer.Paths `({
	@Renderer.Path(value = "/article-object.jsp"),
	@Renderer.Path(context = "module", value = "/module-article-object.jsp")
})
public class Article extends Content implements Directory.Item {
{% endhighlight %}

## Setting Context

Having set the paths, and the context terms on the object, add the matching context term in the JSP where the object is being rendered, to set when a specific path should be used. There are two ways to do so, either through the `cms:context` tag, or as an attribute on `cms:render`:

{% highlight jsp %}<cms:context name="module">
    <cms:render value="${content.article}"/>
</cms:context>
{% endhighlight %}

{% highlight jsp %}<cms:render context="module" value="${content.article}"/>
{% endhighlight %}

## Previewing Context

Content that has various views, based on context, can be previewed accordingly to the options, using the `Context` drop down within the preview tool:

![](http://docs.brightspot.s3.amazonaws.com/context-preview-2.2.png)

In order to enable this view, the content must also have a `@Renderer.EmbedPath("")` annotation. See the [syndication](syndication.html) section to learn about creating an `embed.jsp`.


## Dari Grid Context

It is often neccessary to determine what grid area a piece of content is being placed into. Each defined area can have a context set on it, which can then be checked:

{% highlight css %}
.home-layout {
    display: -dari-grid;
    -dari-grid-template:
        " content . rail ";

    -dari-grid-definition-columns: 700px 30px 200px;
    -dari-grid-definition-rows: auto;
    -dari-grid-contexts: 0 content 1 rightRail;
}
{% endhighlight %}

By setting the context of each area it can be checked in the jsp rendering:

{% highlight jsp %}
<c:if test="${cms:inContext('content')}">
	// Render in specific way
</c:if>
{% endhighlight %}

Context can also be checked on the object renderer level:

{% highlight java %}
@Renderer.Paths ({
  @Renderer.Path(value = "/WEB-INF/model/article.jsp"),
  @Renderer.Path(context = "rail",
	value = "/WEB-INF/model/rail-article.jsp")
})
@Renderer.LayoutPath("/WEB-INF/common/page-container.jsp")
public class Article extends Content {

	
}
{% endhighlight %}

</div>