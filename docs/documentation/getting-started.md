---
layout: default
title: Getting Started
id: documentation
---

### The Basics

Once you have your CMS instance installed you can start to become familiar with the tool itself. Here are the basics:

**Dashboard**

Once logged into Brightspot you are brought to the Dashboard. Here you will find common widgets, your navigation to other areas of the CMS, a global search, and access to your admin and settings.

Let’s explain what they do:

<img src="http://docs.brightspot.s3.amazonaws.com/latest_dashboard.png"/>

### Global widgets

**Tool Hat**

The Tool Hat is the black toolbar at the top of the screen. It remains visible on all pages in Brightspot. From here you can access your own User Settings, Log-out, change which site you are viewing if using Multisite and implement the [Bookmarklet tool](/brightspot-cms/editorial.html#cms-tools).

**Search**

The search tool can be accessed from any screen in Brightspot. Simply place your cursor in the search field to get started. Start typing to see the results change. As a default, all content types are shown in the results, however with customizable options to filter with, using the filters on the left, you can specify exactly what you are looking for. There is a dedicated section [here](/brightspot-cms/editorial.html#finding-content), which walks through searching for content in Brightspot.

**Create**

Found in the Search tool, you have the ability to create new content from anywhere within Brightspot using the create drop-down. Choose from the list of existing objects or start typing to narrow the results.

### Dashboard

Logging into Brightspot brings a user directly to their dashboard, which contains several widgets, providing quick access to content and publishing workflows.

**Site Map**

The Site Map is a hierarchical view of your website content, showing the structure for the entire site. It is used to either find specific content, or to get a better understanding of the various items within each section. You can also filter a directory to show a particular object type within it.

**Recent Activity**

Using the Recent Activity, you can jump to a piece of content that has just been edited, or scroll through all edits made in one day. It can also be used to verify when content was started and finished by a user. You can also adjust results by filtering between just you and other users.

**Page Builder**

Create a whole new webpage using Page Builder. Select from a list of existing templates to get started, each one associated with a specific piece of content. These templates are created under Admin > Templates & Sections.

**Bulk Upload**

Also available within the Page Builder section is a bulk uploader. Clicking on `Upload Files` provides a popup window, where you can choose your file type, and upload in bulk. Alternatively, drag and drop your files in the page builder section.

<img src="http://docs.brightspot.s3.amazonaws.com/bulk_choose_pagebuilder.png"/>

<img src="http://docs.brightspot.s3.amazonaws.com/bulk_drag_pagebuilder.png"/>


**Schedules** 

Content that is set to go live at a future date appears in the Schedules section. Each day has a section, with any content due to go live on that day visible. Click into the content to edit. Scheduling is set from within the content edit screen, using a date widget. See the dedicated section on [scheduling](/brightspot-cms/editorial.html#scheduling) here. 

**Drafts**

All content that is not yet completed, but saved in a draft status, appears in the Drafts module. The user who created the content can also be seen.

**Page Thunbnails**

Hovering over any content on the dashboard that is an individual page allows you to see a quick inline preview of the page. This allows editors to get a visual idea of what they want to create or edit. Hovering over the eye preview symbol toggles the view.<h2 id="publishing"> </h2>

<img src="http://docs.brightspot.s3.amazonaws.com/page_thumbs.png"/>

**Custom Layout**

Each CMS user can customize their own dashboard view, moving the widgets around to suit their workflow. Hover over the top right corner of a widget to move. Click on an arrow direction to move the widgets around. 

<img src="http://docs.brightspot.s3.amazonaws.com/custom_widgets.png"/>


<h2 id="hello-world"> </h2>
### Hello World!

**Create Template**

Click One Off Page, found in the Page Builder section of the dashboard. One off pages are static pages, with a defined URL. Examples would be a Contact Us page, or Sitemap page.

Add a name for the page, and a URL. URLs are added manually on one off pages. See the Template Tool section for guidelines on dynamic URL generation. In the Unnamed Section click into settings, choose Script with Content, JSP as the Engine and add a path to a JSP file, Example `/model/helloWorld.jsp`.

Click `Continue Editing`.

![](http://docs.brightspot.s3.amazonaws.com/new-page.png)


![](http://docs.brightspot.s3.amazonaws.com/hello-text.png)

You will now have the option to add which object / content will be displayed on the page. In the `Create` drop down select the standard `Text` object and add some content.

Publish.


**Create JSP**

You have placed a new content object, `Text` into a template section, and chosen to render the content with a JSP, which we have pathed to. Let's create that JSP.

Copying the path to the JSP file you have already defined, create your JSP. Note, you can also place your jsp files in the WEB-INF directory.

Example JSP:
    
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
    <%@ taglib prefix="cms" uri="http://psddev.com/cms" %>

    <h1><c:out value="${content.text}"/></h1>
    
Using JSTL we can render `content` and the field within it `text`.

Save.

**Access the Page**

Once the JSP is saved access your page at the defined URL to see your content rendered. You can also use the Preview tool within the content edit screen.


<h2 id="creating-objects"> </h2>


### Creating new Objects

In the simple Hello World tutorial we chose an object that already exists within the CMS - `Text`. The next step is to create our own content type and add it to the CMS.


**Create the Class**

Within your new project, start by creating a new class, call it `Article.java`. This should be saved in `/src/main/java/com/package/name/`.   
Objects that are to be used directly within the CMS must extend the main Content class.

	
	public class Article extends Content {
	
	    private String headline;
	    private String author;
	    private ReferentialText body;

	    // Getters and Setters
	}


As you can see from the code above, our Article has a headline, an Author and some body text. When adding a body of text, we import the `ReferentialText` library, which comes as standard with Brightspot and provides a Rich Text Editor user interface within the CMS.

Save your Article.java class.

**Building**

We have created our Article, so let's see how it appears in the CMS. For the first build of your new class you may need to perform the following steps if it does not appear.

Access your CMS and trigger the reloader. Add `?_reload=true` to your URL

`http://localhost:8080/cms/?_reload=true`

You will be prompted to install the reloader application, which will allow you to see Java code changes compiled automatically. Once the application has been reloaded, click into `Search` and in the `Create` drop-down find your next Article content type. All new objects added in the CMS appear in this drop-down.

If the reloader is not prompted, run `mvn clean install` to rebuild your project. This should only have to be done for your first time class creation - the reloader will handle subsequent changes.

You'll notice that in the CMS your object automatically has UI associated with it, derived from the names you have given the fields. To see all the UI elements used see the
<a href="/brightspot-cms/ui.html" >User Interface</a> Section.

**Associating Objects**

Our Article currently allows the user to specify an Author name via a String of text. Our next step is to create a separate `Author.java` content type, and allow the user to select one of these objects when creating their Article.

**Create Author.java**

Create a new class and name it `Author.java`

As we did with the Article, extend Content. For our Author we simply require a first and last name, two text fields.


	public class Author extends Content {

		private String firstName;
		private String lastName;

		// Getters and Setters

	}

Save your `Author.java`. Because we have added a new class, we can trigger the reloader tool to update the CMS.


**Create Authors**

Before we go any further, it's time to create some authors within the CMS. We can delete these later, but for now, create three or four so we have objects to select from, and query. Click into Search and `Create` - find the Author object and start publishing. You can use the `Create Another` link at the top, after you create one, to speed up the process.

Now that we have some sample authors, use the search tool to filter, and show them all. You can see that while we are able to see our authors, we can only see their first names.

**Adding an Annotation - Define Label Fields**

We can fix this by specifying what information we would like displayed as a label for the `Author` content type. As a default, Brightspot uses the first text field in a piece of content as the label field for that object. This annotation allows you to customize that label.

Go back into your `Author.java` and add the following annotation above the main class:

    @LabelFields({"firstName", "lastName"})
    public class Author extends Content {

Save. We have defined our label fields as being the first and last name.

Annotations are currently not picked up by the reloader, therefore trigger within the CMS by adding `?_reload=true` to the end of the URL.

We are now able to see both the first and last names of our authors when we search for them in the CMS.

There are many annotations that can be used both at a class and field level within Brightspot. You can check them all out here, in the <a href="/brightspot-cms/annotations.html">annotation section</a>.

**Update Article.java**

Our initial `Article.java` class defined the Author as a String of text, so let's go back and update it, to allow the CMS user to select one of the authors we have just created, using our `Author.java` content type. All that is required for this step is a change of the `returnType` from `String` to `Author`.


	public class Article extends Content {
	
		private String headline;
		private Author author;
		private ReferentialText body;

		// Getters and Setters
	
	}


Save and refresh your CMS to see the changes.

**Finish and Publish**

With the `Article.java` updated we can see that our reference to the Author content type which already exists in Brightspot was all it took to automatically offer a drop-down choice of authors.

Finish this step by publishing an Article, and choosing an author. We will now move on to display this Article on the front end by building a page.


<h2 id="page-building"> </h2>

---

### Creating a Simple Page

**Building a Basic Test Page**

Once you have created a content type, the next step is to display it on a page and access it via a URL.

Start by creating a new template. In the Page Builder section of the Dashboard click on `One-off Page`.  

In the 'Unnamed Section' click on 'Settings' and select the Type as `Script (with Content)`'. Name your section, and pick JSP as the Engine.

We haven't created a JSP to point to, however we can add the path where we will create it: `/model/articleBody.jsp`.

Once you have clicked `Continue Editing` you get the choice to Select Content. Select the Article you just created in the previous step.

Save.

**URL**

In the right rail you can define a URL where the page can be accessed. For One-off pages, like this, you define a single URL. See the Template Tool section of the documentation to see how to create reusable templates (EG Blog page template) with automatic URLs.

Publish.

**Creating the JSP**

In Step 1 and 2 we have created the most basic of template structures, to house our new Article content. The final step is to build the JSP file that renders the content on our webpage.

Create a directory in which you will place your new JSP file. We have already stated the path in the template `/model/articleBody.jsp`. It should sit within the `WEB-INF` directory of your project, or at the root.

Example: src/main/webapp/WEB-INF/model/articleBody.jsp
Example: src/main/webapp/model/articleBody.jsp

When we created our template we defined the article as the 'Content'. In doing so, we are now able, within our JSP, to refer to this content directly.

    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
    <%@ taglib prefix="cms" uri="http://psddev.com/cms" %>

    <div class="article">
    <h1><c:out value="${content.headline}" /></h1>
    <h3><c:out value="${content.author.firstName}"/> <c:out value="${content.author.lastName}"/></h3>
    <cms:render value="${content.body}" />
    </div>
    
You can see how we have access to the fields within the Author object, as it is associated with the Article object.

**Using the Wireframe Tool**

When creating a JSP to render content Brightspot provides a wireframe tool, that shows all the content and data that is available to it on a specific page. Within the template, open the preview window on the right, and select `Wireframe` in the mode drop down. You are given a wireframe view of your page, with options on each section. In our example we have one Article section. Click on `Available JSTL Expressions` and `Content` to see everything available. The other options, such as Template, Profile, SEO are always offered.

![](http://docs.brightspot.s3.amazonaws.com/wireframe_article_started.png)


Save.

To render the article body text in the JSP we have used the `<cms:render value="${}"/>` tag. This tag processes the rich text as well as any dynamic content that has been added


**Finish and View**

Once you have saved your JSP hit the URL you have defined and view your first webpage:

Example: <http://localhost:8080/article>

