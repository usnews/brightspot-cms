---
layout: default
title: Creating Pages - Brightspot 2.0
id: template-tool
section: cms-basics
---

<div markdown="1" class="span12">


The most recent versions of Brightspot, namely 2.1 and up, use a new template tool and rendering approach. This guide is for Brightspot Version 2.0.

There are two types of template that can be created in Brightspot, a template for a dynamic page, or a static page. The dynamic page templates are created in Admin -> Templates & Sections. The static pages are created by selecting 'One-off Page' from the Page Builder section on the dashboard.


## Dynamic Template Creation

This guide will walk through the creation of an example template for an Article Page.

Create a new Template, access the tool at `Admin -> Templates & Sections`.  Name it and save.

**Create Page Container**
<img src="http://docs.brightspot.s3.amazonaws.com/template-container-detail.png" alt="" />

Under the Visual `Unnamed Section` click Settings to start building. Create a `Container (Vertical)` and name it Page Container. (See Above) There is no need to define engines for rendering the container, so simply click `Continue Editing`.

**Add Sections**

Once the `Page Container` is in place a blue `Add Section` bar will appear, with a green plus icon for adding new sections. Picking a Vertical Container means sections will be added in a vertical position, below one another.

<img src="http://docs.brightspot.s3.amazonaws.com/template-three-sections.png" alt="" />

Add three within the Container, once added you can begin to define each. **Save.**

**Add Section Detail**

Clicking on `Settings` within the newly created sections allows you to define a name, what type will be within the section, an engine for rendering the content, and a path to the actual JSP file that will be used for rendering.

For the header use a script. Select JSP and path to a `header.jsp` file. For the example your header could contain some simple HTML, `<head>` section and CSS.

<img src="http://docs.brightspot.s3.amazonaws.com/template-header-detail.png" alt="" />

**Sections within a section**

This example is an Article template page, which will need to have the article content on the left, and a right rail with another module. The middle section can contain both these new sections.

Start by clicking on Settings, and choosing the `Container (Horizontal)` type. Add the JSP files for the start and end of the section. These typically are the HTML needed to define page structure.

<img src="http://docs.brightspot.s3.amazonaws.com/template-body-detail.png" />

By adding `Container (Horizontal)` the blue `Add Section` bar jumps to the right, you can now add two new sections, both of which are added next to one another.

<img src="http://docs.brightspot.s3.amazonaws.com/template-body.png" />

**Add Article Detail**

The left section will contain the `Article` content. As this template will be the base for multiple Articles, simply define the Object Type - Article. If you have not created an Article you can find out how to do so in the [Creating New Objects](/new-content.html) section. In the Types drop down select `Script with Main Content`. By doing this, you have specified that each time you create an Article object, it associates it with this Template.

<img src="http://docs.brightspot.s3.amazonaws.com/template-article-detail.png" alt="" />

**Add RightRail Detail**

There are two types of Right Rail Module to create. The first, allows the editor to select a specific piece of content from within the CMS and render it. 

To do this, create a new object that will be your right rail module, calling it `AuthorList.java`. It can be built like the example below. It will be an object that contains a list of other authors, hand picked by the Editor:

<div class="highlight">{% highlight java %}
public class AuthorList extends Content {

private String title;
private ReferentialText description;
private List<Author> authors;

// Getters and Setters

}
{% endhighlight %}</div>

<img src="http://docs.brightspot.s3.amazonaws.com/author-list-module.png" alt="" />
	

The jsp file to render this object could look like the example below:

<div class="highlight">{% highlight java %}
<c:out value="${content.title}" />
  <cms:render value="${content.description}" />
<ul>
  <c:forEach var="item" items="${content.authors}" >
    <li>
    	<cms:render value="${item.firstName}" />
    	<cms:render value="${item.lastName}" />
    </li>
  </c:forEach>
</ul>
{% endhighlight %}</div>
	

If, however, you wanted to return a list of all Author objects, automatically created, you could use a script with a query like the following:

<div class="highlight">{% highlight java %}
<%  List<Author> authors = Query.from(Author.class).selectAll();
	pageContext.setAttribute("authors", authors);
