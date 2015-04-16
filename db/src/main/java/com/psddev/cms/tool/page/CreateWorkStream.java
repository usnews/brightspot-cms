package com.psddev.cms.tool.page;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;

import com.psddev.cms.db.WorkStream;
import com.psddev.cms.tool.PageServlet;
import com.psddev.cms.tool.Search;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.db.Query;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.RoutingFilter;

@RoutingFilter.Path(application = "cms", value = "createWorkStream")
public class CreateWorkStream extends PageServlet {

    public static void reallyDoService(ToolPageContext page) throws IOException, ServletException {

        if (page.requireUser()) {
            return;
        }

        WorkStream object = (WorkStream) page.findOrReserve(WorkStream.class);

        if (page.isFormPost() &&
                page.param(String.class, "action-save") != null) {
            doPost(page, object);
        }

        page.writeStart("div", "class", "widget");
            page.writeFormHeading(object);

            page.include("/WEB-INF/errors.jsp");

            page.writeStart("form",
                    "method", "post",
                    "action", page.objectUrl("", object));
                page.writeElement("input",
                        "type", "hidden",
                        "name", "incompleteIfMatching",
                        page.param(boolean.class, "incompleteIfMatching"));

                page.writeFormFields(object);

                page.writeStart("div", "class", "buttons");
                    page.writeStart("button",
                            "class", "action icon icon-action-save",
                            "name", "action-save",
                            "value", "true");
                    page.writeHtml("Save");
                    page.writeEnd();
                page.writeEnd();
            page.writeEnd();
        page.writeEnd();
    }

    private static void doPost(ToolPageContext page, WorkStream object) {
        try {
            page.updateUsingParameters(object);

            String searchString = page.param(String.class, "search");

            if (!ObjectUtils.isBlank(searchString)) {
                Search search = new Search();
                search.getState().setValues((Map<String, Object>) ObjectUtils.fromJson(searchString));
                object.setSearch(search);

            } else {
                String queryString = page.param(String.class, "query");
                Query<?> query = Query.fromAll();
                query.getState().setValues((Map<String, Object>) ObjectUtils.fromJson(queryString));
                object.setQuery(query);
            }

            object.setIncompleteIfMatching(page.param(boolean.class, "incompleteIfMatching"));

            page.publish(object);

            page.writeStart("script", "type", "text/javascript");
            page.write("window.location = window.location;");
            page.writeEnd();

        } catch (Exception error) {
            page.getErrors().add(error);
        }
    }

    @Override
    protected void doService(ToolPageContext page) throws IOException, ServletException {
        reallyDoService(page);
    }

    @Override
    protected String getPermissionId() {
        return null;
    }
}
