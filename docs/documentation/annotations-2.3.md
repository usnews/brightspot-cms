---
layout: default
title: Annotations Brightspot 2.3
id: annotations-2.3
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

**@InternalNamePrefix(String)**

> Specifies the prefix for the internal names of all fields in the target type.


**@CollectionMaximum(int)**

> Specifies the maximum number of items in the target field.

**@CollectionMinimum(int)**

> Specifies the minimum number of items in the target field.

**@DisplayName(String)**

> Specifies the target field's display name.

**@Embedded**

> Specifies whether the target field value is embedded. This can also be applied at a class level. To embed an object within an object the annotation `@Embedded` is used with a static class. This class is then included as a field within the original class. See the example below:

    public class Company extends Content {

      private String name;
      private Contact contact;

        @Embedded
        public static class Contact extends Content {

            private String address;

        }

    }


**@Ignored**

> Specifies whether the target field is ignored.

**@Indexed**

> Specifies whether the target field value is indexed.

**@Indexed(unique=true)**

> Specifies whether the target field value is indexed, and whether it should be unique.

**@InternalName(String)**

> Specifies the target field's internal name.

**@Maximum(double)**

> Specifies either the maximum numeric value or string length of the target field. Our example uses a 5 Star review option.

**@Minimum(double)**

> Specifies either the minimum numeric value or string length of the target field. The user can input 0 out of 5 for the review.

**@Step(double)**

> Specifies the margin between entries in the target field.

**@Regex(String)**

> Specifies the regular expression pattern that the target field value must match.

**@Required**

> Specifies whether the target field value is required.
	
**@Types(Class<>[])**

> Specifies the valid types for the target field value. `@Types({Image.class, Video.class, Widget.class})` Deprecated @FieldTypes(Class<Recordable>[])

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


**@Denormalized**

> Specifies whether the target field is always denormalized within another instance.
@Denormalized may be used on a class that is referenced by another class, it de-normalizes or copies the data to the referring class. It may be used in site searches. The denormalized data is not visible on the object, it is saved in the solr Index. For example there are two classes, Person and State, where Person has a reference field State on it. Perform a people search to include the State name: 

    public class State extends Content {
      @Indexed (unique = true)
      private String name;
      @Indexed (unique = true)
      @Denormalized
      private String abbreviation;
    }
    
    public class Person extends Content {
     @Indexed
     private String firstName;
     @Indexed
     private String lastName;
     @Indexed
     private State state;
    }
    
> To perform a people search to include the State name:     

    Search search = new Search();
    search.addTypes(Person.class);
    search.toQuery("va").select(0, 5);
    
> Boost the search by the denormalized abbreviation value:

    search.boostFields(5.0, Person.class, "state/abbreviation");

This annotation should be used only when necessary in advanced cases.

**@DisplayName(String)**

> Specifies the target type's display name.

**@Embedded**

> Specifies whether the target type data is always embedded within another type data.

**@InternalName(String)**

> Specifies the target type's internal name.

**@Recordable.LabelFields(String[])**

> Specifies the field names that are used to retrieve the labels of the objects represented by the target type.

**@Recordable.PreviewField**

> Specifies the field name used to retrieve the previews of the objects represented by the target type.

**@Recordable.JunctionField**

> Specifies the name of the field in the junction query that should be used to populate the target field.

**@Recordable.JunctionPositionField**

> Specifies the name of the position field in the junction query that should be used to order the collection in the target field.

**@Recordable.MetricValue**

> Specifies the field the metric is recorded in. This annotation is only applicable to Metric fields. It allows you to specify which MetricInterval to use when storing Metric values. The default is MetricInterval.Hourly.class, so this annotation is optional.

> Example: Using an interval of None eliminates the time series component of the Metric value.

    @MetricValue(interval = com.psddev.dari.db.MetricInterval.None.class)
    Metric myMetric;

> It is also possible to reference a setting key that holds the name of the class to use

    @MetricValue(intervalSetting = "analytics/metricIntervalClass")
    Metric myMetric;


