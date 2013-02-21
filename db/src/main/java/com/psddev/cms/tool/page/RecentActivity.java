package com.psddev.cms.tool.page;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import javax.servlet.ServletException;

import org.joda.time.DateTime;

import com.psddev.cms.db.Content;
import com.psddev.cms.db.Directory;
import com.psddev.cms.db.Template;
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
        DateTime date = new DateTime(page.param(Date.class, "date"));
        UUID userId = page.pageParam(UUID.class, "userId", null);
        long offset = page.param(long.class, "offset");
        int limit = page.pageParam(Integer.class, "limit", 20);

        Query<?> contentQuery = (itemType != null ? Query.fromType(itemType) : Query.from(Content.class)).
                where(page.siteItemsPredicate()).
                and(Content.UPDATE_DATE_FIELD + " != missing").
                and(Content.UPDATE_DATE_FIELD + " <= ?", date).
                sortDescending(Content.UPDATE_DATE_FIELD);

        if (userId != null) {
            contentQuery.and(Content.UPDATE_USER_FIELD + " = ?", userId);
        }

        PaginatedResult<?> contents = contentQuery.select(offset, limit);
        PageWriter writer = page.getWriter();

        writer.start("div", "class", "widget widget-recentActivity");
            writer.start("h1").html("Recent Activity").end();

            writer.start("form",
                    "class", "recentActivity-filters autoSubmit",
                    "method", "get",
                    "action", page.url(null));

                writer.start("span", "class", "recentActivity-filters-itemType");
                    page.typeSelect(
                            Template.Static.findUsedTypes(page.getSite()),
                            itemType,
                            "Everything",
                            "class", "autoSubmit",
                            "name", "itemType",
                            "data-searchable", "true");
                writer.end();

                writer.start("span", "class", "recentActivity-filters-user");
                    writer.start("span", "class", "recentActivity-filters-by");
                        writer.html("by");
                    writer.end();

                    writer.start("select", "name", "userId");
                        writer.start("option",
                                "selected", userId == null ? "selected" : null,
                                "value", "");
                            writer.html("Everyone");
                        writer.end();
                        writer.start("option",
                                "selected", userId != null ? "selected" : null,
                                "value", page.getUser().getId());
                            writer.html("Me");
                        writer.end();
                    writer.end();
                writer.end();

            writer.end();

            writer.start("ul", "class", "pagination");

                if (contents.hasPrevious()) {
                    writer.start("li", "class", "first");
                        writer.start("a",
                                "href", page.url("", "offset", contents.getFirstOffset()));
                            writer.html("Newest");
                        writer.end();
                    writer.end();

                    writer.start("li", "class", "previous");
                        writer.start("a",
                                "href", page.url("", "offset", contents.getPreviousOffset()));
                            writer.html("Newer ").html(limit);
                        writer.end();
                    writer.end();
                }

                writer.start("li");
                    writer.start("form",
                            "class", "autoSubmit",
                            "method", "get",
                            "action", page.url(null));
                        writer.start("select", "name", "limit");
                            for (int l : LIMITS) {
                                writer.start("option",
                                        "value", l,
                                        "selected", limit == l ? "selected" : null);
                                    writer.html("Show ");
                                    writer.html(l);
                                writer.end();
                            }
                        writer.end();
                    writer.end();
                writer.end();

                if (contents.hasNext()) {
                    writer.start("li", "class", "next");
                        writer.start("a",
                                "href", page.url("", "offset", contents.getNextOffset()));
                            writer.html("Older ").html(limit);
                        writer.end();
                    writer.end();
                }

            writer.end();

            writer.start("table", "class", "links table-striped pageThumbnails").start("tbody");

                String lastDateString = null;

                for (Object content : contents.getItems()) {
                    State contentState = State.getInstance(content);
                    String permalink = contentState.as(Directory.ObjectModification.class).getPermalink();
                    Content.ObjectModification contentData = contentState.as(Content.ObjectModification.class);
                    DateTime updateDate = new DateTime(contentData.getUpdateDate());
                    ToolUser updateUser = contentData.getUpdateUser();
                    String dateString = updateDate.toString("E, MMM d, yyyy");
                    String statusId = contentData.getStatusId();

                    writer.start("tr", "data-preview-url", permalink);
                        writer.start("td", "class", "date");
                            if (!dateString.equals(lastDateString)) {
                                writer.html(dateString);
                                lastDateString = dateString;
                            }
                        writer.end();

                        writer.start("td", "class", "time");
                            writer.html(updateDate.toString("hh:mm a"));
                        writer.end();

                        writer.start("td");
                            writer.typeLabel(content);
                        writer.end();

                        writer.start("td", "data-preview-anchor", "");
                            if (statusId != null) {
                                writer.start("span", "class", "contentStatusLabel contentStatusLabel-" + statusId);
                                    writer.html(contentData.getStatus());
                                writer.end();
                            }

                            writer.html(" ");

                            writer.start("a",
                                    "href", page.objectUrl("/content/edit.jsp", content),
                                    "target", "_top");
                                writer.objectLabel(content);
                            writer.end();
                        writer.end();

                        writer.start("td");
                            writer.objectLabel(updateUser);
                        writer.end();
                    writer.end();
                }

            writer.end().end();

        writer.end();
    }
}
