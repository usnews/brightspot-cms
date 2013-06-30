package com.psddev.cms.tool.page;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.ServletException;

import com.psddev.cms.tool.PageServlet;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.util.ErrorUtils;
import com.psddev.dari.util.RoutingFilter;

@RoutingFilter.Path(application = "cms", value = "toolUserContentLock")
@SuppressWarnings("serial")
public class ToolUserContentLock extends PageServlet {

    @Override
    protected String getPermissionId() {
        return null;
    }

    @Override
    protected void doService(ToolPageContext page) throws IOException, ServletException {
        if (!page.isFormPost()) {
            throw new IllegalStateException("Form must be posted!");
        }

        UUID id = page.param(UUID.class, "id");

        if (id == null) {
            ErrorUtils.errorIfNull(id, "id");

        } else {
            page.getResponse().setContentType("text/plain");
            page.writeRaw(page.getUser().lockContent(id).getId());
        }
    }
}
