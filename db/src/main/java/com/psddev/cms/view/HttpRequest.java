package com.psddev.cms.view;

import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.TypeDefinition;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

public class HttpRequest {

    public static void applyServletHttpRequest(HttpServletRequest request, Object object) {

        Class<?> objectClass = object.getClass();

        for (Field field : TypeDefinition.getInstance(objectClass).getAllFields()) {

            Param param = field.getAnnotation(Param.class);

            if (param != null) {
                String paramName = param.value();

                if (paramName != null) {

                    Object paramValue;

                    Class<?> fieldType = field.getType();

                    if (List.class.isAssignableFrom(fieldType) ||
                            Set.class.isAssignableFrom(fieldType) ||
                            fieldType.isArray()) {

                        paramValue = request.getParameterValues(paramName);

                    } else {
                        paramValue = request.getParameter(paramName);
                    }

                    try {
                        field.set(object, ObjectUtils.to(field.getType(), paramValue));
                    } catch (IllegalAccessException e) {
                        // do nothing
                    }
                }
            }

            Header header = field.getAnnotation(Header.class);

            if (header != null) {
                String headerName = header.value();

                if (headerName != null) {

                    Object headerValue;

                    Class<?> fieldType = field.getType();

                    if (List.class.isAssignableFrom(fieldType) ||
                            Set.class.isAssignableFrom(fieldType) ||
                            fieldType.isArray()) {

                        headerValue = Collections.list(request.getHeaders(headerName));

                    } else {
                        headerValue = request.getHeader(headerName);
                    }

                    try {
                        field.set(object, ObjectUtils.to(field.getType(), headerValue));
                    } catch (IllegalAccessException e) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Param {
        String value();
    }

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Header {
        String value();
    }
}
