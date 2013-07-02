---
layout: default
title: Storage Item
id: storage-item
---

<div markdown="1" class="span12">

## Uploading Files

A file upload UI can be created within Brightspot by using the `StorageItem` return type. This allows a file to be stored either locally, in a database, or using a service such as Amazon S3.

![](http://docs.brightspot.s3.amazonaws.com/storage-item-ui.png)


**Image Object**

Here's an example of an Image object:

<div class="highlight">
{% highlight java %}
@ToolUi.Referenceable
@PreviewField("file")
@Renderer.Path"/WEB-INF/modules/image.jsp")
public class Image extends Content {

    @Required
    private String name;
    private StorageItem file;
    private String altText;

}
{% endhighlight %}
</div>

By defining a renderer script for the file it can be rendered within the Rich Text Editor. The `@ToolUi.Referenceable` annotation allows the Image class to be added to a `ReferentialText` area within the CMS.

To use the storage item to preview the object in the CMS the annotation `@PreviewField("file")` is used, where "file" is the name of the property.

**Bulk Uploads**

Brightspot provides a bulk upload widget on the dashboard, that allows editors to upload multiple files at once. When the object being uploaded contains `@Required` fields, the objects are created in a draft status, and placed in the unpublished drafts widget. where they can have fields updated.

In order to populate required fields on upload, the following can be added to the Image class, making the file name the text for the required field:

<div class="highlight">
{% highlight java %}

    @Override
    public void beforeSave() {
        if (StringUtils.isBlank(name)) {
            if (file != null) {
                Map<String, Object> metadata = file.getMetadata();
                if (!ObjectUtils.isBlank(metadata)) {
                    String fileName = (String) metadata.get("originalFilename");
                    if (!StringUtils.isEmpty(fileName)) {
                        name = fileName;
                    }
                }
            }
        }
    }
{% endhighlight %}
</div>

**Creating the Image JSP**

The Image object .jsp file can be as simple as the example below.

    <cms:img src="${content}" alt="${content.altText}" />

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

<div class="highlight">
{% highlight java %}
	<Environment name="dari/storage/local/class" override="false" type="java.lang.String" value="com.psddev.dari.util.LocalStorageItem" />
	<Environment name="dari/storage/local/rootPath" override="false" type="java.lang.String" value="PATH/webapps/media" />
	<Environment name="dari/storage/local/baseUrl" override="false" type="java.lang.String" value="http://localhost:8080/media" />
{% endhighlight %}
</div>

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
<div class="highlight">
{% highlight java %}
	<Environment name="dari/storage/STORAGE_NAME/class" override="false" type="java.lang.String" value="com.psddev.dari.util.AmazonStorageItem" />
	<Environment name="dari/storage/STORAGE_NAME/baseUrl" override="false" type="java.lang.String" value="CDN_URL" />
	<Environment name="dari/storage/STORAGE_NAME/access" override="false" type="java.lang.String" value="AWS_KEY" />
	<Environment name="dari/storage/STORAGE_NAME/secret" override="false" type="java.lang.String" value="AWS_SECRET" />
	<Environment name="dari/storage/STORAGE_NAME/bucket" override="false" type="java.lang.String" value="BASE_URL" />
{% endhighlight %}
</div>

When attempting to store uploaded files using the Dari `AmazonStorageItem` an error: ClassNotFounfException, `org.jets3t.service.ServiceException`  will appear if the correct dependency is missing from your `pom.xml`.

Add the following Jets3t dependency to resolve this issue:
<div class="highlight">
{% highlight java %}
        <dependency>
            <groupId>net.java.dev.jets3t</groupId>
            <artifactId>jets3t</artifactId>
            <version>0.8.0</version>
        </dependency>
{% endhighlight %}
</div>

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
