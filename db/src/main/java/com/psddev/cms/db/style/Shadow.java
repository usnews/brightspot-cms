package com.psddev.cms.db.style;

import java.io.IOException;

import com.psddev.cms.db.ToolUi;
import com.psddev.dari.util.HtmlWriter;

public class Shadow extends Decoration {

    private boolean inset;
    private int offsetX;
    private int offsetY;
    private int blur;

    @ToolUi.ColorPicker
    private String color;

    private Corners radius;
    private Sides offset;

    public boolean isInset() {
        return inset;
    }

    public void setInset(boolean inset) {
        this.inset = inset;
    }

    public int getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(int offsetX) {
        this.offsetX = offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(int offsetY) {
        this.offsetY = offsetY;
    }

    public int getBlur() {
        return blur;
    }

    public void setBlur(int blur) {
        this.blur = blur;
    }

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
        writer.writeCss(selector,
                "box-shadow",
                        getOffsetX() + "px " +
                        getOffsetY() + "px " +
                        getBlur() + "px 0 " +
                        getColor());

        writeRadius(writer, selector, getRadius());
        writeOffset(writer, selector, getOffset());
    }
}
