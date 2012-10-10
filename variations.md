---
layout: default
title: Variations
id: variations
section: variations
---

## Variations


The Variation Tool within Brightspot provides a powerful way to define a host of actions that are based on specified input. The simplest example is a variation based on the device that is accessing the website. Let's look at how to use the Variation Tool to present a different page template for users accessing with a mobile device.

**Create the Variation**

Navigate to the Variations Tool, found at Admin -> Variations and Profiles. Add a new variation, name it and save.

<a href="javascript:;"><img src="http://docs.brightspot.s3.amazonaws.com/variation-new.png" alt="" /></a>

**Define the Variation**

We are going to create a mobile variation. Select `Match Any` in the top drop-down and then add `Device`. We have chosen three, `Android`, `iPad` and `iPhone`. Select `Use Embedded Variation Data` in the final drop-down.

Save.

<a href="javascript:;"><img src="http://docs.brightspot.s3.amazonaws.com/variation-create.png" alt="" /></a>

**Create the new Template**

Now that our variation `Mobile` has been saved we want to create the template that users viewing the site with a mobile device will access.

Access the Templates and Sections Tool, select the Template you want to create a new version of. We are going to add a new Client Full Page template. In the upper right of the template, select Variation, and choose your new variation. It will say "Default".

<a href="javascript:;"><img src="http://docs.brightspot.s3.amazonaws.com/template-variation.png" alt="" /></a>

The new template will be an exact copy of the Default original. To test the variation, we are going to add a Raw Script of `Hello World`. We rename this template, adding `Mobile`. Save.

<a href="javascript:;"><img src="http://docs.brightspot.s3.amazonaws.com/template-variation-test.png" alt="" /></a>

**Test the new variation**

Once you have saved your new template, simply access the page with your browser, and also with the device. As you can see below, access with an iPhone shows the added `Hello World` text at the bottom of the page.

<a href="javascript:;"><img src="http://docs.brightspot.s3.amazonaws.com/test-variation.png" alt="" /></a>

Now that we have confirmed the variation is working, we can create a completely new page template, with new .jsp files and layout, designed for mobile devices.

This is a basic example, of how to implement a variation for a page Template. Take a look around the rules, such as Browser access, to test further.

### Object Variations

As well as variations of templates, variations of objects can exist, with logic in place to access a specific version. Once a new object is created the ability to add a variant is given. In our example we can choose the Spanish variation of a blog post, allowing an editor to create the same content in Spanish.

<a href="javascript:;"><img src="http://docs.brightspot.s3.amazonaws.com/object-variation-choose.png" alt="" /></a>

Once saved, the two variations are part of the object. We can use the `_debug/query` tool to look at the object, where we see the variation.

<a href="javascript:;"><img src="http://docs.brightspot.s3.amazonaws.com/object-variation-result.png" alt="" /></a>

## Creating New Operators

Extend from class `Operation` to create a new Operator:

<img class="smaller" src="http://docs.brightspot.s3.amazonaws.com/new_operation.png" alt="" />

    @Operation.DisplayName("This is a new Operation")
    public class Test extends Operation {

    @Override
    public void evaluate(
            Variation variation, Profile profile, Object object) {
        State state = State.getInstance(object);
        Map<String, Object> variationData = (Map<String, Object>)
                state.getValue("variations/" + variation.getId());
        if (variationData != null) {
            state.getValues().putAll(variationData);
        }
      }
    }

Adding your own rule is done through extending the `Rule` class:

    public class TestRule extends Rule {

     private string name;
     
     public boolean evaluate(Variation variation, Profile profile, Object object){

}
