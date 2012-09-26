package com.psddev.cms.db;

import com.psddev.dari.db.Record;
import com.psddev.dari.util.ObjectUtils;

import java.util.Map;

@Record.Embedded
public class ImageCrop extends Record {

    private double x;
    private double y;
    private double width;
    private double height;

    public ImageCrop() {
    }

    public ImageCrop(Map<String, Object> map) {
        x = ObjectUtils.to(double.class, map.get("x"));
        y = ObjectUtils.to(double.class, map.get("y"));
        width = ObjectUtils.to(double.class, map.get("width"));
        height = ObjectUtils.to(double.class, map.get("height"));
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

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }
}
