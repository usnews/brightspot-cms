package com.psddev.cms.db;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import com.psddev.dari.util.HtmlWriter;
import com.psddev.dari.util.ObjectUtils;

public class Head {

    private List<HeadNode> nodes;

    /**
     * @return Never {@code null}. Mutable.
     */
    public List<HeadNode> getNodes() {
        if (nodes == null) {
            nodes = new ArrayList<HeadNode>();
        }
        return nodes;
    }

    /**
     * @param nodes May be {@code null} to clear the list.
     */
    public void setNodes(List<HeadNode> nodes) {
        this.nodes = nodes;
    }

    /**
     * @param tagName If blank, does nothing.
     * @param attributes May be {@code null}.
     */
    public void addTag(String tagName, Object... attributes) {
        if (!ObjectUtils.isBlank(tagName)) {
            getNodes().add(HeadNode.tag(tagName, attributes));
        }
    }

    /**
     * @param title If blank, does nothing.
     */
    public void setTitle(String title) {
        if (!ObjectUtils.isBlank(title)) {
            getNodes().add(HeadNode.element("title").
                    child(HeadNode.text(title)));
        }
    }

    /**
     * @param name If blank, does nothing.
     * @param content May be {@code null}.
     */
    public void addMetaName(String name, String content) {
        if (!ObjectUtils.isBlank(name)) {
            addTag("meta",
                    "name", name,
                    "content", content);
        }
    }

    /**
     * @param property If blank, does nothing.
     * @param content May be {@code null}.
     */
    public void addMetaProperty(String property, String content) {
        if (!ObjectUtils.isBlank(property)) {
            addTag("meta",
                    "property", property,
                    "content", content);
        }
    }

    /**
     * @param httpEquiv If blank, does nothing.
     * @param content May be {@code null}.
     */
    public void addHttpEquiv(String httpEquiv, String content) {
        if (!ObjectUtils.isBlank(httpEquiv)) {
            addTag("meta",
                    "http-equiv", httpEquiv,
                    "content", content);
        }
    }

    /**
     * @param href If blank, does nothing.
     */
    public void addStyleSheet(String href) {
        if (!ObjectUtils.isBlank(href)) {
            addTag("link",
                    "rel", href.endsWith(".less") ? "stylesheet/less" : "stylesheet",
                    "type", "text/css",
                    "href", ElFunctionUtils.resource(href));
        }
    }

    /**
     * @param src If blank, does nothing.
     */
    public void addScript(String src) {
        if (!ObjectUtils.isBlank(src)) {
            getNodes().add(HeadNode.element("script",
                    "type", "text/javascript",
                    "src", ElFunctionUtils.resource(src)));
        }
    }

    public void writeHtml(HtmlWriter writer) throws IOException {
        for (HeadNode node : getNodes()) {
            node.writeHtml(writer);
        }
    }

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
}
