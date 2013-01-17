---
layout: editorial
title: Editorial Tools
id: editorialTools
section: editorialTools
---

## Editorial Tools

### Introduction



Brightspot boasts an intuitive UI experience, coupled with a toolset tailored for the professional publisher. Rich text editing, WYSIWYG formatting, advanced image editing and visual inline publishing allows the editor complete access to manage content quickly and efficiently.

<img src="http://docs.brightspot.s3.amazonaws.com/latest_dashboard.png"/>

The beauty of the platform is the manner in which it is tailored to suit each client’s specific requirements. Assets are defined and created with exact structure and terminology, and the UI is automatically generated from your requirements.

This editorial guide will walk through the basic publishing workflow, as well as the tools present in the CMS that make editorial tasks easier.


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


### Publishing


**Creating Content**

For the following publisher tasks, we will be using a Demo Brightspot instance as an example. This example CMS powers a mock version of a Perfect Sense Digital company website. Your Brightspot interface will not share all of the same items, as it is derived from your own objects, but these examples should give you an understanding of how to carry out basic editorial tasks, and use the widgets.


We are going to start by creating a new a Article. From the Dashboard we select ‘News Article’, found in the Page Builder section on the right. *(Note, Content that has an assigned Template will appear in the Page Builder section. Creating content from here results in a new page, with a dedicated URL)*

<a href="javascript:;" ><img src="http://docs.brightspot.s3.amazonaws.com/editor_production_help.png"/></a>

There are lots of options here, but for now, let’s focus on creating our first piece of content.

* We enter a Title, a blurb and some body text.
* When we click into an area of text notice how a toolbar appears. This is your Rich Text Editor, allowing you to style your content, add images and links.
* If we are unsure as to what a particular field is being used for, of how it is shown on the front end, we can toggle the production assistant guide, using the small `?` found in each field (right side). Once toggled it shows us using the preview tool exactly where the content is being used on the page.


**Preview Content**

Now we have our content in place, we can make sure it is looking ok reviewing the preview window. If you do not see the preview window on the right side of the content look for the `Preview` link text under Publish. If you are using the inline preview on a small screen click on the preview bar at the top to slide out the live preview view.

The preview feature provides a live preview of our content positioned on our site, and allows us to check and then make edits before the content goes live.

