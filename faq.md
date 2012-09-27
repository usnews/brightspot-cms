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