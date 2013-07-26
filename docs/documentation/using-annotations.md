---
layout: default
title: Using Annotations
id: using-annotations
section: documentation
---

<div markdown="1" class="span12">

Brightspot uses annotations within the Java class to provide control over data indexing, user actions (Validation, max and min list sizes), the user interface (Ordering and layout of fields), and editorial instructions (help text and notes).

This section looks at some of the most commonly used and helpful annotations. For a full list see the dedicated [Annotations Section](/annotations.html). 


## Data Indexing

The `@Indexed` annotation is key in defining data that you want to be able to query. It is a common mistake to get carried away and add the @Indexed annotation to all the fields on every class you create. Doing so creates extra potentially unnecessary rows in the underlying database and can lead to poor performance in systems with large amounts of data. Only add the annotation to fields that you think you will query on. Note, fields can be returned and rendered without indexing, however in order to query on a field, it must be indexed. In the example below, if we want to query for all Articles where the headline starts with A we would need the field to be `@Indexed`. See the [Querying](/querying.html) documentation for more on this.

<div class="highlight">{% highlight java %}package com.psddev.brightspot;

import com.psddev.cms.db.*;
import com.psddev.cms.tool.*;
import com.psddev.dari.db.*;
import com.psddev.dari.util.*;
import java.util.*;

public class Article extends Content {

    @Indexed
    private String headline;
    private ReferentialText body;
    private List<Tag> tags;
    private Set<String> keywords;

    // Getters and Setters

}
{% endhighlight %}</div>

You can always add the `@Indexed` annotation to a field, and reindex any content that was already added using the `_debug/db-bulk` tool, which you can read about [here](/dari-developer-tools.html#bulk).

## Validation

There are a number of annotations that are designed to help with validation on publish. This helps editors create content that will work, based on front-end limitations such as required fields, maximum character length or list size.

To specify a field as being required simply add `@Required`, or to make sure it remains unique, add `@Indexed(Unique=true)`. To control the size of lists, use `@CollectionMaximum` or `@CollectionMinimum`. To set a soft limit on characters, which offers the user a `Too Long` pop-up, add `@ToolUi.FieldSuggestedMaximum`.

<div class="highlight">{% highlight java %}package com.psddev.brightspot;

import com.psddev.cms.db.*;
import com.psddev.cms.tool.*;
import com.psddev.dari.db.*;
import com.psddev.dari.util.*;
import java.util.*;

public class Athlete extends Content {

    @Indexed(Unique=true)
    private String name;
    @Required
    private String age;
    @CollectionMaximum(5)
    @CollectionMinimum(2)
    private List<Sport> sports;
    @ToolUi.FieldSuggestedMaximum(250)
    private String bio;

    // Getters and Setters

}
{% endhighlight %}</div>

## User Interface

While the properties within the Java class automatically create the user interface, there are several annotations that allow more customization.

Add rich text capabilities to a string field with `@ToolUi.RichText`. This allows formatting of text, but not the addition of enhancements. Leave instructional notes for editors, with `@ToolUi.Note("Note here")` or provide structure with `@ToolUi.Heading("Heading Name")` which creates a horizontal line to break up the interface. You can also move fields into a new tab. Each field with the `@ToolUi.Tab("New Tab")` annotation will be combined on the new tab.

<div class="highlight">{% highlight java %}package com.psddev.brightspot;

import com.psddev.cms.db.*;
import com.psddev.cms.tool.*;
import com.psddev.dari.db.*;
import com.psddev.dari.util.*;
import java.util.*;

public class BlogPost extends Content {

    @ToolUi.RichText
    private String title;
    @ToolUi.Note("Small blurb that appears under the title")
    private String subTitle;
    private ReferentialText body;
    
    @ToolUi.Heading("Social Info")
    private String twitterHandle;
    
    @ToolUi.Tab("Advanced")
    private AdvertPackage adPackage;

    // Getters and Setters

}
{% endhighlight %}</div>


![](http://docs.brightspot.s3.amazonaws.com/ui-annotations.png)