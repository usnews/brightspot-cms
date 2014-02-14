---
layout: default
title: CMS Tags
id: cms-tags
section: documentation
---

<div markdown="1" class="span12">

There are several unique CMS tags that can be used when building the JSP files for your project. These can be included with `<%@ taglib prefix="cms" uri="http://psddev.com/cms" %>`

## cms:img

Used to display an image file added within the CMS. Objects or a URL can be passed in to the src attribute provided the image class contains `getUrl()`.

<div class="highlight">{% highlight java %}
<cms:img src="${content.photo}" size="internalCropName" overlay="true"/>
{% endhighlight %}</div>

<div class="highlight">{% highlight jsp %}    <tag>
        <name>img</name>
        <tag-class>com.psddev.cms.db.ImageTag</tag-class>
        <body-content>empty</body-content>
        <dynamic-attributes>true</dynamic-attributes>
        <attribute>
            <name>src</name>
            <rtexprvalue>true</rtexprvalue>
            <required>true</required>
        </attribute>
        <attribute>
            <name>field</name>
            <rtexprvalue>true</rtexprvalue>
        </attribute>
        <attribute>
            <name>editor</name>
            <rtexprvalue>true</rtexprvalue>
        </attribute>
        <attribute>
            <name>size</name>
            <rtexprvalue>true</rtexprvalue>
        </attribute>
        <attribute>
            <name>width</name>
            <rtexprvalue>true</rtexprvalue>
        </attribute>
        <attribute>
            <name>height</name>
            <rtexprvalue>true</rtexprvalue>
        </attribute>
        <attribute>
            <name>cropOption</name>
            <rtexprvalue>true</rtexprvalue>
        </attribute>
        <attribute>
            <name>resizeOption</name>
            <rtexprvalue>true</rtexprvalue>
        </attribute>
        <attribute>
            <name>tagName</name>
            <rtexprvalue>true</rtexprvalue>
        </attribute>
        <attribute>
            <name>srcAttr</name>
            <rtexprvalue>true</rtexprvalue>
        </attribute>
        <attribute>
            <name>hideDimensions</name>
            <rtexprvalue>true</rtexprvalue>
        </attribute>
        <attribute>
            <name>overlay</name>
            <rtexprvalue>true</rtexprvalue>
        </attribute>
    </tag>{% endhighlight %}</div>

## cms:a

A tag for creating links, much like a normal `a href`. If the object has a defined URL, passing the object itself will be all that is required.

`<cms:a href="${content.link}"></cms:a>`

<div class="highlight">{% highlight java %}<tag>
    <name>a</name>
    <tag-class>com.psddev.cms.db.AnchorTag</tag-class>
    <body-content>JSP</body-content>
    <dynamic-attributes>true</dynamic-attributes>
    <attribute>
        <name>href</name>
        <rtexprvalue>true</rtexprvalue>
        <required>true</required>
    </attribute>
</tag>
{% endhighlight %}</div>

## cms:render

Used to render Strings, ReferentialText, Objects and Areas, it can be implemented in the following way:

#### Rich Text Rendering

<div class="highlight">{% highlight java %}
<cms:render value="${content.bodyText}" />
{% endhighlight %}</div>

This will render any images contained within a `ReferentialText` area, provided a JSP is attached to the Image class as a renderer engine `@Renderer.Path`. This can also render any `Referencable` modules added to the RTE.

#### Rendering objects (Modules / Widgets)

If you have a module (Related Content), with it's own JSP to render it, you can pass that content into a `<cms:render/>` tag, and it will render it:

<div class="highlight">{% highlight java %}

// Example Java Class

public class Article extends Content {

	private String headline;
	private Author author;
	private ReferentialText body;
	private RelatedContent relatedContentModule;
	
	// Getters Setters
}
{% endhighlight %}</div>


<div class="highlight">{% highlight jsp %}

<cms:render value="${content.headline}"/>
<cms:render value="${content.author.name}"/>
<cms:render value="${content.body}"/>
<cms:render value="${content.relatedContentModule}"/>

{% endhighlight %}</div>

Context can also be added as an attribute within the render tag:

<div class="highlight">{% highlight jsp %}
<cms:render context="slideshow" value="${content.image}" />
{% endhighlight %}</div>

This will drive the choice of jsp made on the Java class. See [Contextual Rendering](/contextual-rendering.html) for full documentation on rendering based on context.


#### Text Markers

Text Markers can also be inserted by editors, to create truncation or "Read More" links in the body copy. They can also be used to generate pages for longer bodies of text.

Start by creating a new Referential Text Marker, found at Admin > Settings. 

