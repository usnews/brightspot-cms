package com.psddev.cms.db;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import com.psddev.dari.db.Modification;
import com.psddev.dari.db.ObjectType;
import com.psddev.dari.db.Recordable.FieldInternalNamePrefix;
import com.psddev.dari.db.State;
import com.psddev.dari.util.ErrorUtils;
import com.psddev.dari.util.HtmlElement;
import com.psddev.dari.util.HtmlNode;
import com.psddev.dari.util.HtmlText;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.TypeDefinition;

/**
 * CMS-specific HTTP servlet page response.
 *
 * <p>This class is commonly used to easily update and render the contents
 * of the {@code <head>} element. For example, given the following class:</p>
 *
 * <blockquote><pre><code data-type="java">
 *public class Article extends Content implements PageResponse.Updatable {
 *
 *    private String title;
 *
 *    public String getTitle() {
 *        return title;
 *    }
 *
 *    @Override
 *    public void updateResponse(PageResponse response) {
 *        response.setTitle(getTitle());
 *    }
 *}
 * </code></pre></blockquote>
 *
 * <p>The following JSP fragment:</p>
 *
 * <blockquote><pre><code data-type="jsp">
 *&lt;head&gt;
 *&lt;cms:render value="${response.headNodes}" /&gt;
 *&lt;/head&gt;
 * </code></pre></blockquote>
 *
 * <p>Will output:</p>
 *
 * <blockquote><pre><code data-type="html">{@literal
 *<head>
 *<title>The Article Title</title>
 *</head>
 * }</code></pre></blockquote>
 *
 * <p>Additionally, classes can share the update logic using the
 * {@link UpdateClass} annotation:</p>
 *
 * <blockquote><pre><code data-type="java">
 *public class DefaultPageResponseUpdater implements PageResponse.Updatable {
 *
 *    @Override
 *    public void updateResponse(PageResponse response) {
 *        response.setTitle(getTitle());
 *    }
 *}
 *
 *@PageResponse.UpdateClass(DefaultPageResponseUpdater.class)
 *public class Article extends Content {
 *}
 * </code></pre></blockquote>
 */
public class PageResponse extends HttpServletResponseWrapper {

    public final List<HtmlNode> headNodes;

    /**
     * Creates an instance that wraps the given {@code response} and shares
     * the internal state of the given {@code parent}. Typically, it's not
     * necessary to create this manually, because {@link PageResponseFilter}
     * will ensure that the response object is always wrapped.
     *
     * @param response Can't be {@code null}.
     * @param parent May be {@code null}.
     */
    public PageResponse(HttpServletResponse response, PageResponse parent) {
        super(response);

        this.headNodes = parent != null ? parent.headNodes : new ArrayList<HtmlNode>();
    }

    /**
     * Returns the list of all nodes in the {@code <head>} element.
     *
     * @return Never {@code null}. Mutable.
     */
    public List<HtmlNode> getHeadNodes() {
        return headNodes;
    }

    /**
     * Finds an element with the the given {@code name} and
     * {@code attributes} within the {@code <head>}.
     *
     * @param name Can't be blank.
     * @param attributes May be {@code null}.
     * @return May be {@code null}.
     */
    public HtmlElement findHeadElement(String name, Object... attributes) {
        ErrorUtils.errorIfBlank(name, "name");

        for (HtmlNode node : getHeadNodes()) {
            if (!(node instanceof HtmlElement)) {
                continue;
            }

            HtmlElement element = (HtmlElement) node;

            if (name.equals(element.getName()) &&
                    element.hasAttributes(attributes)) {
                return element;
            }
        }

        return null;
    }

    /**
     * Finds or creates an element with the given {@code name} and
     * {@code attributes} within the {@code <head>}.
     *
     * @param name Can't be blank.
     * @param attributes May be {@code null}.
     * @return Never {@code null}.
     */
    public HtmlElement findOrCreateHeadElement(String name, Object... attributes) {
        HtmlElement element = findHeadElement(name, attributes);

        if (element == null) {
            element = new HtmlElement();
            
            element.setName(name);
            element.addAttributes(attributes);
            getHeadNodes().add(element);
        }

        return element;
    }

