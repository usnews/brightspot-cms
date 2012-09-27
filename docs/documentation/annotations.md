---
layout: default
title: Annotations
id: annotations
---


## Tool UI

The @ToolUi Library gives you more options for controlling the UI display in BrightSpot. It must be imported from:

 `import com.psddev.cms.db.ToolUi;`

**@ToolUi.Note**

To provide the user with an annotation for a field simply use `@ToolUi.Note`. Within the UI it will appear above the specified field.

<img class="smaller" src="http://docs.brightspot.s3.amazonaws.com/annotate.png">

    @ToolUi.Note("Leave blank to use the name.")
    private String title;

You can also add the `@ToolUi.Note` annotation to a class, to provide a Note for that object type within the CMS.

    @ToolUi.Note("This is an instructional note for the Content object.")
    public class Article extends Content {

**@ToolUi.Heading**

This annotation provides a horizontal rule within the Content Object, allowing new sections to be created with headings.

    @ToolUi.Heading("This is a section heading")
    private ReferentialText body;


**@ToolUi.Hidden** 

By using  `@ToolUi.Hidden` a target field can be hidden from the UI. Below we have hidden the `oldContent` 

    public class Article extends Content {
        
        @ToolUi.Hidden
        private String oldContent;


**@ToolUi.OnlyPathed**

If you want the target field to only contain objects with a path `@ToolUi.OnlyPathed` can be used.

**@ToolUi.ReadOnly**

Specifies that the target field is read-only.


**@ToolUi.Referenceable**

Adding `@ToolUi.Referenceable` specifies whether the instance of the target type can be referenced (added) by a referential text object (rich text editor).

**@ToolUi.CompatibleTypes** 

`@ToolUi.CompatibleTypes` Specifies an array of compatible types that the target type may switch to.

Within the CMS, the UI updates to show a drop down of available content types.

**@ToolUi.FieldSuggestedMaximum**

This annotation is used to indicate a suggested upper limit on the length of the field.

    @ToolUi.FieldSuggestedMaximum(50)
    private String title;

The value passed to the annotation is the limiting value.  When a user is modifying a field annotated with `@ToolUi.FieldSuggestedMaximum`, an indicator will appear when the input size has exceeded the specified limit.

![Screenshot of text field shorter than suggested maximum](http://docs.brightspot.s3.amazonaws.com/cms_1.5.0_soft_validation_below_maximum.png)

Similarly, the indicator will disappear as the user removes content from the input field, dropping it below the specified limit.

![Screenshot of text field longer than suggested maximum](http://docs.brightspot.s3.amazonaws.com/cms_1.5.0_soft_validation_above_maximum.png)

**@ToolUi.FieldSuggestedMinimum**

This annotation is used to indicate a suggested lower limit on the length of the field.

    @ToolUi.FieldSuggestedMinimum(3)
    private String title;

The value passed to the annotation is the limiting value.  When a user is modifying a field annotated with `@ToolUi.FieldSuggestedMinimum`, and indicator will appear whtn the input size falls below the specified limit.

![Screenshot of text field shorter than suggested minimum](http://docs.brightspot.s3.amazonaws.com/cms_1.5.0_soft_validation_below_minimum.png)

Similarly, the indicator will disappear when the field length exceeds the specified limit.  

![Screenshot of text field shorter than suggested minimum](http://docs.brightspot.s3.amazonaws.com/cms_1.5.0_soft_validation_above_minimum.png)