package com.psddev.cms.tool.page;

import java.io.IOException;

import javax.servlet.ServletException;

import com.psddev.cms.db.WorkStream;
import com.psddev.cms.tool.PageServlet;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.RoutingFilter;

@RoutingFilter.Path(application = "cms", value = "/content/editWorkStream")
public class EditWorkStream extends PageServlet {

    @Override
    protected void doService(ToolPageContext page) throws IOException, ServletException {

        if (page.requireUser()) {
            return;
        }

        WorkStream object = (WorkStream) page.findOrReserve();

        if (page.isFormPost()) {
            doPost(page, object);
        }

        page.writeFormHeading(object, "class", "icon icon-object-workStream");

        if (object.getSearch() != null) {
            page.writeStart("p");
                page.writeStart("a",
                        "class", "icon icon-action-search",
                        "href", page.cmsUrl("/misc/savedSearch.jsp", "search", ObjectUtils.toJson(object.getSearch().getState().getSimpleValues())),
                        "target", "miscSavedSearch");
                    page.writeHtml("View Items");
                page.writeEnd();
            page.writeEnd();
        }

        page.include("/WEB-INF/errors.jsp");

        page.writeStart("form",
                "method", "post",
                "enctype", "multipart/form-data",
                "action", page.objectUrl("", object));
            page.writeFormFields(object);

            page.writeStart("div", "class", "buttons");
                page.writeStart("button",
                        "class", "action action-save",
                        "name", "action-save",
                        "value", true);
                    page.writeHtml("Save");
                page.writeEnd();

                page.writeStart("button",
                        "class", "action action-delete action-pullRight link",
                        "name", "action-delete",
                        "value", true);
                    page.writeHtml("Delete");
                page.writeEnd();
            page.writeEnd();
        page.writeEnd();
    }

    private static void doPost(ToolPageContext page, Object object) {
        try {
            if (page.param(String.class, "action-save") != null) {
                page.updateUsingParameters(object);
                page.publish(object);

            } else if (page.param(String.class, "action-delete") != null) {
                page.trash(object);
            }

            page.writeStart("script", "type", "text/javascript");
                page.write("top.window.location = top.window.location;");
            page.writeEnd();

        } catch (Exception ex) {
            page.getErrors().add(ex);
        }
    }

    @Override
    protected String getPermissionId() {
        return null;
    }
}