![](http://docs.brightspot.s3.amazonaws.com/preview_tool_inline.png)

**URL**

Once you have everything as it should be we can close our preview window, or slide in the preview tool. URLs are automatically generated, visible in the right rail widget.  The logic at play grabs the headline / title of the content and creates a URL. If your current headline is too long for your desired URL, you can select 'Manual' and add item, a simple URL 'demo-blog'. A slash is automatically added before the word demo.

<img class="smaller" src="http://docs.brightspot.s3.amazonaws.com/change-url.png"/>

We’ve added content, checked it and given it a URL – we’re now ready to publish, by clicking ‘Publish’. Here's a view of a published blog post.

<a href="javascript:;" ><img src="http://docs.brightspot.s3.amazonaws.com/new_article_published.png"/></a>

**Versioning**

Notice that a new ‘History’ section appears in the right rail once you click publish. In Brightspot we archive all versions automatically.

Also, notice the ‘Create Another’ button that appeared in the top green bar.

**Saving as Draft**

If you are not ready to publish, you have the option to 'Save as Draft', which saves a version for you to access later.
<h2 id="images"> </h2>
Our content is now published. Let’s return to the Dashboard by clicking the ‘Pages & Content’ tab.

**Adding Images**

In Brightspot we can add various items to content as 'Enhancements'.

Clicking into the 'Body' Section of our content, seen below, opens the Rich Text Editor tools, and we can click 'Add Enhancement'


Once clicked you will see a grey 'Empty Enhancement' in your content. Click the Edit link to open the find tool. Here you can either select an existing image or any other content that has been designated as being applicable for inclusion. If you want to add a new object you can do that from here also.

<a href="javascript:;" ><img src="http://docs.brightspot.s3.amazonaws.com/add_enhancement_article.png"/></a>


We can search for an image already in the CMS, or upload a new one. Click on it to select, then scroll down to save. Close your enhancement window, you should now see the grey enhancement area populated with your image name. (You will not see the image, just a reference to it. To see the image in place, use the preview tool.)

<a href="javascript:;" ><img src="http://docs.brightspot.s3.amazonaws.com/edit_enhancement_article.png"/></a>


Once an enhancement has been added, we can use the same menu to position it within our content. The arrows offer alignment options, and also allow you to shift the enhancement above blocks of text. To try this out move your image to the very top of the article using the up arrow.

Use the preview tool to view your newly added image in place. If it looks good, click publish.

Often images are added to a piece of content outside of a rich text area, an example would be a promo image, or gallery of images. 

**Editing Images**

With every image object, you have the ability to apply numerous advanced edits. Brightness, contrast and filters can be chosen, as well as orientation changed.

![](http://docs.brightspot.s3.amazonaws.com/advanced_image_editing.png)

A new text overlay tool has also been added, allowing rich text to be added to any image and moved to where the editor desires. The text overlay is shown when a crop size has been selected for a given image.

![](http://docs.brightspot.s3.amazonaws.com/text_overlay.png)



### Search

<h2 id="finding-content"> </h2>
Once content has been published, we can use the search tool to find it. Clicking into the 'Search' field in the top right of the Dashboard page, or any page globally brings up the search widget.

<a href="javascript:;" ><img src="http://docs.brightspot.s3.amazonaws.com/food_network_search.png"/></a>

There are many ways in which you can narrow down your content in Brightspot. Typing automatically filters based on a text search, and using the filter on the left, you can specify the exact type of content you wish to search within.

You also have a drop down menu that allows the sort filters to be changed, such as newest, or in alphabetical order.

The search is also persistent, and can be reset using the reset link.

Once content has been accessed using the search tool, the results are still available, allowing an editor to click through results quickly in the content edit screen:

<a href="javascript:;" ><img src="http://docs.brightspot.s3.amazonaws.com/food_network_results.png"/></a>


**Finding a module**

Often Editors want to find and edit module content, based on the page on which they saw it. This type of discovery is fully supported within Brightspot search.

If an editor has seen something they would like to change on a module on the homepage, they can start with a search for that page within the CMS:

![add img](http://docs.brightspot.s3.amazonaws.com/find_page.png)

Clicking on the page result shows the template view, which provides a visual layout of the page, complete with references to the various modules included. Clicking on one opens it in an edit window, allowing the editor to make changes.

![add img](http://docs.brightspot.s3.amazonaws.com/find_on_template.png)

![add img](http://docs.brightspot.s3.amazonaws.com/edit_module_from_template.png)

I can also find the module with a text only search, here I have used the title of the module to locate it directly.

![add img](http://docs.brightspot.s3.amazonaws.com/find_module_text.png)

If you are editing a module directly, and are unsure of where it is currently being used on the site, a handy `References` widget, found in the right rail, provides links to where the module is in use. This helps editors gain insight as to where any changes they have made will be seen.

![add img](http://docs.brightspot.s3.amazonaws.com/references_widget.png)


<h2 id="cms-tools"> </h2>

### CMS Tools 

**Bookmarklet**

Often editors will want to review their web content visually, not from within the CMS interface. When doing so, Brightspot gives you the ability to edit the content from the actual web view, without having to log back into the CMS to make changes.

The Bookmarklet tool that provides this editorial view allows live content to be updated, images changed and if needed, full CMS access at a single click.

The tool is accessed via a bookmark within your browser. Start by logging into the CMS. In the Toolhat you will see a `More Tools` link. Clicking on this opens a small window with another link. Drag this link (Your Bookmarklet trigger) to your bookmark bar on your browser, as seen below.

![](http://docs.brightspot.s3.amazonaws.com/bookmarklet_start.png)
![](http://docs.brightspot.s3.amazonaws.com/bookmarklet_place.png)

Once in place, visit your site and find a page you wish to edit. Click on the new bookmark you just added to launch the tool on that page. You will then see an overlay of boxes as you hover over modules, showing the various items within the page layout. Any module or content with an `Edit` icon can be accessed and updated. You can also click `Edit in Full` to be brought into the CMS full content edit view. Save your changes by scrolling down to the `Save` button. Refresh the page to see the changes. *Note, you must be logged into your CMS to authenticate the tool.*

![](http://docs.brightspot.s3.amazonaws.com/edit_book.png)
![](http://docs.brightspot.s3.amazonaws.com/edit_content_book.png)

**Managing Content History**

Content is automatically versioned within Brightspot. The right rail widget, found in all content edit views is populated with a new version each time a save or publish is carried out. This allows the user to simply click into an older version to roll-back.


<img src="http://docs.brightspot.s3.amazonaws.com/history-new.png"/>

Within the yellow banner, which is shown when an older version is being displayed, an `Edit History` link allows the user to label a particular version. Providing a new name will create a new list of Named Histories in the right rail, allowing easier tracking of versions.

<img src="http://docs.brightspot.s3.amazonaws.com/history-name.png"/>

<h2 id="scheduling"> </h2> To revert to an older version, click Publish when viewing the desired content to be made current. Roll-over and use the preview thumbnail link on each piece of content to get a quick look at it.

**Scheduling Content**

Content does not have to be published the moment it is created. With scheduling, you can specify a time and day for when the content should go live.

Click into content and notice the right rail shows calendar link above the 'Publish` button. 

If we want to set the date for scheduling, simply click into the calendar view, and select a date. The `Publish` button is now `Reschedule`. Once a piece of content has been scheduled it appears in the Schedules widget on the dashboard.

<img src="http://docs.brightspot.s3.amazonaws.com/reschedule.png"/>

<h2 id="admin"> </h2>

<!--### Administration

**Users**

Found in Admin -> Users and Roles, Brightspot provides a user interface that allows Administrators to add new users, and define their roles.

Create a new user, by default, all new users have all access. The `Current Site` allows users with MultiSite access to define the default site for the user. 

<img class="smaller" src="http://docs.brightspot.s3.amazonaws.com/users-roles.png"/>

Once a user has been created, a `New Role` can be added, with a customizable access setup. In the dropdowns you can choose to limit access to specific types, for example a role can have read only access to an Article. Simple find the object type you want access limited to, and change the check boxes. The `Areas` dropdown controls the top navigation the user sees within the CMS. Giving no access means no Admin access for that role within BrightSpot

<img src="http://docs.brightspot.s3.amazonaws.com/users_roles.png"/>

<img src="http://docs.brightspot.s3.amazonaws.com/add-user.png"/>
-->
