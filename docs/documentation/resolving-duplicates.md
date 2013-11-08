---
layout: default
title: Resolving Duplicates
id: resolvingDuplicates
section: documentation
---

<div markdown="1" class="span12">

While developing, you may add classes, then remove them from your project. The database still retains the information regarding their `internalName` etc. This means, if the class name is used again - it will be flagged as a duplicate ObjectType.

In order to remove an old unused ObjectType, to either clean up the data, or to allow for a new version with the same name, the code tool can be used.

Use the `internalName` - fully qualified path to Class, to find it.

<div class="highlight">{% highlight java %}

 Query.from(ObjectType.class).where("internalName = 'com.psddev.brightspot.Article'").first();
 
{% endhighlight %}</div>

Once you have located the ObjectType delete it:

<div class="highlight">{% highlight java %}

 Query.from(ObjectType.class).where("internalName = 'com.psddev.brightspot.Article'").deleteAll();
 
{% endhighlight %}</div>

If the duplicate error relates to duplicate Permalinks, there are likely two reasons.

1. You already have content with that permalink.

To solve this issue - either pick a new url, that doesn't match, or if you do not want the old one, remove it from the original piece of content.

2. You have content in Admin > Trash that is in a "trashed" state, but has a permalink still on it. Delete Permanently to remove the trashed object and free up the permalink.


</div>