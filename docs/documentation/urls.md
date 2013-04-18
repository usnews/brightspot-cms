---
layout: default
title: URLs
id: urls
section: documentation
---
<div markdown="1" class="span12">

## Overview

URLs are typically generated based on text input by an editor. A basic example is to prefix all content with a directory structure, and then attach specific field data, from the content, such as a headline or name as the unique URL path. *Example: /blogs/blog-headline*

Brightspot automatically follows SEO practices, applying lowercase to all words, and adding hyphens where spaces are used by the editor. If a duplicate URL exists a number is added to the end of the URL as a default.

The documentation below is for version 2.1 of Brightspot CMS, for 2.0 URL documentation, see the [dedicated section](urls-2.0.html).

## Creating URLs

When a template is created within Brightspot it must have a content type chosen as `mainContent`. This class will contain the logic that automatically generates a URL for a user when creating a new page using the template.

In the example below, an Article class is the `mainContent` being used for a Template. 

#### Step 1. Implement Directory.Item

As well as extending content, the class being used to create the URL permalink must implement `Directory.Item`.

{% highlight java %}public class Article extends Content implements Directory.Item {

  private String headline;
  private ReferentialText body;
  private Author author;

  // Getters and Setters
}
{% endhighlight %}

#### Step 2. Using createPermalink

For this example, the Article class `headline` field will be used to create the URL. A directory structure is also added by prefixing the headline field with `/article/`. The `Site` object is used to set the context of which site is being used, which comes into play when using [Multi-Site](multi-site.html). The example also shows a null check.

{% highlight java %}public class Article extends Content implements Directory.Item {

  private String headline;
  private ReferentialText body;
  private Author author;

  // Getters and Setters

  @Override
  public String createPermalink(Site site) {

      if (this.getHeadline() != null){
                return "/article/" + StringUtils.toNormalized(headline);
      } else {  
         return null;
      }    
  }

}
{% endhighlight %}

![](http://docs.brightspot.s3.amazonaws.com/creating-urls-2.1.png)



## Creating Dynamic URLs

You can create a URL structure beyond a simple text field. A good example would be using another property on the object to structure the URL. In the example below the category object on the article is used to structure a new URL by adding a new directory. The category object contains a string field, `Name`.


{% highlight java %}public class Article extends Content implements Directory.Item{

  private String headline;
  private ReferentialText body;
  private Author author;
  private Category category;

  // Getters and Setters

  @Override
  public String createPermalink (Site site) {
        
  Category category = this.getCategory();
        if (!ObjectUtils.isBlank(category)) {
        	return "/article/" + StringUtils.toNormalized(category.getName()) + "/" + StringUtils.toNormalized(headline);
        } else {
        	return "/article/" + StringUtils.toNormalized(headline);
        }
    }
{% endhighlight %}

The addition of the category in the Article adds the category name to the URL.

![](http://docs.brightspot.s3.amazonaws.com/urls-2.1-category.png)

Removing or changing the category changes the URL automatically.

![](http://docs.brightspot.s3.amazonaws.com/urls-2.1-category-missing.png)

## URL Options

As well as the permalink that can be given to content, Brightspot allows editors to create aliases or redirects for each piece of content.

![](http://docs.brightspot.s3.amazonaws.com/urls-2.1-alias-redirect.png)

#### Alias

This url is a new point of access to the page, allowing a shorter, custom or vanity URL to be given for a piece of content. For example, in the shot above, `/alias-for-vanity-urls` will return the same page as `/article/category/this-is-our-headline`. The address bar for the user will show `/alias-for-vanity-urls`.

#### Redirect

This url will direct users to the permalink for the page. Perfect for when content moves, or is removed, and an existing URL needs to be redirected to new content. In the example above, `/this-url-redirects` will redirect users to `/article/category/this-is-our-headline` with the address bar url changing also.

</div>