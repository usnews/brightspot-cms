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

{% highlight java %}@Renderer.Paths({
	@Renderer.Path(value = "/article-object.jsp"),
	@Renderer.Path(context = "module", value = "/module-article-object.jsp")
})
public class Article extends Content implements Directory.Item {
{% endhighlight %}

## Setting Context

Having set the paths, and the context terms on the object, add the matching context term in the JSP where the object is being rendered, to set when a specific path should be used.

{% highlight jsp %}<cms:context name="module">
	<cms:render value="${content.article}"/>
</cms:context>
{% endhighlight %}

</div>