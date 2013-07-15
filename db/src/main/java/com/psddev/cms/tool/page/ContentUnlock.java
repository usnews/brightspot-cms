package com.psddev.cms.tool.page;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.ServletException;

import com.psddev.cms.db.ToolUser;
import com.psddev.cms.tool.PageServlet;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.util.JspUtils;
import com.psddev.dari.util.RoutingFilter;

@RoutingFilter.Path(application = "cms", value = "contentUnlock")
public class ContentUnlock extends PageServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected String getPermissionId() {
        return null;
    }

    @Override
    protected void doService(ToolPageContext page) throws IOException, ServletException {
        ToolUser user = page.getUser();
        UUID contentId = page.param(UUID.class, "id");
        RuntimeException lastError = null;

        for (int i = 0; i < 5; ++ i) {
            try {
                user.unlockContent(contentId);
                user.lockContent(contentId);

            } catch (RuntimeException error) {
                lastError = error;
            }
        }

        if (lastError != null) {
            throw lastError;

        } else {
            JspUtils.redirect(page.getRequest(), page.getResponse(), page.param(String.class, "returnUrl"));
        }
    }
}
