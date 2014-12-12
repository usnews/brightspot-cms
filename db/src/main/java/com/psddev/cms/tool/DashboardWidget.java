package com.psddev.cms.tool;

import java.io.IOException;

import javax.servlet.ServletException;

import com.psddev.dari.db.Record;
import com.psddev.dari.util.StringUtils;

public abstract class DashboardWidget extends Record {

    public abstract void writeHtml(
            ToolPageContext page,
            Dashboard dashboard,
            DashboardColumn column)
            throws IOException, ServletException;

    @Override
    public String getLabel() {
        return StringUtils.toLabel(getClass().getSimpleName());
    }
}
