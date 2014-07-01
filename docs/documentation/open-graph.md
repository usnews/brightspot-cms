---
layout: default
title: Open Graph Configuration
id: open-graph
section: documentation
---
<div markdown="1" class="span12">

Open Graph allows a web page to have the same funtionality as an object on a social media website. Brightspot provides a straightforward approach to implementing Open Graph.

### Universal Site Open Graph

First create an object for your site application and extend the Tool class.
 
Then define the Open Graph Meta data fields:

	public class MyApplication extends Tool {

    	@Tab("OpenGraph Metadata") //remove
    	private String openGraphTitle;

    	@Tab("OpenGraph Metadata")
    	private String openGraphDescription;

    	@Tab("OpenGraph Metadata")
    	private String openGraphUrl;

    	@Tab("OpenGraph Metadata")
    	private String openGraphSiteName;

    	@Tab("OpenGraph Metadata")
    	private StorageItem openGraphImage;
    	
    	//getters and setters
    }
    
Once these fields are defined, they will be accessible in the Site Settings in the CMS for the Admin user. 

The next step is to create the class that writes the Open Graph data to the pages on the site. Create a class implementing PageStage.SharedUpdateable:

	public class MyPageStageUpdater implements PageStage.SharedUpdatable {
	
		//Override the updateStageBefore method in PageStage.SharedUpdatable
		@override
		public void updateStageBefore(final Object object, PageStage stage){

			//Get the MyApplication Object
			MyApplication myApp = Application.Static.getInstance(MyApplication.class);            
			           
          	stage.findOrCreateHeadElement("meta",
          			"property", "og:title",
                    "content", myApp.getOpenGraphTitle());

            stage.findOrCreateHeadElement("meta",
            		"property", "og:description",
                    "content", myApp.getMyOpenGraphDescription());

            stage.findOrCreateHeadElement("meta",
                    "property", "og:url",
                    "content", myApp.getOpenGraphUrl());

            stage.findOrCreateHeadElement("meta",
                    "property", "og:site_name",
                    "content", myApp.getOpenGraphSiteName());

            StorageItem openGraphImage = myApp.getOpenGraphImage();
            if (openGraphImage != null) {
            	String openGraphImageUrl = openGraphImage.getPublicUrl();
            	if (openGraphImageUrl != null) {
                	stage.findOrCreateHeadElement("meta",
                        "property", "og:image",
                        "content", openGraphImageUrl);
            	}
            }
    	}
    }	
   

The code above creates the MyPageStageUpdater class and uses the `findOrCreateHeadElement` method to add the Open Graph meta data to each page of the website application. 

Include `<cms:render value="${stage.headNodes}" />` in the head of the jsp being rendered.


### Content Specific Open Graph 

Open Graph data can also be set for specific content on a website. Create a class for the main content and define the values of the Open Graph data:

	public class MyMainContent extends Content implements Directory.Item {

		private String openGraphTitle;

		public String getOpenGraphTitle() {
   			return title;
		}

		public void setOpenGraphTitle(String title) {
    		this.title = title
		}
	}
 
 The MyMainContent class is the content object the Open Graph data is to apply to. 
 
 Next create a class implementing PageStage.SharedUpdateable and call the MyMainContent object there to access the Open Graph data. The class will append all Open Graph data to the specific content. 
 
 	public class MyPageStageUpdater implements PageStage.SharedUpdateable {
 		
 		@Override
		public void updateStageBefore(final Object object, PageStage stage) {
			
        	if (object instanceof MyMainContent) {

            	stage.findOrCreateHeadElement("meta",
                    "property", "og:title",
                    "content", ((MyMainContent) object).getOpenGraphTitle());
            }
		}
	}

### Rendering

To render the Open Graph data on the front end, include the code snippet below in the main header JSP file of the site. 

	
	<cms:render value="${stage.headNodes}" />
	

### Editorial Guide

Managing Open Graph data for an entire website can be done from the Settings tab under Admin. In the left panel of the Settings page, click the site name under the Applications section. You may then enter the Open Graph data for the site application. 

![open-graph](http://docs.brightspot.s3.amazonaws.com/open-graph.png)

 </div>


	
 		
 