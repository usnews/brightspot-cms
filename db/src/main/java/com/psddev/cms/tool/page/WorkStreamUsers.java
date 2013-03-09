package com.psddev.cms.tool.page;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;

import com.psddev.cms.db.ToolUser;
import com.psddev.cms.db.WorkStream;
import com.psddev.cms.tool.PageServlet;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.db.Query;
import com.psddev.dari.util.RoutingFilter;

@RoutingFilter.Path(application = "cms", value = "/workStreamUsers")
@SuppressWarnings("serial")
public class WorkStreamUsers extends PageServlet {

    @Override
    protected String getPermissionId() {
        return "area/dashboard";
    }

    @Override
    protected void doService(final ToolPageContext page) throws IOException, ServletException {
        page.getWriter();

        WorkStream workStream = Query.from(WorkStream.class).where("_id = ?", page.param(UUID.class, "id")).first();
        List<ToolUser> users = workStream.getUsers();

        Collections.sort(users);

        page.writeStart("div", "class", "widget");
            page.writeStart("h1", "class", "icon icon-object-workStream");
                page.writeHtml("Users Working On ");
                page.writeHtml(page.getObjectLabel(workStream));
            page.writeEnd();

            page.writeStart("table", "class", "table-striped");
                page.writeStart("thead");
                    page.writeStart("tr");
                        page.writeStart("th").writeHtml("User").writeEnd();
                        page.writeStart("th").writeHtml("Currently On").writeEnd();
                        page.writeStart("th").writeHtml("Completed").writeEnd();
                    page.writeEnd();
                page.writeEnd();

                page.writeStart("tbody");
                    for (ToolUser user : users) {
                        Object currentItem = workStream.getCurrentItem(user);

                        page.writeStart("tr");
                            page.writeStart("td");
                                page.writeHtml(page.getObjectLabel(user));
                            page.writeEnd();

                            page.writeStart("td");
                                page.writeStart("a",
                                        "href", page.objectUrl("/content/edit.jsp", currentItem),
                                        "target", "_top");
                                    page.writeHtml(page.getObjectLabel(currentItem));
                                page.writeEnd();
                            page.writeEnd();

                            page.writeStart("td");
                                page.writeHtml(workStream.countComplete(user));
                            page.writeEnd();
                        page.writeEnd();
                    }
                page.writeEnd();
            page.writeEnd();
        page.writeEnd();
    }
}
