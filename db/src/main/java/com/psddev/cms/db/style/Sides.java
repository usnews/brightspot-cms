package com.psddev.cms.db.style;

import java.io.IOException;

import com.psddev.dari.db.Record;
import com.psddev.dari.util.HtmlWriter;

@Sides.Embedded
public class Sides extends Record {

    private int left;
    private int right;

    private int top;
    private int bottom;

    public Sides() {
    }

    public Sides(int left, int right, int top, int bottom) {
        this.left = left;
        this.right = right;
        this.top = top;
        this.bottom = bottom;
    }

    public int getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public int getRight() {
        return right;
    }

    public void setRight(int right) {
        this.right = right;
    }

    public int getTop() {
        return top;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public int getBottom() {
        return bottom;
    }

    public void setBottom(int bottom) {
        this.bottom = bottom;
    }

    public void writeCss(HtmlWriter writer, String selector, String property) throws IOException {
        writer.writeCss(selector, property,
                getTop() + "px " +
                getRight() + "px " +
                getBottom() + "px " +
                getLeft() + "px");
    }
}
