package com.psddev.cms.db;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import com.psddev.dari.util.HtmlGrid;
import com.psddev.dari.util.HtmlObject;
import com.psddev.dari.util.HtmlWriter;

@SuppressWarnings("serial")
public class LayoutTag extends BodyTagSupport {

    private String cssClass;
    private transient HtmlGrid grid;
    private transient Map<String, String> areas;
    private transient HtmlWriter writer;

    public void setCssClass(String cssClass) {
        this.cssClass = cssClass;
    }

    public Map<String, String> getAreas() {
        return areas;
    }

    // --- TagSupport support ---

    @Override
    public int doStartTag() throws JspException {
        try {
            grid = HtmlGrid.Static.find(pageContext.getServletContext(), cssClass);

            if (grid != null) {
                areas = new LinkedHashMap<String, String>();
            }

            writer = new HtmlWriter(pageContext.getOut());
            writer.start("div", "class", cssClass);

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

                for (Map.Entry<String, String> entry : areas.entrySet()) {
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

        private final String item;

        public GridItem(String item) {
            this.item = item;
        }

        @Override
        public void format(HtmlWriter writer) throws IOException {
            if (item != null) {
                writer.write(item);
            }
        }
    }
}
