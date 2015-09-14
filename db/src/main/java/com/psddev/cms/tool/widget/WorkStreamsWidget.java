package com.psddev.cms.tool.widget;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;

import com.psddev.cms.db.ToolUser;
import com.psddev.cms.db.WorkStream;
import com.psddev.cms.tool.Dashboard;
import com.psddev.cms.tool.DefaultDashboardWidget;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.db.Query;

public class WorkStreamsWidget extends DefaultDashboardWidget {

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
        List<WorkStream> workStreams = Query.from(WorkStream.class).where(page.siteItemsPredicate()).selectAll();

        UUID stop = page.param(UUID.class, "stop");
        ToolUser user = page.getUser();

        for (WorkStream workStream : workStreams) {
            if (workStream.getId().equals(stop)) {
                workStream.stop(user);
                page.redirect(null);
                return;
            }
        }

        page.writeHeader();
            page.writeStart("div", "class", "widget p-workStreams");
                page.writeStart("h1", "class", "icon icon-object-workStream").writeHtml("Work Streams").writeEnd();

                if (workStreams.isEmpty()) {
                    page.writeStart("div", "class", "message message-info");
                        page.writeHtml("No work streams yet!");
                    page.writeEnd();

                } else {
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
                                    page.writeHtml("Continue");
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
                                    page.writeHtml("Stop");
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
                                    page.writeHtml("Start");
                                page.writeEnd();
                            }

                            page.writeStart("div", "class", "progress");
                                page.writeStart("div", "class", "progressBar", "style", "width:" + ((total - incomplete) * 100.0 / total) + "%");
                                page.writeEnd();

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

}
