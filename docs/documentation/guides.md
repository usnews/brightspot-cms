---
layout: default
title: Implementation Guides
id: implementation-guides
section: documentation
---
<div markdown="1" class="span12">

## Overview

This section provides some insight into best practices for implementing common features using Brightspot and Dari. These guides give a high-level overview of the steps for implementing these features. Brightspot places no limitations on the way to implement the data model, the properties used, or the front-end code.

<h3 id="galleries">Photo Galleries</h3>

Brightspot can be used to create unique gallery experiences such as single [gallery pages](http://www.hgtvgardens.com/flowers-and-plants/20-flowers-and-plants-rabbits-hate), [embedded galleries](http://health.usnews.com/health-news/health-wellness/slideshows/12-spring-superfoods-from-leeks-to-beets) or complete [immersive experiences](http://www.coca-colacompany.com/greencommutinggallery).

The following guide walks through how to create these experiences using Brightspot


**Step One: Create an Image Object**

A gallery will most likely contain a collection of images. Start by creating this Image object. Extend the parent Content class and add some properties to be included on the object. It must include a Dari `StorageItem` class, to upload the image file to the default storage mechanism:

	public class Image extends Content {
	
		private String name;
		private StorageItem file;
		
		// Getters and Setters
	
	}

**Step Two: Create a Gallery Object**

Next step is to create a Gallery object. The example below has a Title and a List of the Image object:

	public class PhotoGallery extends Content {
		
		private String name;
    	private List<Image> images;
   
		// Gettters and Setters
	}

**Step Three: Adding Annotations**

Annotations are used in Brightspot to build out the control for how objects are rendered. They are also used to control the use of classes and their properties (Validation and UI Changes). Start by updating the Image class so it has a JSP attached to render it when it is accessed from within the Gallery experience. Use the `@Renderer.Path` annotation, which points to the JSP to render the Image. The example below also includes the `@Recordable.PreviewField("file")` annotation, which is used to define which field (The Image File) is used to preview within Brightspot:
	
	@Recordable.PreviewField("file")
	@Renderer.Path("/path/to/image.jsp")
	public class Image extends Content {
	
		private String name;
		private StorageItem file;
		
		// Getters and Setters
	
	}
	
The JSP accesses the properties within the Image by name `content.name`. It also uses the `<cms:img>` tag. This automatically recognizes the content has a `StorageItem` and renders it:	

    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
	<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
	<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
	<%@ taglib prefix="cms" uri="http://psddev.com/cms" %>
	
	<cms:img src="${content}" alt="${content.name}"/>

  
To create a Gallery page, a `@Renderer.LayoutPath` annotation should be added to the Gallery object to provide the page structure. 

	@Renderer.LayoutPath("/path/to/page-container.jsp")
	public class PhotoGallery extends Content {
		
		private String name;
    	private List<Image> images;
   
	}


The page should include a `<cms:render>` tag requesting the object `mainContent` included, in this case the Gallery object being accessed:


	<!DOCTYPE html>
	<html>
	<head>
	    <title></title>
	</head>
	<body>
	
	<link rel="stylesheet" type="text/css" href="style.css">
	
	<div class="container">
	    <div class="header"></div>
	        <div class="content"><cms:render value="${mainContent}"/></div>
	    <div class="footer"></div>
	</div>
		
	</body>
	</html>
	
The Gallery must also have it's own `@Renderer.Path` JSP, which renders the properties on the object (The List of images):
	
	@Renderer.LayoutPath("/path/to/page-container.jsp")
	@Renderer.Path("/path/to/gallery-object.jsp")
	public class PhotoGallery extends Content {
		
		private String title;
    	private List<Image> images;
   
	}
	
The JSP can use JSTL to loop over the list of Images and render them using custom HTML and CSS:

	<h1><cms:render value="${content.name}"/></h1>
	<c:forEach items="${content.images}" var="image" >
	    <div>
	        <cms:img src="${image}"/>     
	    </div>
	</c:forEach>


The Gallery object can have a URL set on it manually in the CMS, or configure dynamic URL creation using [these guidelines](/urls.html).

The Gallery user interface in Brightspot will look like the example below:

![](http://docs.brightspot.s3.amazonaws.com/new-gallery-creation.png)

**Step Four: Galleries as Modules**

Galleries can be enabled to be used within bodies of text, as enhancements or as modules. To do so, simply add two new annotations to the PhotoGallery:

Add `@Renderer.Paths`, (plural) which allows multiple rendering JSP files to be given to an object. See example below. Also add `@ToolUi.Referenceable` so that it can be chosen in a Rich Text Area as an enhancement:

	@Renderer.LayoutPath("/path/to/page-container.jsp")
	@Renderer.Paths ({
		@Renderer.Path(value = "/path/to/gallery-object.jsp"),
		@Renderer.Path(context = "module", value="/path/to/gallery-module.jsp")
	})
	@ToolUi.Referenceable
	public class PhotoGallery extends Content {
		
		private String title;
    	private List<Image> images;
   
	}
	
The new `gallery-module.jsp` is called when the context flag matches `module`. That `gallery-module.jsp` may have different CSS or HTML for rendering content in a body of an article. The context flag is an attribute of the `<cms:render>` tag. Example of an Article object JSP below:

	<h1><cms:render value="${content.headline}"/></h1>
	
	<cms:render context="module" value="${content.bodyText}"/>
	
Any Gallery objects added to the Rich Text Editor and rendered in `${content.bodyText}` will be done with the context flag `module`.

**Working Examples**

The Coca-Cola Company website features an Immersive Gallery experience with a very rich and interactive design. The images display in full browser width, and upon hover the enhanced gallery tools such as Comments, Social Sharebar, and related content display over the image. 

[See the Immersive Gallery](http://www.coca-colacompany.com/greencommutinggallery)

They also provide editors with the ability to add Galleries inline into their article text.

[Inline Gallery Module](http://www.coca-colacompany.com/stories/happiness-from-the-skies-watch-coke-drones-refresh-guest-workers-in-singapore)

HGTV Gardens website features a photo album page that implements the enhanced photo gallery within the website design context. The user can click through the images of the album, view the thumbnails of the images, use the Social features, and comment on the album. 

[See the Gallery Page](http://www.hgtvgardens.com/flowers-and-plants/20-flowers-and-plants-rabbits-hate)




</div>


