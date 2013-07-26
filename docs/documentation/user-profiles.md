---
layout: default
title: User Profiles
id: user-profiles
section: documentation
---

<div markdown="1" class="span12">

The Tool User profile can be modified within Brightspot, allowing each user to set defaults for themselves, which can in turn be used to drive default field population as they create new content.

This section looks at some of the most commonly used and helpful annotations. For a full list see the dedicated [Annotations Section](/annotations.html). 


## Create the Modification

Start by creating the modification. Modify the `ToolUser` class. This modification can be seen in Admin > Users & Roles for the User account, or in the Profile drop down in the top right of the CMS.

In this example a default category is going to be provided for each user. When they create new content that has a Category object within it, it will be automatically populated with their chosen default category. Note, it can still be changed to any other category.

<div class="highlight">{% highlight java %}public class ToolUserDefaults extends Modification<ToolUser> {

    @ToolUi.Note("Choose your default Category")
    private Category category;

    public Category getCategory(){
        return category;
    }
    
    public void setCategory(Category category){
        this.category = category;
    }

}{% endhighlight %}</div>

## Update the Content Class

When a user creates a new Blog Post, the category field will be pulled from their default settings. To do this, the following piece of code is added to the BlogPost class. Now, when a new blog is created, the logged in user's default category appears automatically.

<div class="highlight">{% highlight java %}public BlogPost() {

     ToolUser user = getCurrentToolUser();

     if (user != null) {
         ToolUserDefaults category = user.as(ToolUserDefaults.class);

         setCategory(category.getCategory());
     }
 }{% endhighlight %}</div>

![](http://docs.brightspot.s3.amazonaws.com/tool-user-defaults.png)