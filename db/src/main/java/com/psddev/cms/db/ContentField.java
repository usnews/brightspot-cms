package com.psddev.cms.db;

import com.psddev.dari.db.Record;
import com.psddev.dari.util.ObjectUtils;

@ContentField.Embedded
public class ContentField extends Record {

    public static final String DEFAULT_TAB_VALUE = "Main";

    @ToolUi.Placeholder(value = DEFAULT_TAB_VALUE, editable = true)
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
    public void beforeSave() {
        if (DEFAULT_TAB_VALUE.equals(tab)) {
            tab = null;
        }
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
