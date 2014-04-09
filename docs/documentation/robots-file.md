---
layout: default
title: Robots.txt
id: robots.txt
section: documentation
---
<div markdown="1" class="span12">

## Overview

This section looks at how to create and manage a `robots.txt` file with Brightspot. There are two approaches, both are covered. The 

## Raw HTML

In version 2.3 of Brightspot onward a Raw Html object is available, and can be used to create a `robots.txt` file in the CMS:

Create a new RawHTML object, and add your own disallows:

![](http://docs.brightspot.s3.amazonaws.com/robots-text-1.png)

In the advanced tab create a new header called `Content-Type` with the value `text/plain`.

![](http://docs.brightspot.s3.amazonaws.com/robots-text-2.png)

In the URL widget on the right add `robots.txt` as the URL for the object, and it will be accessed and rendered when robots.txt is accessed.

## PageFilter

For earlier versions of Brightspot, and if you wish to have a more sophisticated approach to the `robots.txt` file, you can use a PageFilter to handle the request through an associated class. This is of use when you wish to have multiple robots.txt files, for example on a MultiSite implementation.


{% highlight xml %}<filter>
        <filter-name>RobotsTxtFilter</filter-name>
        <filter-class>com.psddev.brightspot.RobotsTxtFilter</filter-class>
    </filter>
    <filter-mapping>
        <filter-name>RobotsTxtFilter</filter-name>
        <url-pattern>/robots.txt</url-pattern>
    </filter-mapping>   
{% endhighlight %}

## Error Page Object

The logic that is processed for the filter is handled in a dedicated filter class, extending AbstractFilter. The content for the `robots.txt` file can either be coded into this file, or alternatively entered within the CMS, with the `RobotsTxtFilter` class returning that object. For example, a SiteSettings object, (extending Tool) could have a robots.txt string field that is accessed here:

{% highlight java %}public class RobotsTxtFilter extends AbstractFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RobotsTxtFilter.class);
    private static final String ROBOTS_TXT = "robots.txt";

    @Override
    protected void doRequest(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
    throws IOException, ServletException {
        String servletPath = request.getServletPath();
        if (servletPath.endsWith(ROBOTS_TXT)) { 

            // LOGIC HERE FOR YOUR ROBOTS.TXT
        
        }
        chain.doFilter(request, response);
    }
}
{% endhighlight %}

</div>