---
layout: default
title: Annotations
id: annotations
---

## Annotations

Annotations provide information on how a particular field or model class
should behave. The most commonly used annotations are `@Indexed` and
`@Embedded`.

### Field Annotations

`@InternalNamePrefix(String)`

> Specifies the prefix for the internal names of all fields in the target type.

`@CollectionMaximum(int)`

> Specifies the maximum number of items in the target field.

`@CollectionMinimum(int)`

> Specifies the minimum number of items in the target field.

`@DisplayName(String)`

> Specifies the target field's display name.

`@Embedded`

> Specifies whether the target field value is embedded. This can also be applied at a class level.

`@Ignored`

> Specifies whether the target field is ignored.

`@Indexed`

> Specifies whether the target field value is indexed.

`@Indexed(Unique=true)`

> Specifies whether the target field value is indexed, and whether it should be unique.

`@InternalName(String)`

> Specifies the target field's internal name.

`@Maximum(double)`

> Specifies either the maximum numeric value or string length of the target field. Our example uses a 5 Star review option.

`@Minimum(double)`

> Specifies either the minimum numeric value or string length of the target field. The user can input 0 out of 5 for the review.

`@Step(double)`

> Specifies the margin between entries in the target field, in the example below every 0.5 is allowed. 0.5, 1.0, 1.5 etc.

`@Regex(String)`

> Specifies the regular expression pattern that the target field value must match.

`@Required`

> Specifies whether the target field value is required.
	
`@FieldTypes(Class<Recordable>[])`

> Specifies the valid types for the target field value.

`@FieldUnique`

> Deprecated. Use `@Indexed(Unique=true)` instead.

`@Values`

> Specifies the valid values for the target field value.

### Class Annotations

`@Abstract`

> Specifies whether the target type is abstract and can't be used to create a concrete instance.

`@DisplayName(String)`

> Specifies the target type's display name.

`@Embedded`

> Specifies whether the target type data is always embedded within another type data.

`@InternalName(String)`

> Specifies the target type's internal name.

`@Recordable.LabelFields(String[])`

> Specifies the field names that are used to retrieve the labels of the objects represented by the target type.

`@Recordable.PreviewField`

> Specifies the field name used to retrieve the previews of the objects represented by the target type.


### Tool UI Annotations


The @ToolUi Library  `import com.psddev.cms.db.ToolUi;` gives you more options for controlling the UI display in Brightspot using annotations.

`@ToolUi.Note("String")`

> To provide the user with an instruction or note for a field in the CMS, simply use `@ToolUi.Note`. Within the UI it will appear above the specified field. You can also add the annotation to a class, to provide a Note for that object type within the CMS.

`@ToolUi.NoteHtml("<h1>String</h1>")`

> Specifies the note, in raw HTML, displayed along with the target in the UI.

`@ToolUi.Heading("String")`

> Provides a horizontal rule within the Content Object, allowing new sections to be created with headings.

`@ToolUi.Hidden`

> A target field can be hidden from the UI.

`@ToolUi.OnlyPathed`

> If you want the target field to only contain objects with a path.

`@ToolUi.ReadOnly`

> Specifies that the target field is read-only.

`@ToolUi.Placeholder("String")`

> Specifies the target field's placeholder text.

`@ToolUi.FieldDisplayType`

> Specifies the internal type used to render the target field.

`@ToolUi.Referenceable`

> Specifies whether the instance of the target type can be referenced (added) by a referential text object (rich text editor). For example, an Image object that you want to be available as an Enhancement must have this annotation.

`@ToolUi.CompatibleTypes`

> Specifies an array of compatible types that the target type may switch to.

`@ToolUi.FieldSuggestedMaximum(int)`

> This annotation is used to indicate a suggested upper limit on the length of the field.
The value passed to the annotation is the limiting value.  When a user is modifying a field annotated, an indicator will appear when the input size has exceeded the specified limit.

`@ToolUi.FieldSuggestedMinimum(int)`

> This annotation is used to indicate a suggested lower limit on the length of the field.
The value passed to the annotation is the limiting value.  When a user is modifying a field annotated, an indicator will appear when the input size falls below the specified limit. 

`@ToolUi.FieldSorted`

> Specifies whether the values in the target field should be sorted before being saved.

`@ToolUi.InputProcessorPath`

> Specifies the path to the processor used to render and update the target field.

`@ToolUi.InputSearcherPath`

> Specifies the path to the searcher used to find a value for the target field.

`@ToolUi.RichText`

> Specifies whether the target field should offer rich-text editing options. This allows String fields to contain Rich Text Controls.