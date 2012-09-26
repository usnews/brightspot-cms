---
layout: default
title: Getting Started
id: getting-started
---

<h2 id="hello-world"> </h2>

## Hello World!

A simple `Hello World!` tutorial.


#### Create Template

Start by accessing the CMS and creating a new page, click One Off Page in the Page Builder section of the dashboard.

Add a name for the page, and a URL. In the Unnamed Section click into settings, choose Script with Content, JSP as the Engine and add a path to a JSP file, Example `/model/helloWorld.jsp`

Click `Continue Editing`.

![](http://docs.brightspot.s3.amazonaws.com/new-page.png)


![](http://docs.brightspot.s3.amazonaws.com/hello-text.png)

You will now have the option to add the content that is to be displayed on the page. In the `New` drop down select `Text` and add some content.

Publish.


#### Create JSP

You have placed a new content object, `Text` into a template section, and choosen to render the content with a JSP. Let's create that JSP.

Copying the path to the JSP file you have already defined, create your JSP.

    cd src/main/webapp/WEB-INF
    mkdir model
    cd model
    touch helloWorld.jsp

Example JSP
    
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
    <%@ taglib prefix="cms" uri="http://psddev.com/cms" %>

    <h1><c:out value="${content.text}"/></h1>
    
Save.

#### Access the Page

Once the JSP is saved access your page at the defined URL to see your content rendered.




<h2 id="creating-objects"> </h2>


---
## Creating new Objects

In the simple Hello World tutorial we chose an object that already exists within the CMS - `Text`. The next step is to create our own content type and add it to the CMS.


#### Step 1 - Create the Class

Within your new project, start by creating a new class, call it `Article.java`. This should be saved in `/src/mainjava/com/package/name/`.   
Objects that are to be used directly within the CMS must extend the main Content class.

    package com.package.name;

	import com.psddev.cms.db.Content;
	import com.psddev.cms.db.ToolUi;
	import com.psddev.dari.db.ReferentialText;

	
	public class Article extends Content {
	
		private String headline;
		private String author;
		private ReferentialText body;


		// Getters and Setters
	}



As you can see from the code above, our Article has a headline, an Author and some body text. Note that the package given in the examples will need to be replaced with your own package.

When adding a body of text, we import the `ReferentialText` library, which comes as standard with Brightspot and provides a Rich Text Editor user interface within the CMS.

Add your Getters and Setters, and save your Article.java class.

### Step 2 - Building your project - First Build

We have created our Article, so let's see how it appears in the CMS.

For the first build of your new class you will need to perform the following steps.

Access your CMS and trigger the reloader. Add `?_reload=true` to your URL

`http://localhost:8080/cms/?_reload=true`

You will be promoted to install the reloader application, which will allow you to see Java code changes compiled automatically.

Once the application has been reloaded, click into `Search` and in the `New` drop-down find your next Article content type.


### Associating Objects

Our Article currently allows the user to specify an Author name via a String of text. Our next step is to create a separate `Author.java` content type, and allow the user to select one of these objects when creating their Article.

#### Step 1 - Create Author.java

Create a new class and name it `Author.java`

As we did with the Article, extend Content and import the respective class. For our Author we simply require a first and last name.

	package com.package.name;
 
	import com.psddev.cms.db.Content;

	public class Author extends Content {

		private String firstName;
		private String lastName;

		// Getters and Setters

	}

Save your `Author.java`. Because we have added a new class, we can trigger the reloader tool to update the CMS.

#### Step 2 - Create Authors

Before we go any further, it's time to create some authors within the CMS. We can delete these later, but for now, create three or four so we have objects to select from.

Now that we have some sample authors, use the search tool to filter, and show them all. You can see that while we are able to see our authors, we can only see their first names.

#### Step 3 - Define Label Fields

Let's fix this by specifying what information we would like displayed as a label for the `Author` content type.

Go back into your `Author.java` and add the following annotation above the main class:

    @LabelFields({"firstName", "lastName"})
    public class Author extends Content {

Save.

Annotations are not picked up by the reloader, therefore trigger within the CMS by adding `?_reload=true` to the end of the URL.

We are now able to see both the first and last names of our authors when we search for them in the CMS. We achieved this using the `LabelFields` annotation, specifying the fields within the object model we wanted to include as a label.

#### Step 4 - Update Article.java

Our initial `Article.java` class defined the Author as a String of text, so let's go back and update it, to allow the CMS user to select one of the authors we have just created, using our `Author.java` content type. Don't forget to update the getters and setters.


    package com.package.name;

	import com.psddev.cms.db.Content;
	import com.psddev.cms.db.ToolUi;
	import com.psddev.dari.db.ReferentialText;

	
	public class Article extends Content {
	
		private String headline;
		private Author author;
		private ReferentialText body;


		// Getters and Setters
	
	}


Save and refresh your CMS to see the changes.

#### Step 5 - Finish and Publish

With the `Article.java` updated we can see that our reference to the Author content type which already exists in Brightspot was all it took to automatically offer a drop-down choice of authors.

Finish this step by publishing an Article, and choosing an author. We will now move on to display this Article on the front end by building a page.


<h2 id="page-building"> </h2>

---

## Creating a Simple Page

#### Step 1 - Building a Basic Test Page

Once you have created a content type, the next step is to display it and access it via a URL.

Start by creating a new template. In the Page Builder section of the Dashboard click on `One-off Page`.  

In the 'Unnamed Section' click on 'Settings' and select the Type as `Script (with Content)`'. Name your section, and pick JSP as the Engine.

We haven't created a JSP to point to, however we can add the path where we will create it: `/model/articleBody.jsp`.

Once you have clicked `Continue Editing` you get the choice to Select Content. Select the Article you just created in the previous step.

**Save**

#### Step 2 - URL

In the right rail you can define a URL where the page can be accessed. For One-off pages, like this, you define a single URL. See the Template Tool section of the documentation to see how to create reusable templates (EG Blog page template) with automatic URLs.

**Publish**

#### Step 3 - Creating the JSP

In Step 1 and 2 we have created the most basic of template structures, to house our new Article content. The final step is to build the JSP file that renders the content on our webpage.

Create a directory in which you will place your new JSP file. We have already stated the path in the template `/model/articleBody.jsp`. It should sit within the `WEB-INF` directory of your project.

Example: src/main/webapp/WEB-INF/model/articleBody.jsp

When we created our template we defined the article as the 'Content'. In doing so, we are now able, within our JSP, to refer to this content directly.

    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
    <%@ taglib prefix="cms" uri="http://psddev.com/cms" %>

    <div class="article">
    <h1><c:out value="${content.headline}" /></h1>
    <h3><c:out value="${content.author.firstName}"/> <c:out value="${content.author.lastName}"/></h3>
    <cms:render value="${content.body}" />
    </div>
    
You can see how we have access to the fields within the Author object, as it is associated with the Article object.

**Save**

To render the article body text in the JSP we have used the `<cms:render value="${}"/>` tag.


#### Step 4 - Finish and View

Once you have saved your JSP hit the URL you have defined and view your first webpage:

Example: <http://localhost:8080/article>
