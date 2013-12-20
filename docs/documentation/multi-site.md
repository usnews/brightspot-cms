---
layout: default
title: Multi-Site
id: multisite
section: documentation
---

<div markdown="1" class="span12">

## Overview

Multi-Site allows editors to manage multiple sites from one Brightspot CMS instance. This feature is critical to streamlining publishing workflows across multiple sites that share the same content. Brightspot CMS can maintain unified taxonomies and tagging strategies, conten and modules. This section will walk through how to create new sites, and use multi-site in Brightspot.

To illustrate how to create new sites within Brightspot, and use Multi-site, two domains will be used as examples:

**www.editor.com** - A site for editor information

**www.developer.com** - A site for developer information

Both sites will use the same content types, but have individual page styles, and within the CMS access to each will be limited and controlled.

## Adding Sites

Navigate to **Admin > Sites** and create your new sites. Name them, and add a unique URL, complete with `http://`. Note, in this example development will be done locally, therefore domains will point to localhost. Adjust your [hosts file](http://www.howtogeek.com/howto/27350/beginner-geek-how-to-edit-your-hosts-file/) accordingly. You will also need to add the port number when developing locally.

{% highlight jsp %}
127.0.0.1	editor.com
127.0.0.1	developer.com
{% endhighlight %}

![Adding Sites Multisite](http://docs.brightspot.s3.amazonaws.com/create-new-site-2.2.png)

Having added sites, you will find a new drop-down in the CMS header. Click into the **Site:** option to see the three options. Global is the parent CMS, with access to all content and templates as default.


<div class="alert alert-block">
    <strong>Important</strong>
    <p>Once you have selected a site in the Site: drop-down any new content created will be owned by the selected site. Similarly, if you are having difficulty finding content, make sure you are within the site in which it was created.
    </p>
</div>


## Adding Content

Start by choosing a site to work within. For this example we will create two articles, one within each site.

As we create new content within a site, we do not have access to all global content. If we create new content, such as an author - this lives within the site in which it was created. See below our two dashboard views of the two articles. In the sitemap widget only the articles created within the specific site are shown.

![](http://docs.brightspot.s3.amazonaws.com/ed-article.png)


![](http://docs.brightspot.s3.amazonaws.com/dev-article.png)


## Access Control

Switching between your sites shows that each newly added article is only available within the site in which it was created, as well as both being available within the Global site:

![](http://docs.brightspot.s3.amazonaws.com/site-access-control.png)

You can control the access that each site grants to the objects created within them. Navigate back to one of your articles. In the right rail you will see a **Sites** widget. The default owner is the site in which it was created. As well as changing the ownership, you can grant access to other sites. To test, select some others and allow your developer site to see the editor article. Publish, then access your developer site. You will now see two articles. The new article you have been given access to has no URL, as each site has control over the URL to access content. Navigate back to the original site and remove access and publish.

As a default, all object types within Brightspot are accessible by all sites. To limit access, use [Users and Roles](editorial-guide.html#user-admin) to create a role within a Site, and limit access to creating new objects.


## Options

When you create a site you can also specify the access control that is applied as a default. You can also create a default variation for all content created within that site. For more on Variations, see the [dedicated section](variations.html).

Upload a CMS logo specific to that site, or add a css class to control the look and feel.

![Adding Sites Multisite](http://docs.brightspot.s3.amazonaws.com/create-new-site-2.2.png)


## Site Settings

When multiple sites are running within one instance of Brightspot, often Site Settings need to be applied to each individual site. Rather than extending the `Tool` class, and creating a new tool to store the settings globally, the `Site` object can be modified so that each **Site** has settings defined individually.


{% highlight java %}import com.psddev.cms.db.Site;
import com.psddev.dari.db.Modification;

@Modification.Classes({Site.class})
public class SiteModification extends Modification<Object>{
	
  private String analyticsID;

  // Getters and Setters
}
{% endhighlight %}

Now, when creating or editing a site, the `String analyticsID` is present.

![Site Modification](http://docs.brightspot.s3.amazonaws.com/site-modification.png)

#### Querying Site Settings

You can get access to any Modification on a Site object by querying for it. As you can see in the example below all **Site** objects are returned. The Developer Site has the `analyticsId` property on it.

![Site Modification](http://docs.brightspot.s3.amazonaws.com/site-modification-code.png)
