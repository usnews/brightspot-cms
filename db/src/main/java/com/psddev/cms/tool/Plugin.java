package com.psddev.cms.tool;

import com.psddev.cms.db.Content;
import com.psddev.dari.db.Record;

/** Part of the tool UI that can be modified. */
public class Plugin extends Record {

    private Tool tool;
    private String displayName;
    private String internalName;
    private Content content;

    /** Returns the tool. */
    public Tool getTool() {
        return tool;
    }

    /** Sets the tool. */
    public void setTool(Tool tool) {
        this.tool = tool;
    }

    /** Returns the display name. */
    public String getDisplayName() {
        return displayName;
    }

    /** Sets the display name. */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /** Returns the unique internal name. */
    public String getInternalName() {
        return internalName;
    }

    /** Sets the unique internal name. */
    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    /** Returns the content */
    public Content getContent() {
        return content;
    }

    /** Sets the content */
    public void setContent(Content content) {
        this.content = content;
    }
}
