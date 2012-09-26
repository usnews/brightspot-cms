---
layout: default
title: Adding a Tool
id: adding-a-tool
---

## Adding a Tool

You can add your own Tools or Applications to Brightspot. By extending the Tool class you can add new areas (Top Navigation) and Global and Remote Widgets.  

#### Step 1 - Build a new Tool Class

Start by creating your own Tool class, and extend from the CMS Tool class. 

	public class DemoTool extends Tool {

       @Override
	public void initialize(Logger logger) throws Exception {

	}

#### Step 2 - Modify your settings.properties file

Add the following, with your own GroupID and Class name, to your settings.properties file found at
`src/main/resources/settings.properties`.

    dari/mainApplicationClass=com.psddev.GROUPID.YOURTOOLCLASS

This will add a new Tool in Admin -> Settings:

![Demo Tool ](http://docs.brightspot.s3.amazonaws.com/demo-tool.png)

#### Step 3 - Build your Area / Widget / Tool

While several new tools can be added, with each one extending from the Tool class, new tools that will add widgets or areas MUST all be contained with the one class, extending from Tool.

#### Adding a new Main Tab (Area)

<img class="smaller" src="http://docs.brightspot.s3.amazonaws.com/new-tab.png"/>

	public class DemoTool extends Tool {

	    @Override
	    public void initialize(Logger logger) throws Exception {
	        Area testArea = createArea("Test Area", "demo.Area", null, "/tool/index.jsp");
	        introducePlugin(testArea);
	        
	        logger.info("Initialized the areas");
	    	}

	 }


#### Adding an Item to the Admin Drop-Down

<img class="smaller" src="http://docs.brightspot.s3.amazonaws.com/demo-tool-new.png"/>


    public class DemoTool extends Tool {

    	@Override
    	   public void initialize(Logger logger) throws Exception {

    		for (Area area : findPlugins(Area.class)) {
    			if ("admin".equals(area.getInternalName()) && (area.getTool() instanceof CmsTool)) {
    			introducePlugin(createArea("TestArea", "testarea.main", area, "/admin/testarea.jsp"));

    			}

    		}

    	}

    }

#### Adding a widget to content publication page

Note, when adding a widget, it must be done within the mainApplicationClass.

    JspWidget example = createWidget(JspWidget.class, "Example", "example", null);
	example.setJsp("/WEB-INF/widget/example.jsp");
	example.addPosition(CmsTool.CONTENT_BOTTOM_WIDGET_POSITION, 0.0, 100.0);
	introducePlugin(example);

Widgets can be placed in two areas:

<img src="http://docs.brightspot.s3.amazonaws.com/widget-places.png"/>

**CONTENT_BOTTOM_WIDGET_POSITION**

This adds a widget directly below the main CMS Tool Content. The `100.00` below denotes the row number. To place two widgets below content, and have one sit below the other, order them by increasing this `200.0`. This assumes your main content has less than 100 rows.

`example.addPosition(CmsTool.CONTENT_BOTTOM_WIDGET_POSITION, 0.0, 100.0);`

**CONTENT_RIGHT_WIDGET_POSITION**

<img class="smaller" src="http://docs.brightspot.s3.amazonaws.com/right-rail-widget.png"/>

Widgets can also be added to the right rail, above between or below the default items. Position can be defined using `rightColumn`

`.addPosition(CmsTool.CONTENT_RIGHT_WIDGET_POSITION, 0.0, 20.0);`

__Step 4 - Build your .jsp__

See the index.jsp used for the CMS application for an example.

When building a complete page within the CMS, for your new tool, include the CMS header and footer with `wp.writeHeader();` and `wp.writeFooter();`

Note: A Widget will only appear within the CMS if it is actively displaying text in one form or another. If your widget is not appearing, test by printing out text through your .jsp.

__Step 5 - Add URL__

Add designated URL. Within your new Tool, found in Admin -> Settings define your exact URL path, EG `http://localhost:8080`. 

<img src="http://docs.brightspot.s3.amazonaws.com/demo-tool.png"/>

__Step 6 - Init__

Run a `_debug/init` start and stop Tomcat.