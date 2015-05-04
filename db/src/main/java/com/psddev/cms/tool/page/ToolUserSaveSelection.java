package com.psddev.cms.tool.page;

import com.psddev.cms.db.ToolUser;
import com.psddev.cms.tool.PageServlet;
import com.psddev.cms.tool.SearchResultSelection;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.db.Query;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.RoutingFilter;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.UUID;

@RoutingFilter.Path(application = "cms", value = "toolUserSaveSelection")
public class ToolUserSaveSelection extends PageServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected String getPermissionId() {
        return null;
    }

    @Override
    protected void doService(ToolPageContext page) throws IOException, ServletException {
        if (page.isFormPost()) {
            ToolUser user = page.getUser();
            String name = page.param(String.class, "name");

            if (ObjectUtils.isBlank(name)) {
                throw new IllegalArgumentException("[Name] is required!");
            }

            String selectionId = page.param(String.class, "selectionId");

            if (selectionId == null) {
                throw new IllegalArgumentException("[selectionId] is required!");
            }

            SearchResultSelection selection = ObjectUtils.to(SearchResultSelection.class, Query.fromAll().where("_id = ?", selectionId).first());

            if (selection == null) {
                throw new IllegalArgumentException("Could not find a SearchResultSelection for selectionId " + selectionId);
            }

            user.getSavedSelections().put(name, selectionId);
            user.setCurrentSearchResultSelection(null);
            user.save();

            page.writeStart("div", "id", page.createId());
            page.writeEnd();
            page.writeStart("script", "type", "text/javascript");
                page.writeRaw("$('#").writeRaw(page.getId()).writeRaw("').popup('close');");
            page.writeEnd();
            return;
        }

        page.writeHeader();
            page.writeStart("div", "class", "widget");
                page.writeStart("h1", "class", "icon icon-action-search");
                    page.writeHtml("Save Selection");
                page.writeEnd();

            page.writeStart("form",
                "method", "post",
                "action", page.url(""));
                page.writeStart("div", "class", "inputContainer");
                    page.writeStart("div", "class", "inputLabel");
                        page.writeStart("label");
                            page.writeHtml("Name");
                        page.writeEnd();
                    page.writeEnd();

                    page.writeStart("div", "class", "inputSmall");
                        page.writeElement("input",
                            "type", "text",
                            "name", "name",
                            "placeholder", "(Required)");
                        page.writeEnd();
                    page.writeEnd();

                    page.writeStart("div", "class", "actions");
                        page.writeStart("button",
                            "class", "action icon icon-action-save");
                            page.writeHtml("Save");
                        page.writeEnd();
                    page.writeEnd();
                page.writeEnd();
            page.writeEnd();
        page.writeFooter();
    }
}
