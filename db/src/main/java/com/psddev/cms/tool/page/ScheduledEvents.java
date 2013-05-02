package com.psddev.cms.tool.page;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;

import org.joda.time.DateTime;

import com.psddev.cms.db.Draft;
import com.psddev.cms.db.Schedule;
import com.psddev.cms.db.Site;
import com.psddev.cms.tool.PageServlet;
import com.psddev.cms.tool.PageWriter;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.db.Query;
import com.psddev.dari.util.RoutingFilter;

@RoutingFilter.Path(application = "cms", value = "/misc/scheduledEvents.jsp")
@SuppressWarnings("serial")
public class ScheduledEvents extends PageServlet {

    @Override
    protected String getPermissionId() {
        return "area/dashboard";
    }

    @Override
    protected void doService(ToolPageContext page) throws IOException, ServletException {
        Mode mode = page.pageParam(Mode.class, "mode", Mode.WEEK);
        DateTime date = new DateTime(page.param(Date.class, "date"));
        DateTime begin = mode.getBegin(date);
        DateTime end = mode.getEnd(date);
        Map<DateTime, List<Schedule>> schedulesByDate = new TreeMap<DateTime, List<Schedule>>();
        boolean hasSchedules = false;

        for (DateTime i = begin; i.isBefore(end); i = i.plusDays(1)) {
            schedulesByDate.put(i, new ArrayList<Schedule>());
        }

        Site currentSite = page.getSite();

        for (Schedule schedule : Query.
                from(Schedule.class).
                where("triggerDate >= ? and triggerDate < ?", begin, end).
                sortAscending("triggerDate").
                iterable(0)) {

            if (currentSite != null && !currentSite.equals(schedule.getTriggerSite())) {
                continue;
            }

            DateTime scheduleDate = new DateTime(schedule.getTriggerDate()).toDateMidnight().toDateTime();
            List<Schedule> schedules = schedulesByDate.get(scheduleDate);

            if (schedules != null) {
                schedules.add(schedule);
                hasSchedules = true;
            }
        }

        PageWriter writer = page.getWriter();

        writer.writeStart("div", "class", "widget widget-scheduledEvents" + (hasSchedules ? "" : " widget-scheduledEvents-empty"));
            writer.writeStart("h1", "class", "icon icon-action-schedule");

                writer.writeHtml("Scheduled Events: ");

                String beginMonth = begin.monthOfYear().getAsText();
                int beginYear = begin.year().get();
                String endMonth = end.monthOfYear().getAsText();
                int endYear = end.year().get();

                writer.writeHtml(beginMonth);
                writer.writeHtml(" ");
                writer.writeHtml(begin.dayOfMonth().get());

                if (beginYear != endYear) {
                    writer.writeHtml(", ");
                    writer.writeHtml(beginYear);
                }

                writer.writeHtml(" - ");

                if (!endMonth.equals(beginMonth)) {
                    writer.writeHtml(endMonth);
                    writer.writeHtml(" ");
                }

                writer.writeHtml(end.dayOfMonth().get());
                writer.writeHtml(", ");
                writer.writeHtml(endYear);

            writer.writeEnd();

            writer.writeStart("ul", "class", "piped");
                writer.writeStart("li");
                    writer.writeStart("a",
                            "class", "icon icon-action-create",
                            "href", page.cmsUrl("/scheduleEdit"),
                            "target", "scheduleEdit");
                        writer.writeHtml("New Schedule");
                    writer.writeEnd();
                writer.writeEnd();

                writer.writeStart("li");
                    writer.writeStart("a",
                            "class", "icon icon-action-search",
                            "href", page.cmsUrl("/scheduleList"),
                            "target", "scheduleList");
                        writer.writeHtml("Available Schedules");
                    writer.writeEnd();
                writer.writeEnd();
            writer.writeEnd();

            /*
            writer.writeStart("form", "method", "get", "class", "autoSubmit", "action", page.url(null));
                writer.writeStart("select", "name", "mode");
                    for (Mode m : Mode.values()) {
                        writer.writeStart("option",
                                "value", m.name(),
                                "selected", m.equals(mode) ? "selected" : null);
                            writer.writeHtml(m);
                        writer.writeEnd();
                    }
                writer.writeEnd();
            writer.writeEnd();
            */

            writer.writeStart("ul", "class", "pagination");

                DateTime previous = mode.getPrevious(date);
                DateTime today = new DateTime().toDateMidnight().toDateTime();

                if (!previous.isBefore(today)) {
                    writer.writeStart("li", "class", "previous");
                        writer.writeStart("a",
                                "href", page.url("", "date", previous.getMillis()));
                            writer.writeHtml("Previous ").writeHtml(mode);
                        writer.writeEnd();
                    writer.writeEnd();
                }

                if (begin.isAfter(today) || end.isBefore(today)) {
                    writer.writeStart("li");
                        writer.writeStart("a",
                                "href", page.url("", "date", System.currentTimeMillis()));
                            writer.writeHtml("Today");
                        writer.writeEnd();
                    writer.writeEnd();
                }

                writer.writeStart("li", "class", "next");
                    writer.writeStart("a",
                            "href", page.url("", "date", mode.getNext(date).getMillis()));
                        writer.writeHtml("Next ").writeHtml(mode);
                    writer.writeEnd();
                writer.writeEnd();

            writer.writeEnd();

            mode.display(page, schedulesByDate);
        writer.writeEnd();
    }

