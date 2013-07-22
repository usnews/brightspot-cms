package com.psddev.cms.db;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.psddev.dari.db.Query;
import com.psddev.dari.db.Record;
import com.psddev.dari.db.State;
import com.psddev.dari.util.ObjectUtils;

public class Schedule extends Record {

    private static final Logger LOGGER = LoggerFactory.getLogger(Schedule.class);

    @Indexed(unique = true)
    private String name;

    @Indexed
    private Date triggerDate;

    @ToolUi.Hidden
    private Site triggerSite;

    @ToolUi.Hidden
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

        LOGGER.debug("Triggering [{}] schedule", getLabel());

        try {
            beginWrites();

            for (Draft draft : Query.
                    from(Draft.class).
                    where("schedule = ?", this).
                    selectAll()) {
                LOGGER.debug("Processing [{}] draft in [{}] schedule", draft.getLabel(), getLabel());

                Object object = draft.getObject();

                if (object != null) {
                    ToolUser triggerUser = getTriggerUser();

                    if (triggerUser == null) {
                        triggerUser = draft.as(Content.ObjectModification.class).getUpdateUser();

                        if (triggerUser == null) {
                            triggerUser = draft.getOwner();
                        }
                    }

                    Content.ObjectModification contentData = State.getInstance(object).as(Content.ObjectModification.class);

                    contentData.setDraft(false);
                    contentData.setPublishDate(triggerDate);
                    contentData.setPublishUser(triggerUser);
                    Content.Static.publish(object, getTriggerSite(), triggerUser);
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

    @Override
    public String getLabel() {
        String name = getName();
        StringBuilder label = new StringBuilder();

        if (ObjectUtils.isBlank(name)) {
            Date triggerDate = getTriggerDate();

            label.append(triggerDate != null ?
                    triggerDate.toString() :
                    getId().toString());

        } else {
            label.append(name);
        }

        long draftCount = Query.
                from(Draft.class).
                where("schedule = ?", this).
                count();

        if (draftCount > 1) {
            label.append(" (");
            label.append(draftCount);
            label.append(" items)");
        }

        return label.toString();
    }
}
