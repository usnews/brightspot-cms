package com.psddev.cms.tool.page;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.ServletException;

import com.psddev.cms.tool.PageServlet;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.db.Query;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.RoutingFilter;
import com.psddev.dari.util.StringUtils;

@RoutingFilter.Path(application = "cms", value = "contentTools")
@SuppressWarnings("serial")
public class ContentTools extends PageServlet {

    @Override
    protected String getPermissionId() {
        return null;
    }

    @Override
    protected void doService(ToolPageContext page) throws IOException, ServletException {
        Object object = Query.from(Object.class).where("_id = ?", page.param(UUID.class, "id")).first();
        String returnUrl = page.param(String.class, "returnUrl");

        page.writeHeader();
            page.writeStart("div", "class", "widget");
                page.writeStart("h1", "class", "icon icon-wrench");
                    page.writeHtml("Tools");
                page.writeEnd();

                page.writeStart("ul", "class", "links");
                    if (object != null) {
                        page.writeStart("li");
                            page.writeStart("a",
                                    "target", "_blank",
                                    "href", page.objectUrl("/contentRaw", object));
                                page.writeHtml("View Raw Data");
                            page.writeEnd();
                        page.writeEnd();
                    }

                    if (!ObjectUtils.isBlank(returnUrl)) {
                        page.writeStart("li");
                            if (ObjectUtils.to(boolean.class, StringUtils.getQueryParameterValue(returnUrl, "deprecated"))) {
                                page.writeStart("a",
                                        "target", "_top",
                                        "href", StringUtils.addQueryParameters(returnUrl,
                                                "deprecated", null));
                                    page.writeHtml("Hide Deprecated Fields");
                                page.writeEnd();

                            } else {
                                page.writeStart("a",
                                        "target", "_top",
                                        "href", StringUtils.addQueryParameters(returnUrl,
                                                "deprecated", true));
                                    page.writeHtml("Show Deprecated Fields");
                                page.writeEnd();
                            }
                        page.writeEnd();
                    }
                page.writeEnd();
            page.writeEnd();
        page.writeFooter();
    }
}
