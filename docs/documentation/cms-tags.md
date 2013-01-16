---
layout: default
title: CMS Tags
id: cms-tags
---

## CMS Tags

There are several unique CMS tags that can be used when building the JSP files for your project. These can be included with `<%@ taglib prefix="cms" uri="http://psddev.com/cms" %>`

**cms:img**

Used to display an image file added within the CMS. Objects or a URL can be passed in to the src attribute provided the image class contains `getUrl()`.

`<cms:img src="${content.photo}" size="internalCropName"/>`

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
    </tag>


**cms:a**

A tag for creating links, much like a normal `a href`. If the object has a defined URL, passing the object itself will be all that is required.

`<cms:a href="${objectName}"></cms:a>`
    
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

**cms:render**

Used to render areas of ReferentialText, it can be implemented in the following way:

`<cms:render value="${content.bodyText}" />

This will render any images contained within a `ReferentialText` area, provided a JSP is attached to the Image class as a renderer engine. This can also render any `Referencable` modules added to the RTE.

**cms:cache**

Specify a duration (milliseconds) for an item to be cached. Within the CMS Template tool this feature has a UI control element for each section.

`<cms:cache name="${}" duration="60000"> </cms:cache>`


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
    
**cms:resource**

The `cms:resource` function allows files to be automatically uploaded to your default CDN on their first view.

Point to the local file from within your .jsp file. This can be any kind of file, examples: CSS, JavaScript or any image file.


    <link rel="stylesheet" href="<c:url value="${cms:resource('/main.css')}"/>"/>
    
`<script src="${cms:resource('path/to/file.js')}"></script>`

`<img src="${cms:resource('/files/images/image.jpg')}" />`


On first view, files that are rendered using the tag will automatically be placed on the default CDN Storage.


On subsequent runs, file changes are automatically detected, and new versions are uploaded to the CDN. CSS files are also parsed at runtime, therefore files contained within CSS, such as background images, are also automatically uploaded
	
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