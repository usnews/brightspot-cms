package com.psddev.cms.db;

import com.psddev.dari.db.Record;
import com.psddev.dari.util.ObjectUtils;

@ContentField.Embedded
public class ContentField extends Record {

    @ToolUi.Placeholder("Main")
    private String tab;

    @Required
    private String displayName;

    @Required
    private String internalName;

    public String getTab() {
        return tab;
    }

    public void setTab(String tab) {
        this.tab = tab;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getInternalName() {
        return internalName;
    }

    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    @Override
    public String getLabel() {
        StringBuilder label = new StringBuilder();
        String tab = getTab();

        label.append(ObjectUtils.isBlank(tab) ? "Main" : tab);
        label.append(" \u2192 ");
        label.append(getDisplayName());
        return label.toString();
    }
}
