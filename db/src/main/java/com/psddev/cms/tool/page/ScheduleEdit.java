package com.psddev.cms.tool.page;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;

import com.psddev.cms.db.Draft;
import com.psddev.cms.db.Schedule;
import com.psddev.cms.tool.PageServlet;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.db.Query;
import com.psddev.dari.util.RoutingFilter;

@RoutingFilter.Path(application = "cms", value = "/scheduleEdit")
@SuppressWarnings("serial")
public class ScheduleEdit extends PageServlet {

    @Override
    protected String getPermissionId() {
        return null;
    }

    @Override
    protected void doService(final ToolPageContext page) throws IOException, ServletException {
        Schedule schedule = (Schedule) page.findOrReserve(Schedule.class);

        if (page.isFormPost()) {
            try {
                boolean newSchedule = schedule.getState().isNew();

                page.include("/WEB-INF/objectPost.jsp", "object", schedule);
                page.publish(schedule);

                if (newSchedule) {
                    page.getUser().setCurrentSchedule(schedule);
                    page.getUser().save();
                }

                page.writeStart("script", "type", "text/javascript");
                    page.writeRaw("window.location = window.location;");
                page.writeEnd();

                return;

            } catch (Exception error) {
                page.getErrors().add(error);
            }
        }

        page.writeStart("div", "class", "widget");
            page.writeFormHeading(schedule, "class", "icon icon-object-schedule");
            page.include("/WEB-INF/errors.jsp");

            page.writeStart("form",
                    "method", "post",
                    "action", page.url("", "id", schedule.getId()));
                page.include("/WEB-INF/objectForm.jsp", "object", schedule);

                page.writeStart("div", "class", "buttons");
                    page.writeStart("button", "class", "action action-save");
                        page.writeHtml("Save");
                    page.writeEnd();
                page.writeEnd();
            page.writeEnd();

            List<Draft> drafts = Query.
                    from(Draft.class).
                    where("schedule = ?", schedule).
                    selectAll();

            if (!drafts.isEmpty()) {
                page.writeStart("h2").writeHtml("Items").writeEnd();

                page.writeStart("ul", "class", "links");
                    for (Draft draft : drafts) {
                        page.writeStart("li");
                            page.writeStart("a",
                                    "href", page.objectUrl("/content/edit.jsp", draft),
                                    "target", "_top");
                                page.writeHtml(page.getObjectLabel(draft));
                            page.writeEnd();
                        page.writeEnd();
                    }
                page.writeEnd();
            }
        page.writeEnd();
    }
}
