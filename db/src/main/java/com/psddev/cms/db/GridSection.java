package com.psddev.cms.db;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.psddev.dari.db.State;
import com.psddev.dari.util.CollectionUtils;
import com.psddev.dari.util.HtmlFormatter;
import com.psddev.dari.util.HtmlWriter;

public class GridSection extends Section {

    @ToolUi.Code("css-grid")
    private String widths;

    @ToolUi.Code("css-grid")
    private String heights;

    @ToolUi.Code("css-grid")
    private String template;

    @Embedded
    private List<Section> children;

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

    /** Returns the children. */
    public List<Section> getChildren() {
        if (children == null) {
            children = new ArrayList<Section>();
        }
        return children;
    }

    /** Sets the children. */
    public void setChildren(List<Section> children) {
        this.children = children;
    }

    @Override
    public Map<String, Object> toDefinition() {
        Map<String, Object> definition = super.toDefinition();
        List<Map<String, Object>> childMaps = new ArrayList<Map<String, Object>>();

        definition.put("children", childMaps);

        for (Section child : getChildren()) {
            if (child != null) {
                childMaps.add(child.toDefinition());
            }
        }

        return definition;
    }

    @Override
    public void writeLayoutPreview(HtmlWriter writer) throws IOException {
        @SuppressWarnings("all")
        HtmlWriter previewWriter = new HtmlWriter(writer);

        previewWriter.putOverride(HtmlWriter.Area.class, new HtmlFormatter<HtmlWriter.Area>() {
            @Override
            public void format(HtmlWriter writer, HtmlWriter.Area area) throws IOException {
                Object section = CollectionUtils.getByPath(getChildren(), area.getName());

                writer.start("div", "class", "name");
                    writer.html(section != null ?
                            State.getInstance(section).getLabel() :
                            area.getName());
                writer.end();
            }
        });
        
        previewWriter.grid(null, widths, heights, template);
    }
}
