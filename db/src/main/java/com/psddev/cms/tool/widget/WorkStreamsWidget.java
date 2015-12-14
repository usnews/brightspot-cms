package com.psddev.cms.tool.widget;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;

import com.google.common.collect.ImmutableMap;
import com.psddev.cms.db.ToolUser;
import com.psddev.cms.db.WorkStream;
import com.psddev.cms.tool.Dashboard;
import com.psddev.cms.tool.DefaultDashboardWidget;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.db.Query;
import com.psddev.dari.util.PaginatedResult;
import org.apache.commons.lang.StringUtils;

// TODO: Replace localization page.localize(RecentActivityWidget.class, "pagination.newest") calls with this class - add to localization data
public class WorkStreamsWidget extends DefaultDashboardWidget {
    private static final int[] LIMITS = { 10, 20, 50 };
    //private static final int[] LIMITS = {2, 4, 10};
    public static final String PARAM_ASSIGNED_USERS = "assignedTo";
    public static final String PARAM_ASSIGNED_USERS_ANYONE = "anyone";
    public static final String PARAM_ASSIGNED_USERS_ME = "me";
    public static final String PARAM_OFFSET = "offset";
    public static final String PARAM_LIMIT = "limit";
    public static final String PARAM_STOP = "stop";

    @Override
    public int getColumnIndex() {
        return 0;
    }

    @Override
    public int getWidgetIndex() {
        return 1;
    }

    @Override
    public void writeHtml(ToolPageContext page, Dashboard dashboard) throws IOException, ServletException {
        String queryAssignedTo = page.pageParam(String.class, PARAM_ASSIGNED_USERS, PARAM_ASSIGNED_USERS_ANYONE);
        long offset = page.param(long.class, PARAM_OFFSET);
        int limit = page.pageParam(Integer.class, PARAM_LIMIT, LIMITS[0]);
        UUID stop = page.param(UUID.class, PARAM_STOP);

        ToolUser user = page.getUser();

        // Actions
        if (stop != null) {
            WorkStream toStop = Query.from(WorkStream.class).where("id = ?", stop).first();
            if (toStop != null) {
                toStop.stop(user);
            }
            page.redirect(null);
            return;
        }

        Query<WorkStream> query = Query.from(WorkStream.class).where(page.siteItemsPredicate());
        if (StringUtils.equals(queryAssignedTo, PARAM_ASSIGNED_USERS_ME)) {
            query.and("assignedUsers = ?", page.getUser());
        }
        PaginatedResult<WorkStream> results = query.select(offset, limit);
        //List<WorkStream> workStreams = query.selectAll();

        page.writeHeader();
        page.writeStart("div", "class", "widget p-workStreams");
            page.writeStart("h1", "class", "icon icon-object-workStream");
                page.writeHtml(page.localize(WorkStreamsWidget.class, "title"));
            page.writeEnd();

            // Filters
            page.writeStart("div", "class", "widget-filters");
                page.writeStart("form",
                        "method", "get",
                        "action", page.url(null));

                page.writeStart("select",
                        "data-bsp-autosubmit", "",
                        "name", PARAM_ASSIGNED_USERS,
                        "data-searchable", "true");
                    page.writeStart("option",
                            "value", PARAM_ASSIGNED_USERS_ANYONE,
                            "selected", (StringUtils.equals(queryAssignedTo, PARAM_ASSIGNED_USERS_ANYONE) || StringUtils.isBlank(queryAssignedTo)) ? "selected" : null);
                        page.writeHtml(page.localize(WorkStreamsWidget.class, "label.anyone"));
                    page.writeEnd();
                    page.writeStart("option",
                            "value", PARAM_ASSIGNED_USERS_ME,
                            "selected", StringUtils.equals(queryAssignedTo, PARAM_ASSIGNED_USERS_ME) ? "selected" : null);
                        page.writeHtml(page.localize(WorkStreamsWidget.class, "label.me"));
                    page.writeEnd();
                page.writeEnd();

            page.writeEnd();
        page.writeEnd();

        if (!results.hasPages()) { //workStreams.isEmpty()) {
            page.writeStart("div", "class", "message message-info");
                page.writeHtml(page.localize(WorkStreamsWidget.class, "message.noWorkStreams"));
            page.writeEnd();

        } else {
            writePaginationHtml(page, results, limit);

            // Results
            List<WorkStream> workStreams = results.getItems();

            for (WorkStream workStream : workStreams) {
                List<ToolUser> users = workStream.getUsers();
                long skipped = workStream.countSkipped(user);
                long complete = workStream.countComplete();
                long incomplete = workStream.countIncomplete() - skipped;
                long total = complete + incomplete + skipped;
                boolean working = workStream.isWorking(user);

                page.writeStart("div",
                        "class", "block " + (working ? "p-workStreams-working" : "p-workStreams-notWorking"),
                        "style", page.cssString(
                                "padding-right", working ? "165px" : "75px",
                                "position", "relative"));

                //TODO: LOCALIZE
                if (users.isEmpty()) {
                    page.writeHtml("No users");

                } else {
                    page.writeStart("a",
                            "href", page.url("/workStreamUsers", "id", workStream.getId()),
                            "target", "workStream");
                        page.writeHtml(users.size());
                        page.writeHtml(" users");
                    page.writeEnd();
                }

                page.writeHtml(" working on ");

                page.writeStart("a",
                        "href", page.objectUrl("/content/editWorkStream", workStream, "reload", true),
                        "target", "workStream");
                    page.writeObjectLabel(workStream);
                page.writeEnd();

                if (working) {
                    page.writeStart("a",
                            "class", "button p-workStreams-continue",
                            "href", page.url("/content/edit.jsp", "workStreamId", workStream.getId(), "_", System.currentTimeMillis()),
                            "target", "_top",
                            "style", page.cssString(
                                    "bottom", 0,
                                    "position", "absolute",
                                    "right", "70px",
                                    "text-align", "center",
                                    "width", "90px"));
                        page.writeHtml(page.localize(WorkStreamsWidget.class, "action.continue"));
                    page.writeEnd();

                    page.writeStart("a",
                            "class", "button p-workStreams-stop",
                            "href", page.url("", "stop", workStream.getId()),
                            "style", page.cssString(
                                    "bottom", 0,
                                    "position", "absolute",
                                    "right", 0,
                                    "text-align", "center",
                                    "width", "65px"));
                        page.writeHtml(page.localize(WorkStreamsWidget.class, "action.stop"));
                    page.writeEnd();

                } else {
                    page.writeStart("a",
                            "class", "button p-workStreams-start",
                            "href", page.url("/content/edit.jsp", "workStreamId", workStream.getId(), "_", System.currentTimeMillis()),
                            "target", "_top",
                            "style", page.cssString(
                                    "bottom", 0,
                                    "position", "absolute",
                                    "right", 0,
                                    "text-align", "center",
                                    "width", "70px"));
                        page.writeHtml(page.localize(WorkStreamsWidget.class, "action.start"));
                    page.writeEnd();
                }

                page.writeStart("div", "class", "progress");
                    page.writeStart("div", "class", "progressBar", "style", "width:" + ((total - incomplete) * 100.0 / total) + "%");
                    page.writeEnd();

                    //TODO: LOCALIZE
                    page.writeStart("strong");
                        page.writeHtml(incomplete);
                    page.writeEnd();

                    page.writeHtml(" of ");

                    page.writeStart("strong");
                        page.writeHtml(total);
                    page.writeEnd();

                    page.writeHtml(" left ");

                    if (complete > 0L || skipped > 0L) {
                        page.writeHtml("(");
                    }

                    if (complete > 0L) {
                        page.writeStart("strong");
                            page.writeHtml(complete);
                        page.writeEnd();

                        page.writeHtml(" complete");

                        if (skipped > 0L) {
                            page.writeHtml(", ");
                        }
                    }

                    if (skipped > 0L) {
                        page.writeStart("strong");
                            page.writeHtml(skipped);
                        page.writeEnd();

                        page.writeHtml(" skipped");
                    }

                    if (complete > 0L || skipped > 0L) {
                        page.writeHtml(")");
                    }

                    page.writeEnd();
                page.writeEnd();
            }
        }
        page.writeEnd();
        page.writeFooter();
    }

