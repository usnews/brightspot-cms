---
layout: default
title: Annotations Brightspot 2.2
id: annotations-2.2
section: documentation
---

<div markdown="1" class="span12">

<a class="btn btn-mini" href="/annotations-2.0.html">Brightspot 2.0</a>&nbsp;&nbsp;
<a class="btn btn-mini" href="/annotations-2.1.html">Brightspot 2.1</a>&nbsp;&nbsp;
<a class="btn btn-mini" href="/annotations-2.2.html">Brightspot 2.2</a>&nbsp;&nbsp;
<a class="btn btn-mini" href="/annotations-2.3.html">Brightspot 2.3</a>&nbsp;&nbsp;
<a class="btn btn-mini" href="/annotations.html">Brightspot 2.4</a>
<br>

Annotations provide information on how a particular field or model class
should behave. The most commonly used annotations, and examples of their implementation are documented below:

#### Field Annotations

**@DisplayName**

> Specifies the target's display name.

**@Embedded**

> Specifies whether the target data is always embedded within another instance.

**@FieldInternalNamePrefix**

> Specifies the prefix for the internal names of all fields in the target type.

**@Ignored**

> Specifies whether the target field is ignored.

**@Indexed**

> Specifies whether the target field value is indexed.

**@InternalName**

> Specifies the target's internal name.

**@JunctionField**

> Specifies the name of the field in the junction query that should be used to populate the target field.

**@JunctionPositionField**

> Specifies the name of the position field in the junction query that should be used to order the collection in the target field.

**@LabelFields**

> Specifies the field names that are used to retrieve the labels of the objects represented by the target type.

**@Maximum**

> Specifies either the maximum numeric value or string length of the target field.

**@Minimum**

> Specifies either the minimum numeric value or string length of the target field.

**@MetricValue**

> Specifies the field the metric is recorded in.

**@PreviewField**

> Specifies the field name used to retrieve the previews of the objects represented by the target type.

**@Regex**

> Specifies the regular expression pattern that the target field value must match.

**@Required**

> Specifies whether the target field value is required.

**@SourceDatabaseClass**

> Specifies the source database class for the target type.

**@SourceDatabaseName**

> Specifies the source database name for the target type.

**@Step**

> Specifies the step between the minimum and the maximum that the target field must match.

**@Types**

> Specifies the valid types for the target field value.

**@Values**

> Specifies the valid values for the target field value.




### Class Annotations

**@Abstract**

> Specifies whether the target type is abstract and can't be used to create a concrete instance.

**@BeanProperty**

> Specifies the JavaBeans property name that can be used to access an instance of the target type as a modification.
For example when a class has been modified, and a field needs to be accessed to render here's how the annotation will be used. The Modification class:

	import com.psddev.dari.db.Modification;

    public class DefaultPromotable extends Modification<Promotable> {

        @Indexed
        private String title;
        private Image image;

        // Getters Setters

    }
> The class implementing the modification:

	public class Blog extends Content implements Promotable {

        private String title;
    
        // Getters Setters
    
    }

> In the example above, a new `title` and `image` can be added to the objects implementing the interface. To access those fields when rendering the content, the modification class must be annotated with @BeanProperty. 

    @BeanProperty("promotable")
    public class DefaultPromotable extends Modification<Promotable> {

        @Indexed
        private String title;
        private Image image;

        // Getters Setters

    }

> The annotation allows direct access when rendering content:

    <cms:render value="${content.promotable.promoTitle}"/>

    <cms:img src="${content.promotable.promoImage}"/>

**@CollectionMaximum**

> Specifies the maximum number of items allowed in the target field.

**@CollectionMinimum**

> Specifies the minimum number of items required in the target field.

**@Denormalized**

> Specifies whether the target field is always denormalized within another instance.


### Tool UI Annotations

**@ToolUi.CodeType**

> Specifies that the target field should be displayed as code of the given type.

**@ToolUi.CssClass**

> Specifies the CSS class to add to the input container displaying the target field.

**@ToolUi.DisplayFirst**

> Specifies that the target field should be displayed before any other fields.

**@ToolUi.DisplayLast**

> Specifies that the target field should be displayed after all other fields.

**@ToolUi.DropDown**

> Specifies whether the target field should be displayed as a drop-down menu.

**@ToolUi.Filterable**

