---
layout: default
title: Variations
id: variations
section: documentation
---

<div markdown="1" class="span12">


The Variation Tool within Brightspot provides a powerful way to define a host of actions that are based on specified input. The simplest example is a variation based on the device that is accessing the website. In this example the Variation Tool will be used to present different content for users accessing with a mobile device.

## Create the Variation

Navigate to the Variations Tool, found at Admin -> Variations and Profiles. Add a new variation, name it and save. In this example the variation will be applied to an existing Homepage object, which is selected. To apply a global variations, which can be added to Content sections, templates etc, leave the Content Type blank.

![](http://docs.brightspot.s3.amazonaws.com/mobile-variation-2.1.png)

## Define the Variation

The Rule will match a **Device** and the type for the example is **iPhone**. The operation can use a custom script, or the default embedded CMS variation data.

Save.

## Create the Content Variation

Now that your variation `Mobile` has been saved, you can create new content with two variations, one using the default, and one with the new rule.

This example uses a Homepage object. Clicking into `Original` offers a new choice, `Mobile`.

![](http://docs.brightspot.s3.amazonaws.com/switching-variations-2.1.png)

Choosing Mobile shows an exact copy of the Default original. To test the variation, change the content to reflect a Mobile view. In the example a mobile welcome message has been added:

![](http://docs.brightspot.s3.amazonaws.com/mobile-variation-created-2.1.png)

## Test the new variation

Access the page in a Desktop browser, and on a mobile device that matches your Rule, in this case an iPhone.

<img class="no-shadow" src="http://docs.brightspot.s3.amazonaws.com/mobile-variation.png" alt="" />


## Creating New Operators

Extend from class `Operation` to create a new Operator:

<img src="http://docs.brightspot.s3.amazonaws.com/new_operation.png" alt="" />

<div class="highlight">{% highlight java %}
@Operation.DisplayName("This is a new Operation")
public class Test extends Operation {

    @Override
    public void evaluate (Variation variation, Profile profile, Object object) {
        State state = State.getInstance(object);
        Map<String, Object> variationData = (Map<String, Object>)
        state.getValue("variations/" + variation.getId());
    
        if (variationData != null) {
            state.getValues().putAll(variationData);
        }
    }
 }
{% endhighlight %}</div>

Adding your own rule is done through extending the `Rule` class:

<div class="highlight">{% highlight java %}public class TestRule extends Rule {

    private string name;
     
    public boolean evaluate(Variation variation, Profile profile, Object object){

}
{% endhighlight %}</div>