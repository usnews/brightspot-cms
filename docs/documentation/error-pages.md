---
layout: default
title: Error Pages
id: errorPages
section: documentation
---
<div markdown="1" class="span12">

## Overview

This section looks at how to create and manage error pages using Brightspot.

## Error Page Element

Start by adding your error-page elements to your web.xml and specifying the url on which they are accessed. Below the 404 error-code maps to `/404`. This could equally be `/no-page-found`:

{% highlight xml %}
    <error-page>
        <error-code>404</error-code>
        <location>/404</location>
    </error-page>

    <error-page>
        <error-code>505</error-code>
        <location>/505</location>
    </error-page>
{% endhighlight %}

## Error Page Object

Create an object that renders your visual error page. Add the URL that matches the mapping in your web.xml. 

![](http://docs.brightspot.s3.amazonaws.com/error-page.png)

{% highlight java %}

@Renderer.Path("/render/common/error.jsp")
@Renderer.LayoutPath("/render/common/page-container.jsp")
public class ErrorPage extends Content {

	private String errorMessage;
	
	// Getters and Setters
}
{% endhighlight %}

</div>