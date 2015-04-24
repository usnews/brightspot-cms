package com.psddev.cms.tool.page;

import java.io.IOException;

import javax.servlet.ServletException;

import com.psddev.cms.tool.PageServlet;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.util.ClassFinder;
import com.psddev.dari.util.RoutingFilter;
import com.psddev.dari.util.TypeDefinition;

@RoutingFilter.Path(application = "cms", value = "profilePanel")
@SuppressWarnings("serial")
public class ProfilePanel extends PageServlet {

    @Override
    protected String getPermissionId() {
        return null;
    }

    @Override
    protected void doService(ToolPageContext page) throws IOException, ServletException {

        page.writeHeader();
            page.writeStart("div", "class", "widget p-tud");
                page.writeStart("h1");
                    page.writeHtml(page.getUser().getName());
                page.writeEnd();

                page.writeStart("div", "class", "tabbed tabbed-vertical");

                    for (Class<? extends ProfilePanelTab> c : ClassFinder.Static.findClasses(ProfilePanelTab.class)) {
                        TypeDefinition.getInstance(c).newInstance().writeHtml(page);
                    }

                page.writeEnd();
            page.writeEnd();
        page.writeFooter();
    }
}
