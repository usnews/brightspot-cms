---
layout: default
title: FAQ
id: faq
---

## Frequently Asked Questions

### Solrj Version Error

`Invalid version (expected 2, but 1) or the data in not in 'javabin' format (java.lang.RuntimeException)`

The error above is due to a Solr version issue. Make sure that the version of Solr you have deployed matches that outlined in your `pom.xml`. To fix, define an explicit version of the solrj to match your Solr version.

Within your Maven Project `pom.xml` add/update the following:

        <dependency>
            <groupId>org.apache.solr</groupId>
            <artifactId>solr-solrj</artifactId>
            <version>3.6.0</version>
        </dependency>

Rebuild your war and deploy to see the fix.

### Build Errors

**Failed to execute goal org.codehaus.mojo
**

	[ERROR] Failed to execute goal org.codehaus.mojo:exec-maven-plugin:1.2.1:java
	(default) on project petfinder: Execution default of goal
	org.codehaus.mojo:exec-maven-plugin 1.2.1:java failed: Plugin
	org.codehaus.mojo:exec-maven-plugin:1.2.1 or one of its dependencies could
	not be resolved
	
The build error above is produced when the following Plugin snippet is not included in your project pom.xml. Add and then rebuild to fix.
	
	 <pluginRepositories>
        <pluginRepository>
            <id>public.psddev</id>
            <url>http://public.psddev.com/maven</url>
            <snapshots>
                <updatePolicy>always</updatePolicy>
            </snapshots>
        </pluginRepository>
    </pluginRepositories>