package com.psddev.cms.view.servlet;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.servlet.http.HttpServletRequest;

/**
 * Populates a field with the value of the request URL from an HTTP request.
 */
@ServletViewRequestAnnotationProcessorClass(HttpUrlProcessor.class)
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface HttpUrl {
}

class HttpUrlProcessor implements ServletViewRequestAnnotationProcessor<HttpUrl> {

    @Override
    public Object getValue(HttpServletRequest request, String fieldName, HttpUrl annotation) {
        return request.getRequestURL().toString();
    }
}
