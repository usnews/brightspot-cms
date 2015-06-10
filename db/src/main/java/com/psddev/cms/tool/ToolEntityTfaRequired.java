package com.psddev.cms.tool;

import com.psddev.dari.util.StringUtils;

public enum ToolEntityTfaRequired {
    REQUIRED, NOT_REQUIRED;

    @Override
    public String toString() {
        return StringUtils.toLabel(name());
    }
}
