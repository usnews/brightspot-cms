---
layout: default
title: Page Staging
id: pageStaging
section: documentation
---
<div markdown="1" class="span12">

## Overview

While a global page container may work for most pages on your site, sometimes you want to insert custom styles for a specific page. To do so, `PageStage` can be used. This class is commonly used to easily update and render the contents of the `<head>` element.

## Using PageStage.Updatable

Start by implementing `PageStage.Updatable` from your class. The example below shows how to construct the `title` within a `<head>` tag.


{% highlight java %}public class Article extends Content implements PageStage.Updatable {

    private String title;

    public String getTitle() {
        return title;
    }

    @Override
    public void updateStage(PageStage stage) {
        stage.setTitle(getTitle());
    }
}{% endhighlight %}

Within the JSP file, the following code will output the Article Title text as the page title.

{% highlight jsp %}<head>
<cms:render value="${stage.headNodes}" />
</head>{% endhighlight %}

## Including Scripts

Stylesheets or JavaScript that is needed on the page specifically, can be added through `PageStage.Updatable`

{% highlight java %}@Override
public void updateStage(PageStage stage) {
    stage.addStyleSheet("http://static.yoursitename.com/css/section-global.css");
    stage.addScript("/static/js/jquery-1.9.0.min.js");
        
}
{% endhighlight %}

These are added into the `<head>` section of the page.

For full java-docs [documentation on PageStage](http://www.brightspotcms.com/javadocs/com/psddev/cms/db/PageStage.html) see the docs.

</div>


