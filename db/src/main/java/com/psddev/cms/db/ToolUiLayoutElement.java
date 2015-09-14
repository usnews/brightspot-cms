package com.psddev.cms.db;

import com.psddev.dari.db.Record;
import com.psddev.dari.util.CompactMap;

import java.util.Map;

@ToolUiLayoutElement.Embedded
public class ToolUiLayoutElement extends Record {

    private String name;
    private int left;
    private int top;
    private int width;
    private int height;
    private String dynamicText;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public int getTop() {
        return top;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getDynamicText() {
        return dynamicText;
    }

    public void setDynamicText(String dynamicText) {
        this.dynamicText = dynamicText;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new CompactMap<String, Object>();

        map.put("name", getName());
        map.put("left", getLeft());
        map.put("top", getTop());
        map.put("width", getWidth());
        map.put("height", getHeight());
        map.put("dynamicText", getDynamicText());

        return map;
    }
}
