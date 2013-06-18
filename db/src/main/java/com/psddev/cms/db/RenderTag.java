package com.psddev.cms.db;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.DynamicAttributes;
import javax.servlet.jsp.tagext.Tag;

import com.psddev.cms.tool.CmsTool;
import com.psddev.dari.db.Application;
import com.psddev.dari.db.Reference;
import com.psddev.dari.db.ReferentialText;
import com.psddev.dari.util.HtmlWriter;
import com.psddev.dari.util.ObjectUtils;

/**
 * Renders the given {@code value} safely in HTML context.
 *
 * <p>If the value is blank, the expression inside the tag is evaluated.
 * For example, given the following script where <code>${foo}</code> is
 * {@code null}:</p>
 *
 * <blockquote><pre><code data-type="java">{@literal
 *<cms:render value="${foo}">
 *    This is the fallback text.
 *</cms:render>
 * }</code></pre></blockquote>
 *
 * <p>The output would be {@code This is the fallback text.}</p>
 *
 * <p>If the value is an instance of {@link Iterable}, each item in it is
 * rendered in order.</p>
 *
 * <p>If the value is an instance of {@link ReferentialText}, the text is
 * written to the output as-is, and the objects in the references are rendered
 * according to the rules here.</p>
 *
 * <p>If the value is an instance of {@link String}, unsafe characters are
 * escaped, and the result is written to the output.</p>
 *
 * <p>Otherwise, the value is rendered using {@link PageFilter#renderObject}.
 * </p>
 */
@SuppressWarnings("serial")
public class RenderTag extends BodyTagSupport implements DynamicAttributes {

    private static final Pattern EMPTY_PARAGRAPH_PATTERN = Pattern.compile("(?is)\\s*<p[^>]*>\\s*&nbsp;\\s*</p>\\s*");

    private String area;
    private String context;
    private Object value;
    private String beginMarker;
    private int beginOffset;
    private String endMarker;
    private int endOffset;
    private final Map<String, String> attributes = new LinkedHashMap<String, String>();

    private transient HtmlWriter pageWriter;
    private transient LayoutTag layoutTag;
    private transient Map<String, Object> areas;

    public void setArea(String area) {
        this.area = area;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public void setBeginMarker(String beginMarker) {
        this.beginMarker = beginMarker;
    }

    public void setBeginOffset(int beginOffset) {
        this.beginOffset = beginOffset;
    }

    public void setEndMarker(String endMarker) {
        this.endMarker = endMarker;
    }

    public void setEndOffset(int endOffset) {
        this.endOffset = endOffset;
    }

    // --- DynamicAttributes support ---

    @Override
    public void setDynamicAttribute(String uri, String localName, Object value) {
        if (value != null) {
            attributes.put(localName, value.toString());
        }
    }

    // --- TagSupport support ---

    @Override
    @SuppressWarnings("deprecation")
    public int doStartTag() throws JspException {
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();

        if (!ObjectUtils.isBlank(context)) {
            ContextTag.Static.pushContext(request, context);
        }

        try {
            pageWriter = new HtmlWriter(pageContext.getOut());
            layoutTag = null;
            areas = null;

            for (Tag parent = getParent(); parent != null; parent = parent.getParent()) {
                if (parent instanceof RenderTag) {
                    break;

                } else if (parent instanceof LayoutTag) {
                    layoutTag = ((LayoutTag) parent);
                    areas = layoutTag.getAreas();
                    break;
                }
            }

            if (ObjectUtils.isBlank(value)) {
                if (areas != null) {
                    return EVAL_BODY_BUFFERED;

                } else {
                    if (!attributes.isEmpty()) {
                        pageWriter.writeStart("div", attributes);
                    }

                    setBodyContent(null);
                    return EVAL_BODY_INCLUDE;
                }

            } else {
                if (value instanceof Map) {
                    for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                        writeArea(request, entry.getKey(), entry.getValue());
                    }

                } else if (value instanceof Iterable &&
                        !(value instanceof ReferentialText)) {
                    int index = 0;
                    for (Object item : (Iterable<?>) value) {
                        writeArea(request, index, item);
                        ++ index;
                    }

                } else if (value instanceof Page.Area) {
                    Page.Area pageArea = (Page.Area) value;
                    writeArea(request, pageArea.getInternalName(), pageArea.getContents());

                } else if (value instanceof Section) {
                    Section section = (Section) value;
                    writeArea(request, section.getInternalName(), section);

                } else {
                    writeArea(request, area, value);
                }

                setBodyContent(null);
                return SKIP_BODY;
            }

        } catch (IOException error) {
            throw new JspException(error);

        } catch (ServletException error) {
            throw new JspException(error);
        }
    }

