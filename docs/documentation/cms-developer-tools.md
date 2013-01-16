---
layout: home
title: CMS Tools
id: cms-tools-dev
section: cms-tools-dev
---

## CMS Developer Tools

With Brightspot CMS there are a number of tools that make life easier for the developer. We're going to take a look at the Wireframe and Preview Tool, both of which assist when building out page templates.

### Preview Tool

![](http://docs.brightspot.s3.amazonaws.com/new_preview_tool.png)

The inline preview tool provides the ability to see a visual representation of a page right within the CMS. Any changes made to the page template or content are seen automatically. An additional `Mode` drop down allows the choice of view to be changed, with developers now able to run the [Contextual Debugger](/brightspot-cms/debugging.html) inline. Production mode can also be viewed using the drop down, which previews the page when running using Production settings (Hidden JSP errors etc).

When using a smaller screen size, the preview tool slides to the right, hidden out of sight, it can be moved into view by toggling the top bar.

![](http://docs.brightspot.s3.amazonaws.com/new_preview_hidden.png)


The live page link now opens a new window with the previewed page, and the share tool allows a non published piece of content to be shared with another user. This content is not indexed or accessible without the share URL.

**Settings**

Found in Admin -> Settings, the Preview Popup can be enabled, replacing the inline preview view.

![](http://docs.brightspot.s3.amazonaws.com/cms-settings-preview.png)


### Wireframe Tool

![](http://docs.brightspot.s3.amazonaws.com/wireframe_tool.png)

Also available within the inline preview, the wireframe tool uses the page template to create a wire framed visual of any page. Section names and layouts are used to produce a wireframe mock of your page layout. This feature is available from any Brightspot built page, by adding `?_wireframe=true` to a URL.

![](http://docs.brightspot.s3.amazonaws.com/wireframe_tool_browser.png)

Within each page section the content chosen, or not chosen can be accessed directly. The tool helps to highlight any gaps in development, as well as providing a detailed list of all the JSTL hooks available for each section. The drop down menus for each section highlight the properties available on that page section.

![](http://docs.brightspot.s3.amazonaws.com/wireframe_detail.png)


![](http://docs.brightspot.s3.amazonaws.com/wireframe_detail_more.png)