    private void writePaginationHtml(ToolPageContext page, PaginatedResult<WorkStream> results, int limit) throws IOException {
        if (!hasPagination(page, results)) {
            return;
        }

        // Pagination
        page.writeStart("ul", "class", "pagination");

        if (results.hasPrevious()) {
            page.writeStart("li", "class", "first");
            page.writeStart("a", "href", page.url("", PARAM_OFFSET, results.getFirstOffset()));
            page.writeHtml(page.localize(WorkStreamsWidget.class, "pagination.newest"));
            page.writeEnd();
            page.writeEnd();

            page.writeStart("li", "class", "previous");
            page.writeStart("a", "href", page.url("", PARAM_OFFSET, results.getPreviousOffset()));
            page.writeHtml(page.localize(ImmutableMap.of("count", limit), "pagination.newerCount"));
            page.writeEnd();
            page.writeEnd();
        }

        if (results.getOffset() > 0 || results.hasNext() || results.getItems().size() > LIMITS[0]) {
            page.writeStart("li");
            page.writeStart("form",
                    "data-bsp-autosubmit", "",
                    "method", "get",
                    "action", page.url(null));
            page.writeStart("select", "name", PARAM_LIMIT);
            for (int l : LIMITS) {
                page.writeStart("option",
                        "value", l,
                        "selected", limit == l ? "selected" : null);
                page.writeHtml(page.localize(WorkStreamsWidget.class, ImmutableMap.of("count", l), "option.showCount"));
                page.writeEnd();
            }
            page.writeEnd();
            page.writeEnd();
            page.writeEnd();
        }

        if (results.hasNext()) {
            page.writeStart("li", "class", "next");
            page.writeStart("a", "href", page.url("", "offset", results.getNextOffset()));
            page.writeHtml(page.localize(WorkStreamsWidget.class, ImmutableMap.of("count", limit), "pagination.olderCount"));
            page.writeEnd();
            page.writeEnd();
        }

        page.writeEnd();

    }

    public boolean hasPagination(ToolPageContext page, PaginatedResult<WorkStream> results) {
        if (results.hasPrevious()) {
            return true;
        }
        if (results.getOffset() > 0 || results.hasNext() || results.getItems().size() > LIMITS[0]) {
            return true;
        }
        if (results.hasNext()) {
            return true;
        }

        return false;
    }
}
