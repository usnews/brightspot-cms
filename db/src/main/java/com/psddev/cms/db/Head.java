package com.psddev.cms.db;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.psddev.dari.util.ErrorUtils;
import com.psddev.dari.util.HtmlWriter;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.TypeDefinition;

/**
 * Collection of all nodes within the {@code <head>} element.
 */
public class Head {

    private List<HeadNode> nodes;

    /**
     * Returns the list of all nodes in the {@code <head>} element.
     *
     * @return Never {@code null}. Mutable.
     */
    public List<HeadNode> getNodes() {
        if (nodes == null) {
            nodes = new ArrayList<HeadNode>();
        }
        return nodes;
    }

    /**
     * Sets the list of all nodes in the {@code <head>} element.
     *
     * @param nodes May be {@code null} to clear the list.
     */
    public void setNodes(List<HeadNode> nodes) {
        this.nodes = nodes;
    }

    @SuppressWarnings("unchecked")
    private <T extends HeadNode.Tag> T findOrCreate(Class<T> tagClass, String name, Object... attributes) {
        ErrorUtils.errorIfBlank(name, "name");

        for (HeadNode node : getNodes()) {
            if (!node.getClass().equals(tagClass)) {
                continue;
            }

            HeadNode.Tag tag = (HeadNode.Tag) node;

            if (name.equals(tag.getName()) &&
                    tag.hasAttributes(attributes)) {
                return (T) tag;
            }
        }

        HeadNode.Tag tag = TypeDefinition.getInstance(tagClass).newInstance();

        tag.setName(name);
        tag.addAttributes(attributes);
        getNodes().add(tag);
        return (T) tag;
    }

    /**
     * Finds or creates a self-closing tag with the given {@code name} and
     * {@code attributes}.
     *
     * @param name Can't be blank.
     * @param attributes May be {@code null}.
     * @return Never {@code null}.
     */
    public HeadNode.Tag findOrCreateTag(String name, Object... attributes) {
        return findOrCreate(HeadNode.Tag.class, name, attributes);
    }

    /**
     * Finds or creates an element with the given {@code name} and
     * {@code attributes}.
     *
     * @param name Can't be blank.
     * @param attributes May be {@code null}.
     * @return Never {@code null}.
     */
    public HeadNode.Element findOrCreateElement(String name, Object... attributes) {
        return findOrCreate(HeadNode.Element.class, name, attributes);
    }

    /**
     * Removes all tags with the given {@code name} and {@code attributes}.
     *
     * @param name If blank, does nothing.
     * @param attribues May be {@code null}.
     */
    public void removeTag(String name, Object... attributes) {
        if (ObjectUtils.isBlank(name)) {
            return;
        }

        for (Iterator<HeadNode> i = getNodes().iterator(); i.hasNext(); ) {
            HeadNode node = i.next();

            if (!(node instanceof HeadNode.Tag)) {
                continue;
            }

            HeadNode.Tag tag = (HeadNode.Tag) node;

            if (name.equals(tag.getName()) &&
                    tag.hasAttributes(attributes)) {
                i.remove();
            }
        }
    }

    /**
     * Sets {@code <title>...</title>}.
     *
     * @param title If blank, removes the element.
     */
    public void setTitle(String title) {
        if (ObjectUtils.isBlank(title)) {
            removeTag("title");

        } else {
            List<HeadNode> children = findOrCreateElement("title").getChildren();

            children.clear();
            children.add(HeadNode.text(title));
        }
    }

    /**
     * Sets {@code <link rel="canonical" href="...">}.
     *
     * @param url If blank, removes the tag.
     */
    public void setCanonicalUrl(String url) {
        if (ObjectUtils.isBlank(url)) {
            removeTag("link", "rel", "canonical");

        } else {
            findOrCreateTag("link", "rel", "canonical").getAttributes().put("href", url);
        }
    }

    /**
     * Sets {@code <meta name="..." content="...">}.
     *
     * @param name If blank, does nothing.
     * @param content If blank, removes the tag.
     */
    public void setMetaName(String name, String content) {
        if (!ObjectUtils.isBlank(name)) {
            if (ObjectUtils.isBlank(content)) {
                removeTag("meta", "name", name);

            } else {
                findOrCreateTag("meta", "name", name).getAttributes().put("content", content);
            }
        }
    }

    /**
     * Sets {@code <meta property="..." content="...">}.
     *
     * @param property If blank, does nothing.
     * @param content If blank, removes the tag.
     */
    public void setMetaProperty(String property, String content) {
        if (!ObjectUtils.isBlank(property)) {
            if (ObjectUtils.isBlank(content)) {
                removeTag("meta", "property", property);

            } else {
                findOrCreateTag("meta", "property", property).getAttributes().put("content", content);
            }
        }
    }

    /**
     * Sets {@code <meta http-equiv="..." content="...">}.
     *
     * @param httpEquiv If blank, does nothing.
     * @param content If blank, removes the tag.
     */
    public void setHttpEquiv(String httpEquiv, String content) {
        if (!ObjectUtils.isBlank(httpEquiv)) {
            if (ObjectUtils.isBlank(content)) {
                removeTag("meta", "http-equiv", httpEquiv);

            } else {
                findOrCreateTag("meta", "http-equiv", httpEquiv).getAttributes().put("contnet", content);
            }
        }
    }

    /**
     * Adds {@code <link rel="stylsheet" type="text/css" href="...">}.
     *
     * @param href If blank, does nothing.
     */
    public void addStyleSheet(String href) {
        if (!ObjectUtils.isBlank(href)) {
            findOrCreateTag("link",
                    "rel", href.endsWith(".less") ? "stylesheet/less" : "stylesheet",
                    "type", "text/css",
                    "href", ElFunctionUtils.resource(href));
        }
    }

    /**
     * Adds {@code <script type="text/javascript" src="..."></script>}.
     *
     * @param src If blank, does nothing.
     */
    public void addScript(String src) {
        if (!ObjectUtils.isBlank(src)) {
            findOrCreateElement("script",
                    "type", "text/javascript",
                    "src", ElFunctionUtils.resource(src));
        }
    }

    /**
     * Writes the contents of the {@code <head>} to the given {@code writer}.
     */
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
