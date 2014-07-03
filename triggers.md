---
layout: default
title: Triggers
id: triggers
section: documentation
---

<div markdown="1" class="span12">

Through annotations, validation of data within properties can be controlled in Brightspot. While this accounts for many of the common uses cases, see example, [Validation](/using-annotations.html) there are other instances, where data needs to be validated, modified or thrown-out when content is saved.

### beforeSave

Some of the most common uses for `beforeSave()` are the transformation of data and the population of hidden fields. In the example below, a hidden field called `internalName` is populated with a normalized version of the `name` field data added by the editor.

	public class Project extends Content {

    	@Required
    	private String name;

    	@Indexed
    	@ToolUi.Hidden
    	private String internalName;

		// Getters and Setters

    	@Override
    	public void beforeSave() {
        	this.internalName = StringUtils.toNormalized(name);
    	}

    	public String toString() {
        	return internalName;
    	}

	}

Looking at the Raw Data of the object shows the `internalName` field populated in a normalized way.

![](http://docs.brightspot.s3.amazonaws.com/before-save-raw.png)

#### afterSave

The afterSave method specifies action to be performed after the record is saved. A common use for it is to log data, or send notification once the record has been saved. To use the example above, after saving the project record, the `name` field entered by the editor is saved as  `internalName` instead of the normalized value:

    public void afterSave() {
        this.internalName = name;
    }
    
#### beforeDelete

The beforeDelete method specifies action to be performed before the record is deleted. Often times it is used to verify and validate data before the record is deleted. Following the examples above, the method could check if the record is not null before deleting it.

#### afterDelete

The afterDelete method specifies action to be performed after the record is deleted. A use for this is to query for other locations of the content, and delete them all. 

</div>

