package com.psddev.cms.tool.page;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletException;

import com.psddev.cms.tool.PageServlet;
import com.psddev.cms.tool.Search;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.db.ObjectField;
import com.psddev.dari.db.ObjectType;
import com.psddev.dari.db.Query;
import com.psddev.dari.db.State;
import com.psddev.dari.util.JspUtils;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.RoutingFilter;

@RoutingFilter.Path(application = "cms", value = "contentEditBulk")
public class ContentEditBulk extends PageServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected String getPermissionId() {
        return null;
    }

    @Override
    protected void doService(ToolPageContext page) throws IOException, ServletException {
        List<UUID> ids = page.params(UUID.class, ContentSearchAdvanced.ITEMS_PARAMETER);
        Query<?> query = ids.isEmpty() ? new Search(page).toQuery(page.getSite()) : Query.fromAll().where("_id = ?", ids);
        long count = query.count();
        ObjectType type = ObjectType.getInstance(page.param(UUID.class, "typeId"));
        State state = State.getInstance(type.createObject(page.param(UUID.class, "id")));

        state.clear();

        page.writeHeader();
            if (page.isFormPost() &&
                    page.param(String.class, "action-save") != null) {
                try {
                    JspUtils.include(
                            page.getRequest(),
                            page.getResponse(),
                            page,
                            page.cmsUrl("/WEB-INF/objectPost.jsp"),
                            "object", state.getOriginalObject());

                    Map<String, Object> values = state.getSimpleValues();
                    Map<String, Object> newValues = new LinkedHashMap<String, Object>();

                    for (ObjectField field : type.getFields()) {
                        String name = field.getInternalName();
                        Object value = values.get(name);

                        if (!ObjectUtils.isBlank(value)) {
                            newValues.put(name, value);
                        }
                    }

                    for (Object item : query.selectAll()) {
                        State itemState = State.getInstance(item);

                        itemState.putAll(newValues);
                        itemState.save();
                    }

                    state.clear();

                    page.writeStart("div", "class", "message message-success");
                        page.writeHtml("Successfully saved ");
                        page.writeHtml(count);
                        page.writeHtml(" items. ");

                        String returnUrl = page.param(String.class, "returnUrl");

                        if (!ObjectUtils.isBlank(returnUrl)) {
                            page.writeStart("a",
                                    "href", returnUrl);
                                page.writeHtml("Go back to search.");
                            page.writeEnd();
                        }
                    page.writeEnd();

                } catch (Exception error) {
                    page.writeObject(error);
                }
            }

            page.writeStart("div", "class", "widget");
                page.writeStart("h1");
                    page.writeHtml("Bulk Edit ");
                    page.writeHtml(count);
                    page.writeHtml(" Items");
                page.writeEnd();

                page.writeStart("div", "class", "message", "message-info");
                    page.writeHtml("Any of the fields that you fill out here will replace the corresponding fields in all items.");
                page.writeEnd();

                page.writeStart("form",
                        "method", "post",
                        "action", page.url(null, "id", state.getId()));

                    for (String paramName : page.paramNamesList()) {
                        for (String value : page.params(String.class, paramName)) {
                            page.writeElement("input",
                                    "type", "hidden",
                                    "name", paramName,
                                    "value", value);
                        }
                    }

                    JspUtils.include(
                            page.getRequest(),
                            page.getResponse(),
                            page,
                            page.cmsUrl("/WEB-INF/objectForm.jsp"),
                            "object", state.getOriginalObject());

                    page.writeStart("div", "class", "actions");
                        page.writeStart("button",
                                "class", "action icon icon-action-save",
                                "name", "action-save",
                                "value", "true");
                            page.writeHtml("Bulk Save");
                        page.writeEnd();
                    page.writeEnd();
                page.writeEnd();
            page.writeEnd();
        page.writeFooter();
    }
}
