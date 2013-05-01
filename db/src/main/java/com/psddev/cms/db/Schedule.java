package com.psddev.cms.db;

import com.psddev.dari.db.Record;
import com.psddev.dari.db.Query;
import com.psddev.dari.db.State;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Schedule extends Record {

    private static final Logger LOGGER = LoggerFactory.getLogger(Schedule.class);

    private String name;
    private @Indexed Date triggerDate;
    private Site triggerSite;
    private ToolUser triggerUser;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getTriggerDate() {
        return triggerDate;
    }

    public void setTriggerDate(Date triggerDate) {
        this.triggerDate = triggerDate;
    }

    public Site getTriggerSite() {
        return triggerSite;
    }

    public void setTriggerSite(Site triggerSite) {
        this.triggerSite = triggerSite;
    }

    public ToolUser getTriggerUser() {
        return triggerUser;
    }

    public void setTriggerUser(ToolUser triggerUser) {
        this.triggerUser = triggerUser;
    }

    /**
     * @return {@code true} if this schedule was triggered.
     */
    public boolean trigger() {
        Date triggerDate = getTriggerDate();

        if (triggerDate == null ||
                !triggerDate.before(new Date())) {
            return false;
        }

        LOGGER.info("Triggering [{}] schedule", getLabel());
        try {
            beginWrites();
            for (Draft draft : Query
                    .from(Draft.class)
                    .where("schedule = ?", this)
                    .selectAll()) {
                LOGGER.debug(
                        "Processing [{}] draft in [{}] schedule",
                        draft.getLabel(), getLabel());
                Object object = draft.getObject();
                if (object != null) {
                    Content.Static.publish(object, getTriggerSite(), getTriggerUser());
                }
                draft.delete();
            }
            delete();
            commitWrites();
            return true;
        } finally {
            endWrites();
        }
    }
}
