---
layout: default
title: Query Parameters
id: queryParameters
section: documentation
---
<div markdown="1" class="span12">

## Overview

There are a number of query parameters available when working with Brightspot and Dari. This section looks at what is available, and what they return.

## Contextual Debugger

{% highlight html %}
http://yoursite.com/?_debug=true
http://yoursite.com/about-us/?_debug=true
{% endhighlight %}

The Contextual Debugging Tool gives you an instant view of webpage metrics. By adding `?_debug=true` to a page URL the Dari Contextual Debugger is activated. This provides a view of the load time, in milliseconds, for each module (jsp) on the page. Color hotspots are added, with relative size, to provide a clear illustration of the slowest loading modules on the page. The larger the circle, and the darker the red, the slower the load time. 

## Dari Grid

{% highlight html %}
http://yoursite.com/?_grid=true
http://yoursite.com/about-us/?_grid=true
{% endhighlight %}

The Dari Grid parameter presents the webpage you are accessing with an overlay of the grid it is using. Each layout and grid area is labeled, so you can see how the page is constructed visually.

## Reloader

{% highlight html %}
http://yoursite.com/cms/?_reload=true
{% endhighlight %}

To automatically trigger the Dari Reloader, which compiles and redeploys any new changes made to your source files, simply prompt the reload by using `?_reload=true`. This will kick-off a reload of source to war.

## Output Format

{% highlight html %}
http://yoursite.com/?_format=json
http://yoursite.com/?_format=jsonp&_callback=""
http://yoursite.com/?_format=js
http://yoursite.com/?_format=json&_result=html
{% endhighlight %}

Use the `?_format=` parameter to change the format in which the page is output. To control this, use microdata tags. Documentation available here: [Microdata](/microdata.html)

Adding `&_result=html` to `?_format=json` will present the frame response inside JSON.

For JavaScript output add `?_format=js`.

## Context

{% highlight html %}
http://yoursite.com/about-us/?_context="module"
http://yoursite.com/gallery/about-us-gallery/?_context="module"

{% endhighlight %}

If context (Documentation for [Contextual Rendering](/contextual-rendering.html)) is set on an object, that context can be accessed through the URL query parameter `?_context="contextValue"`

## Cache

{% highlight html %}
http://yoursite.com/about-us/?_cache=false
{% endhighlight %}

Brightspot and Dari automatically provide SQL query caching when returning objects. This limits the number of SQL queries on a page where the same content is being requested in two separate queries, consolidating queries. 

To turn this off, add `?_cache=false` to your page request. To see the difference, view the change using `?_debug=true&_cache=false`. Scroll down to the Dari Profile table, and view the count for SQL with and without caching.

![](http://docs.brightspot.s3.amazonaws.com/cache-testing.png)


</div>
