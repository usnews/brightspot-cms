package com.psddev.cms.tool;

import com.psddev.dari.db.Record;
import com.psddev.dari.db.State;

import java.util.Map;

/** Represents various parts of the CMS that can be modified. */
public class Plugin extends Record {

    @Required
    private Tool tool;

    @Indexed
    @Required
    private String displayName;

    @Required
    private String internalName;

    /** Returns the tool. */
    public Tool getTool() {
        if (tool == null) {
            Object trash = getState().getValue("dari.trash.tool");
            if (trash instanceof Record) {
                State trashState = ((Record) trash).getState();
                tool = new Tool();
                State toolState = tool.getState();
                toolState.setId(trashState.getId());
                toolState.setTypeId(trashState.getTypeId());
                toolState.setValues(trashState.getValues());
            }
        }
        return tool;
    }

    /** Sets the tool. */
    public void setTool(Tool tool) {
        this.tool = tool;
        getState().setDatabase(tool.getState().getDatabase());
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
}
