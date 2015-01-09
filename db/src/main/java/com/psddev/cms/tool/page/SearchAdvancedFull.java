package com.psddev.cms.tool.page;

import java.io.IOException;

import javax.servlet.ServletException;

import com.psddev.cms.tool.CmsTool;
import com.psddev.cms.tool.PageServlet;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.util.JspUtils;
import com.psddev.dari.util.RoutingFilter;

@RoutingFilter.Path(application = "cms", value = "searchAdvancedFull")
public class SearchAdvancedFull extends PageServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected String getPermissionId() {
        return null;
    }

    @Override
    protected void doService(ToolPageContext page) throws IOException, ServletException {
        page.writeHeader();
            page.writeStart("div", "class", "widget");
                JspUtils.include(
                        page.getRequest(),
                        page.getResponse(),
                        page,
                        page.toolPath(CmsTool.class, "/WEB-INF/search.jsp"),
                        "resultJsp", "/searchAdvancedFullResult");
            page.writeEnd();
        page.writeFooter();
    }
}
