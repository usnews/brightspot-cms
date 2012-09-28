---
layout: default
title: Storage Item
id: storage-item
---



## Storage Item


### Uploading Files

A file upload UI can be created within Brightspot by using the `StorageItem` return type. This allows a file to be stored either locally, in a database, or using a service such as Amazon S3.

![](http://docs.brightspot.s3.amazonaws.com/storage-item-ui.png)

The configurations for this storage can be found [here](http://www.dariframework.org/configuration.html#configuration_settings), in the Dari Framework documentation.


**Image Object**

Here's an example of an Image object:

    @ToolUi.Referenceable
    @Renderer.Engine("JSP")
    @Renderer.Script("/WEB-INF/modules/image.jsp")
	public class Image extends Content {

        private StorageItem file;
        private String altText;

    }


By defining a renderer script for the file it can be rendered within the Rich Text Editor. The `@ToolUi.Referenceable` annotation allows the Image class to be added to a `ReferentialText` area within the CMS.