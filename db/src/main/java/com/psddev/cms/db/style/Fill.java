package com.psddev.cms.db.style;

import java.io.IOException;

import com.psddev.cms.db.ToolUi;
import com.psddev.dari.util.HtmlWriter;

public class Fill extends Decoration {

    @ToolUi.ColorPicker
    private String color;

    private Corners radius;
    private Sides offset;

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
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
        writer.writeCss(selector, "background-color", getColor());
        writeRadius(writer, selector, getRadius());
        writeOffset(writer, selector, getOffset());
    }
}