    /**
     * Removes all tags with the given {@code name} and {@code attributes}
     * within the {@code <head>} element.
     *
     * @param name If blank, does nothing.
     * @param attribues May be {@code null}.
     */
    public void removeHeadTag(String name, Object... attributes) {
        if (ObjectUtils.isBlank(name)) {
            return;
        }

        for (Iterator<HtmlNode> i = getHeadNodes().iterator(); i.hasNext(); ) {
            HtmlNode node = i.next();

            if (!(node instanceof HtmlElement)) {
                continue;
            }

            HtmlElement element = (HtmlElement) node;

            if (name.equals(element.getName()) &&
                    element.hasAttributes(attributes)) {
                i.remove();
            }
        }
    }

    /**
     * Returns the title (the text in the {@code <title>} element).
     *
     * @return Never {@code null}.
     */
    public String getTitle() {
        StringBuilder titleText = new StringBuilder();
        HtmlElement titleElement = findHeadElement("title");

        if (titleElement != null) {
            for (HtmlNode child : titleElement.getChildren()) {
                if (child instanceof HtmlText) {
                    titleText.append(((HtmlText) child).getText());
                }
            }
        }

        return titleText.toString();
    }

    /**
     * Sets the title (the text in the {@code <title>} element).
     *
     * @param title If {@code null}, removes the element.
     */
    public void setTitle(String title) {
        if (ObjectUtils.isBlank(title)) {
            removeHeadTag("title");

        } else {
            HtmlText text = new HtmlText();
            List<HtmlNode> children = findOrCreateHeadElement("title").getChildren();

            text.setText(title);
            children.clear();
            children.add(text);
        }

        setMetaProperty("og:title", title);
    }

    /**
     * Returns the description (the value of the {@code content} attribute
     * in either {@code <meta name="description">} or
     * {@code <meta property="og:description">}).
     *
     * @return Never {@code null}.
     */
    public String getDescription() {
        HtmlElement descriptionElement = findHeadElement("meta", "name", "description");

        if (descriptionElement == null) {
            descriptionElement = findHeadElement("meta", "property", "og:description");
        }

        if (descriptionElement != null) {
            String description = descriptionElement.getAttributes().get("content");

            if (description != null) {
                return description;
            }
        }

        return "";
    }

    /**
     * Sets the description (the value of the {@code content} attribute
     * in both {@code <meta name="description" content="description">}
     * and {@code <meta property="og:description" content="description">}).
     *
     * @param description If {@code null}, removes the elements.
     */
    public void setDescription(String description) {
        setMetaName("description", description);
        setMetaProperty("og:description", description);
    }

    /**
     * Returns the canonical URL (the value of the {@code href} attribute
     * in {@code <link rel="canonical">}.
     *
     * @return May be {@code null}.
     */
    public String getCanonicalUrl() {
        HtmlElement urlElement = findHeadElement("link", "rel", "canonical");

        return urlElement != null ?
                urlElement.getAttributes().get("href") :
                null;
    }

    /**
     * Sets the canonical URL (the value of the {@code href} attribute in
     * {@code <link rel="canonical">}.
     *
     * @param canonicalUrl If {@code null}, removes the element.
     */
    public void setCanonicalUrl(String canonicalUrl) {
        if (ObjectUtils.isBlank(canonicalUrl)) {
            removeHeadTag("link", "rel", "canonical");

        } else {
            findOrCreateHeadElement("link", "rel", "canonical").getAttributes().put("href", canonicalUrl);
        }
    }

    /**
     * Sets {@code <meta name="name" content="content">}.
     *
     * @param name If blank, does nothing.
     * @param content If blank, removes the tag.
     */
    public void setMetaName(String name, String content) {
        if (!ObjectUtils.isBlank(name)) {
            if (ObjectUtils.isBlank(content)) {
                removeHeadTag("meta", "name", name);

            } else {
                findOrCreateHeadElement("meta", "name", name).getAttributes().put("content", content);
            }
        }
    }

