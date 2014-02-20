package com.psddev.cms.db;

import com.psddev.dari.db.Record;
import com.psddev.dari.util.ObjectUtils;

@GridLayout.Embedded
public class GridLayout extends Record {

    private String prefix;

    @Required
    @ToolUi.CodeType("text/plain")
    private String template;

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    @Override
    public String getLabel() {
        return ObjectUtils.isBlank(getPrefix()) ? "Default" : super.getLabel();
    }
}

