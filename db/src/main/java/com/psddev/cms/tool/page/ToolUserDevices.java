package com.psddev.cms.tool.page;

import java.io.IOException;

import javax.servlet.ServletException;

import com.psddev.cms.db.ToolUser;
import com.psddev.cms.db.ToolUserAction;
import com.psddev.cms.db.ToolUserDevice;
import com.psddev.cms.tool.PageServlet;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.util.RoutingFilter;

@RoutingFilter.Path(application = "cms", value = "toolUserDevices")
@SuppressWarnings("serial")
public class ToolUserDevices extends PageServlet {

    @Override
    protected String getPermissionId() {
        return null;
    }

    @Override
    protected void doService(ToolPageContext page) throws IOException, ServletException {
        ToolUser user = page.getUser();

        page.writeHeader();
            page.writeStart("div", "class", "widget");
                page.writeStart("h1", "class", "icon icon-object-toolUserDevice");
                    page.writeHtml("Devices");
                page.writeEnd();

                page.writeStart("ul");
                    for (ToolUserDevice device : user.getDevices()) {
                        page.writeStart("li");
                            page.writeHtml(device.getUserAgentDisplay());
                            page.writeStart("ul");
                                for (ToolUserAction action : device.getActions()) {
                                    page.writeStart("li");
                                        action.writeDisplayHtml(page);
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
