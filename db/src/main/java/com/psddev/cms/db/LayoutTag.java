package com.psddev.cms.db;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.DynamicAttributes;

import com.psddev.dari.util.HtmlGrid;
import com.psddev.dari.util.HtmlObject;
import com.psddev.dari.util.HtmlWriter;

@SuppressWarnings("serial")
public class LayoutTag extends BodyTagSupport implements DynamicAttributes {

    private final Map<String, String> attributes = new LinkedHashMap<String, String>();
    private transient HtmlGrid grid;
    private transient Map<String, Object> areas;
    private transient HtmlWriter writer;

    public Map<String, Object> getAreas() {
        return areas;
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
    public int doStartTag() throws JspException {
        try {
            String cssClass = attributes.get("class");
            grid = HtmlGrid.Static.find(pageContext.getServletContext(), cssClass);
            areas = grid != null ? new LinkedHashMap<String, Object>() : null;
            writer = new HtmlWriter(pageContext.getOut());

            writer.start("div", attributes, "class", cssClass);

        } catch (IOException error) {
            throw new JspException(error);
        }

        return areas != null ? EVAL_BODY_BUFFERED : EVAL_BODY_INCLUDE;
    }

    @Override
    public int doEndTag() throws JspException {
        try {
            if (areas != null) {
                Map<String, GridItem> items = new LinkedHashMap<String, GridItem>();

                for (Map.Entry<String, Object> entry : areas.entrySet()) {
                    items.put(entry.getKey(), new GridItem(entry.getValue()));
                }

                writer.grid(items, grid);
            }

            writer.end();

        } catch (IOException error) {
            throw new JspException(error);
        }

        return EVAL_PAGE;
    }

    private class GridItem implements HtmlObject {

        private final Object item;

        public GridItem(Object item) {
            this.item = item;
        }

        @Override
        public void format(HtmlWriter writer) throws IOException {
            if (item != null) {
                writer.write(item.toString());
            }
        }
    }
}
