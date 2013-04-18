---
layout: default
title: Multi-Site
id: multisite
section: documentation
---

<div markdown="1" class="span12">

## Overview

Multi-Site allows editors to manage multiple sites from one Brightspot CMS instance. This feature is critical to streamlining publishing workflows across multiple sites that share the same content. Brightspot CMS can maintain unified taxonomies and tagging strategies, templates and modules. This section will walk through how to create new sites, and use multi-site in Brightspot.

To illustrate how to create new sites within Brightspot, and use Multi-site, two domains will be used as examples:

**www.editor.com** - A site for editor information

**www.developer.com** - A site for developer information

Both sites will use the same content types, but have individual template styles, and within the CMS access to each will be limited and controlled.

## Adding Sites

Navigate to **Admin > Sites** and create your new sites. Name them, and add a unique URL, complete with `http://`. Note, in this example development will be done locally, therefore domains will point to localhost. Adjust your [hosts file](http://www.howtogeek.com/howto/27350/beginner-geek-how-to-edit-your-hosts-file/) accordingly. You will also need to add the port number when developing locally.

{% highlight jsp %}
127.0.0.1	editor.com
127.0.0.1	developer.com
{% endhighlight %}

![Adding Sites Multisite](http://docs.brightspot.s3.amazonaws.com/developer-multisite.png)

Having added sites, you will find a new drop-down in the CMS header. Click into the **Site:** option to see the three options. Global is the parent CMS, with access to all content and templates as default.


<div class="alert alert-block">
    <strong>Important</strong>
    <p>Once you have selected a site in the Site: drop-down any new content created will be owned by the selected site. Similarly, if you are having difficulty finding content, make sure you are within the site in which it was created.
    </p>
</div>


## Adding Pages

Start by choosing a site. For this example two new Homepage objects will be created, one for each. This will result in **editor.com** and **developer.com** having their own landing pages, both using `/`.

If you do not have a Homepage class, create one by extending the `Page` class:

{% highlight java %}@Renderer.LayoutPath("/layout/homepage-template.jsp")
@Renderer.Path("/homepage-object.jsp")
public class Homepage extends Page {

  private String welcomeText;
  private ReferentialText welcomeMessage;

  //Getters and Setters
}{% endhighlight %}

Full documentation on creating a Homepage and the `JSP` files required for this example can be found in the [Create a Page](create-a-page.html) section.

Create a new Homepage, as the example below shows. Name it according to the site you are within, and add a ROOT url `/`. Once published, switch to the other site, and create another Homepage, also using ROOT `/` as the URL.


![Adding Homepage Multisite](http://docs.brightspot.s3.amazonaws.com/developer-homepage.png)

## Access Control

Switching between your sites shows that each newly added homepage is only available within the site in which it was created, as well as both being available within the Global site:

![Adding Homepage Multisite](http://docs.brightspot.s3.amazonaws.com/global-dashboard.png)

You can control the access that each site grants to the objects created within them. Navigate back to one of your Homepage objects. In the right rail you will see a **Sites** widget. The default owner is the site in which it was created. As well as changing the ownership, you can grant access to other sites. To test, select some others and allow your second site to see the homepage. Publish, then access your second site. You will now see two Homepage objects. The new Homepage object you have been given access to has no URL, as each site has control over the URL to access content. Navigate back to the original site and remove access and publish.

As a default, all object types within Brightspot are accessible by all sites. To limit access, use [Users and Roles](editorial-guide.html#user-admin) to create a role within a Site, and limit access to creating new objects.

## Preview

Using the domain names you created in your hosts file, navigate to each Homepage to see the two unique pages.

![Previewing Homepage Multisite](http://docs.brightspot.s3.amazonaws.com/multi-sites-preview.png)

## Creating Templates

Both **editor.com** and **developer.com** will have article template pages, using the same Article object. Start by choosing the **Global Site**. Templates should be created within Global, and then the ownership site set.

In the search tool, click into the **Create** drop-down and select Template. Name your template and choose a content type as `mainContent`. This example uses an existing Article object.

Add a custom path. In the right rail, change the **Owner** from **Global** to the site you are creating the template for. Do the same for the other site, making sure to create it from within **Global**, and set ownership prior to publishing.


![Adding Homepage Multisite](http://docs.brightspot.s3.amazonaws.com/developer-template.png)

In the Create New widget on the dashboard, you should now see two options when viewing within Global, and one for each of the sites. 

## Options

When you create a site you can also specify the access control that is applied as a default. You can also create a default variation for all content created within that site. For more on Variations, see the [dedicated section](variations.html).

![Previewing Options Multisite](http://docs.brightspot.s3.amazonaws.com/multi-sites-options.png)

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
