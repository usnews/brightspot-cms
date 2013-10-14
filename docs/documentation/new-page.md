---
layout: default
title: Creating New Layouts
id: templates
section: documentation
---

<div markdown="1" class="span8">

Brightspot implements a subset of the W3 Grid Layout specification for defining grid layouts. This provides a flexible way to define grid layouts using a compact CSS format. 

Using the `<cms:render>` JSP Tag content can be rendered into the CSS grid items defined by CSS layouts.

### Overview

Grid layouts are defined in regular CSS files. Brightspot will automatically read all
CSS files in the project and find any defined grid layouts.

For each content type being rendered there should be a grid layout defined in
CSS, a JSP to use the CSS grid layout and a JSP to render the content.

Note: Brightspot does not require the use of CSS grid layouts but it makes defining
layouts much easier and is recommended.

### Creating a Grid Layout

Grid layouts are defined in CSS files contained within your project directories. 

Brightspot implements the following css properties defined in 
the W3 Grid Layout specification: **display: -dari-grid, -dari-grid-template, -dari-grid-definition-columns, -dari-grid-definition-rows**

Grid lengths allow the following units: **percentages, em, px, auto and fr**.

Grids defined in css should use class selectors (`.grid`) instead of id selectors (`#grid`). 

An example grid:

{% highlight css %}
.layout-global {
    display: -dari-grid;
    -dari-grid-template: ".    header  .   "
                         "main  main   main"
                         ".    footer  .   ";
 
    -dari-grid-definition-columns: 1fr 1140px 1fr;
    -dari-grid-definition-rows: 158px auto auto;

    margin-bottom: 10px;
}
{% endhighlight %}

It is useful to look at a `-dari-grid-template` like a table. For this version there are three columns, and three rows. The header sits in the middle column, and the main area sits across all three. The footer, like the header, sits in the middle. The `-dari-grid-definition-columns` define the widths of each column, and the `-dari-grid-definition-rows` define the height.

### Placing Content in the Grid

Grids can be used to lay pages out in JSPs using the `<cms:layout>` JSP Tag.
This tag takes the name of the css class selector that defines a grid and uses it to
output the appropriate `<div>` tags to effect the layout.

Content can be placed inside of named (or numbered) grid items using the
`<cms:render>` JSP tag.

