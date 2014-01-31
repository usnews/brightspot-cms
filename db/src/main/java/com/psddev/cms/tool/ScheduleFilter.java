package com.psddev.cms.tool;

import com.psddev.cms.db.Schedule;

import com.psddev.dari.db.ObjectType;
import com.psddev.dari.db.Query;
import com.psddev.dari.util.AbstractFilter;
import com.psddev.dari.util.Task;

import java.io.PrintWriter;
import java.io.StringWriter;

/** Triggers scheduled events for publishing. */
public class ScheduleFilter extends AbstractFilter {

    public static final String SCHEDULE_THREAD_NAME = "ScheduleFilter";

    public final Task scheduler = new Task() {

        @Override
        public void doTask() {
            if (ObjectType.getInstance(Schedule.class.getName()) == null) {
                return;
            }

            Thread.currentThread().setName(SCHEDULE_THREAD_NAME);

            for (Schedule schedule : Query.
                    from(Schedule.class).
                    sortAscending("triggerDate").
                    master().
                    noCache().
                    iterable(0)) {

                try {
                    schedule.trigger();

                } catch (Exception ex1) {
                    try {
                        StringWriter writer = new StringWriter();
                        ex1.printStackTrace(new PrintWriter(writer));
                        schedule.getState().putValue("cms.lastException", writer.toString());
                        schedule.save();
                    } catch (Exception ex2) {
                    }
                }
            }
        }
    };

    // --- AbstractFilter support ---

    @Override
    protected void doInit() {
        scheduler.schedule(60.0, 60.0);
    }

    @Override
    protected void doDestroy() {
        scheduler.stop();
    }
}
