---
layout: default
title: Storage Item
id: storage-item
---



## Storage Item


### Uploading Files

A file upload UI can be created within Brightspot by using the `StorageItem` return type. This allows a file to be stored either locally, in a database, or using a service such as Amazon S3.

![](http://docs.brightspot.s3.amazonaws.com/storage-item-ui.png)


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

### Storage Item Configuration

The `com.psddev.dari.util.StorageItem` class provides a mechanism for storing
file-based data without worrying about the underlying storage.  The follow
configuration determines where items are stored.

Multiple storage locations can be configured at a time by namespacing it
like `dari/storage/{storageName}/`.

**Key:** `dari/defaultStorage` **Type:** `java.lang.String`

> The name of the default storage configuration item. This will be used by
> `com.psddev.dari.util.StorageItem.Static.create()`.

#### Local StorageItem

StorageItem implementation that stores files on local disk.

**Key:** `dari/storage/{storageName}/class` **Type:** `java.lang.String`

> This should be `com.psddev.dari.util.LocalStorageItem` for local
> filesystem storage.

**Key:** `dari/storage/{storageName}/rootPath` **Type:** `java.lang.String`

> Path to location to store files on the local filesystem.

**Key:** `dari/storage/{storageName}/baseUrl` **Type:** `java.lang.String`

> URL to the document root defined by `rootPath`.

#### Amazon S3 StorageItem

StorageItem implementation that stores files on Amazon S3.

**Key:** `dari/storage/{storageName}/class` **Type:** `java.lang.String`

> This should be `com.psddev.dari.util.AmazonStorageItem.java` for
> Amazon S3 storage.

**Key:** `dari/storage/{storageName}/access` **Type:** `java.lang.String`

> This is your AWS Access Key ID (a 20-character,
> alphanumeric string). For example: AKIAIOSFODNN7EXAMPLE

**Key:** `dari/storage/{storageName}/secret` **Type:** `java.lang.String`

> This is your AWS Secret Access Key (a 40-character string). For example:
> wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY

**Key:** `dari/storage/{storageName}/bucket` **Type:** `java.lang.String`

> The name of the S3 bucket to store objects in.

**Key:** `dari/storage/{storageName}/baseUrl` **Type:** `java.lang.String`

> URL to the bucket root defined by `bucket`.

#### Brightcove StorageItem

**Key:** `dari/storage/{storageName}/class` **Type:** `java.lang.String`

> This should be `com.psddev.dari.util.BrightcoveStorageItem.java.java` for
> Brightcove video storage.

**Key:** `dari/storage/{storageName}/encoding` **Type:** `java.lang.String`

**Key:** `dari/storage/{storageName}/readServiceUrl` **Type:** `java.lang.String`

**Key:** `dari/storage/{storageName}/writeServiceUrl` **Type:** `java.lang.String`

**Key:** `dari/storage/{storageName}/readToken` **Type:** `java.lang.String`

**Key:** `dari/storage/{storageName}/readUrlToken` **Type:** `java.lang.String`

**Key:** `dari/storage/{storageName}/writeToken` **Type:** `java.lang.String`

**Key:** `dari/storage/{storageName}/previewPlayerKey` **Type:** `java.lang.String`

**Key:** `dari/storage/{storageName}/previewPlayerId` **Type:** `java.lang.String`