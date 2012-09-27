---
layout: default
title: Custom SEO
id: custom-seo
---

### Adding Custom SEO

All content that is assigned a URL appears within the CMS alongside a global SEO widget. This widget, when populated, provides SEO Title, Description and Keywords that overwrite the defaults from the content.

Within the respective .jsp files these fields must be chosen, so as to appear on the front end.

By clicking on `Raw Content` found to the right of `Save Draft` in the content edit screen, and shown as a wrench icon, a view of the SEO fields is seen in code.

    "cms.seo.title" : "Our New Title",
      "cms.seo.description" : "This description is very different from the default",
      "cms.seo.keywords" : [ "Added", "Are", "Here", "Keywords", "Shown" ],
      "cms.directory.pathsMode" : "MANUAL",
      "cms.directory.paths" : [ "8b48aee0-42d1-11e1-9309-12313d23e8f7/seotest" ],
      "cms.directory.pathTypes" : {
        "8b48aee0-42d1-11e1-9309-12313d23e8f7/seotest" : "PERMALINK"