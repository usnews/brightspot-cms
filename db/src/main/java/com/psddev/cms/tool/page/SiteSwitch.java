package com.psddev.cms.tool.page;

import com.psddev.cms.db.Site;
import com.psddev.cms.db.ToolUser;
import com.psddev.cms.tool.PageServlet;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.db.Query;
import com.psddev.dari.util.JspUtils;
import com.psddev.dari.util.RoutingFilter;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.UUID;

@RoutingFilter.Path(application = "cms", value = "/siteSwitch")
public class SiteSwitch extends PageServlet {

    @Override
    protected String getPermissionId() {
        return null;
    }

    @Override
    protected void doService(ToolPageContext page) throws IOException, ServletException {
        if (page.param(boolean.class, "switch")) {
            ToolUser user = page.getUser();

            user.setCurrentSite(Query.from(Site.class).where("_id = ?", page.param(UUID.class, "id")).first());
            user.save();
            JspUtils.redirect(page.getRequest(), page.getResponse(), page.cmsUrl("/"));
            return;
        }

        page.writeHeader();
            page.writeStart("div", "class", "widget");
                page.writeStart("h1");
                    page.writeHtml("Switch Site");
                page.writeEnd();

                page.writeStart("div", "class", "siteSwitch-content fixedScrollable");
                    page.writeStart("ul", "class", "links");
                        page.writeStart("li");
                            page.writeStart("a",
                                    "href", page.cmsUrl("/siteSwitch", "switch", true),
                                    "target", "_top");
                                page.writeHtml("Global");
                            page.writeEnd();
                        page.writeEnd();

                        for (Site site : Site.Static.findAll()) {
                            page.writeStart("li");
                                page.writeStart("a",
                                        "href", page.cmsUrl("/siteSwitch", "switch", true, "id", site.getId()),
                                        "target", "_top");
                                    page.writeObjectLabel(site);
                                page.writeEnd();
                            page.writeEnd();
                        }
                    page.writeEnd();
                page.writeEnd();
            page.writeEnd();
        page.writeFooter();
    }
}
