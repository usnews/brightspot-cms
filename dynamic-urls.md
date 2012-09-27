---
layout: default
title: Dynamic URLs
id: dynamic-urls
---

### Dynamic URLs

By importing Dari Utils, we are able to utlize ObjectUtils and StringUtils classes, to dynamically change the way our URL structure is created.

Within the Template tool, using EMCAScript, URLs can be created dynamically using CMS Object Names with the following :`"/article/" + objectName;`.

Below we have given another example. In this instance the Object Name is the default URL structure, unless an author has been selected, at which point the first and last names become the URL structure.

![Mark up shot](http://docs.brightspot.s3.amazonaws.com/full-page-7.png)

    importPackage(com.psddev.dari.util);

	var author = object.getState().getValue("author");
	if (!ObjectUtils.isBlank(author)) {
    "/" + StringUtils.toNormalized(author.getState().getValue("firstName")) +StringUtils.toNormalized(author.getState().getValue("lastName")) + "/" + objectName;
	} else {
    "/fullpage/" + objectName;
	}