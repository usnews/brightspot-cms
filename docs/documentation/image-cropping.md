---
layout: default
title: Adding and Editing Images
id: image-cropping
section: documentation
---

<div markdown="1" class="span12">

Brightspot uses DIMS to manipulate images within the CMS. You can store and render images using local storage, which is defined in your `context.xml`, however in order to apply crops, resizes or advanced image editing capabilities, DIMS must be used.

Download and install DIMS [here](https://github.com/beetlebugorg/mod_dims)

Configuration is managed through your context.xml. Define your `baseUrl` including the App ID and the `sharedSecret`.

<div class="highlight">{% highlight java %}
<!-- DIMs -->
<Environment name="dari/defaultImageEditor" override="false" type="java.lang.String" value="dims" />
<Environment name="dari/imageEditor/dims/class" override="false" type="java.lang.String" value="com.psddev.dari.util.DimsImageEditor" />
<Environment name="dari/imageEditor/dims/baseUrl" override="false" type="java.lang.String" value="http://example.com/dims4/APP_ID" />
<Environment name="dari/imageEditor/dims/sharedSecret" override="false" type="java.lang.String" value="S3cret_H3re" />
<Environment name="dari/imageEditor/dims/quality" override="false" type="java.lang.Integer" value="90" />
{% endhighlight %}</div>

## Building the Image Class

Various crops will be used throughout a website, for example, the desired image size for a blog post will be different from a Author Bio picture. Before looking at how to add new crops, make sure your Image object contains all the right references.

Start by adding the `@ToolUi.Referenceable` class annotation, so the Image can be referenced from within the Rich Text Editor, and added as an enhancement. Next, attached a specific .jsp file to render the image. See example below:

<div class="highlight">{% highlight java %}@ToolUi.Referenceable
@Renderer.Script("/WEB-INF/common/image.jsp")
public class Image extends Content {

	private StorageItem file;
	private String altText;

}
{% endhighlight %}</div>

## Creating the Image JSP

The Image object .jsp file can be as simple as the example below. The size refers to the internal crop name which we will provide from within the CMS UI, and that can be refered to within the object .jsp. Note the use of the `cms:img` tag.

<div class="highlight">{% highlight java %}
<cms:img src="${content}" size="${imageSize}" alt="${content.altText}" />
{% endhighlight %}</div>

![](http://cdn.psddev.com/88/07/ee4cb627440e9c6e1fedcace1cab/text-overlay-20.55.44%20AM.png)

Editors within the CMS can now add rich text overlays on any image. In order to enable this ability, when using the `cms:img` tag simply add the `overlay=true` parameter:

<div class="highlight">{% highlight java %}
<cms:img src="${content.leadImage}" size="productLeadImage" overlay="true" />
{% endhighlight %}</div>

## Adding the Crop

From within Brightspot, you need to create the required image crop size. Access Admin -> Settings and in the left hand rail select New Standard Image Size. Create a crop for use with all blog post images, the internal name is `blogCrop`.

The simplest implementation does not require any other option to be selected.

![](http://docs.Brightspot.s3.amazonaws.com/new-crop.png)


## Creating the Blog Page JSP

Once a crop has been created within the CMS it appears alongside the image editing UI. Crops of the same aspect ratio are grouped together. To associate a particular crop with an object (so editors are given the only crops that are applicable, use the `@ToolUi.StandardImageSizes` annotation on your object, with the internal name of the crop that should be applied. Note that as default, all crops are shown, without the need for annotations associating.

![](http://docs.brightspot.s3.amazonaws.com/image_crop_ui_2.1.png)

The .jsp file used to render the Blog post object will reference the crop that is desired directly. Use the `<cms:render>` tag to render the Rich Text area `blog.body`, and any images added within it as enhancements are rendered.

<div class="highlight">{% highlight java %}
<%@ include file="/WEB-INF/modules/includes.jsp" %>

<c:set var="imageSize" value="blogCrop" scope="request" />

<div>
<cms:render value="${blog.body}" />
</div>
{% endhighlight %}</div>