    /**
     * Sets {@code <meta property="property" content="content">}.
     *
     * @param property If blank, does nothing.
     * @param content If blank, removes the tag.
     */
    public void setMetaProperty(String property, String content) {
        if (!ObjectUtils.isBlank(property)) {
            if (ObjectUtils.isBlank(content)) {
                removeHeadTag("meta", "property", property);

            } else {
                findOrCreateHeadElement("meta", "property", property).getAttributes().put("content", content);
            }
        }
    }

    /**
     * Sets {@code <meta http-equiv="httpEquiv" content="content">}.
     *
     * @param httpEquiv If blank, does nothing.
     * @param content If blank, removes the tag.
     */
    public void setHttpEquiv(String httpEquiv, String content) {
        if (!ObjectUtils.isBlank(httpEquiv)) {
            if (ObjectUtils.isBlank(content)) {
                removeHeadTag("meta", "http-equiv", httpEquiv);

            } else {
                findOrCreateHeadElement("meta", "http-equiv", httpEquiv).getAttributes().put("contnet", content);
            }
        }
    }

    /**
     * Adds {@code <link rel="stylsheet" type="text/css" href="href">}.
     *
     * @param href If blank, does nothing.
     */
    public void addStyleSheet(String href) {
        if (!ObjectUtils.isBlank(href)) {
            findOrCreateHeadElement("link",
                    "rel", href.endsWith(".less") ? "stylesheet/less" : "stylesheet",
                    "type", "text/css",
                    "href", ElFunctionUtils.resource(href));
        }
    }

    /**
     * Adds {@code <script type="text/javascript" src="src"></script>}
     * within the {@code <head>} element.
     *
     * @param src If blank, does nothing.
     */
    public void addScript(String src) {
        if (!ObjectUtils.isBlank(src)) {
            findOrCreateHeadElement("script",
                    "type", "text/javascript",
                    "src", ElFunctionUtils.resource(src));
        }
    }

    /**
     * Updates this response using the given {@code object}.
     *
     * @param object Can't be {@code null}.
     */
    public void update(Object object) {
        State state = State.getInstance(object);
        ObjectType type = state.getType();

        if (type != null) {
            Updatable updatable = type.as(TypeData.class).createUpdatable();

            if (updatable != null) {
                updatable.updateResponse(this);
            }
        }

        if (object instanceof Updatable) {
            ((Updatable) object).updateResponse(this);
        }
    }

    /**
     * Specifies the {@link Updatable} class that will update the
     * {@link PageResponse} for all instances of the target type. If the class
     * directly implements {@link Updatable}, this will run after.
     */
    @Documented
    @Inherited
    @ObjectType.AnnotationProcessorClass(UpdateClassProcessor.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface UpdateClass {

        Class<? extends Updatable> value();
    }

    private static class UpdateClassProcessor implements ObjectType.AnnotationProcessor<UpdateClass> {

        @Override
        public void process(ObjectType type, UpdateClass annotation) {
            type.as(TypeData.class).setUpdateClassName(annotation.value().getName());
        }
    }

    /**
     * Type modification that adds {@link PageResponse}-specific data.
     */
    @FieldInternalNamePrefix("cms.pageResponse.")
    public static class TypeData extends Modification<ObjectType> {

        private String updateClassName;

        public String getUpdateClassName() {
            return updateClassName;
        }

        public void setUpdateClassName(String updateClassName) {
            this.updateClassName = updateClassName;
        }

        /**
         * Creates an updatable object appropriate for this type.
         *
         * @return May be {@code null}.
         */
        @SuppressWarnings("unchecked")
        public Updatable createUpdatable() {
            Class<?> c = ObjectUtils.getClassByName(getUpdateClassName());

            return c != null && Updatable.class.isAssignableFrom(c) ?
                    TypeDefinition.getInstance((Class<? extends Updatable>) c).newInstance() :
                    null;
        }
    }

    /**
     * {@link PageFilter} will call {@link #updateResponse} before rendering
     * if the main content implements this interface.
     */
    public static interface Updatable {

        /**
         * Updates the given {@code response}. This is typically used to set
         * the nodes within the {@code <head>} element, such as the
         * {@code <title>}.
         */
        public void updateResponse(PageResponse response);
    }
}
