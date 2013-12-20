---
layout: default
title: Syndication
id: syndication
section: documentation
---
<div markdown="1" class="span12">

## Overview

With Brightspot, individual content types can be syndicated and embeded externally, on other sites. They can retain their original look and feel, or inherit styles, and are self contained.

## Add Embed Paths

Any piece of content with a URL can be embedded outside of Brightspot. Start by adding an `@Renderer.EmbedPath()` annotation on the class that is to be syndicated. The `embed.jsp` file should contain any JavaScript or CSS that is needed to power the content. It also needs to include the `cms taglibs` for the `<cms:render>` tag to function.


{% highlight java %}@Renderer.EmbedPath("/embed.jsp")
@Renderer.Path("/generic-module.jsp")
public class GenericModule extends Content {

    private String title;
    private ReferentialText body;

    // Getters and Setters
}
{% endhighlight %}

## Create JSP

{% highlight jsp %}<link href="file.css" rel="stylesheet" type="text/css"/>
<script src="file.js" type="text/javascript"></script>

<cms:render value="${mainContent}"/>
{% endhighlight %}
Once you have added a URL, in the content edit view, click on the Advanced Tools icon (the wrench) to expose the embed script, which can be added on an external page.

The Site URL which is defined can be set in the Admin -> Settings section, called Default Site URL.

![](http://docs.brightspot.s3.amazonaws.com/embed-shot.png)

The embed code can be inserted into any webpage, to render the module outside of the Brightspot site.

</div>


