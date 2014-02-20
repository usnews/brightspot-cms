package com.psddev.cms.db.style;

import java.io.IOException;

import com.psddev.dari.db.Record;
import com.psddev.dari.util.HtmlWriter;

@Corners.Embedded
public class Corners extends Record {

    private int topLeft;
    private int topRight;

    private int bottomLeft;
    private int bottomRight;

    public Corners() {
    }

    public Corners(int topLeft, int topRight, int bottomLeft, int bottomRight) {
        this.topLeft = topLeft;
        this.topRight = topRight;
        this.bottomLeft = bottomLeft;
        this.bottomRight = bottomRight;
    }

    public int getTopLeft() {
        return topLeft;
    }

    public void setTopLeft(int topLeft) {
        this.topLeft = topLeft;
    }

    public int getTopRight() {
        return topRight;
    }

    public void setTopRight(int topRight) {
        this.topRight = topRight;
    }

    public int getBottomLeft() {
        return bottomLeft;
    }

    public void setBottomLeft(int bottomLeft) {
        this.bottomLeft = bottomLeft;
    }

    public int getBottomRight() {
        return bottomRight;
    }

    public void setBottomRight(int bottomRight) {
        this.bottomRight = bottomRight;
    }

    public void writeCss(HtmlWriter writer, String selector, String property) throws IOException {
        writer.writeCss(selector, property,
                getTopLeft() + "px " +
                getTopRight() + "px " +
                getBottomRight() + "px " +
                getBottomLeft() + "px");
    }
}
