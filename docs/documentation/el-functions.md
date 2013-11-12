---
layout: default
title: CMS El Functions
id: el-functions
section: documentation
---

<div markdown="1" class="span12">

### cms:inContext

Use `cms:inContext` when attempting to check the context. It returns true if the request is in the given context.

Example - Context set and checked

<div class="highlight">{% highlight jsp %}
<cms:context name="module"> CONTENT HERE </cms:context>
{% endhighlight %}</div>

<div class="highlight">{% highlight jsp %}
<c:if test="${cms:inContext('module')}">
   // Logic for action if in module context
</c:if>
{% endhighlight %}</div>

<div class="highlight">{% highlight jsp %}<function>
    <name>inContext</name>
    <function-class>com.psddev.cms.db.ElFunctionUtils</function-class>
    <function-signature>
        java.lang.boolean inContext(java.lang.String)
    </function-signature>
</function>
{% endhighlight %}</div>

### cms:instanceOf

Use `cms:instanceOf` to return true if the given object is an instance of the class represented by the given `className`.

Example - Show PhotoGallery title only when on the Gallery page, not when it is used as a module:

<div class="highlight">{% highlight jsp %}
<c:if test="${cms:instanceOf(mainContent,'com.psddev.brightspot.PhotoGallery')}">
    <h1><c:out value="${content.name}" /></h1>
</c:if>
{% endhighlight %}</div>

<div class="highlight">{% highlight jsp %}<function>
    <name>instanceOf</name>
    <function-class>com.psddev.cms.db.ElFunctionUtils</function-class>
    <function-signature>
        java.lang.boolean instanceOf(java.lang.Object, java.lang.String)
    </function-signature>
</function>
{% endhighlight %}</div>

### cms:listLayouts

When using `@Renderer.ListLayouts` in your Java class to create a list of potential content types in the CMS for editors to populate, render them in order with `cms:listLayouts` in the JSP.

See the [CMS Grid Layouts](http://www.brightspotcms.com/new-page.html#cms-grid-layouts) documentation for more documentation on using ListLayouts.

<div class="highlight">{% highlight jsp %}
<cms:layout class="${cms:listLayouts(content, 'modules')}">
	<cms:render value="${content.modules}" />
</cms:layout>
{% endhighlight %}</div>

<div class="highlight">{% highlight jsp %}<function>
<function>
    <name>listLayouts</name>
    <function-class>com.psddev.cms.db.ElFunctionUtils</function-class>
    <function-signature>
        java.util.List listLayouts(java.lang.Object, java.lang.String)
    </function-signature>
</function>
{% endhighlight %}</div>

### cms:html

Use `cms:html` when you need to escape the given string so that it's safe to use in Html.

<div class="highlight">{% highlight jsp %}
<function>
    <name>html</name>
    <function-class>com.psddev.cms.db.ElFunctionUtils</function-class>
    <function-signature>
        java.lang.String html(java.lang.String)
    </function-signature>
</function>
{% endhighlight %}</div>

### cms:js

Use `cms:js` when you need to escape the given string so that it's safe to use in JavaScript.

<div class="highlight">{% highlight jsp %}
<function>
    <name>js</name>
    <function-class>com.psddev.cms.db.ElFunctionUtils</function-class>
    <function-signature>
        java.lang.String js(java.lang.String)
    </function-signature>
</function>
{% endhighlight %}</div>

### cms:query

Use `cms:query` to build a query. The example below finds the widget class (which extends tool, so `.first()` can be used. *Note: Tomcat 7 is required.*

<div class="highlight">{% highlight jsp %}
<c:set var="content" value="${cms:query('com.psddev.brightspot.utils.Widget').first()}"/>

<cms:render value="${content.widgetName}"/>
{% endhighlight %}</div>

For the JavaDocs version of this documentation see the [ElFunctionUtils](http://www.brightspotcms.com/javadocs/com/psddev/cms/db/ElFunctionUtils.html) doc.