> Specifies whether the target field should be offered as a filterable field in search.

**@ToolUi.GlobalFilter**

> Specifies whether the target type shows up as a filter that can be applied to any types in search.

**@ToolUi.Heading**

> Allows the editor to enter a heading that will be displayed before the content. 

**@ToolUi.Hidden**

> A target field can be hidden from the UI.

**@ToolUi.IconName**

> Specifies the name of the icon that represents the target type.

**@ToolUi.InputProcessorApplication**

> Specifies the path to the processor used to render and update the target field.

**@ToolUi.InputProcessorPath**

> Specifies the path to the processor used to render and update the target field.

**@ToolUi.InputSearcherPath**

> Specifies the path to the searcher used to find a value for the target field.

**@ToolUi.NoteHtml**

> Specifies the note, in raw HTML, displayed along with the target in the UI.

The note can also display dynamic content. In the example below the editor can be alerted to the content that will be used if the field is left blank. See the `@ToolUi.Placeholder` annotation for more options here also:


        public class Image extends Content {

        	private String name;
        	private StorageItem file;
        	@ToolUi.NoteHtml("<span data-dynamic-html='<strong>${content.name}</strong>
        	will be used as altText if this is left blank'></span>")
        	private String altText;
        }


![](http://docs.brightspot.s3.amazonaws.com/note-html-ui.png)

**@ToolUi.NoteRendererClass**

> Specifies the class that can render the note displayed along with the target in the UI.

**@ToolUi.Placeholder**

> Specifies the target field's placeholder text.

You can also add dynamic content as placeholder text, using any existing attribute on the content, or a dynamic not. This allows the editorial interface to accurately represent any overrides of content that happen on the front-end.

In the example below the name field appears as a placeholder in the altText field of the image object. If an editor clicks into the altText field they can add to or modify the text thanks to the `editable=true` option . This increases editor efficiency.

        public class Image extends Content {

            private String name;
            private StorageItem file;
            @ToolUi.Placeholder(dynamicText = "${content.name}", editable=true)
            private String altText;

        }


In the CMS user interface, the placeholder text is shown in grey - and darkens on hover:

![](http://docs.brightspot.s3.amazonaws.com/placeholder-text-ui.png)

**ToolUi.Referenceable**

> Specifies whether the instance of the target type can be referenced (added) by a referential text object (rich text editor). For example, an Image object that you want to be available as an Enhancement must have this annotation:

    @ToolUi.Referenceable
    @Recordable.PreviewField("file")
    @Renderer.Path("/WEB-INF/common/image.jsp")
    public class Image extends Content{
    
        private String name;
	    private StorageItem file;
	    
	    //Getters and Setters
	    
	}

**@ToolUi.ReadOnly**

> Specifies that the target field is read-only. For example an object has an ID field that the editor should not be able to edit, but is when an instance of the object is saved. 

    public class Article extends Content {
        
        private String Title;
        @ToolUi.ReadOnly
        private String ArticleID;
        
        //Getters and Setters
        
    }

**@ToolUi.RichText**

> Specifies whether the target field should offer rich-text editing options. This allows String fields to contain rich text controls.

**@ToolUi.Secret**

> Specifies whether the target field display should be scrambled.

**@ToolUi.Sortable**

> Specifies whether the target field should be offered as a sortable field in search.

**@ToolUi.StandardImageSizes**

> Specifies the standard image sizes that would be applied to the target field. 

**@ToolUi.Suggestions**

> Specifies whether the target field should offer suggestions.

**@ToolUi.SuggestedMaximum**

> Specifies the suggested maximum size of the target field value.

**@ToolUi.SuggestedMinimum**

> Specifies the suggested minimum size of the target field value.

**@ToolUi.Tab**

> Specifies the tab that the target field belongs to.

**@ToolUi.CompatibleTypes**

> Specifies an array of compatible types that the target type may switch to.

**@ToolUi.FieldDisplayType**
> Specifies the internal type used to render the target field.

**@ToolUi.FieldSorted**
> Specifies whether the values in the target field should be sorted before being saved.

**@ToolUi.OnlyPathed**
> If you want the target field to only contain objects with a URL path.

     @ToolUi.OnlyPathed
     private List<AbstractArticle> articles;

> In the example above, the article list field will only collect and display articles that have a URL Path.

</div>