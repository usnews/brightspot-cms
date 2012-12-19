package com.psddev.cms.tool.page;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import javax.servlet.ServletException;

import org.joda.time.DateTime;

import com.psddev.cms.db.Content;
import com.psddev.cms.db.ToolUser;
import com.psddev.cms.tool.ToolPage;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.cms.tool.ToolPageWriter;
import com.psddev.dari.db.Query;
import com.psddev.dari.util.PaginatedResult;
import com.psddev.dari.util.RoutingFilter;

@RoutingFilter.Path(application = "cms", value = "/misc/recentActivity.jsp")
@SuppressWarnings("serial")
public class RecentActivity extends ToolPage {

    @Override
    protected String getPermissionId() {
        return "area/dashboard";
    }

    @Override
    protected void doService(ToolPageContext page) throws IOException, ServletException {

        DateTime date = new DateTime(page.param(Date.class, "date"));
        UUID userId = page.pageParam(UUID.class, "userId", null);
        long offset = page.param(long.class, "offset");
        int limit = page.pageParam(Integer.class, "limit", 20);

        Query<Content> contentQuery = Query.
                from(Content.class).
                where(page.siteItemsPredicate()).
                and(Content.UPDATE_DATE_FIELD + " != missing").
                and(Content.UPDATE_DATE_FIELD + " <= ?", date).
                sortDescending(Content.UPDATE_DATE_FIELD);

        if (userId != null) {
            contentQuery.and(Content.UPDATE_USER_FIELD + " = ?", userId);
        }

        PaginatedResult<Content> contents = contentQuery.select(offset, limit);
        ToolPageWriter writer = page.getWriter();

        writer.start("div", "class", "widget widget-recentActivity");

            writer.start("h1", "class", "icon-list");
                writer.html("Recent Activity");
            writer.end();

            writer.start("form",
                    "class", "autoSubmit",
                    "method", "get",
                    "action", page.url(null));

                writer.tag("input",
                        "id", page.createId(),
                        "type", "radio",
                        "name", "userId",
                        "value", page.getUser().getId(),
                        "checked", userId != null ? "checked" : null);
                writer.start("label", "for", page.getId()).html("Me").end();

                writer.html(" ");

                writer.tag("input",
                        "id", page.createId(),
                        "type", "radio",
                        "name", "userId",
                        "value", "",
                        "checked", userId == null ? "checked" : null);
                writer.start("label", "for", page.getId()).html("Everyone").end();

                writer.html(" ");

                writer.tag("input",
                        "type", "submit",
                        "value", "Go");

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

                if (contents.hasNext()) {
                    writer.start("li", "class", "next");
                        writer.start("a",
                                "href", page.url("", "offset", contents.getNextOffset()));
                            writer.html("Older ").html(limit);
                        writer.end();
                    writer.end();
                }

            writer.end();

            writer.start("table", "class", "links table-striped").start("tbody");

                String lastDateString = null;

                for (Content content : contents.getItems()) {
                    DateTime updateDate = new DateTime(content.getUpdateDate());
                    ToolUser updateUser = content.getUpdateUser();
                    String dateString = updateDate.toString("E, MMM d, yyyy");

                    writer.start("tr");
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
