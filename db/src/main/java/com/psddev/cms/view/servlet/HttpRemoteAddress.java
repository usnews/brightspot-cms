package com.psddev.cms.view.servlet;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.servlet.http.HttpServletRequest;

import com.psddev.dari.util.JspUtils;

/**
 * Populates a field with the remote address from an HTTP request.
 */
@ServletViewRequestAnnotationProcessorClass(HttpRemoteAddressProcessor.class)
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface HttpRemoteAddress {
}

class HttpRemoteAddressProcessor implements ServletViewRequestAnnotationProcessor<HttpRemoteAddress> {

    @Override
    public Object getValue(HttpServletRequest request, String fieldName, HttpRemoteAddress annotation) {
        return JspUtils.getRemoteAddress(request);
    }
}
