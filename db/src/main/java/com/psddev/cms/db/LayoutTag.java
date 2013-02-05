package com.psddev.cms.db;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.DynamicAttributes;

import com.psddev.dari.util.HtmlGrid;
import com.psddev.dari.util.HtmlObject;
import com.psddev.dari.util.HtmlWriter;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.TypeReference;

@SuppressWarnings("serial")
public class LayoutTag extends BodyTagSupport implements DynamicAttributes {

    private final Map<String, Object> attributes = new LinkedHashMap<String, Object>();
    private transient HtmlWriter writer;
    private transient Map<String, HtmlGrid> grids;
    private transient Map<String, Object> areas;

    public Map<String, Object> getAreas() {
        return areas;
    }

    // --- DynamicAttributes support ---

    @Override
    public void setDynamicAttribute(String uri, String localName, Object value) {
        if (value != null) {
            attributes.put(localName, value);
        }
    }

    // --- TagSupport support ---

    @Override
    public int doStartTag() throws JspException {
        try {
            writer = new HtmlWriter(pageContext.getOut());
            grids = new LinkedHashMap<String, HtmlGrid>();
            List<String> cssClasses = ObjectUtils.to(new TypeReference<List<String>>() { }, attributes.remove("class"));

            if (cssClasses != null) {
                for (Object cssClassObject : cssClasses) {
                    if (cssClassObject != null) {
                        String cssClass = cssClassObject.toString();
                        HtmlGrid grid = HtmlGrid.Static.find(pageContext.getServletContext(), cssClass);

                        if (grid != null) {
                            grids.put(cssClass, grid);
                        }
                    }
                }
            }

            if (grids.isEmpty()) {
                areas = null;
                writer.start("div",
                        attributes,
                        "class", cssClasses != null && !cssClasses.isEmpty() ?
                                cssClasses.get(0) :
                                null);

            } else {
                areas = new LinkedHashMap<String, Object>();
            }

        } catch (IOException error) {
            throw new JspException(error);
        }

        return areas != null ? EVAL_BODY_BUFFERED : EVAL_BODY_INCLUDE;
    }

    @Override
    public int doEndTag() throws JspException {
        try {
            if (grids.isEmpty()) {
                writer.end();

            } else {
                List<Object> areasList = new ArrayList<Object>(areas.values());
                int areaSize = areasList.size();
                int gridOffset = 0;

                for (Map.Entry<String, HtmlGrid> gridEntry : grids.entrySet()) {
                    String cssClass = gridEntry.getKey();
                    HtmlGrid grid = gridEntry.getValue();
                    Map<String, GridItem> items = new LinkedHashMap<String, GridItem>();
                    int gridAreaSize = grid.getAreas().size();

                    for (Map.Entry<String, Object> areaEntry : areas.entrySet()) {
                        items.put(areaEntry.getKey(), new GridItem(areaEntry.getValue()));
                    }

                    for (int i = 0; i < gridAreaSize && gridOffset + i < areaSize; ++ i) {
                        items.put(String.valueOf(i), new GridItem(areasList.get(gridOffset + i)));
                    }

                    gridOffset += gridAreaSize;

                    writer.start("div", attributes, "class", cssClass);
                        writer.grid(items, grid);
                    writer.end();
                }
            }

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
