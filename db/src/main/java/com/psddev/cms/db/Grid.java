package com.psddev.cms.db;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.psddev.dari.db.State;
import com.psddev.dari.util.HtmlGrid;
import com.psddev.dari.util.HtmlObject;
import com.psddev.dari.util.HtmlWriter;
import com.psddev.dari.util.ObjectUtils;

@Grid.Embedded
public class Grid extends Content implements Renderer {

    @Required
    private ContentStream contents;

    @Required
    private List<GridLayout> layouts;

    private List<GridStyle> styles;

    public ContentStream getContents() {
        return contents;
    }

    public void setContents(ContentStream contents) {
        this.contents = contents;
    }

    public List<GridLayout> getLayouts() {
        if (layouts == null) {
            layouts = new ArrayList<GridLayout>();
        }
        return layouts;
    }

    public void setLayouts(List<GridLayout> layouts) {
        this.layouts = layouts;
    }

    public List<GridStyle> getStyles() {
        if (styles == null) {
            styles = new ArrayList<GridStyle>();
        }
        return styles;
    }

    public void setStyles(List<GridStyle> styles) {
        this.styles = styles;
    }

    @Override
    protected void beforeSave() {
        for (GridLayout l : getLayouts()) {
            new HtmlGrid(l.getTemplate());
        }
    }

    @Override
    public void renderObject(
            HttpServletRequest request,
            HttpServletResponse response,
            HtmlWriter writer)
            throws IOException {

        String cssClass = "_gl-" + getId();
        int maxSize = 0;
        HtmlGrid maxGrid = null;

        writer.writeStart("style", "type", "text/css");
            writer.writeCommonGridCss();

            for (GridLayout l : getLayouts()) {
                HtmlGrid grid = new HtmlGrid(l.getTemplate());
                int size = grid.getAreas().size();
                String prefix = l.getPrefix();

                if (maxSize < size) {
                    maxSize = size;
                    maxGrid = grid;
                }

                writer.writeGridCss(
                        (ObjectUtils.isBlank(prefix) ? "." : prefix + " .") + cssClass,
                        grid);
            }
        writer.writeEnd();

        List<HtmlObject> contentRenderers = new ArrayList<HtmlObject>();

        for (Object content : getContents().findContents(0, maxSize)) {
            contentRenderers.add(new ContentRenderer(request, response, content));
        }

        writer.writeStart("div", "class", cssClass);
            writer.writeGrid(contentRenderers, maxGrid);
        writer.writeEnd();
    }

    private class ContentRenderer implements HtmlObject {

        private final HttpServletRequest request;
        private final HttpServletResponse response;
        private final Object content;

        public ContentRenderer(HttpServletRequest request, HttpServletResponse response, Object content) {
            this.request = request;
            this.response = response;
            this.content = content;
        }

        @Override
        public void format(HtmlWriter writer) throws IOException {
            try {
                ContentStyle style = null;

                for (GridStyle s : getStyles()) {
                    if (State.getInstance(content).getType().equals(s.getType())) {
                        style = s.getStyle();
                        break;
                    }
                }

                if (style != null) {
                    writer.writeStart("style", "type", "text/css");
                        writer.writeCss("._da, ._dj",
                                "-moz-transition", "all 0.4s ease",
                                "-ms-transition", "all 0.4s ease",
                                "-o-transition", "all 0.4s ease",
                                "-webkit-transition", "all 0.4s ease",
                                "transition", "all 0.4s ease");

                        style.writeCss(writer);
                    writer.writeEnd();

                    style.writeHtml(writer, content);

                } else {
                    PageFilter.renderObject(request, response, writer, content);
                }

            } catch (ServletException error) {
                throw new IOException(error);
            }
        }
    }
}