    private enum Mode {
        WEEK("Week") {

            @Override
            public DateTime getBegin(DateTime date) {
                return date.toDateMidnight().toDateTime();
            }

            @Override
            public DateTime getEnd(DateTime date) {
                return getBegin(date).plusWeeks(1);
            }

            @Override
            public DateTime getPrevious(DateTime date) {
                return date.plusWeeks(-1);
            }

            @Override
            public DateTime getNext(DateTime date) {
                return date.plusWeeks(1);
            }

            @Override
            public void display(ToolPageContext page, Map<DateTime, List<Schedule>> schedulesByDate) throws IOException {
                PageWriter writer = page.getWriter();

                writer.writeStart("div", "class", "calendar calendar-week");
                    for (Map.Entry<DateTime, List<Schedule>> entry : schedulesByDate.entrySet()) {
                        DateTime date = entry.getKey();
                        List<Schedule> schedules = entry.getValue();

                        writer.writeStart("div", "class", "calendarRow");
                            writer.writeStart("div", "class", "calendarDay" + (date.equals(new DateTime().toDateMidnight()) ? " calendarDay-today" : ""));
                                writer.writeStart("span", "class", "calendarDayOfWeek").writeHtml(date.dayOfWeek().getAsShortText()).writeEnd();
                                writer.writeStart("span", "class", "calendarDayOfMonth").writeHtml(date.dayOfMonth().get()).writeEnd();
                            writer.writeEnd();

                            writer.writeStart("div", "class", "calendarCell").writeStart("table", "class", "links table-striped pageThumbnails").writeStart("tbody");
                                for (Schedule schedule : schedules) {
                                    DateTime triggerDate = new DateTime(schedule.getTriggerDate());
                                    List<Draft> drafts = Query.from(Draft.class).where("schedule = ?", schedule).selectAll();

                                    if (drafts.isEmpty()) {
                                        continue;
                                    }

                                    boolean first = true;

                                    for (Draft draft : drafts) {
                                        Object draftObject = draft.getObject();

                                        writer.writeStart("tr", "data-preview-url", "/_preview?_cms.db.previewId=" + draft.getId());
                                            writer.writeStart("td", "class", "time");
                                                if (first) {
                                                    writer.writeHtml(triggerDate.toString("hh:mm a"));
                                                    first = false;
                                                }
                                            writer.writeEnd();

                                            writer.writeStart("td");
                                                writer.typeLabel(draftObject);
                                            writer.writeEnd();

                                            writer.writeStart("td", "data-preview-anchor", "");
                                                writer.writeStart("a",
                                                        "href", page.objectUrl("/content/edit.jsp", draft),
                                                        "target", "_top");
                                                    writer.objectLabel(draftObject);
                                                writer.writeEnd();
                                            writer.writeEnd();
                                        writer.writeEnd();
                                    }
                                }
                            writer.writeEnd().writeEnd().writeEnd();
                        writer.writeEnd();
                    }
                writer.writeEnd();
            }
        },

        MONTH("Month") {

            @Override
            public DateTime getBegin(DateTime date) {
                return date.toDateMidnight().withDayOfMonth(1).toDateTime();
            }

            @Override
            public DateTime getEnd(DateTime date) {
                return getBegin(date).plusMonths(1);
            }

            @Override
            public DateTime getPrevious(DateTime date) {
                return date.plusMonths(-1);
            }

            @Override
            public DateTime getNext(DateTime date) {
                return date.plusMonths(1);
            }

            @Override
            public void display(ToolPageContext page, Map<DateTime, List<Schedule>> schedulesByDate) throws IOException {
            }
        };

        private final String displayName;

        private Mode(String displayName) {
            this.displayName = displayName;
        }

        public abstract DateTime getBegin(DateTime date);

        public abstract DateTime getEnd(DateTime date);

        public abstract DateTime getPrevious(DateTime date);

        public abstract DateTime getNext(DateTime date);

        public abstract void display(ToolPageContext page, Map<DateTime, List<Schedule>> schedulesByDate) throws IOException;

        @Override
        public String toString() {
            return displayName;
        }
    }
}
