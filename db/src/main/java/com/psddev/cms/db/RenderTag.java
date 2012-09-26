package com.psddev.cms.db;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTagSupport;

import com.psddev.dari.db.Reference;
import com.psddev.dari.db.ReferentialText;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.StringUtils;

/**
 * Renders the given {@code value}, which may be a {@linkplain String
 * string}, {@linkplain ReferentialText referential text}, or a Dari
 * object.
 */
@SuppressWarnings("serial")
public class RenderTag extends BodyTagSupport {

    private static final Pattern EMPTY_PARAGRAPH_PATTERN = Pattern.compile("(?is)\\s*<p[^>]*>\\s*&nbsp;\\s*</p>\\s*");

    private Object value;
    private String beginMarker;
    private int beginOffset = 0;
    private String endMarker;
    private int endOffset = 0;

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

    /**
     * Finds the offset to the {@linkplain ReferentialTextMarker marker}
     * with the given {@code internalName} and {@code offset} in the given
     * {@code items}.
     */
    private int findMarker(List<Object> items, String internalName, int offset) {
        int itemIndex = 0;
        int markerIndex = 0;
        for (Object item : items) {
            if (item instanceof Reference) {
                Object referenced = ((Reference) item).getObject();
                if (referenced instanceof ReferentialTextMarker) {
                    if (internalName.equals(((ReferentialTextMarker) referenced).getInternalName())) {
                        if (offset == markerIndex) {
                            return itemIndex;
                        } else {
                            ++ markerIndex;
                        }
                    }
                }
            }
            ++ itemIndex;
        }
        return -1;
    }

    @Override
    public int doStartTag() throws JspException {
        if (value != null) {
            HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
            HttpServletResponse response = (HttpServletResponse) pageContext.getResponse();
            JspWriter writer = pageContext.getOut();

            try {
                if (value instanceof ReferentialText) {
                    List<Object> items = new ArrayList<Object>();
                    items.addAll((ReferentialText) value);
                    // handle text marker
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
                                    for (Map.Entry<String, Object> e : itemReference.entrySet()) {
                                        String key = e.getKey();
                                        if (key != null && !key.startsWith("_")) {
                                            oldAttributes.put(key, request.getAttribute(key));
                                            request.setAttribute(key, e.getValue());
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

                } else if (value instanceof String) {
                    writer.write(StringUtils.escapeHtml((String) value));

                } else {
                    PageFilter.renderObject(request, response, writer, value);
                }

            } catch (IOException ex) {
                throw new JspException(ex);

            } catch (ServletException ex) {
                throw new JspException(ex);
            }
        }

        setBodyContent(null);
        return SKIP_BODY;
    }

    @Override
    public int doEndTag() throws JspException {
        if (bodyContent != null) {
            String body = bodyContent.getString();
            if (!ObjectUtils.isBlank(body)) {
                try {
                    pageContext.getOut().print(body);
                } catch (IOException ex) {
                    throw new JspException(ex);
                }
            }
        }
        return EVAL_PAGE;
    }
}
