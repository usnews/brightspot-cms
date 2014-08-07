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


	<cms:img src="${content.photo}" size="internalCropName" overlay="true"/>


The `cms:img` tag has a number of attributes that can be set within the tag to specify how the image being referenced should display. Below are some more frequently used attributes and their descriptions:

**src:** This is the property defined by the StorageItem type. An object or a URL can be passed in, and the tag automatically renders a StorageItem attached: 

	 public class Author extends Content {
	
		private String name;
		private StorageItem picture;
		// private Image picture;
	
		// Getters and Setters
		
		}



In the example code above, there is a StorageItem property of the Author class that can be passed in the src attribute of the image tag as 
`<cms:img src="${content.picture}"/>`

To pass an object in the src attribute, start by creating the object class. The example code below is for an Image object.


	@Renderer.Path("/image.jsp")
	public class Image extends Content {
	
	    private String name;
	    private StorageItem file;
	
	    // Getters and Setters
	}


In the Image JSP file, image.jsp, the object is referenced in the cms:img tag as `<cms:img src="${content}"/>`



- **editor:** Sets the name of the image editor to use 
- **size:** Sets the internal name of the image size to use. This is usually a pre-set image crop size. For example in the code above, `<cms:img src="${content.photo}" size="internalCropName" overlay="true"/>` the crop size is `internalCropName` which is a crop that is set up in the CMS with specified dimensions. 
Information on how to create crop sizes is located [here](/image-cropping.html). 
- **width:** This is used to override the width provided by the image size
- **height:** This is used to override the height provided by the image size
- **cropOption:** This is used to override the crop settings provided by the image size attribute
- **resizeOption:** This is used to override the resize settings provided by the image size
- **hideDimensions:** When set to true, suppresses the "width" and "height" attributes from the final HTML output
- **overlay:** Indicates whether an image object has an overlay object so that it is displayed in the HTML output. The overlay text is added when the image crop is selected on the image in the CMS. Overlay text is part of the [ImageCrop](http://www.brightspotcms.com/javadocs/) class.  


	<tag>
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
    </tag>


##cms:a

A tag for creating links, much like a normal `a href`. If the object has a defined URL, passing the object itself will be all that is required: 
`<cms:a href="${content.link}"></cms:a>`.
If the object does not have a permalink or a defined URL, only a string is displayed, and it is not linked. Below are the attributes of the `<cms:a>`: 

	<tag>
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

## cms:render

Used to render Strings, ReferentialText, Objects and Areas, it can be implemented in the following way:

**Rich Text Rendering**

	<cms:render value="${content.bodyText}" />

This will render any images contained within a `ReferentialText` area, provided a JSP is attached to the Image class as a renderer engine `@Renderer.Path`. This can also render any `Referencable` modules added to the RTE.

**Rendering objects (Modules/Widgets)**

If you have a module (Related Content), with it's own JSP to render it `@Renderer.Path()`, you can pass that content into a `<cms:render/>` tag, and it will render it:

	// Example Java Class

	public class Article extends Content {

		private String headline;
		private Author author;
		private ReferentialText body;
		private RelatedContent relatedContentModule;
	
		// Getters Setters
	}


Example JSP

	<!-- Example JSP -->
	<cms:render value="${content.headline}"/>
	<cms:render value="${content.author.name}"/>
	<cms:render value="${content.body}"/>
	<cms:render value="${content.relatedContentModule}"/>


Context can also be added as an attribute within the render tag:

	<cms:render context="slideshow" value="${content.image}" />

This will drive the choice of jsp made on the Java class. See [Contextual Rendering](/contextual-rendering.html) for full documentation on rendering based on context, allowing multiple rendering JSP files to be provided to an object.

**Text Markers**

Text Markers can also be inserted by editors, to create truncation or "Read More" links in the body copy. They can also be used to generate pages for longer bodies of text.

Start by creating a new Referential Text Marker, found at Admin > Settings. 

![](http://docs.brightspot.s3.amazonaws.com/new-text-marker.png)

This adds a new Text Marker as an option to be inserted into any rich text area by an editor. Click on `Marker` to see a list of all available text markers.

![](http://docs.brightspot.s3.amazonaws.com/adding-text-marker.png)

*Truncation*

This example allows editors to add a Truncation marker, where text should truncate when needed. Start by creating a marker in the CMS and name it.

In the JSP where the truncated text should be used, the `cms:render` tag can be updated with the `endMarker` attribute, where the name matches that of the internalName of the text marker in the CMS that was created.

	<cms:render endMarker="truncate" value="${content.body}"/>
	
*Page Breaks*

In this example, editors can add text markers to denote where new pages should start in long bodies of text. Start by creating the required text marker for page breaks in the CMS.

![](http://docs.brightspot.s3.amazonaws.com/page-break-marker.png)

Next, add a method to determine the PageCount from the object - in this case an Article. This is based on the number of markers that have been added by the editor.

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

In the jsp rendering the article, get the current page count, and determine the previous/next behaviours. In the example below the `pageCount` method is used to find how many pages there are, and the buttons update accordingly on the page for the user.

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

The cms:render tag has the following attributes: 

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
        <name>markerCount</name>
        <rtexprvalue>true</rtexprvalue>
    </attribute>
    <attribute>
        <name>offset</name>
        <rtexprvalue>true</rtexprvalue>
    </attribute>
	</tag>

## cms:context

Use this tag to set the rendering context across an area. See the documentation on [Contextual Rendering](/contextual-rendering.html) for more.
For example, given the following Article.java:

    @Renderer.Path(value = "article.jsp")
    class Article {
        Image mainImage;
    }

And Image.java:

    @Renderer.Paths({
    @Renderer.Path(value = "image-in-article.jsp", context = "article"),
    @Renderer.path(value = "image.jsp")})
    class Image { ... }
    
The cms:render tag in the following article.jsp would use image-in-article.jsp to render the Image instance because the renderer annotation specifies the image-in-article JSP when the context is an article.

    <cms:context name="article">
        <cms:render value="${article.mainImage}" />
    </cms:context>

Or in a `cms:render` tag: `<cms:render context="article" value="${article.mainImage}"/>`

The cms:context tag has the name attribute:

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

##cms:cache

Specify a duration (milliseconds) for an item to be cached. Within the CMS Template tool this feature has a UI control element for each section.

`<cms:cache name="${}" duration="60000"> </cms:cache>`

Below are the attributes of the `<cms:cache>` tag:

	<tag>
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

## cms:resource

The `cms:resource` function allows files to be automatically uploaded to your default CDN on their first view.

In your `context.xml` add:
`<Environment name="cms/isResourceInStorage" override="false" type="java.lang.Boolean" value="true" />`

Point to the local file from within your .jsp file. This can be any kind of file, examples: CSS, JavaScript or any image file.
`<script src="${cms:resource('path/to/file.js')}"></script>`

`<img src="${cms:resource('/files/images/image.jpg')}" />`


On first view, files that are rendered using the tag will automatically be placed on the default CDN Storage.

On subsequent runs, file changes are automatically detected, and new versions are uploaded to the CDN. CSS files are also parsed at runtime, therefore files contained within CSS, such as background images, are also automatically uploaded.

	<function>
    	<name>resource</name>
    	<function-class>com.psddev.cms.db.PageFilter</function-class>
    	<function-signature>
        java.lang.String getResource(java.lang.String)
    	</function-signature>
	</function>

To add https to the resource, simply update your context.xml file:

`https://s3.amazonaws.com/cdn.yoursite.com`

Non http pages can use https but https pages should only use https.

## cms:frame

The `cms:frame` tag allows you to designate an area of a page to be rendered and refreshed independently, without reloading the entire page. Use cases include 'load more' functionality, tabbed content or paginated result sets. Here are the frame tag attributes: 

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

In the example below, page 2 of the results set will be rendered in the `<cms:frame>` area.

	<a target="results" href="${mainContent.permalink}?page=2">See more results</a>

	<cms:frame name="results">

	    <!-- Logic to return results -->

	</cms:frame>

Placement of new content, in relation to content already loaded in the frame, is controlled using the `mode` attribute. Options include: `replace, append, prepend`

	<cms:frame mode="append" name="results">
    <!-- New content appears after existing content -->
	</cms:frame>

	<cms:frame mode="replace" name="results">
    <!-- New content replaces existing content -->
	</cms:frame>

	<cms:frame mode="prepend" name="results">
    <!-- New content appears before existing content -->
	</cms:frame>

Control of the loading of in frame content can be done through the `lazy` attribute. When true, the ajax request for the in frame content is sent once the page has loaded:

	<cms:frame lazy="true" name="link-module">
    <!--Content loaded in frame when page has loaded -->
	</cms:frame>


## cms:local

The `<cms:local>` tag is used to specify the value of a content property within a section of the JSP code. For example in the code below, the `<cms:render>` tag is used to render the value in `${content}` but in the `<cms:local>` tags, the value in `${content}` is set to a new value, that value is rendered only within the cms:local tag. 

    <cms:render value="${content}" /><!-- outputs oldValue -->
    <cms:local>
	    <cms:attr name="content" value="newValue" />
	    <cms:render value="${content}" /><!-- outputs newValue -->
    </cms:local>
    <cms:render value="${content}" /><!-- outputs oldValue -->

A simple way to understand the cms:local tag is to know that within the tags a variable can have a new value but in other parts of the code, the variable has the regular value. 
The `<cms:local>` tag is used with the `<cms:attr>` tag. In the cms:local tag, the cms:attr tag is used to set the new value of the content being previously rendered. 

## cms:form

The `<cms:form>` is generally best used for submitting User Generated Content via a form. The cms:form tag has similar attributes to the HTML form tag. 
The `<cms:form>` tag has the following attributes available:

- id: the name of the form
- class: the style class for the form
- method: either a POST or GET method
- action: the URL where to display the response after the form has been processed
- enctype: the encryption type when submitting form data
- processor: specifies the class that processes the form 
- varProcessor: defines the name of the variable that should contain an instance of the FormProcessor object
- varSuccess: defines the name of the page-scoped variable that stores the form processing success flag i.e. true/false
- varError: defines the name of the page-scoped variable that stores the form processing error object if something goes wrong
- varResult: defines the name of the page-scoped variable that stores the form processing result, i.e. the value returned from the process method of FormProcessor

Not all the attributes available must be used in the tag; the varProcessor, varSuccess, varError, varResult attributes are convenience attributes used to access and display the outcome of the process method. If the varSuccess field returns true, then the varResult field will be populated, if varSuccess returns false, the varError field will be populated. 

When using the cms:form tag, a form processor class needs to be created. The class needs to implement FormProcessor which is a Dari class. A basic implementation is as follows:

Creating the form processor class:

    public class myFormProcessor implements FormProcessor {
    
    //some processing logic and returns a value
    
    }


The Javadoc documenting the FormProcessor class is located [here](http://www.dariframework.org/javadocs/com/psddev/dari/util/FormProcessor.html). 

The form javascript:

    <cms:form id="myForm" method="POST" action="/formresult" processor="myFormProcessor">
        <input name="id" type="text">
        <input type="submit" value="Submit">
    </cms:form>
    

## cms:input

The `<cms:input>` tag is often used in conjunction with the `<cms:form>` tag. Within the cms:form tag, `<cms:input>` is used to collect data from the user. It can be wrapped in css stylings. Below is an example of its use:

    <div class="my-style">
        <cms:input name="title" />
        <cms:input name="author" />
        <cms:input name="date" />
    </div>
    
In the example above, the `name` attribute is used to specify the name of the field. For the input collection to actually function, the processor class on the parent cms:form tag needs to extend the FormWriter class as well as implement the FormProcessor, alternatively the processor class may simply extend the AbstractForm class. If neither of these are done, then the `writerClass` attribute will have to be specified in the tag. 

    <cms:input name="title" writerClass="com.example.MyFormWriter" />
    
Below are the attributes of the `<cms:input>` tag:

- name: the field name to be entered
- writerClass: an object of FormWriter, see class documentation [here](http://www.dariframework.org/javadocs/com/psddev/dari/db/FormWriter.html)
- object: specifies the Record whose field is being written. If this is not specified, the cms:input tag is smart enough to reference it from the parent cms:form tag. 



