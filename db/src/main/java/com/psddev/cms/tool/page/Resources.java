package com.psddev.cms.tool.page;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;

import com.psddev.cms.tool.CmsTool;
import com.psddev.cms.tool.PageServlet;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.RoutingFilter;

@RoutingFilter.Path(application = "cms", value = "/resources")
@SuppressWarnings("serial")
public class Resources extends PageServlet {

    @Override
    protected String getPermissionId() {
        return "area/dashboard";
    }

    @Override
    protected void doService(final ToolPageContext page) throws IOException, ServletException {
        CmsTool cms = page.getCmsTool();
        List<CmsTool.ResourceItem> resources = cms.getResources();

        if (resources.isEmpty()) {
            return;
        }

        page.writeStart("div", "class", "widget");
            page.writeStart("h1", "class", "icon icon-globe").writeHtml("Resources").writeEnd();

            page.writeStart("ul", "class", "links");
                for (CmsTool.ResourceItem item : resources) {
                    String url = item.getUrl();

                    if (!ObjectUtils.isBlank(url)) {
                        page.writeStart("li");
                            page.writeStart("a",
                                    "href", url,
                                    "target", item.isSameWindow() ? null : "_blank");
                                page.writeHtml(item.getName());
                            page.writeEnd();
                        page.writeEnd();
                    }
                }
            page.writeEnd();
        page.writeEnd();
    }
}
