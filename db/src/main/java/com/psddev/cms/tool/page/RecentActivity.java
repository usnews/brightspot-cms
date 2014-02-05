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
        Type type = page.pageParam(Type.class, "type", Type.ANYONE);
        String valueParameter = type + ".value";
        Object valueObject = Query.from(Object.class).where("_id = ?", page.pageParam(UUID.class, valueParameter, null)).first();
        long offset = page.param(long.class, "offset");
        int limit = page.pageParam(Integer.class, "limit", 20);

        if (type == null) {
            type = Type.ANYONE;
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

        page.writeStart("div", "class", "widget");
            page.writeStart("h1", "class", "icon icon-list");
                page.writeHtml("Recent Activity");
            page.writeEnd();

            page.writeStart("form",
                    "method", "get",
                    "action", page.url(null));
                page.writeStart("ul", "class", "oneLine");
                    page.writeStart("li");
                        page.writeTypeSelect(
                                Template.Static.findUsedTypes(page.getSite()),
                                itemType,
                                "Any Types",
                                "class", "autoSubmit",
                                "name", "itemType",
                                "data-searchable", "true");
                    page.writeEnd();

                    page.writeStart("li");
                        page.writeHtml("by ");
                        page.writeStart("select", "class", "autoSubmit", "name", "type");
                            for (Type t : Type.values()) {
                                if (t != Type.ROLE || Query.from(ToolRole.class).first() != null) {
                                    page.writeStart("option",
                                            "selected", t.equals(type) ? "selected" : null,
                                            "value", t.name());
                                        page.writeHtml(t.getDisplayName());
                                    page.writeEnd();
                                }
                            }
                        page.writeEnd();
                    page.writeEnd();

                    page.writeStart("li");
                        Query<?> valueQuery;

                        if (type == Type.ROLE) {
                            valueQuery = Query.from(ToolRole.class).sortAscending("name");

                        } else if (type == Type.USER) {
                            valueQuery = Query.from(ToolUser.class).sortAscending("name");

                        } else {
                            valueQuery = null;
                        }

                        if (valueQuery == null) {
                            page.writeHtml("\u0020");

                        } else {
                            if (valueQuery.hasMoreThan(250)) {
                                State valueState = State.getInstance(valueObject);

                                page.writeElement("input",
                                        "type", "text",
                                        "class", "autoSubmit objectId",
                                        "data-editable", false,
                                        "data-label", valueState != null ? valueState.getLabel() : null,
                                        "data-typeIds", ObjectType.getInstance(ToolRole.class).getId(),
                                        "name", valueParameter,
                                        "value", valueState != null ? valueState.getId() : null);

                            } else {
                                page.writeStart("select",
                                        "class", "autoSubmit",
                                        "name", valueParameter,
                                        "data-searchable", "true");

                                    page.writeStart("option", "value", "").writeEnd();

                                    for (Object v : valueQuery.selectAll()) {
                                        State state = State.getInstance(v);

                                        page.writeStart("option",
                                                "value", state.getId(),
                                                "selected", v.equals(valueObject) ? "selected" : null);
                                            page.writeHtml(state.getLabel());
                                        page.writeEnd();
                                    }

                                page.writeEnd();
                            }
                        }
                    page.writeEnd();
                page.writeEnd();
            page.writeEnd();

            if (result == null) {
                page.writeStart("div", "class", "message message-warning");
                    page.writeStart("p");
                        page.writeHtml("Please select a ");
                        page.writeHtml(type.getDisplayName());
                        page.writeHtml(".");
                    page.writeEnd();
                page.writeEnd();

            } else if (!result.hasPages()) {
                page.writeStart("div", "class", "message message-info");
                    page.writeStart("p");
                        page.writeHtml("No recent activity!");
                    page.writeEnd();
                page.writeEnd();

            } else {
                page.writeStart("ul", "class", "pagination");

                    if (result.hasPrevious()) {
                        page.writeStart("li", "class", "first");
                            page.writeStart("a",
                                    "href", page.url("", "offset", result.getFirstOffset()));
                                page.writeHtml("Newest");
                            page.writeEnd();
                        page.writeEnd();

                        page.writeStart("li", "class", "previous");
                            page.writeStart("a",
                                    "href", page.url("", "offset", result.getPreviousOffset()));
                                page.writeHtml("Newer ").writeHtml(limit);
                            page.writeEnd();
                        page.writeEnd();
                    }

                    if (result.getOffset() > 0 ||
                            result.hasNext() ||
                            result.getItems().size() > LIMITS[0]) {
                        page.writeStart("li");
                            page.writeStart("form",
                                    "class", "autoSubmit",
                                    "method", "get",
                                    "action", page.url(null));
                                page.writeStart("select", "name", "limit");
                                    for (int l : LIMITS) {
                                        page.writeStart("option",
                                                "value", l,
                                                "selected", limit == l ? "selected" : null);
                                            page.writeHtml("Show ");
                                            page.writeHtml(l);
                                        page.writeEnd();
                                    }
                                page.writeEnd();
                            page.writeEnd();
                        page.writeEnd();
                    }

                    if (result.hasNext()) {
                        page.writeStart("li", "class", "next");
                            page.writeStart("a",
                                    "href", page.url("", "offset", result.getNextOffset()));
                                page.writeHtml("Older ").writeHtml(limit);
                            page.writeEnd();
                        page.writeEnd();
                    }

                page.writeEnd();

                page.writeStart("table", "class", "links table-striped pageThumbnails").writeStart("tbody");

                    String lastUpdateDate = null;

                    for (Object content : result.getItems()) {
                        State contentState = State.getInstance(content);
                        String permalink = contentState.as(Directory.ObjectModification.class).getPermalink();
                        Content.ObjectModification contentData = contentState.as(Content.ObjectModification.class);
                        DateTime updateDateTime = page.toUserDateTime(contentData.getUpdateDate());
                        String updateDate = page.formatUserDate(updateDateTime);
                        ToolUser updateUser = contentData.getUpdateUser();

                        page.writeStart("tr", "data-preview-url", permalink);
                            page.writeStart("td", "class", "date");
                                if (!updateDate.equals(lastUpdateDate)) {
                                    page.writeHtml(updateDate);
                                    lastUpdateDate = updateDate;
                                }
                            page.writeEnd();

                            page.writeStart("td", "class", "time");
                                page.writeHtml(page.formatUserTime(updateDateTime));
                            page.writeEnd();

                            page.writeStart("td");
                                page.writeTypeLabel(content);
                            page.writeEnd();

                            page.writeStart("td", "data-preview-anchor", "");
                                page.writeStart("a",
                                        "href", page.objectUrl("/content/edit.jsp", content),
                                        "target", "_top");
                                    page.writeObjectLabel(content);
                                page.writeEnd();
                            page.writeEnd();

                            page.writeStart("td");
                                page.writeObjectLabel(updateUser);
                            page.writeEnd();
                        page.writeEnd();
                    }

                page.writeEnd().writeEnd();
            }

        page.writeEnd();
    }

    private enum Type {

        ANYONE("Anyone"),
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
