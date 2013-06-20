package com.psddev.cms.tool.page;

import java.io.IOException;

import javax.servlet.ServletException;

import com.psddev.cms.db.ToolUser;
import com.psddev.cms.db.ToolUserAction;
import com.psddev.cms.db.ToolUserDevice;
import com.psddev.cms.tool.PageServlet;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.util.RoutingFilter;

@RoutingFilter.Path(application = "cms", value = "toolUserHistory")
@SuppressWarnings("serial")
public class ToolUserHistory extends PageServlet {

    @Override
    protected String getPermissionId() {
        return null;
    }

    @Override
    protected void doService(ToolPageContext page) throws IOException, ServletException {
        ToolUser user = page.getUser();

        page.writeHeader();
            page.writeStart("div", "class", "widget");
                page.writeStart("h1", "class", "icon icon-object-history");
                    page.writeHtml("History");
                page.writeEnd();

                page.writeStart("ul");
                    for (ToolUserDevice device : user.getDevices()) {
                        page.writeStart("li");
                            page.writeHtml(device.getUserAgentDisplay());
                            page.writeHtml(" - ");

                            page.writeStart("a",
                                    "class", "icon icon-facetime-video",
                                    "target", "_blank",
                                    "href", page.cmsUrl("/lookingGlass", "id", device.getLookingGlassId()));
                                page.writeHtml("Looking Glass");
                            page.writeEnd();

                            page.writeStart("ul", "class", "links");
                                for (ToolUserAction action : device.getActions()) {
                                    Object actionContent = action.getContent();

                                    if (actionContent == null) {
                                        continue;
                                    }

                                    page.writeStart("li");
                                        page.writeStart("a",
                                                "target", "_top",
                                                "href", page.objectUrl("/content/edit.jsp", actionContent));
                                            page.writeTypeObjectLabel(actionContent);
                                        page.writeEnd();
                                    page.writeEnd();
                                }
                            page.writeEnd();
                        page.writeEnd();
                    }
                page.writeEnd();
            page.writeEnd();
        page.writeFooter();
    }
}