![](http://docs.brightspot.s3.amazonaws.com/new-text-marker.png)

This adds a new Text Marker as an option to be inserted into any rich text area by an editor. Click on `Marker` to see a list of all available text markers.

![](http://docs.brightspot.s3.amazonaws.com/adding-text-marker.png)

**Truncation**

This example allows editors to add a Truncation marker, where text should truncate when needed. Start by creating a marker in the CMS and name it.

In the JSP where the truncated text should be used, the `cms:render` tag can be updated with the `endMarker` attribute, where the name matches that of the internalName of the text marker in the CMS that was created.

    <cms:render endMarker="truncate" value="${content.body}"/>

**Page Breaks**

In this example, editors can add text markers to denote where new pages should start in long bodies of text. Start by creating the required text marker for page breaks in the CMS.

![](http://docs.brightspot.s3.amazonaws.com/page-break-marker.png)

Next, add a method to determine the PageCount from the object - in this case an Article. This is based on the number of markers that have been added by the editor.

<div class="highlight">{% highlight java %}
public int getPageCount() {

    int count = 1;

    for (Object obj : this.body) {
        if (obj instanceof Reference) {
            Object referenced = ((Reference) obj).getObject();
            if (referenced instanceof ReferentialTextMarker) {
                if ((((ReferentialTextMarker) referenced).getInternalName().equals("pagination-marker"))) {
                    count++;
                }
            }
        }
    }
    return count;
}
{% endhighlight %}</div>

In the jsp rendering the article, get the current page count, and determine the previous/next behaviours. In the example below the `pageCount` method is used to find how many pages there are, and the buttons update accordingly on the page for the user.

<div class="highlight">{% highlight jsp %}
<%  int pageNum = 1;
    String pageNumber = request.getParameter("page");
    if (pageNumber!=null){
        try {
            pageNum = Integer.parseInt(pageNumber);
        }
        catch (Exception e){            
        }
    }
    pageContext.setAttribute("pageNumber", pageNum);
%>

<c:set var="pageCount" value="${content.pageCount}"/>  

<div class="container">
    <h1><cms:render value="${content.headline}"/></h1>
    <h5>Written by: <c:out value="${content.author.name}"/></h5>
    <c:choose>
        <c:when test="${pageCount eq 1}">
            <cms:render value="${content.body}" />                    
        </c:when>
        <c:otherwise>
            <cms:render value="${content.body}"
                beginOffset="${pageNumber < 2 ? '' : pageNumber - 2}"
                endOffset="${pageNumber == pageCount ? '' : pageNumber - 1}"
                beginMarker="${pageNumber < 2 ? '' : 'pagination-marker'}"
                endMarker="${pageNumber == pageCount ? '':'pagination-marker'}" />                    
        </c:otherwise>
    </c:choose>
	    
    <c:if test="${pageCount > 1}">
    <div class="pagination clrfix">
        <ul class="clrfix">
            <li class="prev">
             <c:choose>
                 <c:when test="${pageNumber <= 1}">
                       <a class="prev btn disabled"></a> 
                 </c:when>
                 <c:otherwise>
                    <a class="prev btn" href="${content.permalink}/?page=${pageNumber-1}"></a> 
                 </c:otherwise>
             </c:choose>                   
            </li>
            <li class="status">
                <span class="current">${pageNumber}</span>
                of
                <span class="total">${pageCount}</span>
            </li>
            <li class="next">
             <c:choose>
                 <c:when test="${pageNumber >= pageCount}">
                    <a class="next btn disabled"></a>
                 </c:when>
                 <c:otherwise>
                    <a class="next btn" href="${content.permalink}/?page=${pageNumber+1}"></a>
                 </c:otherwise>
             </c:choose>                   
           </li>
        </ul>
    </div>                 
    </c:if>
	<hr>
</div>
{% endhighlight %}</div>


    
<div class="highlight">{% highlight java %}
<tag>
    <name>render</name>
    <tag-class>com.psddev.cms.db.RenderTag</tag-class>
    <body-content>JSP</body-content>
    <dynamic-attributes>true</dynamic-attributes>
    <attribute>
        <name>area</name>
        <rtexprvalue>true</rtexprvalue>
    </attribute>
    <attribute>
        <name>context</name>
        <rtexprvalue>true</rtexprvalue>
    </attribute>
    <attribute>
        <name>value</name>
        <rtexprvalue>true</rtexprvalue>
    </attribute>
    <attribute>
        <name>beginMarker</name>
        <rtexprvalue>true</rtexprvalue>
    </attribute>
    <attribute>
        <name>beginOffset</name>
        <rtexprvalue>true</rtexprvalue>
    </attribute>
    <attribute>
        <name>endMarker</name>
        <rtexprvalue>true</rtexprvalue>
    </attribute>
    <attribute>
        <name>endOffset</name>
        <rtexprvalue>true</rtexprvalue>
    </attribute>
    <attribute>
        <name>marker</name>
        <rtexprvalue>true</rtexprvalue>
    </attribute>
    <attribute>
        <name>offset</name>
        <rtexprvalue>true</rtexprvalue>
    </attribute>
</tag>
{% endhighlight %}</div>


## cms:context

Use this tag to set the context across an area. See the documentation on [Contextual Rendering](/contextual-rendering.html) for more.

`<cms:context name="module">
<cms:render value="${content.article}"/>
</cms:context>`

Or in a `cms:render` tag: `<cms:render context="module" value="${content.article}"/>`

<div class="highlight">{% highlight java %}
<tag>
    <name>context</name>
    <tag-class>com.psddev.cms.db.ContextTag</tag-class>
    <body-content>JSP</body-content>
    <attribute>
        <name>name</name>
        <rtexprvalue>true</rtexprvalue>
        <required>true</required>
    </attribute>
</tag>
{% endhighlight %}</div>

## cms:cache

Specify a duration (milliseconds) for an item to be cached. Within the CMS Template tool this feature has a UI control element for each section.

`<cms:cache name="${}" duration="60000"> </cms:cache>`

<div class="highlight">{% highlight java %}<tag>
    <name>cache</name>
    <tag-class>com.psddev.cms.db.CacheTag</tag-class>
    <body-content>JSP</body-content>
    <attribute>
        <name>name</name>
        <rtexprvalue>true</rtexprvalue>
        <required>true</required>
    </attribute>
    <attribute>
        <name>duration</name>
        <rtexprvalue>true</rtexprvalue>
        <required>true</required>
    </attribute>
</tag>
{% endhighlight %}</div>

## cms:resource

The `cms:resource` function allows files to be automatically uploaded to your default CDN on their first view.

In your `context.xml` add:

`<Environment name="cms/isResourceInStorage" override="false" type="java.lang.Boolean" value="true" />`

Point to the local file from within your .jsp file. This can be any kind of file, examples: CSS, JavaScript or any image file.

    
`<script src="${cms:resource('path/to/file.js')}"></script>`

`<img src="${cms:resource('/files/images/image.jpg')}" />`


On first view, files that are rendered using the tag will automatically be placed on the default CDN Storage.


On subsequent runs, file changes are automatically detected, and new versions are uploaded to the CDN. CSS files are also parsed at runtime, therefore files contained within CSS, such as background images, are also automatically uploaded

<div class="highlight">{% highlight java %}
<function>
    <name>resource</name>
    <function-class>com.psddev.cms.db.PageFilter</function-class>
    <function-signature>
        java.lang.String getResource(java.lang.String)
    </function-signature>
</function>
{% endhighlight %}</div>

To add https to the resource, simply update your context.xml file:

`https://s3.amazonaws.com/cdn.yoursite.com`

Non http pages can use https but https pages should only use https.

## cms:frame

The `cms:frame` tag allows you to designate an area of a page to be rendered and refreshed independently, without reloading the entire page. Use cases include 'load more' functionality, tabbed content or paginated result sets.

<div class="highlight">{% highlight jsp %}
    <tag>
        <name>frame</name>
        <tag-class>com.psddev.dari.util.FrameTag</tag-class>
        <body-content>JSP</body-content>
        <dynamic-attributes>true</dynamic-attributes>
        <attribute>
            <name>tagName</name>
            <rtexprvalue>true</rtexprvalue>
        </attribute>
        <attribute>
            <name>name</name>
            <rtexprvalue>true</rtexprvalue>
            <required>true</required>
        </attribute>
        <attribute>
            <name>lazy</name>
            <rtexprvalue>true</rtexprvalue>
        </attribute>
        <attribute>
            <name>mode</name>
            <rtexprvalue>true</rtexprvalue>
            <type>com.psddev.dari.util.FrameTag$InsertionMode</type>
        </attribute>
    </tag>
    {% endhighlight %}</div>
    
In the example below, page 2 of the results set will be rendered in the `<cms:frame>` area.

<div class="highlight">{% highlight jsp %}

<a target="results" href="${mainContent.permalink}?page=2">See more results</a>

<cms:frame name="results">

// Logic to return results

</cms:frame>

{% endhighlight %}</div>

Placement of new content, in relation to content already loaded in the frame, is controlled using the `mode` attribute. Options include: `replace, append, prepend`

<div class="highlight">{% highlight jsp %}

<cms:frame mode="append" name="results">
 // New content appears after existing content
</cms:frame>

<cms:frame mode="replace" name="results">
 // New content replaces existing content
</cms:frame>

<cms:frame mode="prepend" name="results">
 // New content appears before existing content
</cms:frame>

{% endhighlight %}</div>

Control of the loading of in frame content can be done through the `lazy` attribute. When true, the ajax request for the in frame content is sent once the page has loaded:

<div class="highlight">{% highlight jsp %}

<cms:frame lazy="true" name="link-module">
 // Content loaded in frame when page has loaded
</cms:frame>


{% endhighlight %}</div>
