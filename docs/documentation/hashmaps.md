---
layout: default
title: HashMaps
id: hashmaps
section: documentation
---

<div markdown="1" class="span12">


In Dari, there is a unique implementation of the Java class, HashMap, called ObjectMap. ObjectMap is a `Map<String,Object>` backed by an object, with the option of using a [Converter class](http://docs.oracle.com/javaee/5/api/javax/faces/convert/Converter.html) to convert the keys. This allows the getting/setting of fields of the object via the Map interface.

If a Converter is defined, keys are passed through it to get the actual key used when interacting with the object. Additionally, fields that do not belong to the backing object are stored in a secondary store by the ObjectMap, pretending they are part of the object. The Key of an ObjectMap should be a String object, while the value of an ObjectMap can be any object. 

In addition to methods implemented from HashMap, ObjectMap has the following methods. 

**getObject()**

This method returns the object of the key and value pair. 

	public Object getObject() {
        return object;
    }

**getConverter()**

This method returns the converter object used to convert the key.

	public Converter getConverter() {
        return converter != null ? converter : DEFAULT_CONVERTER;
    }

**setObject()**

This method sets the object of the key and value pair.


**setConverter()**

This method sets the converter object.  

	public void setConverter(Converter converter) {
        this.converter = converter;
    }
</div>
	
