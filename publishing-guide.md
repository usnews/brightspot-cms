---
layout: guides
title: Publishing Guide
id: introduction
section: documentation
---

<div markdown="1" class="span8">


## Overview

The example screens used in this guide are from a demo version of Brightspot. Your Brightspot interface will not share all of the same names or user interface elements, as it is derived from your own objects, however these examples should give you a clear understanding of how to carry out basic editorial tasks.

## Creating New Content

To create a new content type select one from the Create New widget on the dashboard. *(Note, Content that has an assigned Template will appear in this widget. Creating content from here results in a new page, with a dedicated URL)*. You can also create new content using the Create drop-down in the Search Tool.

![](http://docs.brightspot.s3.amazonaws.com/create_new_2.1.png)

## Text Fields

All text fields within Brightspot have a word and character count built-in, also, any spelling mistakes are highlighted in text fields through your browser language choice. 

![](http://docs.brightspot.s3.amazonaws.com/text-fields-2.1.png)


## Rich Text Editor

A full set of tools are available when creating rich text. Bold, italics, underline, text align, lists and indentation of text are there by default. Custom text styles can also be added. If you have content already created in an MS Word or Google Doc you can paste directly into Brightspot and retain the text formatting. You can also import directly from Google Docs.

![](http://docs.brightspot.s3.amazonaws.com/rich-text-2.1.png)

## Adding Links

Select text you would like to turn into a hyperlink and click the Link icon in the Rich Text Toolbar. Add your URl, and select the choice of pop-up window. To Unlink content, click on a link and select, Unlink. To link to an existing piece of content in the CMS click the Content link to choose.

![](http://docs.brightspot.s3.amazonaws.com/link-ui-2.2.png)

## Full Screen Editing

To move into a full-screen editor mode, click the full screen icon in the Rich Text toolbar. This opens the editor into full screen, without any widgets visible. Return to the content edit view by clicking on the fullscreen icon again.

![](http://docs.brightspot.s3.amazonaws.com/full-screen-mode-2.2.png)


## Adding Images

In Brightspot you can add items to content as 'Enhancements'. These can be various forms of media or modular content types.

In the Rich Text Editor toolbar, click 'Add Enhancement' to open the menu.

![](http://docs.brightspot.s3.amazonaws.com/adding-enhance-2.1.png)

Once clicked you will see an 'Empty Enhancement' in your content. Click the Edit link to open the find tool. Here you can either select an existing image or any other content that has been designated as being applicable for inclusion. If you want to add a new object you can do that from here also.


![](http://docs.brightspot.s3.amazonaws.com/adding-enhancement-2.1.png)

You can search for an image already in the CMS, or upload a new one. Click on an image to select, then scroll down to save. Close your enhancement window, you should now see the enhancement area populated with your image name in a grey box. Clicking publish on the content will show the actual image inline.

Once an enhancement has been added, you can use the same menu to position it within your content. The arrows offer alignment options, and also allow you to shift the enhancement above blocks of text. To try this out move your image to the very top of the article using the up arrow.

Often images are added to a piece of content outside of a rich text area, using the find tool, covered in the next section.

## Find Tool

When existing content is referenced within an object, the Find Tool is used to locate it. The list is automatically filtered to only show the content allowed to be referenced. Use filters or start typing to narrow the list even further. If you cannot find the content you need, clicking the New button allows a new instance to be created inline.

![](http://docs.brightspot.s3.amazonaws.com/find-tool-2.1.png)

## External Content

External content can be embedded into a rich text area by simply pasting a URL. Add an Enhancement, and select the External Content type. Paste in the URL of your desired content and save. Use preview to see the content inline. Examples include adding a tweet, a YouTube video or a Flickr image. 

![](http://docs.brightspot.s3.amazonaws.com/external-youtube.png)
![](http://docs.brightspot.s3.amazonaws.com/external-twitter.png)

## Previewing

Brightspot provides editors with a live preview tool, allowing real-time updates to content to be shown in context. To use the tool, click on the preview link below the publish button. If you are using the inline preview on a small screen click on the preview bar at the top to slide out the live preview view. The preview feature provides a live preview of your content positioned on your site, and allows you to check and then make edits before the content goes live. To close the preview tool click on the X in the upper right. To slide it out of view briefly, click the top bar.

![](http://docs.brightspot.s3.amazonaws.com/live-preview-2.1.png)

## Share

Brightspot allows content that is not yet live to be shared, with a preview URL. This URL is not accessible from your site, and is not indexed by search engines. Click Preview to see the Share option. Clicking Share opens a new window with the URL. Pass this URL along to whomever you would like to preview the page. Example: `_preview?_cms.db.previewId=00000-123456-12345`. This page can be viewed on the preview URL, even if the content is not yet published.

![](http://docs.brightspot.s3.amazonaws.com/share-link-2.1.png)

## Drafts

If you are not ready to publish your content, you have the option to 'Save as Draft', which saves a version for you to access later. All drafts can be found in the Revisions widget, in the right rail, as well as in the Unpublished Drafts widget on the the dashboard. You can see all drafts in various statuses in the dashboard widget. You can also search for drafts just like normal content using the global Search.

You can delete a draft at any time by selecting it and clicking Delete. This does not delete the published version of the content, only the draft currently being viewed.

![](http://docs.brightspot.s3.amazonaws.com/drafts-2.1.png)

## Creating URLs

URLs are generally automatically generated based on editorial input and are visible in the right rail widget.  The logic at play typically grabs the first text field in your object (headline / title) and creates a URL. If your current headline is too long for your desired URL, you can click 'Add URL' and add a simple URL. If you want to change URL, uncheck the 'Keep' flag beside a URL and add a new one. Clicking on a URL opens the live page. There are other options, as well as creating a permalink:

**Alias**

This url is a new point of access to the page, allowing a shorter, custom or vanity URL to be given for a piece of content. For example an alias `/alias-for-vanity-urls` will return the same page a permalink `/article/category/this-is-our-headline`. The address bar for the user will show `/alias-for-vanity-urls`.

**Redirect**

This url will direct users to the permalink for the page. Perfect for when content moves, or is removed, and an existing URL needs to be redirected to new content. A redirect `/this-url-redirects` will redirect users to the permalink `/article/category/this-is-our-headline` with the address bar url changing also.

## Publish

Once you are happy with your content, the URL, and have used preview to see it in context of the site, you can publish. If you would like to schedule the content to go live in the future you can pick a date using the calendar tool found in the publishing widget. Once picked hit schedule. For a full guide to scheduling content, and bulk scheduling, see the dedicated section.


## Revisions

After editing or publishing content a new ‘Revisions’ section appears in the right rail of the content edit screen. In Brightspot all versions of content are archived automatically. In the widget you can see Drafts, Schedules and past Versions.

Click on any past version to view a side-by-side difference view, or use the quick preview. Clicking publish when viewing the version will replace the current content with the past version you are viewing, allowing rolling back in a single click.

If you wish to save the past historic version specifically, edit the revision name, which in turn, once saved, places it in the right rail. From there it can be easily found and reused later. To remove a named revision, click Name Revision and remove the custom name. Then click save.

![](http://docs.brightspot.s3.amazonaws.com/revisions-2.1.png)

## Tracking Changes

Brightspot automatically tracks changes between past revisions, upcoming scheduled content, or drafts. By clicking on a past version of content, or an upcoming scheduled change the content edit view splits, showing any fields or text that has changed between the versions.

![](http://docs.brightspot.s3.amazonaws.com/tracking-changes-2.1.png)


## References

If a piece of content is being used (referenced) by other content types or pages in the CMS, a references widget will appear. This shows all the locations where the content is in use.

![](http://docs.brightspot.s3.amazonaws.com/references-widget-2.1.png)

## Delete / Trash

Content can be moved to the trash at any time by clicking Trash. This removes the content from your site, but keeps it in a Trashed status in the CMS. It can be restored, or Deleted Permanently, using the Publication widget. See below:

![](http://docs.brightspot.s3.amazonaws.com/trashed-content-2.1.png)

*Note: Trashed content that is being used within other content, example a Trashed Author within an Article, is removed from the front-end website once trashed. The reference, however, is still kept within the Article object, so the Author can be restored with one click, and an editor can see that the Article now has an Author missing.*

![](http://docs.brightspot.s3.amazonaws.com/trashed-reference-2.1.png)

To delete items in bulk start by finding them in the CMS global search. Then click `Advanced Search`. You can now select multiple files and bulk trash.



</div>
<div class="span4 dari-docs-sidebar">
<div markdown="1" style="position:scroll;" class="well sidebar-nav">


* auto-gen TOC:
{:toc}

</div>
</div>
