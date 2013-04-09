package com.psddev.cms.tool.page;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.ServletException;

import org.joda.time.DateTime;

import com.psddev.cms.db.Content;
import com.psddev.cms.db.Directory;
import com.psddev.cms.db.Template;
import com.psddev.cms.db.ToolRole;
import com.psddev.cms.db.ToolUser;
import com.psddev.cms.tool.PageServlet;
import com.psddev.cms.tool.PageWriter;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.db.ObjectType;
import com.psddev.dari.db.Query;
import com.psddev.dari.db.State;
import com.psddev.dari.util.PaginatedResult;
import com.psddev.dari.util.RoutingFilter;

@RoutingFilter.Path(application = "cms", value = "/misc/recentActivity.jsp")
@SuppressWarnings("serial")
public class RecentActivity extends PageServlet {

    private static final int[] LIMITS = { 10, 20, 50 };

    @Override
    protected String getPermissionId() {
        return "area/dashboard";
    }

    @Override
    protected void doService(ToolPageContext page) throws IOException, ServletException {
        ObjectType itemType = Query.findById(ObjectType.class, page.pageParam(UUID.class, "itemType", null));
        Type type = page.pageParam(Type.class, "type", Type.EVERYONE);
        String valueParameter = type + ".value";
        Object valueObject = Query.from(Object.class).where("_id = ?", page.pageParam(UUID.class, valueParameter, null)).first();
        long offset = page.param(long.class, "offset");
        int limit = page.pageParam(Integer.class, "limit", 20);

        if (type == null) {
            type = Type.EVERYONE;
        }

        PaginatedResult<?> result;

        if (valueObject == null && (type == Type.ROLE || type == Type.USER)) {
            result = null;

        } else {
            Query<?> contentQuery = (itemType != null ? Query.fromType(itemType) : Query.fromGroup(Content.SEARCHABLE_GROUP)).
                    where(page.siteItemsSearchPredicate()).
                    and(Content.UPDATE_DATE_FIELD + " != missing").
                    sortDescending(Content.UPDATE_DATE_FIELD);

            switch (type) {
                case ROLE :
                    contentQuery.and(Content.UPDATE_USER_FIELD + " = ?", Query.from(ToolUser.class).where("role = ?", valueObject));
                    break;

                case USER :
                    contentQuery.and(Content.UPDATE_USER_FIELD + " = ?", valueObject);
                    break;

                case ME :
                    contentQuery.and(Content.UPDATE_USER_FIELD + " = ?", page.getUser());
                    break;

                default :
                    break;
            }

            result = contentQuery.select(offset, limit);
        }

        PageWriter writer = page.getWriter();

        writer.writeStart("div", "class", "widget widget-recentActivity");
            writer.writeStart("h1").writeHtml("Recent Activity").writeEnd();

            writer.writeStart("form",
                    "class", "recentActivity-filters",
                    "method", "get",
                    "action", page.url(null));

                writer.writeStart("span", "class", "recentActivity-filters-itemType");
                    page.writeTypeSelect(
                            Template.Static.findUsedTypes(page.getSite()),
                            itemType,
                            "Everything",
                            "class", "autoSubmit",
                            "name", "itemType",
                            "data-searchable", "true");
                writer.writeEnd();

                writer.writeStart("span", "class", "recentActivity-filters-prep");
                    writer.writeHtml("by");
                writer.writeEnd();

                writer.writeStart("span", "class", "recentActivity-filters-type");
                    writer.writeStart("select", "class", "autoSubmit", "name", "type");
                        for (Type t : Type.values()) {
                            if (t != Type.ROLE || Query.from(ToolRole.class).first() != null) {
                                writer.writeStart("option",
                                        "selected", t.equals(type) ? "selected" : null,
                                        "value", t.name());
                                    writer.writeHtml(t.getDisplayName());
                                writer.writeEnd();
                            }
                        }
                    writer.writeEnd();
                writer.writeEnd();

                writer.writeStart("span", "class", "recentActivity-filters-value");
                    Query<?> valueQuery;

                    if (type == Type.ROLE) {
                        valueQuery = Query.from(ToolRole.class).sortAscending("name");

                    } else if (type == Type.USER) {
                        valueQuery = Query.from(ToolUser.class).sortAscending("name");

                    } else {
                        valueQuery = null;
                    }

                    if (valueQuery == null) {
                        writer.writeHtml("\u0020");

                    } else {
                        if (valueQuery.hasMoreThan(250)) {
                            State valueState = State.getInstance(valueObject);

                            writer.writeTag("input",
                                    "type", "text",
                                    "class", "autoSubmit objectId",
                                    "data-editable", false,
                                    "data-label", valueState != null ? valueState.getLabel() : null,
                                    "data-typeIds", ObjectType.getInstance(ToolRole.class).getId(),
                                    "name", valueParameter,
                                    "value", valueState != null ? valueState.getId() : null);

                        } else {
                            writer.writeStart("select",
                                    "class", "autoSubmit",
                                    "name", valueParameter,
                                    "data-searchable", "true");

                                writer.writeStart("option", "value", "").writeEnd();

                                for (Object v : valueQuery.selectAll()) {
                                    State state = State.getInstance(v);

                                    writer.writeStart("option",
                                            "value", state.getId(),
                                            "selected", v.equals(valueObject) ? "selected" : null);
                                        writer.writeHtml(state.getLabel());
                                    writer.writeEnd();
                                }

                            writer.writeEnd();
                        }
                    }
                writer.writeEnd();

            writer.writeEnd();

            if (result == null) {
                writer.writeStart("div", "class", "recentActivity-warning recentActivity-warning-value");
                    writer.writeStart("p");
                        writer.writeHtml("Please select a ");
                        writer.writeHtml(type.getDisplayName());
                        writer.writeHtml(".");
                    writer.writeEnd();
                writer.writeEnd();

            } else if (!result.hasItems()) {
                writer.writeStart("div", "class", "recentActivity-warning");
                    writer.writeStart("p");
                        writer.writeHtml("No recent activity!");
                    writer.writeEnd();
                writer.writeEnd();

            } else {
                writer.writeStart("ul", "class", "pagination");

                    if (result.hasPrevious()) {
                        writer.writeStart("li", "class", "first");
                            writer.writeStart("a",
                                    "href", page.url("", "offset", result.getFirstOffset()));
                                writer.writeHtml("Newest");
                            writer.writeEnd();
                        writer.writeEnd();

                        writer.writeStart("li", "class", "previous");
                            writer.writeStart("a",
                                    "href", page.url("", "offset", result.getPreviousOffset()));
                                writer.writeHtml("Newer ").writeHtml(limit);
                            writer.writeEnd();
                        writer.writeEnd();
                    }

                    if (result.getOffset() > 0 ||
                            result.hasNext() ||
                            result.getItems().size() > LIMITS[0]) {
                        writer.writeStart("li");
                            writer.writeStart("form",
                                    "class", "autoSubmit",
                                    "method", "get",
                                    "action", page.url(null));
                                writer.writeStart("select", "name", "limit");
                                    for (int l : LIMITS) {
                                        writer.writeStart("option",
                                                "value", l,
                                                "selected", limit == l ? "selected" : null);
                                            writer.writeHtml("Show ");
                                            writer.writeHtml(l);
                                        writer.writeEnd();
                                    }
                                writer.writeEnd();
                            writer.writeEnd();
                        writer.writeEnd();
                    }

                    if (result.hasNext()) {
                        writer.writeStart("li", "class", "next");
                            writer.writeStart("a",
                                    "href", page.url("", "offset", result.getNextOffset()));
                                writer.writeHtml("Older ").writeHtml(limit);
                            writer.writeEnd();
                        writer.writeEnd();
                    }

                writer.writeEnd();

                writer.writeStart("table", "class", "links table-striped pageThumbnails").writeStart("tbody");

                    String lastDateString = null;

                    for (Object content : result.getItems()) {
                        State contentState = State.getInstance(content);
                        String permalink = contentState.as(Directory.ObjectModification.class).getPermalink();
                        Content.ObjectModification contentData = contentState.as(Content.ObjectModification.class);
                        DateTime updateDate = new DateTime(contentData.getUpdateDate());
                        ToolUser updateUser = contentData.getUpdateUser();
                        String dateString = updateDate.toString("E, MMM d, yyyy");
                        String statusId = contentData.getStatusId();

                        writer.writeStart("tr", "data-preview-url", permalink);
                            writer.writeStart("td", "class", "date");
                                if (!dateString.equals(lastDateString)) {
                                    writer.writeHtml(dateString);
                                    lastDateString = dateString;
                                }
                            writer.writeEnd();

                            writer.writeStart("td", "class", "time");
                                writer.writeHtml(updateDate.toString("hh:mm a"));
                            writer.writeEnd();

                            writer.writeStart("td");
                                writer.typeLabel(content);
                            writer.writeEnd();

                            writer.writeStart("td", "data-preview-anchor", "");
                                if (statusId != null) {
                                    writer.writeStart("span", "class", "contentStatusLabel contentStatusLabel-" + statusId);
                                        writer.writeHtml(contentData.getStatus());
                                    writer.writeEnd();
                                }

                                writer.writeHtml(" ");

                                writer.writeStart("a",
                                        "href", page.objectUrl("/content/edit.jsp", content),
                                        "target", "_top");
                                    writer.objectLabel(content);
                                writer.writeEnd();
                            writer.writeEnd();

                            writer.writeStart("td");
                                writer.objectLabel(updateUser);
                            writer.writeEnd();
                        writer.writeEnd();
                    }

                writer.writeEnd().writeEnd();
            }

        writer.writeEnd();
    }

    private enum Type {

        EVERYONE("Everyone"),
        ME("Me"),
        ROLE("Role"),
        USER("User");

        private String displayName;

        private Type(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
