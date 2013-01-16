---
layout: home
title: Debugging
id: debugging
---

## Debugging


### Contextual Debugger 

The Contextual Debugging Tool gives you an instant view of webpage metrics. By adding `?_debug=true` to a page URL the Dari Contextual Debugger is activated. This provides a view of the load time, in milliseconds, for each module (jsp) on the page. Color hotspots are added, with relative size, to provide a clear illustration of the slowest loading modules on the page. The larger the circle, and the darker the red, the slower the load time. Below, we can see the 90 milliseconds load time is the slowest on our page.

**Getting Context**

Hovering over a hotspot on the page provides specific data in the top right of the page for that module:

![](http://docs.brightspot.s3.amazonaws.com/hotspots-debugger.png)

**Overview of Page**

At the bottom of the browser window is a full waterfall view, in order, of all items loaded on the page. There is an overview of the includes, and events, and you can use the color codes to locate quickly, as well as toggle the view off and on, to see the various events.

![](http://docs.brightspot.s3.amazonaws.com/profile-overview.png)

**Finding Code**

Clicking on a specific hotspot brings you to the full waterfall view of the page load at the bottom of the browser window. The anchor from the hotspot will be found automatically. Here we have clicked on the 90 milliseconds hotspot, and can see the top of the page anchored at our `page_js_start.jsp`

![](http://docs.brightspot.s3.amazonaws.com/waterfall-profile.png)

**Edit Code**

Utilizing the on-the-fly code compilation from Dari, any code can be edited by clicking into a jsp or query link. These are opened automatically in the code tool.

***View Code***

![](http://docs.brightspot.s3.amazonaws.com/edit-code-tool.png)

***Execute Query***

![](http://docs.brightspot.s3.amazonaws.com/execute-code-tool.png)



Also, when hovering over the visual module on the page a red border appears, outlining the section rendered by a specific jsp. Clicking on the module will open inline the code editor, overlaying the page.

Note changes made within this code tool view are made when running locally. The source code files are automatically updated when the `Save` button is clicked.

