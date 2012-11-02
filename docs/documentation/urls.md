---
layout: default
title: URLs
id: urls
---

## URLS


**Basic URL creation**

In the [Template Tool](/brightspot-cms/template-tool.html) section we looked at how to construct URLs when creating dynamic templates. These URLs are then automatically generated, based on editor input for each object name. The basic example is to prefix all content with a directory structure, and then attach the specific `objectName` field as the unique URL path. *Note `objectName` is the first `String` field within any Java object you create.*

Brightspot automatically follows SEO practices, lowercasing all words, and adding hyphens where spaces are used by the editor. If a duplicate URL exists a number is added to the end of the URL as a default

Here we have created a URL structure to place all Article content at the URL path `/article`

![](http://docs.brightspot.s3.amazonaws.com/create-url-structure.png)


**Automatic URLs**

When the Editor enters a headline, the `objectName` is used as the URL. See the right rail widget below:
![](http://docs.brightspot.s3.amazonaws.com/auto-url-structure.png)

**Manual URLs**

If a manual URL is required, this can be added, or a vanity URL can be given as an alias:

![](http://docs.brightspot.s3.amazonaws.com/permalink-alias-options.png)


**Dynamic URLs**

Using the Dari Utils library we can create a URL structure for pages, based entirely on editor input, beyond a simple text field. A good example would be using the `ECMAScript` functionality within the template tool to create logic for adding a selected category for an article as part of the URL, `/article/news/objectName` with News being a selected category within the Article object.

![](http://docs.brightspot.s3.amazonaws.com/category-url-added.png)

If we remove the category field, you can see the URL automatically returns to the default structure.

![](http://docs.brightspot.s3.amazonaws.com/category-url-removed.png)

The script below created this feature:


	importPackage(com.psddev.dari.util);

	var category = object.getState().getValue("category");
	if (!ObjectUtils.isBlank(category)) {
	    "/article/" + StringUtils.toNormalized(category.getState().getValue("name")) + "/" + objectName;
	} else {
	    "/article/" + objectName;
	}