For example, given the example grid defined in [Creating a Grid
Layout](#creating-a-grid-layout) the following JSP will render content in
each named grid item.

{% highlight jsp %}
<cms:layout class="layout-global">
    <cms:render area="header">
        Header Area
    </cms:render>
    <cms:render area="main">
        Main
    </cms:render>
    <cms:render area="footer">
        Footer
    </cms:render>
</cms:layout>
{% endhighlight %}

It is also possible to render content objects directly using the `<cms:render>` tag.

To view your grid on a page you have created add `?_prod=false&_grid=true` to the end of the URL.

{% highlight jsp %}
<cms:render value="${mainContent}" area="main">
{% endhighlight %}

Content can be rendered from the object, or the template. An example, where the header and footer objects are defined on the template:

{% highlight jsp %}
<cms:layout class="layout-global">
    <cms:render value="${template.header}" area="header"/>
    <cms:render value="${mainContent}" area="main"/>
    <cms:render value="${template.footer}" area="footer"/>
</cms:layout>
{% endhighlight %}


### CMS Grid Layouts

As well as providing an easy to implement front-end solution, the grid spec is also used within the CMS, where it can be used to build out an editorial interface representative of the front-end display. This makes it easy for developers to define content that can be placed in areas of the grid, and for editors to build the pages out.

In this example a page is going to be created for an example site. The goal is to create a page that allows an editor to place modules in three layouts. The layouts can be combined to build out the entire page:

- Single Module: A single module spanning the entire layout width.

- Two Modules: Two modules side-by-side, across the entire layout width.

- Three Modules: Three modules side-by-side, across the entire layout width. 

A set of applicable modules that can be used on the page can be grouped together using an interface. For an example of how this can be modeled, see the [Interfaces](/creating-modules.html#interfaces) documentation.

**Start by creating the page:**

Start by creating the Java class that will be the model for your page. Extend `Content` and add two annotations, one to render the page, the `page-container.jsp`, and the other to render the content in the page object, the `page-object.jsp`

{% highlight java %}

@Renderer.LayoutPath("/WEB-INF/common/page-container.jsp")
@Renderer.Path("/WEB-INF/model/page-object.jsp")
public class YourPage extends Content {

	private String name;
	
	// Getters and Setters

}

{% endhighlight %}

**Create the three layouts:**

Use the grid layout spec to build out the three layouts. The first has a grid area `0` representing the single module spanning the page.

{% highlight css %}
.grid-1-module {
    display: -dari-grid;
    -dari-grid-template:
        "0";

    -dari-grid-definition-columns: 940px;
    -dari-grid-definition-rows: auto;
    padding-bottom: 30px;

}
{% endhighlight %}

The second layout adds a new grid area `1` to contain the second module. Adjust the `px` and margins to make room for the other module.

{% highlight css %}

.grid-2-modules {
    display: -dari-grid;
    -dari-grid-template:
        "0 . 1";

    -dari-grid-definition-columns: 460px 20px 460px;
    -dari-grid-definition-rows: auto;
    margin-bottom: 30px;
}
{% endhighlight %}

The third layout adds another grid area, `2` and adjusts the spacing to make all three areas the same width across the page. The ListLayout processes the grid areas in order. Always add new areas in numerical order if using numbers.
{% highlight css %}
.grid-3-modules {
    display: -dari-grid;
    -dari-grid-template:
        "0 . 1 . 2";

    -dari-grid-definition-columns: 300px 20px 300px 20px 300px;
    -dari-grid-definition-rows: auto;
    margin-bottom: 30px;
}
{% endhighlight %}

**Add Layouts to Page:**

Once you have defined the grid layouts for your page, add them as options for an editor to choose from. The actual CSS class names must be used at the grid layouts in the Java class.

Add a list of the content types that can be placed into the areas you have defined.

In the example below `Placeable.class` is an interface that any module that can be added implements. For the grid areas defined for a layout, specify the content that can be added. In the example, `ImageModule.class` is the only module that can be placed in the one wide grid layout. Also, the third module area in the three wide layout must always be a `TextModule.class`. These modules implement `Placeable.class` so can also be added in the other areas.


{% highlight java %}
@Renderer.LayoutPath("/WEB-INF/common/page-container.jsp")
@Renderer.Path("/WEB-INF/model/page-object.jsp")
public class YourPage extends Content {

	@Required
	private String name;
	
	@ToolUi.Heading("Modules Grid")
        @Renderable.ListLayouts(map={
        
          @Renderable.ListLayout(name="grid-1-module",
          itemClasses={ImageModule.class}),
        
          @Renderable.ListLayout(name="grid-2-modules",
          itemClasses={Placeable.class, Placeable.class}),
        
          @Renderable.ListLayout(name="grid-3-modules",
          itemClasses={Placeable.class, Placeable.class, TextModule.class})    
        
        })
    
	private List<Placeable> modules;

	// Getters and Setters
{% endhighlight %}

**Render the Layout:**

To render the content, use the `<cms:layout>` tag to render the list of content in your defined area.

{% highlight jsp %}
<cms:layout class="${cms:listLayouts(content, 'modules')}">
    <cms:render value="${content.modules}" />
</cms:layout>
{% endhighlight %}

##### Creating the Page:

The editorial interface is representative of the layouts defined in the grids. An editor can choose from any of the three layouts, and is given options to fill the grid areas. The areas are presented in the same dimensions as the grid.

Layouts can be dragged and dropped, added and removed and adjusted at any time by the editor.

In the shot below notice that the single wide grid can only accept an Image module, as defined in the `itemClasses` property.


![](http://docs.brightspot.s3.amazonaws.com/list-layout-example-2.2.png)

### Mobile Devices

To modify the layout dynamically, based on the size of the screen being used to view, a `@media` query can be used, to override the layout class. In the example below, once the screen size drops below 700px the right rail content appears below the main content, and the content width is reduced.

{% highlight css %}
.layout {
    display: -dari-grid;
    -dari-grid-template: ". main . right .";
 
    -dari-grid-definition-columns: 1fr 680px 80px 360px 1fr;
    -dari-grid-definition-rows: auto;

    padding: 10px;
    }

@media only screen and (min-width: 300px) and (max-width:700px) {
        .layout {
        display: -dari-grid;
        -dari-grid-template: ".  main    ."
                             ".  right   .";
     
        -dari-grid-definition-columns: 1fr 320px 1fr;
        -dari-grid-definition-rows: auto auto;

        padding: 10px;
    }
}

{% endhighlight %}



### JSP Tags

API Definitions for JSP tags used in the grid layout system.

**&lt;cms:layout&gt;**

> `class` - Name of css class that defines a grid.

**&lt;cms:render&gt;**

The render tag will render the contents of the `value` attribute into a
provided area.

> `area` - Name of area to render content into.

> `value` - Value to render. This can be Content, ReferentialText, an Iterable or a String. 


</div>

<div class="span4 dari-docs-sidebar">
<div markdown="1" style="position:scroll;" class="well sidebar-nav">


* auto-gen TOC:
{:toc}

</div>
</div>
