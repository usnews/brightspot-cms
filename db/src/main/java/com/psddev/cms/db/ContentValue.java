package com.psddev.cms.db;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Throwables;
import com.psddev.dari.db.Record;
import com.psddev.dari.db.Recordable;
import com.psddev.dari.util.ObjectUtils;

@ContentValue.Embedded
public abstract class ContentValue extends Record {

    @ToolUi.DisplayLast
    private List<ContentValueFilter> filters;

    public List<ContentValueFilter> getFilters() {
        if (filters == null) {
            filters = new ArrayList<ContentValueFilter>();
        }
        return filters;
    }

    public void setFilters(List<ContentValueFilter> filters) {
        this.filters = filters;
    }

    protected abstract Object doFindValue(Object content) throws Exception;

    public final Object findValue(Object content) {
        try {
            if (!ObjectUtils.isBlank(content)) {
                content = doFindValue(content);

                for (ContentValueFilter filter : getFilters()) {
                    content = filter.filter(content);

                    if (ObjectUtils.isBlank(content)) {
                        break;
                    }
                }
            }

            return content;

        } catch (Exception error) {
            throw Throwables.propagate(error);
        }
    }

    public static class JavaBeanProperty extends ContentValue {

        private String property;

        public String getProperty() {
            return property;
        }

        public void setProperty(String property) {
            this.property = property;
        }

        @Override
        protected Object doFindValue(Object content) throws IllegalAccessException, IntrospectionException, InvocationTargetException {
            PropertyDescriptor[] descs = Introspector.getBeanInfo(content.getClass()).getPropertyDescriptors();

            if (descs != null) {
                String property = getProperty();

                for (PropertyDescriptor desc : descs) {
                    if (desc.getName().equals(property)) {
                        Method reader = desc.getReadMethod();

                        if (reader != null) {
                            return reader.invoke(content);
                        }
                    }
                }
            }

            return null;
        }
    }

    public static class DariStatePath extends ContentValue {

        private String path;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        @Override
        protected Object doFindValue(Object content) throws IllegalAccessException, IntrospectionException, InvocationTargetException {
            if (content instanceof Recordable) {
                Object value = ((Recordable) content).getState().getByPath(getPath());

                if (!ObjectUtils.isBlank(value)) {
                    return value;
                }
            }

            return null;
        }
    }
}
