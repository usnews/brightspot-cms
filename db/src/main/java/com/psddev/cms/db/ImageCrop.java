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
    private String text;
    private double textSize;
    private double textX;
    private double textY;
    private double textWidth;

    public ImageCrop() {
    }

    public ImageCrop(Map<String, Object> map) {
        x = ObjectUtils.to(double.class, map.get("x"));
        y = ObjectUtils.to(double.class, map.get("y"));
        width = ObjectUtils.to(double.class, map.get("width"));
        height = ObjectUtils.to(double.class, map.get("height"));
        text = ObjectUtils.to(String.class, map.get("text"));
        textSize = ObjectUtils.to(double.class, map.get("textSize"));
        textX = ObjectUtils.to(double.class, map.get("textX"));
        textY = ObjectUtils.to(double.class, map.get("textY"));
        textWidth = ObjectUtils.to(double.class, map.get("textWidth"));
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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public double getTextSize() {
        return textSize;
    }

    public void setTextSize(double textSize) {
        this.textSize = textSize;
    }

    public double getTextX() {
        return textX;
    }

    public void setTextX(double textX) {
        this.textX = textX;
    }

    public double getTextY() {
        return textY;
    }

    public void setTextY(double textY) {
        this.textY = textY;
    }

    public double getTextWidth() {
        return textWidth;
    }

    public void setTextWidth(double textWidth) {
        this.textWidth = textWidth;
    }
}
