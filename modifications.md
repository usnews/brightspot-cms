---
layout: default
title: Modifications
id: modifications
section: documentation
---

<div markdown="1" class="span12">

The `Modification` class, found within [Dari](http://www.dariframework.org/javadocs/com/psddev/dari/db/Modification.html) can be used to provide inheritance to multiple object types from one singular class. It is typically used when a property, not common among a group of objects, needs to be added after they have already been created.

Normal inheritance can be achieved as would be expected with Java. Here is an example of an abstract class, with several fields on it, which are inherited by anything extending it.

<div class="highlight">{% highlight java %}public abstract class AbstractArticle extends Content {

    private String headline;
    private ReferentialText body;

    // Getters Setters

}
{% endhighlight %}</div>

The User interface that is created in Brightspot when extending a parent class is automatically generated. The inherited fields appear at the top, and additional fields are added below. The `newsTags` property will only appear when NewsArticles are created.

<div class="highlight">{% highlight java %}
public class NewsArticle extends AbstractArticle {

    private List<Tag> newsTags;
}
{% endhighlight %}</div>


**Example of a Modification, using a common Interface**

A good example use case of implementing a modification would be in the case of a global property, `promoTitle` or `promoImage` which needs to be added on a group of  objects, such as `Blog`, `Article`, `Image`, `Slideshow`. They do not inherit from one global class, therefore there is no quick means to apply the property to them all. In this case, a modification can be used to add the fields to all the objects.

**Step 1. Create Common Interface**

<div class="highlight">{% highlight java %}import com.psddev.dari.db.Recordable;

public interface Promotable extends Recordable {

}
{% endhighlight %}</div>

**Step 2. Create your Modification**

<div class="highlight">{% highlight java %}import com.psddev.dari.db.Modification;

public class DefaultPromotable extends Modification<Promotable> {

    @Indexed
    private String promoTitle;
    private Image promoImage;

    // Getters Setters

}
{% endhighlight %}</div>

**Step 3. Implement Modification** 

<div class="highlight">{% highlight java %}public class Blog extends Content implements Promotable {

    private String title;
    
    // Getters Setters
    
}
{% endhighlight %}</div>

**Accessing Modification Fields**

In the example above, a new `promoTitle` and `promoImage` can be added to the objects implementing the interface. To access these fields when rendering the content, you don't have direct access:

The following will not work:

<div class="highlight">{% highlight jsp %}<cms:render value="${content.promoTitle}"/>

<cms:img src="${content.promoImage}"/>

{% endhighlight %}</div>

In order to access those field, you must annotate the modification class, to give it a `@BeanProperty`:

<div class="highlight">{% highlight java %}import com.psddev.dari.db.Modification;

@BeanProperty("promotable")
public class DefaultPromotable extends Modification<Promotable> {

    @Indexed
    private String promoTitle;
    private Image promoImage;

    // Getters Setters

}
{% endhighlight %}</div>

This allows direct access when rendering your content. It must be unique.

<div class="highlight">{% highlight jsp %}<cms:render value="${content.promotable.promoTitle}"/>

<cms:img src="${content.promotable.promoImage}"/>

{% endhighlight %}</div>


**No Common Interface**

With Dari there is another method by which you can implement multiple inheritance with modifications, without the need for the common interface. By using `Modification.Classes` for a new class, and then defining the classes that are to inherit, you can modify multiple objects from one single class.

**Step 1. Implement Modification** 

<div class="highlight">{% highlight java %}@Modification.Classes({Blog.class, Article.class})
public class DefaultPromotable extends Modification<Object> {

    @Indexed
    private String promoTitle;
    private Image promoImage;

    // Getters Setters

}
{% endhighlight %}</div>

**When not to use Modifications**

Modifications, while providing the ability to group objects, and apply properties across them all, should not be used to replace sub classes. See below for an example:


Take three objects, `Blog`, `Video` and `News`, none of which have a common class they are abstracted from. The requirement is to query from the three objects, and return a list of the most recent items.

With a modification, a `RecentItems` interface could be created, and the modification could contain the property on which the query would be run, however the limitation is that the interface, which groups them cannot be queried directly. Namely, `Query.from(RecentItems.class).sortDescending('date').selectAll();` will not work. In order for it to work, the methods from your modification would need to be moved into your interface.

At this point, the modification becomes a bad practice for achieving the desired result, and a parent and subclass structure is recommended.
	