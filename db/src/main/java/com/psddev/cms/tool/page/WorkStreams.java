package com.psddev.cms.tool.page;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;

import com.psddev.cms.db.ToolUser;
import com.psddev.cms.db.WorkStream;
import com.psddev.cms.tool.PageServlet;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.db.Query;
import com.psddev.dari.util.RoutingFilter;

@RoutingFilter.Path(application = "cms", value = "/workStreams")
@SuppressWarnings("serial")
public class WorkStreams extends PageServlet {

    @Override
    protected String getPermissionId() {
        return "area/dashboard";
    }

    @Override
    protected void doService(final ToolPageContext page) throws IOException, ServletException {
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

        page.writeStart("div", "class", "widget");
            page.writeStart("h1", "class", "icon icon-object-workStream").writeHtml("Work Streams").writeEnd();

            if (workStreams.isEmpty()) {
                page.writeStart("div", "class", "message message-warning");
                    page.writeStart("p");
                        page.writeHtml("No work streams yet!");
                    page.writeEnd();
                page.writeEnd();

            } else {
                for (WorkStream workStream : workStreams) {
                    List<ToolUser> users = workStream.getUsers();
                    long incomplete = workStream.countIncomplete();
                    long total = workStream.getQuery().count();
                    boolean working = workStream.isWorking(user);

                    page.writeStart("div",
                            "class", "block",
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
                                "href", page.objectUrl("/content/workStreamEdit.jsp", workStream, "reload", true),
                                "target", "workStream");
                            page.writeHtml(page.getObjectLabel(workStream));
                        page.writeEnd();

                        if (working) {
                            page.writeStart("a",
                                    "class", "button",
                                    "href", page.url("/content/edit.jsp", "workStreamId", workStream.getId()),
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
                                    "class", "button",
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
                                    "class", "button",
                                    "href", page.url("/content/edit.jsp", "workStreamId", workStream.getId()),
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

                            page.writeHtml(" left");
                        page.writeEnd();
                    page.writeEnd();
                }
            }
        page.writeEnd();
    }
}
