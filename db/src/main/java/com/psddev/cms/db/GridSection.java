package com.psddev.cms.db;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.psddev.dari.db.Record;
import com.psddev.dari.util.HtmlFormatter;
import com.psddev.dari.util.HtmlWriter;

public class GridSection extends Section {

    @ToolUi.Code("css-grid")
    private String widths;

    @ToolUi.Code("css-grid")
    @ToolUi.Note("You can leave this blank and put the heights in the template using the slash syntax (e.g. A B C /auto).")
    private String heights;

    @ToolUi.Code("css-grid")
    private String template;

    private List<Area> areas;

    public String getWidths() {
        return widths;
    }

    public void setWidths(String widths) {
        this.widths = widths;
    }

    public String getHeights() {
        return heights;
    }

    public void setHeights(String heights) {
        this.heights = heights;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public List<Area> getAreas() {
        if (areas == null) {
            areas = new ArrayList<Area>();
        }
        return areas;
    }

    public void setAreas(List<Area> areas) {
        this.areas = areas;
    }

    @Override
    public Map<String, Object> toDefinition() {
        return super.toDefinition();
    }

    @Override
    public void writeLayoutPreview(HtmlWriter writer) throws IOException {
        @SuppressWarnings("all")
        HtmlWriter previewWriter = new HtmlWriter(writer);

        previewWriter.putOverride(HtmlWriter.Area.class, new HtmlFormatter<HtmlWriter.Area>() {
            @Override
            public void format(HtmlWriter writer, HtmlWriter.Area area) throws IOException {
                Area gridArea = null;

                for (Area ga : getAreas()) {
                    if (area.getName().equals(ga.getName())) {
                        gridArea = ga;
                        break;
                    }
                }

                writer.start("div", "class", "heading");

                    writer.start("div", "class", "name");
                        writer.html(area.getName());
                    writer.end();

                    if (gridArea != null) {
                        writer.start("ol");
                            for (Content content : gridArea.getContents()) {
                                writer.start("li");
                                    writer.start("a",
                                            "href", "/cms/content/object.jsp?id=" + content.getId(),
                                            "target", "gridContent");
                                        writer.html(content.getLabel());
                                    writer.end();
                                writer.end();
                            }
                        writer.end();
                    }

                writer.end();
            }
        });
        
        previewWriter.grid(null, widths, heights, template);
    }

    @Embedded
    public static class Area extends Record {

        private String name;
        private List<Content> contents;

        public String getName() {
            return name;
        }

        public List<Content> getContents() {
            if (contents == null) {
                contents = new ArrayList<Content>();
            }
            return contents;
        }
    }
}
