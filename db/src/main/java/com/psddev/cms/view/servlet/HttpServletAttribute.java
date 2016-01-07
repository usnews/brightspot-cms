package com.psddev.cms.view.servlet;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.servlet.http.HttpServletRequest;

import com.psddev.dari.util.StringUtils;

/**
 * Populates a field with the servlet request attribute value from a Servlet
 * based HTTP request. The attribute fetched has the same name as the field it
 * populates unless otherwise specified.
 */
@ServletViewRequestAnnotationProcessorClass(HttpServletAttributeProcessor.class)
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface HttpServletAttribute {
    String value() default "";
}

class HttpServletAttributeProcessor implements ServletViewRequestAnnotationProcessor<HttpServletAttribute> {

    @Override
    public Object getValue(HttpServletRequest request, String fieldName, HttpServletAttribute annotation) {

        String attributeName = annotation.value();
        if (StringUtils.isBlank(attributeName)) {
            attributeName = fieldName;
        }

        return request.getAttribute(attributeName);
    }
}