%>
<ul>
  <c:forEach var="item" items="${authors}">
	<li><cms:render value="${item.fullName}" /></li>
  </c:forEach>
</ul>
{% endhighlight %}</div>

On each page template you have access to the Main Content object that is included, and can control the dynamic content in a module based on this content.

An example would perhaps be creating a right rail module that shows all the articles by the Author of the currently viewed Article page.

<div class="highlight">{% highlight java %}
<% Object o = request.getAttribute("mainContent");
	if (o instanceof Article) {
	Article a = (Article) o;
	Author au = a.getAuthor();
	List<Article> articles = Query.from(Article.class).
	where("author = ?", au).selectAll();
	pageContext.setAttribute("articles", articles);

	}
%>

<ul>
  <c:forEach var="item" items="${articles}">
    <li><cms:a href="${item}"><cms:render value="${item.headline}" /></cms:a></li>
  </c:forEach>
</ul>
{% endhighlight %}</div>

Even if a specific piece of content is used within a section, the mainContent of the template is accessible.

*Note: Record, Object and Content, mainRecord, mainObject and mainContent are interchangeable. Best practice is to use content mainContent*

The full list of attributes are contained within the `PageContextFilter.class`


**Choose Content Type**

The Article Section contains the Main Content. Specify the exact Object Type to display within the template, this is done at the bottom of the page.

<img src="http://docs.brightspot.s3.amazonaws.com/template-choose-type.png" />

The `ECMA Script` is the typical choice. A path for the template can be defined here. `objectName` defaults to the first field within the Object.

<img src="http://docs.brightspot.s3.amazonaws.com/template-complete.png" />


### Section Options

When creating a new section within a template there are several options available such as making it shareable or cached.

**Shareable Sections**

By checking the `Shareable` function within a section, you distinguish it as being able to exist on more than one template. This allows global sections, such as Header or Footer sections, to be controlled in one place, with changes being made on all shared sections.


<img src="http://docs.brightspot.s3.amazonaws.com/share-check.png" />

Sections that are `Shareable` appear with a striped background pattern,

<img src="http://docs.brightspot.s3.amazonaws.com/share-stripes.png" />

Once shared, a section will appear in the left hand rail, under `Shareable Sections`. From here, changes can be made, and all templates containing the section will be updated.

<img class="smaller" src="http://docs.brightspot.s3.amazonaws.com/share-section.png" />

If a section contains other sections, and is made `Shareable` the sections contained within are incorporated as `Children`. 

<img src="http://docs.brightspot.s3.amazonaws.com/share-children.png" />


**Type Options**

![Type Options ](http://docs.brightspot.s3.amazonaws.com/type-options.png)

__Container (Horizontal)__: Create a new horizontal container, with the 'Add Section' function now creating sections side-by-side on the template:

![Type Options ](http://docs.brightspot.s3.amazonaws.com/horizontal-container.png)

__Container (Vertical)__: A vertical container choice moves the 'Add Section' to below each new section, stacking them on top of each other.

![Type Options ](http://docs.brightspot.s3.amazonaws.com/vertical-container.png)

__Script__: Render a script (JSP)

![Type Options ](http://docs.brightspot.s3.amazonaws.com/script-type.png)

__Script (with Main Content)__: For every new template created, it must be associated with a Main Content Type.

By default, when you create a new content type in Brightspot it becomes a Misc Content Type, however associating the content with a template as 'Main Content' will re-label the content as Main Content Type. The two different types can be seen clearly when looking in the 'New' drop-down list found within the Search Tool.

Once you choose 'Script (with Main Content)' you must then define which Content Type you wish to have as Main for the template. You do this at the bottom of the Template Page.

![Type Options ](http://docs.brightspot.s3.amazonaws.com/full-page-7.png)

__Script (with Content)__: Content Types existing within the CMS can be chosen for a section by selecting the (with Content) Script option. The content can easily be changed by clicking into the section again.

![Type Options ](http://docs.brightspot.s3.amazonaws.com/choose-content-type.png)