> Context.xml should be updated as follows:

    <Environment name="analytics/metricIntervalClass" type="java.lang.String" value="com.psddev.dari.db.MetricInterval$Minutely" override="false" />
    
**@Recordable.SourceDatabaseClass**

> Specifies the source database class for the target type.

**@Recordable.SourceDatabaseName**

> Specifies the source database name for the target type.

### Tool UI Annotations


The @ToolUi Library  `import com.psddev.cms.db.ToolUi;` gives you more options for controlling the UI display in Brightspot using annotations.

**@ToolUi.Note("String")**

> To provide the user with an instruction or note for a field in the CMS, simply use `@ToolUi.Note`. Within the UI it will appear above the specified field. You can also add the annotation to a class, to provide a Note for that object type within the CMS.

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

Notes can be applied at a Class level, adding a dynamic note at the top of the content edit view:

    @ToolUi.NoteHtml("<span data-dynamic-html='${content.author.name}'></span>")
    public class Article extends Content {

    	private String headline;
    	private Author author;
    	private ReferentialText bodyText;
    }


![](http://docs.brightspot.s3.amazonaws.com/note-class-level.png)


**@ToolUi.Heading("String")**

> Provides a horizontal rule within the Content Object, allowing new sections to be created with headings.

**@ToolUi.Hidden**

> A target field can be hidden from the UI.

**@ToolUi.OnlyPathed**

> If you want the target field to only contain objects with a URL path.

     @ToolUi.OnlyPathed
     private List<AbstractArticle> articles;

> In the example above, the article list field will only collect and display articles that have a URL Path.  

**@ToolUi.ReadOnly**

> Specifies that the target field is read-only. For example an object has an ID field that the editor should not be able to edit, but is when an instance of the object is saved. 

    public class Article extends Content {
        
        private String Title;
        @ToolUi.ReadOnly
        private String ArticleID;
        
        //Getters and Setters
        
    }

**@ToolUi.Placeholder("String")**

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

Either use the `beforeSave()` method in your class to populate the field on save ([documentation on beforeSave](/triggers.html)), or if the placeholder text is being used to indicate what will be rendered in it's place on the front-end if left blank, add logic to your JSP. In the example below when the altText field is left null, the name field is used.


    <c:choose>
       	<c:when test="${empty content.altText}">
       		<cms:img src="${content}" size="${imageSize}" overlay="true" alt="${content.name}"/>
       	</c:when>
       	<c:otherwise>
       		<cms:img src="${content}" size="${imageSize}" overlay="true" alt="${content.altText}"/>
       	</c:otherwise>
    </c:choose>


A method can also be used:


    public class Image extends Content {

        private String name;
        private StorageItem file;
        @ToolUi.Placeholder(dynamicText = "${content.example()}", editable=true)
        private String altText;

        public String example() {
           return "Return the placeholder content here"
        }
    }


**@ToolUi.DisplayType**

> Specifies the internal type used to render the target field.

**@ToolUi.Referenceable**

> Specifies whether the instance of the target type can be referenced (added) by a referential text object (rich text editor). For example, an Image object that you want to be available as an Enhancement must have this annotation:

    @ToolUi.Referenceable
    @Recordable.PreviewField("file")
    @Renderer.Path("/WEB-INF/common/image.jsp")
    public class Image extends Content{
    
        private String name;
	    private StorageItem file;
	    
	    //Getters and Setters
	    
	}

**@ToolUi.CompatibleTypes**

> Specifies an array of compatible types that the target type may switch to.

**@ToolUi.SuggestedMaximum(int)**

> This annotation is used to indicate a suggested upper limit on the length of the field.
The value passed to the annotation is the limiting value.  When a user is modifying a field annotated, an indicator will appear when the input size has exceeded the specified limit.

**@ToolUi.SuggestedMinimum(int)**

> This annotation is used to indicate a suggested lower limit on the length of the field.
The value passed to the annotation is the limiting value.  When a user is modifying a field annotated, an indicator will appear when the input size falls below the specified limit. 

**@ToolUi.FieldSorted**

> Specifies whether the values in the target field should be sorted before being saved.

**@ToolUi.InputProcessorPath / InputProcessorApplication**

> Specifies the path to the processor used to render and update the target field.

**@ToolUi.InputSearcherPath**

> Specifies the path to the searcher used to find a value for the target field.

**@ToolUi.RichText**

> Specifies whether the target field should offer rich-text editing options. This allows String fields to contain rich text controls.

**@ToolUi.Suggestions**

> Specifies whether the target field should offer suggestions.

**@ToolUi.DropDown**

> Specifies whether the target field should be displayed as a drop-down menu.

**@ToolUi.GlobalFilter**

> Specifies whether the target type shows up as a filter that can be applied to any types in search.

**@ToolUi.Filterable**

> Specifies whether the target field should be offered as a filterable field in search.

**@ToolUi.Sortable**

> Specifies whether the target field should be offered as a sortable field in search.

**@ToolUi.Tab("tabName")**

> Creates a new Tab interface in the content edit view, with the annotated fields appearing within it.

**@ToolUi.Secret** 

> Specifies whether the target field display should be scrambled. 

**@ToolUi.DisplayFirst / @ToolUi.DisplayLast**

> Annotate fields added through a class modification to change the default behavior (appearing last) and order them accordingly.

**@ToolUi.CodeType**

> Specifies the type of input text. Example String fields can be defined as `@ToolUi.CodeType("text/css")` to present inline numbers and css code styles. For a full list of valid values see [CodeMirror Documentation](http://codemirror.net/mode/). Use the MIME type.

**@ToolUi.CssClass**

> Add a custom CSS Class that can style the .inputContainer

**@ToolUi.BulkUpload**

> Specifies whether the target field should enable and accept files using the bulk upload feature.

**@ToolUi.Expanded**

> Specifies whether the target field should always be expanded in an embedded display.

**@ToolUi.ColorPicker**

> Specifies whether the target field should display the color picker.

**@ToolUi.IconName**

> Specifies the name of the icon that represents the target type.

**@ToolUi.NoteRenderer**

> Renders the note displayed along with a type or a field.

**@ToolUi.NoteRendererClass**

> Specifies the class that can render the note displayed along with the target in the UI.

**@ToolUi.StandardImageSizes**

> Specifies the standard image sizes that would be applied to the target field. For example there is an image object, that standard size needs to be specifies, here is how the annotation would work:

    public static class Profile extends Record {

        @Required
        private String name;
        @ToolUi.StandardImageSizes("myProfile")
        private Image image;
    }
    
> The Profile class has been define with the Image object annotated with `@ToolUi.StandardImageSizes("myProfile"). When the image is to be rendered, the size specified for it in the JSP will be referenced. So for example the render file for the Profile class is profile.jsp: 

    <cms:img src="${content.image}" size="myProfile" />

**@ToolUi.StoragePreviewProcessorPath**

> Specifies the path to the processor used to render previews of StorageItems fields. It is very similar to @ToolUi.InputProcessorPath, and its usage is identical. The difference is it is only applicable to StorageItem fields, and the specified JSP is only responsible for rendering the preview of the uploaded file, not the file upload control. For example:

    @ToolUi.StoragePreviewProcessorPath("/WEB-INF/_plugins/myCustomFilePreview.jsp")
    StorageItem myFile;

Then in myCustomFilePreview.jsp:

    <%
        State state = State.getInstance(request.getAttribute("object"));
        ObjectField field = (ObjectField) request.getAttribute("field");
        String fieldName = field.getInternalName();
        StorageItem fieldValue = (StorageItem) state.getValue(fieldName);
        if (fieldValue == null) return; 
    %>

The uploaded file is at `<%=fieldValue.getPath()%>`.

**@ToolUi.StorageSetting**

> This annotation references a settings key that indicates which storage will be used when files are uploaded in the CMS. For example:

    @ToolUi.StorageSetting("local")
    StorageItem myLocalFile;

> Then, in context.xml update the following: 

    <Environment name="dari/storage/local/class" override="false" type="java.lang.String" value="com.psddev.dari.util.LocalStorageItem" />
    <!-- etc. -->
    
> This overrides the normal behavior of checking dari/defaultStorage to determine which storage to use for this field only.

</div>