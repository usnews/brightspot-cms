package com.psddev.cms.db;

import java.util.UUID;

import com.psddev.dari.db.Query;
import com.psddev.dari.db.Record;

/**
 * Action taken by a user in the tool.
 */
@Record.Embedded
public class ToolUserAction extends Record {

    private long time;
    private UUID contentId;
    private String url;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public UUID getContentId() {
        return contentId;
    }

    public void setContentId(UUID contentId) {
        this.contentId = contentId;
    }

    public Object getContent() {
        return Query.
                from(Object.class).
                where("_id = ?", getContentId()).
                first();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
