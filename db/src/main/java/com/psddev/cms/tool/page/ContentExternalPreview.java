package com.psddev.cms.tool.page;

import java.io.IOException;

import javax.servlet.ServletException;

import com.psddev.cms.tool.PageServlet;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.util.RoutingFilter;

@RoutingFilter.Path(application = "cms", value = "/content/externalPreview")
@SuppressWarnings("serial")
public class ContentExternalPreview extends PageServlet {

    @Override
    protected String getPermissionId() {
        return null;
    }

    @Override
    protected void doService(final ToolPageContext page) throws IOException, ServletException {
        page.writeHeader();
            page.writeStart("div", "class", "widget");
                page.writeStart("h1",
                        "class", "icon icon-action-preview");
                    page.writeHtml("External Content Preview");
                page.writeEnd();

                page.writeStart("iframe",
                        "src", page.cmsUrl("/content/externalPreviewFrame", "url", page.param(String.class, "url")),
                        "style", page.cssString(
                                "border-style", "none",
                                "width", "100%"));
                page.writeEnd();
            page.writeEnd();
        page.writeFooter();
    }
}
