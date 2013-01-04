package com.psddev.cms.tool.page;

import com.psddev.cms.tool.PageServlet;
import com.psddev.cms.tool.ToolPageContext;

import com.psddev.dari.db.State;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.RoutingFilter;

import java.io.IOException;

import javax.servlet.ServletException;

@RoutingFilter.Path(application = "cms", value = "/misc/updateUserSettings")
@SuppressWarnings("serial")
public class UpdateUserSettings extends PageServlet {

    @Override
    protected String getPermissionId() {
        return "area/dashboard";
    }

    @Override
    protected void doService(ToolPageContext page) throws IOException, ServletException {
        if (!page.isFormPost()) {
            throw new IllegalStateException("Form must be posted!");
        }

        State userState = page.getUser().getState();

        for (String action : page.params(String.class, "action")) {
            if ("liveContentPreview-enable".equals(action)) {
                userState.put("liveContentPreview", true);

            } else if ("liveContentPreview-disable".equals(action)) {
                userState.put("liveContentPreview", false);

            } else if ("dashboardWidgets-position".equals(action)) {
                userState.put("dashboardWidgets", ObjectUtils.fromJson(page.param(String.class, "widgets")));
            }
        }

        userState.save();
    }
}
