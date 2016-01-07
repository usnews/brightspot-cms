package com.psddev.cms.view.servlet;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.psddev.dari.util.StringUtils;

/**
 * Populates a field with the header value from an HTTP request. The header
 * fetched has the same name as the field it populates unless otherwise
 * specified.
 */
@ServletViewRequestAnnotationProcessorClass(HttpHeaderProcessor.class)
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface HttpHeader {
    String value() default "";
}

class HttpHeaderProcessor implements ServletViewRequestAnnotationProcessor<HttpHeader> {

    @Override
    public Object getValue(HttpServletRequest request, String fieldName, HttpHeader annotation) {

        String headerName = annotation.value();
        if (StringUtils.isBlank(headerName)) {
            headerName = fieldName;
        }

        List<String> headerValues = Collections.list(request.getHeaders(headerName));

        if (headerValues.size() == 1) {
            return headerValues.get(0);
        } else {
            return headerValues;
        }
    }
}
