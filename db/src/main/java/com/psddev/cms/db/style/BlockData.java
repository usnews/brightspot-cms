package com.psddev.cms.db.style;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Throwables;
import com.psddev.dari.db.Record;
import com.psddev.dari.util.HtmlWriter;

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

    public static class Text extends BlockData {

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
        public boolean writeHtml(HtmlWriter writer, Object content) throws IOException {
            writer.writeHtml(getText());
            return true;
        }
    }

    public static class JavaBeanProperty extends BlockData {

        private String property;

        public String getProperty() {
            return property;
        }

        public void setProperty(String property) {
            this.property = property;
        }

        @Override
        public boolean writeHtml(HtmlWriter writer, Object content) throws IOException {
            try {
                PropertyDescriptor[] descs = Introspector.getBeanInfo(content.getClass()).getPropertyDescriptors();

                if (descs != null) {
                    String property = getProperty();

                    for (PropertyDescriptor desc : descs) {
                        if (desc.getName().equals(property)) {
                            Method reader = desc.getReadMethod();

                            if (reader != null) {
                                writer.writeHtml(reader.invoke(content));
                                return true;
                            }
                        }
                    }
                }

            } catch (IntrospectionException error) {
                // Ignore if the content class can't be introspected.

            } catch (IllegalAccessException error) {
                throw new IllegalStateException(error);

            } catch (InvocationTargetException error) {
                throw Throwables.propagate(error.getCause());
            }

            return false;
        }
    }
}
