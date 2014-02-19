package com.psddev.cms.db.style;

public enum TextTransform {

    CAPITALIZE("capitalize", "Capitalize"),
    UPPERCASE("uppercase", "Uppercase"),
    LOWERCASE("lowercase", "Lowercase");

    private final String cssPropertyValue;
    private final String label;

    private TextTransform(String cssPropertyValue, String label) {
        this.cssPropertyValue = cssPropertyValue;
        this.label = label;
    }

    public String getCssPropertyValue() {
        return cssPropertyValue;
    }

    @Override
    public String toString() {
        return label;
    }
}
