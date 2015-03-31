package com.psddev.cms.tool.page;

import java.io.IOException;

import javax.servlet.ServletException;

import com.psddev.cms.tool.PageServlet;
import com.psddev.cms.tool.ToolPageContext;

public abstract class ToolUserDashboardWidget extends PageServlet {

    public abstract String getTabName();
    @Override
    protected abstract void doService(ToolPageContext page) throws IOException, ServletException;
}
