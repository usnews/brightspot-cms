package com.psddev.cms.tool.page;

import com.psddev.cms.db.Draft;
import com.psddev.cms.db.Schedule;
import com.psddev.cms.tool.ToolPage;
import com.psddev.cms.tool.ToolPageContext;

import com.psddev.dari.db.Query;
import com.psddev.dari.util.HtmlWriter;
import com.psddev.dari.util.RoutingFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;

import org.joda.time.DateTime;

@RoutingFilter.Path(application = "cms", value = "/misc/scheduledEvents.jsp")
@SuppressWarnings("serial")
public class ScheduledEvents extends ToolPage {

    @Override
    protected String getPermissionId() {
        return "area/dashboard";
    }

    @Override
    protected void doService(
            ToolPageContext page,
            HtmlWriter writer)
            throws IOException, ServletException {

        Mode mode = page.pageParam(Mode.class, "mode", Mode.WEEK);
        DateTime date = new DateTime(page.pageParam(Date.class, "date", new Date()));
        DateTime begin = mode.getBegin(date);
        DateTime end = mode.getEnd(date);
        Map<DateTime, List<Schedule>> schedulesByDate = new TreeMap<DateTime, List<Schedule>>();

        for (DateTime i = begin; i.isBefore(end); i = i.plusDays(1)) {
            schedulesByDate.put(i, new ArrayList<Schedule>());
        }

        for (Schedule schedule : Query.
                from(Schedule.class).
                where("triggerDate >= ? and triggerDate < ?", begin, end).
                sortAscending("triggerDate").
                iterable(0)) {
            DateTime scheduleDate = new DateTime(schedule.getTriggerDate()).toDateMidnight().toDateTime();
            List<Schedule> schedules = schedulesByDate.get(scheduleDate);

            if (schedules != null) {
                schedules.add(schedule);
            }
        }

        writer.start("div", "class", "widget widget-scheduledEvents");
            writer.start("h1", "class", "icon-calendar");

                writer.html("Scheduled Events: ");

                String beginMonth = begin.monthOfYear().getAsText();
                int beginYear = begin.year().get();
                String endMonth = end.monthOfYear().getAsText();
                int endYear = end.year().get();

                writer.html(beginMonth);
                writer.html(" ");
                writer.html(begin.dayOfMonth().get());

                if (beginYear != endYear) {
                    writer.html(", ");
                    writer.html(beginYear);
                }

                writer.html(" - ");

                if (!endMonth.equals(beginMonth)) {
                    writer.html(endMonth);
                    writer.html(" ");
                }

                writer.html(end.dayOfMonth().get());
                writer.html(", ");
                writer.html(endYear);

            writer.end();

            /*
            writer.start("form", "method", "get", "class", "autoSubmit", "action", page.url(null));
                writer.start("select", "name", "mode");
                    for (Mode m : Mode.values()) {
                        writer.start("option",
                                "value", m.name(),
                                "selected", m.equals(mode) ? "selected" : null);
                            writer.html(m);
                        writer.end();
                    }
                writer.end();
            writer.end();
            */

            writer.start("ul", "class", "pagination");

                writer.start("li", "class", "previous");
                    writer.start("a",
                            "href", page.url("", "date", mode.getPrevious(date).getMillis()));
                        writer.html("Previous ").html(mode);
                    writer.end();
                writer.end();

                writer.start("li");
                    writer.start("a",
                            "href", page.url("", "date", System.currentTimeMillis()));
                        writer.html("Today");
                    writer.end();
                writer.end();

                writer.start("li", "class", "next");
                    writer.start("a",
                            "href", page.url("", "date", mode.getNext(date).getMillis()));
                        writer.html("Next ").html(mode);
                    writer.end();
                writer.end();

            writer.end();

            mode.display(page, writer, schedulesByDate);
        writer.end();
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
            public void display(ToolPageContext page, HtmlWriter writer, Map<DateTime, List<Schedule>> schedulesByDate) throws IOException {
                writer.start("div", "class", "calendar calendar-week");
                    for (Map.Entry<DateTime, List<Schedule>> entry : schedulesByDate.entrySet()) {
                        DateTime date = entry.getKey();
                        List<Schedule> schedules = entry.getValue();

                        writer.start("div", "class", "calendarRow");
                            writer.start("div", "class", "calendarDay" + (date.equals(new DateTime().toDateMidnight()) ? " calendarDay-today" : ""));
                                writer.start("span", "class", "calendarDayOfWeek").html(date.dayOfWeek().getAsShortText()).end();
                                writer.start("span", "class", "calendarDayOfMonth").html(date.dayOfMonth().get()).end();
                            writer.end();

                            writer.start("div", "class", "calendarCell").start("table", "class", "links table-striped").start("tbody");
                                for (Schedule schedule : schedules) {
                                    DateTime triggerDate = new DateTime(schedule.getTriggerDate());
                                    List<Draft> drafts = Query.from(Draft.class).where("schedule = ?", schedule).selectAll();

                                    if (drafts.isEmpty()) {
                                        continue;
                                    }

                                    writer.start("tr");
                                        writer.start("td", "class", "time");
                                            writer.html(triggerDate.toString("hh:mm a"));
                                        writer.end();

                                        writer.start("td");
                                            for (Draft draft : drafts) {
                                                writer.start("a",
                                                        "href", page.objectUrl("/content/edit.jsp", draft),
                                                        "target", "_top");
                                                    writer.html(page.objectLabel2(draft.getObject()));
                                                writer.end();
                                                writer.tag("br");
                                            }
                                        writer.end();
                                    writer.end();
                                }
                            writer.end().end().end();
                        writer.end();
                    }
                writer.end();
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
            public void display(ToolPageContext page, HtmlWriter writer, Map<DateTime, List<Schedule>> schedulesByDate) throws IOException {
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

        public abstract void display(ToolPageContext page, HtmlWriter writer, Map<DateTime, List<Schedule>> schedulesByDate) throws IOException;

        @Override
        public String toString() {
            return displayName;
        }
    }
}
