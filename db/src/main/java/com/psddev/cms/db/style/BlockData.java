package com.psddev.cms.db.style;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.psddev.dari.db.Record;
import com.psddev.dari.db.Recordable;
import com.psddev.dari.util.HtmlWriter;
import com.psddev.dari.util.ObjectUtils;

@BlockData.Embedded
public abstract class BlockData extends Record {

    public abstract boolean writeHtml(HtmlWriter writer, Object content) throws IOException;

    public static class Cascade extends BlockData {

        private List<BlockData> data;

        public List<BlockData> getData() {
            if (data == null) {
                data = new ArrayList<BlockData>();
            }
            return data;
        }

        public void setData(List<BlockData> data) {
            this.data = data;
        }

        @Override
        public boolean writeHtml(HtmlWriter writer, Object content) throws IOException {
            for (BlockData d : getData()) {
                if (d.writeHtml(writer, content)) {
                    return true;
                }
            }

            return false;
        }
    }

    public static class Container extends BlockData {

        private List<Block> children;

        public List<Block> getChildren() {
            if (children == null) {
                children = new ArrayList<Block>();
            }
            return children;
        }

        public void setChildren(List<Block> children) {
            this.children = children;
        }

        @Override
        public boolean writeHtml(HtmlWriter writer, Object content) throws IOException {
            for (Block child : getChildren()) {
                child.writeHtml(writer, content);
            }

            return true;
        }
    }

    public static class Text extends ValueBlockData {

        private String text;

        public Text() {
        }

        public Text(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        @Override
        public Object findValue(Object content) {
            return getText();
        }
    }

    public static class JavaBeanProperty extends ValueBlockData {

        private String property;

        public String getProperty() {
            return property;
        }

        public void setProperty(String property) {
            this.property = property;
        }

        @Override
        public Object findValue(Object content) throws IllegalAccessException, IntrospectionException, InvocationTargetException {
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

    public static class StatePath extends ValueBlockData {

        private String path;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        @Override
        public Object findValue(Object content) throws IllegalAccessException, IntrospectionException, InvocationTargetException {
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
