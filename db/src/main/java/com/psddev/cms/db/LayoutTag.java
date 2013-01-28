package com.psddev.cms.db;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import com.psddev.dari.util.Css;
import com.psddev.dari.util.HtmlObject;
import com.psddev.dari.util.HtmlWriter;
import com.psddev.dari.util.IoUtils;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.StringUtils;

@SuppressWarnings("serial")
public class LayoutTag extends BodyTagSupport {

    private String cssClass;
    private transient Grid grid;
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
            grid = createGridFromCssClass(pageContext.getServletContext(), cssClass);

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

                writer.grid(items, grid.widths, grid.heights, grid.template);
            }

            writer.end();

        } catch (IOException error) {
            throw new JspException(error);
        }

        return EVAL_PAGE;
    }

    private static class Grid {

        public final String widths;
        public final String heights;
        public final String template;

        public Grid(String widths, String heights, String template) {
            this.widths = widths;
            this.heights = heights;
            this.template = template;
        }
    }

    private static Grid createGridFromCssClass(ServletContext context, String cssClass) throws IOException {
        return ObjectUtils.isBlank(cssClass) ? null : findGrid(context, "." + cssClass, "/");
    }

    private static Grid findGrid(ServletContext context, String selector, String path) throws IOException {
        @SuppressWarnings("unchecked")
        Set<String> children = (Set<String>) context.getResourcePaths(path);

        if (children != null) {
            for (String child : children) {
                if (child.endsWith(".css")) {
                    InputStream cssInput = context.getResourceAsStream(child);

                    try {
                        Css css = new Css(IoUtils.toString(cssInput, StringUtils.UTF_8));

                        if ("grid".equals(css.getValue(selector, "display"))) {
                            String templateValue = css.getValue(selector, "grid-template");

                            if (templateValue != null) {
                                char[] letters = templateValue.toCharArray();
                                StringBuilder word = new StringBuilder();
                                List<String> list = new ArrayList<String>();

                                for (int i = 0, length = letters.length; i < length; ++ i) {
                                    char letter = letters[i];

                                    if (letter == '"') {
                                        for (++ i; i < length; ++ i) {
                                            letter = letters[i];

                                            if (letter == '"') {
                                                list.add(word.toString());
                                                word.setLength(0);
                                                break;

                                            } else {
                                                word.append(letter);
                                            }
                                        }

                                    } else if (Character.isWhitespace(letter)) {
                                        if (word.length() > 0) {
                                            list.add(word.toString());
                                            word.setLength(0);
                                        }

                                    } else {
                                        word.append(letter);
                                    }
                                }

                                StringBuilder t = new StringBuilder();

                                for (String v : list) {
                                    t.append(v);
                                    t.append("\n");
                                }

                                return new Grid(
                                        css.getValue(selector, "grid-definition-columns"),
                                        css.getValue(selector, "grid-definition-rows"),
                                        t.toString());
                            }
                        }

                    } finally {
                        cssInput.close();
                    }

                } else if (child.endsWith("/")) {
                    Grid grid = findGrid(context, selector, child);

                    if (grid != null) {
                        return grid;
                    }
                }
            }
        }

        return null;
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
