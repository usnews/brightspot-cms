package com.psddev.cms.tool.page;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletException;

import com.psddev.cms.db.Content;
import com.psddev.cms.tool.PageServlet;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.db.Database;
import com.psddev.dari.db.DatabaseEnvironment;
import com.psddev.dari.db.ObjectField;
import com.psddev.dari.db.ObjectType;
import com.psddev.dari.db.Query;
import com.psddev.dari.db.State;
import com.psddev.dari.util.ErrorUtils;
import com.psddev.dari.util.JspUtils;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.RoutingFilter;

@RoutingFilter.Path(application = "cms", value = "/contentEditBulk")
@SuppressWarnings("serial")
public class ContentEditBulk extends PageServlet {

    @Override
    protected String getPermissionId() {
        return null;
    }

    @Override
    protected void doService(final ToolPageContext page) throws IOException, ServletException {
        DatabaseEnvironment environment = Database.Static.getDefault().getEnvironment();
        ObjectType type = environment.getTypeById(page.param(UUID.class, ContentSearchAdvanced.TYPE_PARAMETER));

        ErrorUtils.errorIfNull(type, ContentSearchAdvanced.TYPE_PARAMETER);

        String predicate = page.param(String.class, ContentSearchAdvanced.PREDICATE_PARAMETER);
        Query<Object> query = (type != null ?
                Query.fromType(type) :
                Query.fromGroup(Content.SEARCHABLE_GROUP)).
                where(predicate);

        List<UUID> ids = page.params(UUID.class, ContentSearchAdvanced.ITEMS_PARAMETER);

        if (!ids.isEmpty()) {
            query.and("_id = ?", ids);
        }

        long count = query.count();
        State state = State.getInstance(type.createObject(page.param(UUID.class, "id")));

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
                    page.writeHtml(" items.");
                page.writeEnd();

            } catch (Exception error) {
                page.writeObject(error);
            }
        }

        page.writeHeader();
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
                    page.writeTag("input",
                            "type", "hidden",
                            "name", ContentSearchAdvanced.TYPE_PARAMETER,
                            "value", type.getId());

                    page.writeTag("input",
                            "type", "hidden",
                            "name", ContentSearchAdvanced.PREDICATE_PARAMETER,
                            "value", predicate);

                    for (UUID id : ids) {
                        page.writeTag("input",
                                "type", "hidden",
                                "name", ContentSearchAdvanced.ITEMS_PARAMETER,
                                "value", id);
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
