package com.psddev.cms.db.style;

import java.io.IOException;

import com.psddev.cms.db.ToolUi;
import com.psddev.dari.util.HtmlWriter;

public class Stroke extends Decoration {

    @ToolUi.ColorPicker
    private String color;

    private String style;
    private Sides width;
    private Corners radius;
    private Sides offset;

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public Sides getWidth() {
        return width;
    }

    public void setWidth(Sides width) {
        this.width = width;
    }

    public Corners getRadius() {
        return radius;
    }

    public void setRadius(Corners radius) {
        this.radius = radius;
    }

    public Sides getOffset() {
        return offset;
    }

    public void setOffset(Sides offset) {
        this.offset = offset;
    }

    @Override
    public void writeCss(HtmlWriter writer, String selector) throws IOException {
        writer.writeCss(selector,
                "border-color", getColor(),
                "border-style", getStyle());

        writeSides(writer, selector, getWidth(), "border-width");
        writeRadius(writer, selector, getRadius());
        writeOffset(writer, selector, getOffset());
    }
}
