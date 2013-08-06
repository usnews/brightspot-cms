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

Within a JSP file, most likely your header, place the following tag, to include the extra `headNodes`.

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

These are added into the `<head>` section of the page, using the `<cms:render value="${stage.headNodes}" />` tag.

## Sharing Logic

Rather than applying logic to each class, pages with similiar update logic can share one common class, where the updates are contained. This is done using the PageStage.UpdateClass annotation. Start by creating the logic, implementing `PageStage.SharedUpdatable`:

{% highlight java %}@Override
public class DefaultPageStageUpdater implements PageStage.SharedUpdatable {

    @Override
    public void updateStage(PageStage stage) {
    stage.addStyleSheet("http://static.yoursitename.com/css/section-global.css");
    stage.addScript("/static/js/jquery-1.9.0.min.js");   
    }
 
    @Override
    public void updateStageBefore(Object object, PageStage stage) {
    }

    @Override
    public void updateStageAfter(Object object, PageStage stage) {
        stage.setTitle(stage.getTitle() + " | Site Name");
    }
}
{% endhighlight %}

The class that is to share the logic is then annotated.

{% highlight java %}@PageStage.UpdateClass(DefaultPageStageUpdater.class)
public class Article extends Content {
}
{% endhighlight %}

For full java-docs [documentation on PageStage](http://www.brightspotcms.com/javadocs/com/psddev/cms/db/PageStage.html) see the docs.

</div>


