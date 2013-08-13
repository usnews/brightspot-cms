---
layout: default
title: Creating Modules
id: creating-modules
section: documentation
---
<div markdown="1" class="span8">

## Overview

Once [Pages and Templates](/new-page.html) have been created, the next step is to add Module content types, that can be placed on those pages. Examples include railed modules.

## Creating a Module

Modules are created by building a Java Class with the required fields within it. When included on a page or template, the module is rendered with a JSP file, defined through a `@Renderer.Path("")` annotation on the object.

Start by creating a Java Class and extend `Content`. Add some basic fields. The example below is a generic module with a title and rich text.

{% highlight java %}@Renderer.Path("/generic-module.jsp")
public class GenericModule extends Content {

	private String title;
	private ReferentialText body;

	// Getters and Setters
}
{% endhighlight %}

Once saved, it will appear in the Create drop down, found in the global search tool within Brightspot.

![](http://docs.brightspot.s3.amazonaws.com/generic-module.png)

## Rendering a Module

To render the content of the module, create a JSP file to associate with the object. The example below renders the `title` and `body` fields.

{% highlight jsp %}<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="cms" uri="http://psddev.com/cms" %>

<div>
    <strong><c:out value="${content.title}"/></strong><br/>
    <cms:render value="${content.body}"/>
</div>
{% endhighlight %}

The shot below shows the creation of the module. The preview on the right is a rendering of the jsp file.

![](http://docs.brightspot.s3.amazonaws.com/creating-single-module.png)


## Using Modules on Pages

The module can now be created in the CMS, and rendered by itself. The next step is to add it to a page. The example below adds the module to the right rail of the Article template.

The objective here is to allow modules, such as the generic module, to be added to the right rail of an Article template. Start by adding a reference to the module within the Article class.

<div class="highlight">{% highlight java %}@Renderer.LayoutPath("/page-container.jsp")
@Renderer.Path("/article-object.jsp")
public class Article extends Content implements Directory.item {

    // Existing Fields and Getters and Setters
    
    @ToolUi.Heading("Right Rail Module")
    private GenericModule genericModule;
}
{% endhighlight %}</div>

This will allow a new Generic module, or existing one to be associated with the article:

![](http://docs.brightspot.s3.amazonaws.com/associate-new-rightrail.png)

The article object jsp now needs to include a reference to where the module should be rendered. The positioning on the page is done using the `<cms:layout>` tag in the example given below. It can also be done simply with HTML. See the [Creating Layouts](new-page.html) documentation for more.

Because the module has a `jsp` file associated with it, rendering it using the `<cms:render` tag is all that is required. The object is then rendered itself.

<div class="highlight">{% highlight jsp %}<cms:layout class="article-layout">
    <cms:render area="content">
        <h1><cms:render value="${content.headline}"/></h1>
        <h5>Written by: <c:out value="${content.author.name}"/></h5>
        <cms:render value="${content.body}" /><hr>
	  </cms:render>
    <cms:render area="right">
        <div>
            <strong>Right Rail Module Articles</strong>
            <cms:render value="${content.genericModule}" />
         </div>
    </cms:render>
</cms:layout>
{% endhighlight %}</div>

## Query based Modules

Some sections of a page may not require any use of a CMS content object, but rather require a script to return a result set. When this is the case a jsp include can be used to pull in the required code.

Create the JSP and place it within your webapp directory (Note, it can be placed anywhere within webapp or WEB-INF, as long as the path is correct).

The JSP example below queries from the Author object, and returns all. You can test the `Order By` clause by adding `sortAscending("firstName");` to the query.


<div class="highlight">{% highlight jsp %}<%  List<Author> authors = Query.from(Author.class).selectAll();
    pageContext.setAttribute("authors", authors);
%>
<ul>
  <c:forEach var="item" items="${authors}">
    <li><c:out value="${item.firstName}" /> <c:out value="${item.lastName}" /></li>
  </c:forEach>
</ul>
{% endhighlight %}</div>

A query for content can also be placed directly within a Java Class, and the method used to render the result within the jsp.

## Interfaces

In the example above, the generic module is associated with the Article template, and an instance of one can be placed on each Article page. Often, a choice of modules is desired, so an editor can place more than one type on a page. To do this, interfaces are used, to group content, such as modules together.

Start by creating another module. The example below is a basic list of links.

<div class="highlight">{% highlight java %}@Renderer.Path("/WEB-INF/modules/list-module.jsp")
public class ListModule extends Content {

    private String name;
    private List<Link> links;
}
{% endhighlight %}</div>

The jsp file to render this could look something like:

<div class="highlight">{% highlight jsp %}<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="cms" uri="http://psddev.com/cms" %>

<div>
    <strong><c:out value="${content.name}"/></strong>
    <ul>
     <c:forEach var="item" items="${content.links}">
      <li>
        <cms:a href="${item.url}">
        <cms:render value="${item.text}" />
        </cms:a>
      </li>
     </c:forEach>
    </ul>
</div>
{% endhighlight %}</div>

Now both the `GenericModule` and `ListModule` should be options for placement on an Article template. To do this, create an interface that both modules implement, and add that interface as a return type in the Article.

<div class="highlight">{% highlight java %}public interface RightRail extends Recordable {

	// This is an interface, that groups together RightRail modules.

}{% endhighlight %}</div>


<div class="highlight">{% highlight java %}public class ListModule extends Content implements RightRail {

	private String name;
	private List<Link> links;

}{% endhighlight %}</div>

<div class="highlight">{% highlight java %}public class GenericModule extends Content implements RightRail {

	private String title;
	private ReferentialText body;

}{% endhighlight %}</div>

In the Article object, update the RightRail section to return the interface as a list, so multiples can be added.

<div class="highlight">{% highlight java %}@Renderer.Path("/WEB-INF/model/article-object.jsp")
@Renderer.LayoutPath("/WEB-INF/common/page-container.jsp")
public class NewArticle extends Content implements Directory.Item {

	@Indexed
	@Required
	private String headline;
	@Indexed
	private Author author;
	private ReferentialText body;

 	@ToolUi.Heading("Right Rail Modules")
	private List<RightRail> rightRailModules;{% endhighlight %}</div>
	
The jsp should also be updated, to handle the change of names:

<div class="highlight">{% highlight jsp %}<cms:layout class="article-layout">
    <cms:render area="content">
        <h1><cms:render value="${content.headline}"/></h1>
        <h5>Written by: <c:out value="${content.author.name}"/></h5>
        <cms:render value="${content.body}" /><hr>
	  </cms:render>
    <cms:render area="right">
        <div>
            <strong>Right Rail Modules</strong>
            <cms:render value="${content.rightRailModules}" />
         </div>
    </cms:render>
</cms:layout>
{% endhighlight %}</div>

Now, when adding modules to the Article, both types can be chosen, or added inline.

![](http://docs.brightspot.s3.amazonaws.com/creating-interfaces.png)

## Embedding Modules

Modules can be embedded on pages using an embed script. This can power syndication, with modules appearing on other Brightspot sites. For full documentation see the [Syndication](syndication.html) section.



</div>

<div class="span4 dari-docs-sidebar">
<div markdown="1" style="position:scroll;" class="well sidebar-nav">


* auto-gen TOC:
{:toc}

</div>
</div>