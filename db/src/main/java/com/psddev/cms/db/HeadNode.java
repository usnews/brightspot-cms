package com.psddev.cms.db;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.psddev.dari.util.CompactMap;
import com.psddev.dari.util.ErrorUtils;
import com.psddev.dari.util.HtmlWriter;

/**
 * Node within the {@code <head>} element.
 *
 * @see Head
 */
public abstract class HeadNode {

    /**
     * Creates a self-closing tag with the given {@code name} and
     * {@code attributes}.
     *
     * @param name Can't be blank.
     * @param attributes May be {@code null}.
     */
    public static HeadNode.Tag tag(String name, Object... attributes) {
        ErrorUtils.errorIfBlank(name, "name");

        Tag tag = new Tag();

        tag.setName(name);
        tag.addAttributes(attributes);
        return tag;
    }

    /**
     * Creates an element with the given {@code name} and {@code attributes}.
     *
     * @param name Can't be blank.
     * @param attributes May be {@code null}.
     */
    public static HeadNode.Element element(String name, Object... attributes) {
        ErrorUtils.errorIfBlank(name, "name");

        Element element = new Element();

        element.setName(name);
        element.addAttributes(attributes);
        return element;
    }

    /**
     * Creates a text node with the given {@code text}.
     *
     * @param text If {@code null}, creates a text node with an empty string.
     */
    public static HeadNode.Text text(String text) {
        Text textNode = new Text();

        textNode.setText(text != null ? text : "");
        return textNode;
    }

    /**
     * Writes this node as HTML to the given {@code writer}.
     *
     * @param writer Can't be {@code null}.
     */
    public abstract void writeHtml(HtmlWriter writer) throws IOException;

    @Override
    public String toString() {
        StringWriter string = new StringWriter();
        HtmlWriter html = new HtmlWriter(string);

        try {
            try {
                writeHtml(html);

            } finally {
                html.close();
            }

        } catch (IOException error) {
            throw new IllegalStateException(error);
        }

        return string.toString();
    }

    /**
     * Self-closing tag within the {@code <head>}, such as {@code <link>}.
     */
    public static class Tag extends HeadNode {

        private String name;
        private Map<String, String> attributes;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        /**
         * @return Never {@code null}. Mutable.
         */
        public Map<String, String> getAttributes() {
            if (attributes == null) {
                attributes = new CompactMap<String, String>();
            }
            return attributes;
        }

        /**
         * @param attributes May be {@code null} to clear the map.
         */
        public void setAttributes(Map<String, String> attributes) {
            this.attributes = attributes;
        }

        /**
         * Adds all given {@code attributes}.
         *
         * @param attributes May be {@code null}.
         */
        public void addAttributes(Object... attributes) {
            if (attributes == null) {
                return;
            }

            for (int i = 0, length = attributes.length; i < length; ++ i) {
                Object name = attributes[i];

                if (name == null) {
                    ++ i;

                } else if (name instanceof Map) {
                    for (Map.Entry<?, ?> entry : ((Map<?, ?>) name).entrySet()) {
                        Object n = entry.getKey();
                        Object v = entry.getValue();

                        if (n != null && v != null) {
                            getAttributes().put(n.toString(), v.toString());
                        }
                    }

                } else {
                    ++ i;

                    if (i < length) {
                        Object value = attributes[i];

                        if (value != null) {
                            getAttributes().put(name.toString(), value.toString());
                        }
                    }
                }
            }
        }

        /**
         * Returns {@code true} if this tag contains the given
         * {@code attributes}.
         *
         * @param attributes If {@code null}, always returns {@code true}.
         */
        public boolean hasAttributes(Object... attributes) {
            if (attributes == null) {
                return true;
            }

            int length = attributes.length;

            if (length == 0) {
                return true;
            }

            for (int i = 0; i < length; ++ i) {
                Object name = attributes[i];

                if (name == null) {
                    throw new IllegalArgumentException();

                } else if (name instanceof Map) {
                    for (Map.Entry<?, ?> entry : ((Map<?, ?>) name).entrySet()) {
                        Object n = entry.getKey();

                        if (n == null) {
                            throw new IllegalArgumentException();
                        }

                        Object v = entry.getValue();

                        if (v == null) {
                            throw new IllegalArgumentException();
                        }

                        if (!v.toString().equals(getAttributes().get(n.toString()))) {
                            return false;
                        }
                    }

                } else {
                    ++ i;

                    if (i >= length) {
                        throw new IllegalArgumentException();
                    }

                    Object value = attributes[i];

                    if (value == null) {
                        throw new IllegalArgumentException();
                    }

                    if (!value.toString().equals(getAttributes().get(name.toString()))) {
                        return false;
                    }
                }
            }

            return true;
        }

        @Override
        public void writeHtml(HtmlWriter writer) throws IOException {
            writer.writeTag(getName(), getAttributes());
        }
    }

    /**
     * Element within the {@code <head>}, such as {@code <title>}.
     */
    public static class Element extends HeadNode.Tag {

        private List<HeadNode> children;

        /**
         * @return Never {@code null}. Mutable.
         */
        public List<HeadNode> getChildren() {
            if (children == null) {
                children = new ArrayList<HeadNode>();
            }
            return children;
        }

        /**
         * @param children May be {@code null} to clear the list.
         */
        public void setChildren(List<HeadNode> children) {
            this.children = children;
        }

        @Override
        public void writeHtml(HtmlWriter writer) throws IOException {
            writer.writeStart(getName(), getAttributes());

            List<HeadNode> children = getChildren();

            if (children != null) {
                for (HeadNode child : children) {
                    writer.writeRaw(child.toString());
                }
            }

            writer.writeEnd();
        }
    }

    /**
     * Text within the {@code <head>}, typically as a child in an
     * {@link Element}.
     */
    public static class Text extends HeadNode {

        private String text;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        @Override
        public void writeHtml(HtmlWriter writer) throws IOException {
            writer.writeHtml(text);
        }
    }
}
