package com.psddev.cms.db;

import java.util.Map;

import com.psddev.dari.db.Record;
import com.psddev.dari.util.ObjectUtils;

@Record.Embedded
public class ImageTextOverlay extends Record {

    private String text;
    private double size;
    private double x;
    private double y;
    private double width;

    public ImageTextOverlay() {
    }

    public ImageTextOverlay(Map<String, Object> map) {
        text = ObjectUtils.to(String.class, map.get("text"));
        size = ObjectUtils.to(double.class, map.get("size"));
        x = ObjectUtils.to(double.class, map.get("x"));
        y = ObjectUtils.to(double.class, map.get("y"));
        width = ObjectUtils.to(double.class, map.get("width"));
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }
}
