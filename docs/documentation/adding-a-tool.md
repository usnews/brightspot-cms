---
layout: home
title: Adding a Tool
id: adding-a-tool
---

## Adding a Tool

You can add your own Tools, Applications or Settings to Brightspot. By extending the Tool class you can add new areas (Top Navigation) and Global and Remote Widgets, as well as Application specific settings, such as email addresses for form submissions, Analytics IDs etc.

**Build a new Tool Class**

Start by creating your own class that extends the CMS Tool class. This will create a new Tool in the Admin -> Settings section of the CMS.

	public class DemoTool extends Tool {

	private static Logger LOGGER = LoggerFactory.getLogger(DemoTool.class);

    private String exampleField;
	
	}


![Demo Tool ](http://docs.brightspot.s3.amazonaws.com/demo-tool.png)


**Adding a new Main Tab (Area)**

<img class="smaller" src="http://docs.brightspot.s3.amazonaws.com/new-tab.png"/>

	public class DemoTool extends Tool {

	    @Override
	    public void initialize(Logger logger) throws Exception {
	        Area testArea = createArea("Test Area", "demo.Area", null, "");
	        introducePlugin(testArea);
	        
	        logger.info("Initialized the areas");
	    	}

	 }

**Adding an Item to the Admin Drop-Down**

<img class="smaller" src="http://docs.brightspot.s3.amazonaws.com/demo-tool-new.png"/>


    public class DemoTool extends Tool {

    	@Override
    	   public void initialize(Logger logger) throws Exception {

    		for (Area area : findPlugins(Area.class)) {
    			if ("admin".equals(area.getInternalName()) && (area.getTool() instanceof CmsTool)) {
    			introducePlugin(createArea("TestArea", "testarea.main", area, ""));

    			}

    		}

    	}

    }


Note, when adding a widget, it must be done within the mainApplicationClass. This is defined in either a `settings.properties` file (src/main/resources/settings.properties), or your web.xml (WEB-INF/web.xml)

**web.xml**

	<env-entry>
     <env-entry-name>dari/mainApplicationClass</env-entry-name>
     <env-entry-type>java.lang.String</env-entry-type>
     <env-entry-value>com.package.tool.Name</env-entry-value>
    </env-entry>
    
**settings.properties**

	dari/mainApplicationClass=com.package.tool.DemoTool
	
**Adding a widget to content publication page**

Each widget will have a `setJsp` which points to the jsp being used to render.

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

**Build your JSP**

See the index.jsp used for the CMS application for an example.

When building a complete page within the CMS, for your new tool, include the CMS header and footer with `wp.writeHeader();` and `wp.writeFooter();`

Note: A Widget will only appear within the CMS if it is actively displaying text in one form or another. If your widget is not appearing, test by printing out text through your .jsp.

**Add URL**

Add designated URL. Within your new Tool, found in Admin -> Settings define your exact URL path, EG `http://localhost:8080`. 

<img src="http://docs.brightspot.s3.amazonaws.com/demo-tool.png"/>

**Init**

Run a `_debug/init` to initialize the new application tool, stop and start Tomcat.

Note, you can add as many extra tools as you wish from the same initialize function.