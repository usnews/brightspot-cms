package com.psddev.cms.db;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
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

import com.psddev.dari.db.Reference;
import com.psddev.dari.db.ReferentialText;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.StringUtils;

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
public class RenderTag extends BodyTagSupport {

    private static final Pattern EMPTY_PARAGRAPH_PATTERN = Pattern.compile("(?is)\\s*<p[^>]*>\\s*&nbsp;\\s*</p>\\s*");

    private String area;
    private Object value;
    private String beginMarker;
    private int beginOffset;
    private String endMarker;
    private int endOffset;
    private transient Map<String, String> areas;

    public void setArea(String area) {
        this.area = area;
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

    // --- TagSupport support ---

    @Override
    public int doStartTag() throws JspException {
        LayoutTag parent = (LayoutTag) findAncestorWithClass(this, LayoutTag.class);

        if (parent != null) {
            areas = parent.getAreas();
        }

        try {
            Integer action = writeValue(areas, pageContext.getOut(), null, value);

            if (action != null) {
                return action;
            }

        } catch (IOException error) {
            throw new JspException(error);

        } catch (ServletException error) {
            throw new JspException(error);
        }

        setBodyContent(null);
        return SKIP_BODY;
    }

    // Finds the offset to the marker with the given internalName and offset
    // in the given items.
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

    // Writes the given value to the response.
    private Integer writeValue(
            Map<String, String> areas,
            Writer writer,
            Object area,
            Object value)
            throws IOException, ServletException {

        if (ObjectUtils.isBlank(value)) {
            return EVAL_BODY_BUFFERED;
        }

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

            if (items.isEmpty()) {
                return EVAL_BODY_BUFFERED;
            }

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
                writeValue(areas, writer, entry.getKey(), entry.getValue());
            }

        } else if (value instanceof Iterable) {
            int index = 0;
            for (Object item : (Iterable<?>) value) {
                writeValue(areas, writer, index, item);
                ++ index;
            }

        } else if (value instanceof String) {
            writer.write(StringUtils.escapeHtml((String) value));

        } else if (value instanceof Page.Area) {
            Page.Area pageArea = (Page.Area) value;

            if (areas == null) {
                writeValue(areas, writer, null, pageArea.getContents());

            } else {
                StringWriter stringWriter = new StringWriter();
                writeValue(null, stringWriter, null, pageArea.getContents());
                areas.put(pageArea.getInternalName(), stringWriter.toString());
            }

        } else {
            if (areas == null) {
                PageFilter.renderObject(request, response, writer, value);

            } else {
                if (value instanceof Section) {
                    area = ((Section) value).getInternalName();
                }

                if (!ObjectUtils.isBlank(area)) {
                    StringWriter stringWriter = new StringWriter();
                    PageFilter.renderObject(request, response, stringWriter, value);
                    areas.put(area.toString(), stringWriter.toString());
                }
            }
        }

        return null;
    }

    @Override
    public int doEndTag() throws JspException {
        if (bodyContent != null) {
            String body = bodyContent.getString();

            if (body != null) {
                if (areas != null && !ObjectUtils.isBlank(area)) {
                    areas.put(area, body);

                } else {
                    try {
                        pageContext.getOut().print(body);

                    } catch (IOException ex) {
                        throw new JspException(ex);
                    }
                }
            }
        }

        return EVAL_PAGE;
    }
}
