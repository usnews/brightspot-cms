package com.psddev.cms.db;

public class Text extends Content {

    @Indexed(unique = true)
    @Required
    private String name;

    private String text;

    /** Returns the name. */
    public String getName() {
        return name;
    }

    /** Sets the name. */
    public void setName(String name) {
        this.name = name;
    }

    /** Returns the text. */
    public String getText() {
        return text;
    }

    /** Sets the text. */
    public void setText(String text) {
        this.text = text;
    }
}