    private void writeArea(HttpServletRequest request, Object area, Object value) throws IOException, ServletException {
        if (layoutTag != null && areas != null) {
            if (!ObjectUtils.isBlank(area)) {
                Object oldGridArea = request.getAttribute("gridArea");
                StringWriter body = new StringWriter();

                try {
                    request.setAttribute("gridArea", layoutTag.getAreaName(request, area));
                    writeValueWithAttributes(new HtmlWriter(body), value);
                    areas.put(area.toString(), body.toString());

                } finally {
                    request.setAttribute("gridArea", oldGridArea);
                }
            }

        } else {
            writeValueWithAttributes(pageWriter, value);
        }
    }

    private void writeValueWithAttributes(HtmlWriter writer, Object value) throws IOException, ServletException {
        if (attributes.isEmpty()) {
            writeValue(writer, value);

        } else {
            writer.writeStart("div", attributes);
                writeValue(writer, value);
            writer.writeEnd();
        }
    }

    @SuppressWarnings("deprecation")
    private void writeValue(HtmlWriter writer, Object value) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        HttpServletResponse response = (HttpServletResponse) pageContext.getResponse();

        if (value instanceof ReferentialText) {
            List<Object> items = new ArrayList<Object>((ReferentialText) value);

            // Slice items based on markers.
            if (!(items.isEmpty() || (ObjectUtils.isBlank(beginMarker) && ObjectUtils.isBlank(endMarker)))) {
                int beginIndex = 0;
                int endIndex = items.size();

                if (!ObjectUtils.isBlank(beginMarker)) {
                    beginIndex = findMarker(items, beginMarker, beginOffset);
                }

                if (!ObjectUtils.isBlank(endMarker)) {
                    endIndex = findMarker(items, endMarker, endOffset);
                }

                if (beginIndex < 0 || endIndex < 0 || beginIndex >= endIndex) {
                    items.clear();
                } else {
                    items = items.subList(beginIndex, endIndex);
                }
            }

            Application.Static.getInstance(CmsTool.class).writeCss(request, writer);

            for (Object item : items) {
                if (item instanceof String) {
                    writer.write(EMPTY_PARAGRAPH_PATTERN.matcher((String) item).replaceAll(""));

                } else if (item instanceof Reference) {
                    Map<String, Object> oldAttributes = new LinkedHashMap<String, Object>();

                    try {
                        Reference itemReference = (Reference) item;
                        Object object = itemReference.getObject();

                        if (object != null && !(object instanceof ReferentialTextMarker)) {
                            for (Map.Entry<String, Object> entry : itemReference.entrySet()) {
                                String key = entry.getKey();
                                if (key != null && !key.startsWith("_")) {
                                    oldAttributes.put(key, request.getAttribute(key));
                                    request.setAttribute(key, entry.getValue());
                                }
                            }

                            PageFilter.renderObject(request, response, writer, object);
                        }

                    } finally {
                        for (Map.Entry<String, Object> entry : oldAttributes.entrySet()) {
                            request.setAttribute(entry.getKey(), entry.getValue());
                        }
                    }
                }
            }

        } else if (value instanceof Map) {
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                writeValue(writer, entry.getValue());
            }

        } else if (value instanceof Iterable) {
            for (Object item : (Iterable<?>) value) {
                writeValue(writer, item);
            }

        } else if (value instanceof Page.Area) {
            writeValue(writer, ((Page.Area) value).getContents());

        } else if (value instanceof String) {
            writer.html(value);

        } else {
            PageFilter.renderObject(request, response, writer, value);
        }
    }

    private int findMarker(List<Object> items, String internalName, int offset) {
        int itemIndex = 0;
        int markerIndex = 0;

        for (Object item : items) {
            if (item instanceof Reference) {
                Object referenced = ((Reference) item).getObject();

                if (referenced instanceof ReferentialTextMarker &&
                        internalName.equals(((ReferentialTextMarker) referenced).getInternalName())) {
                    if (offset == markerIndex) {
                        return itemIndex;
                    } else {
                        ++ markerIndex;
                    }
                }
            }

            ++ itemIndex;
        }

        return -1;
    }

    @Override
    public int doEndTag() throws JspException {
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();

        if (!ObjectUtils.isBlank(context)) {
            ContextTag.Static.popContext(request);
        }

        try {
            if (ObjectUtils.isBlank(value)) {
                if (bodyContent != null) {
                    String body = bodyContent.getString();

                    if (body != null) {
                        if (areas != null) {
                            if (!ObjectUtils.isBlank(area)) {
                                if (!attributes.isEmpty()) {
                                    StringWriter stringWriter = new StringWriter();
                                    @SuppressWarnings("resource")
                                    HtmlWriter htmlWriter = new HtmlWriter(stringWriter);

                                    htmlWriter.writeStart("div", attributes);
                                        htmlWriter.write(body);
                                    htmlWriter.writeEnd();

                                    body = stringWriter.toString();
                                }

                                areas.put(area, body);
                            }

                        } else {
                            pageWriter.write(body);

                            if (!attributes.isEmpty()) {
                                pageWriter.writeEnd();
                            }
                        }
                    }
                }
            }

            return EVAL_PAGE;

        } catch (IOException error) {
            throw new JspException(error);
        }
    }
}
