package com.psddev.cms.db;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

/**
 * For setting contexts that can change all renderers within.
 *
 * <p>For example, given the following {@code Article.java}:</p>
 *
 * <blockquote><pre><code data-type="java">
{@literal @}Renderer.Path(value = "article.jsp")
class Article {
    Image mainImage;
}
 * </code></pre></blockquote>
 *
 * <p>And {@code Image.java}:</p>
 *
 * <blockquote><pre><code data-type="java">
{@literal @}Renderer.Paths({
    {@literal @}Renderer.Path(value = "image-in-article.jsp", context = "image"),
    {@literal @}Renderer.path(value = "image.jsp")
})
class Image { ... }
 * </code></pre></blockquote>
 *
 * <p>The {@code cms:render} in the following {@code article.jsp} would
 * use {@code image-in-article.jsp} to render the {@code Image} instance:</p>
 *
 * <blockquote><pre><code data-type="jsp">{@literal
<cms:context name="article">
    <cms:render value="${article.mainImage}" />
</cms:context>
 * }</code></pre></blockquote>
 *
 * @see Renderer
 */
@SuppressWarnings("serial")
public class ContextTag extends BodyTagSupport {

    private static final String ATTRIBUTE_PREFIX = ContextTag.class.getName();
    private static final String CONTEXTS_ATTRIBUTE = ATTRIBUTE_PREFIX + "contexts";

    private String name;

    public void setName(String name) {
        this.name = name;
    }

    // --- BodyTagSupport support ---

    @Override
    public int doStartTag() throws JspException {
        Static.pushContext(pageContext.getRequest(), name);
        return EVAL_BODY_INCLUDE;
    }

    @Override
    public int doEndTag() throws JspException {
        Static.popContext(pageContext.getRequest());
        return EVAL_PAGE;
    }

    /**
     * {@link ContextTag} utility methods.
     */
    public static final class Static {

        /**
         * Returns the deque of all contexts associated with the gieven
         * {@code request} so far.
         *
         * @param request Can't be {@code null}.
         * @return Never {@code null}. Mutable.
         */
        @SuppressWarnings("unchecked")
        public static Deque<String> getContexts(ServletRequest request) {
            Deque<String> contexts = (Deque<String>) request.getAttribute(CONTEXTS_ATTRIBUTE);

            if (contexts == null) {
                contexts = new ArrayDeque<String>();
                request.setAttribute(CONTEXTS_ATTRIBUTE, contexts);
            }

            return contexts;
        }

        /**
         * Pushes the given {@code context} to be current in the given
         * {@code request}.
         *
         * @param request Can't be {@code null}.
         * @param context May be {@code null}.
         */
        public static void pushContext(ServletRequest request, String context) {
            getContexts(request).addLast(context);
            request.setAttribute("context", context);
        }

        /**
         * Pops the current context out of the given {@code request}.
         *
         * @param request Can't be {@code null}.
         */
        public static String popContext(ServletRequest request) {
            Deque<String> contexts = getContexts(request);
            String popped = contexts.removeLast();

            request.setAttribute("context", contexts.peekLast());
            return popped;
        }

        /**
         * Returns {@code true} if the given {@code request} is in the given
         * {@code context}.
         *
         * @param context If {@code null}, returns {@code false}.
         */
        public static boolean isInContext(ServletRequest request, String context) {
            if (context != null) {
                for (Iterator<String> i = getContexts(request).descendingIterator(); i.hasNext(); ) {
                    if (context.equals(i.next())) {
                        return true;
                    }
                }
            }

            return false;
        }
    }
}
