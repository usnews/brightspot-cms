package com.psddev.cms.db;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.psddev.dari.util.HtmlWriter;

public abstract class HeadNode {

    public static HeadNode.Tag tag(String tagName, Object... attributes) {
        return new Tag(tagName, attributes);
    }

    public static HeadNode.Element element(String tagName, Object... attributes) {
        return new Element(tagName, attributes);
    }

    public static HeadNode.Text text(String text) {
        return new Text(text);
    }

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

    public static class Tag extends HeadNode {

        private final String tagName;
        private final Object[] attributes;

        private Tag(String tagName, Object... attributes) {
            this.tagName = tagName;
            this.attributes = attributes != null ? Arrays.copyOf(attributes, attributes.length) : new Object[0];
        }

        @Override
        public void writeHtml(HtmlWriter writer) throws IOException {
            writer.writeTag(tagName, attributes);
        }
    }

    public static class Element extends HeadNode {

        private final String tagName;
        private final Object[] attributes;
        private List<HeadNode> children;

        private Element(String tagName, Object... attributes) {
            this.tagName = tagName;
            this.attributes = attributes != null ? Arrays.copyOf(attributes, attributes.length) : new Object[0];
        }

        public HeadNode child(HeadNode child) {
            if (children == null) {
                children = new ArrayList<HeadNode>();
            }

            children.add(child);
            return this;
        }

        @Override
        public void writeHtml(HtmlWriter writer) throws IOException {
            writer.writeStart(tagName, attributes);

            if (children != null) {
                for (HeadNode child : children) {
                    writer.writeRaw(child.toString());
                }
            }

            writer.writeEnd();
        }
    }

    public static class Text extends HeadNode {

        private final String text;

        private Text(String text) {
            this.text = text;
        }

        @Override
        public void writeHtml(HtmlWriter writer) throws IOException {
            writer.writeHtml(text);
        }
    }
}
