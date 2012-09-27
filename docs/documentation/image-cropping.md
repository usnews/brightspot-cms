---
layout: default
title: Image Cropping
id: image-cropping
---

## Image Cropping

Brightspot uses DIMS to manipulate images within the CMS. 

Download and install DIMS [here](https://github.com/beetlebugorg/mod_dims)

Configuration is managed through your context.xml. Define your `baseUrl` including the App ID and the `sharedSecret`.

    <!-- DIMs -->
    <Environment name="dari/defaultImageEditor" override="false" type="java.lang.String" value="dims" />
    <Environment name="dari/imageEditor/dims/class" override="false" type="java.lang.String" value="com.psddev.dari.util.DimsImageEditor" />
    <Environment name="dari/imageEditor/dims/baseUrl" override="false" type="java.lang.String" value="http://example.com/dims4/APP_ID />
    <Environment name="dari/imageEditor/dims/sharedSecret" override="false" type="java.lang.String" value="S3cret_H3re" />
    <Environment name="dari/imageEditor/dims/quality" override="false" type="java.lang.Integer" value="90" />

**Building the Image Class**

Various crops will be used throughout a website, for example, the desired image size for a blog post will be different from a Author Bio picture. Before looking at how to add new crops, make sure your Image object contains all the right references.

Start by adding the `@ToolUi.Referenceable` class annotation, so the Image can be referenced from within the Rich Text Editor, and added as an enhancement. Next, attached a specific .jsp file to render the image. See example below:


	@ToolUi.Referenceable
	@Renderer.Engine("JSP")
	@Renderer.Script("/WEB-INF/modules/image.jsp")

	public class Image extends Content {

	private StorageItem file;
	private String altText;

	}

**Creating the Image JSP**

The Image object .jsp file can be as simple as the example below. The size refers to the internal crop name which we will provide from within the CMS UI, and that we will refer to within the object .jsp.

    <cms:img src="${content}" size="${imageSize}" alt="${content.altText}" />
    
**Adding the Crop**

From within BrightSpot, we need to create the required image crop size. Access Admin -> Settings and in the left hand rail select New Standard Image Size. We have created a crop for use with all blog post images, the internal name is `blogCrop`.

The simplest implementation does not require any other option to be selected.

![](http://docs.brightspot.s3.amazonaws.com/new-crop.png)


**Creating the Blog Page JSP**

We have defined a crop for our Blog Posts, and within the CMS UI, when adding an image, the crop option Blog Post Crop now appears. 

![](http://docs.brightspot.s3.amazonaws.com/crop-ui-choice.png)

The .jsp file used to render the Blog post object will reference the crop that is desired directly. We use the `<cms:render` tag to render the Rich Text area `blog.body`, and any images added within it as enhancements are rendered.

    <%@ include file="/WEB-INF/modules/includes.jsp" %>

    <c:set var="imageSize" value="blogCrop" scope="request" />

        <div>
          <cms:render value="${blog.body}" />
	  	</div>