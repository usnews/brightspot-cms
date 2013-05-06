---
layout: default
title: Reloader Application
id: non-dari-reloader
section: documentation
---


<div markdown="1" class="span12">

The purpose of this document is to outline the minimum configuration necessary to use the Dari reloader application in a non-Dari Maven project. A non-Dari Maven project is one that does not declare dari-parent as its parent in its pom.xml file, for example:

{% highlight xml %}
<parent>
  <groupId>com.psddev</groupId>
  <artifactId>dari-parent</artifactId>
  <version>2.1-SNAPSHOT</version>
</parent>
{% endhighlight %}

### Modify pom.xml

Add the Groovy plugin with inline script source from [dari-grandparent](https://github.com/perfectsense/dari/blob/a8ca7a79c39458abeeed5d54027a2b7fc900e95d/grandparent/pom.xml#L85-L171) to your project pom.xml:


Add the “dari-util” dependency, which is required for the SourceFilter used in web.xml:
{% highlight xml %}
<dependency>
  <groupId>com.psddev</groupId>
  <artifactId>dari-util</artifactId>
  <version>2.1-SNAPSHOT</version>
</dependency>
{% endhighlight %}
Also add the “slf4j-” dependencies to enable reloader log messages:
{% highlight xml %}
<dependency>
  <groupId>org.slf4j</groupId>
  <artifactId>slf4j-api</artifactId>
  <version>1.6.0</version>
</dependency>

<dependency>
  <groupId>org.slf4j</groupId>
  <artifactId>slf4j-jdk14</artifactId>
  <version>1.6.0</version>
</dependency>
{% endhighlight %}
Add the following repository and pluginRepository elements:
{% highlight xml %}
<repositories>
  <repository>
	<id>psddev</id>
	<url>http://public.psddev.com/maven</url>
	<snapshots>
    <updatePolicy>always</updatePolicy>
	</snapshots>
	</repository>
</repositories>
<pluginRepositories>
  <pluginRepository>
	<id>psddev</id>
	<url>http://public.psddev.com/maven</url>
	<snapshots>
	<updatePolicy>always</updatePolicy>
	</snapshots>
  </pluginRepository>
</pluginRepositories>
{% endhighlight %}
### Modify web.xml

Add the following filter and mapping to your project web.xml:
{% highlight xml %}
<filter>
<filter-name>SourceFilter</filter-name>
	<filter-class>com.psddev.dari.util.SourceFilter</filter-class>
</filter>
<filter-mapping>
	<filter-name>SourceFilter</filter-name>
	<url-pattern>/*</url-pattern>
	<dispatcher>ERROR</dispatcher>
	<dispatcher>FORWARD</dispatcher>
	<dispatcher>INCLUDE</dispatcher>
	<dispatcher>REQUEST</dispatcher>
</filter-mapping>
{% endhighlight %}
</div>
