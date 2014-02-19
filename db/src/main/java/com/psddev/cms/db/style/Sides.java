package com.psddev.cms.db.style;

import java.io.IOException;

import com.psddev.dari.db.Record;
import com.psddev.dari.util.HtmlWriter;

@Sides.Embedded
public class Sides extends Record {

    private int top;
    private int right;
    private int bottom;
    private int left;

    public Sides() {
    }

    public Sides(int top, int right, int bottom, int left) {
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.left = left;
    }

    public int getTop() {
        return top;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public int getRight() {
        return right;
    }

    public void setRight(int right) {
        this.right = right;
    }

    public int getBottom() {
        return bottom;
    }

    public void setBottom(int bottom) {
        this.bottom = bottom;
    }

    public int getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public void writeCss(HtmlWriter writer, String selector, String property) throws IOException {
        writer.writeCss(selector, property,
                getTop() + "px " +
                getRight() + "px " +
                getBottom() + "px " +
                getLeft() + "px");
    }
}
