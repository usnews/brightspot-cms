---
layout: default
title: Extending Brightspot
id: extendingBrightspot
section: documentation
---

<div markdown="1" class="span12">

You can add your own Tools, Applications or Settings to Brightspot. By extending the Tool class you can add new areas (Top Navigation) and Dashboard and Remote Widgets, as well as Application specific settings, such as email addresses for form submissions, Analytics IDs etc.


## Adding a Widget

Widgets can be added as dashboard widgets or content widgets. A dashboard widget appears for all users, providing the ability to customize a tool that is immediately available when first logged in. The content widget appears on the content edit screen, either in the right rail, or underneath the other widgets, such as the SEO widget.

**Create the Class**

Start by creating a new class and extend Tool. We will add our widget/widgets within this class. We introduce the plugin and define the path to the JSP that is to be used to render it. Once created, rebuild your project and run `_debug/init` to initialize the plugins.

You can access these tools via Admin > Settings.

<div class="highlight">{% highlight java %}
public class Widgets extends Tool {

	private static Logger LOGGER = LoggerFactory.getLogger(DashboardWidget.class);
	
    @Override
    public List<Plugin> getPlugins() {
        List<Plugin> plugins = new ArrayList<Plugin>();

        plugins.add(createPageWidget(
                "Dashboard Widget",
                "dashboardWidget",
                "/_widgets/dashboardWidget.jsp",
                DASHBOARD_WIDGET_POSITION, 1, 1));
                
         plugins.add(createPageWidget(
                "Content Widget",
                "contentWidget",
                "/_widgets/contentWidget.jsp",
                CONTENT_RIGHT_WIDGET_POSITION, 0.0, 100.0));
                
         plugins.add(createPageWidget(
                "Content Widget",
                "contentWidget",
                "/_widgets/contentWidget.jsp",
                CONTENT_BOTTOM_WIDGET_POSITION, 0.0, 100.0));

        return plugins;
    }

}
{% endhighlight %}</div>

<a id="editor-widget"></a> 
**Creating the JSP**

The JSP used to render the content of a widget uses any present `h1` tag to create the widget label.

<div class="highlight">{% highlight jsp %}
<div class="widget">
<h1>New Dashboard Widget</h1>
Render your widget content here.
</div>
{% endhighlight %}</div>
	

## Custom Application Settings

Application settings can be configured from within the CMS by creating a class to extend the default Tool class.

**Create the Class**

Start by creating a new class, name it `SiteSettings.java`.

<div class="highlight">{% highlight java %}
package com.psddev.brightspot;

import com.psddev.cms.tool.Tool;

public class SiteSettings extends Tool {

    private String analyticsID;

    public String getAnalyticsID(){
        return analyticsID;
    }
    
    public void setAnalyticsID(String analyticsID){
        this.analyticsID = analyticsID;
    }

}
{% endhighlight %}</div>

Once created, and your application properties are added, rebuild your project and run `_debug/init`. Navigate to Admin > Settings. On the left you will see, below the CMS Tool, your SiteSettings Tool.


**Using the Application Settings**

A basic example of an application setting that you may want to access within a JSP is an Analytics ID for the site. As per the example above, add a String field. In your JSP import the `SiteSettings` object, and the `Query` library. Query from the SiteSettings object and set the `pageContext`. Below is an example of a Google Analytics implementation.


<div class="highlight">{% highlight java %}

<%@page import="com.perfectsensedigital.SiteSettings,com.psddev.dari.db.Query,com.psddev.dari.util.Settings"%>

    <%
    SiteSettings settings = Application.Static.getInstance(SiteSettings.class);
    if (settings != null) {
        pageContext.setAttribute("analyticsID", settings.getAnalyticsID());    }
    %>
    
<c:if test="${not empty analyticsID}">
    <script type="text/javascript">

          var _gaq = _gaq || [];
          _gaq.push(['_setAccount', '${analyticsID}']);
          _gaq.push(['_trackPageview']);

          (function() {
            var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
            ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
            var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
          })();

     </script>
</c:if>

{% endhighlight %}</div>

<a id="menu-widget"></a>

## Adding a Tab or Menu Item

**Adding a new Main Tab (Area)**

To add a new Tab to the navigation within the CMS, simply add an `Area`. This can be placed in an existing class that extends Tool.

<div class="highlight">{% highlight java %}

Area testArea = createArea("Test Area", "demo.Area", null, "/url-goes-here");
introducePlugin(testArea);

{% endhighlight %}</div>

**Adding an Item to the Admin Drop-Down**

<div class="highlight">{% highlight java %}

@Override
public void initialize(Logger LOGGER) throws Exception {
        
    Area adminArea = null;
    for (Area area : findTopAreas()) {
        if (area.getInternalName().equals("admin")) {
            adminArea = area;
        }
    }

    if (!ObjectUtils.isBlank(adminArea)) {
        introducePlugin(createArea("New Nav Item", "navItem", adminArea, "path/to/file.jsp"));
    }

} 
{% endhighlight %}</div>


**Create new CMS pages**

Often when creating new tools within the CMS, you want to inherit the header / footer and other elements common to the CMS Tool when you create a custom page. This can be achieved by using `wp.writeHeader`, as the example below shows:


<div class="highlight">{% highlight java %}
<% ToolPageContext wp=new ToolPageContext(pageContext); %>
<% wp.writeHeader(); %>

<% wp.writeFooter(); %>
{% endhighlight %}</div>