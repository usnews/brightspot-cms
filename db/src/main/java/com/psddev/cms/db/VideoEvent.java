package com.psddev.cms.db;

import com.psddev.dari.db.Record;

/**
 * Abstracts an event in the video such as beginning of a chapter,ad etc
 */
@Record.Embedded
public class VideoEvent extends Record {

    public final static String VIDEO_EVENT_DESCRIPTION_METADATA_KEY="desc";
    public final static String VIDEO_EVENT_THUMBNAIL_METADATA_KEY="thumbUrl";

    @Indexed
    private String name;
    private String description;
    private int startTime;
    private int endTime;
    private String tags;
    private String thumbnailUrl;
    private String externalId;
    private String entryId;

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getEntryId() {
        return entryId;
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public VideoEvent() {
    }

    public String toString() {
        StringBuffer sb=new StringBuffer(" externalId:"+ externalId );
        sb.append(", name:").append(name);
        sb.append(", startTime:").append(startTime);
        sb.append(", endTime:").append(endTime);
        sb.append(", tags:").append(tags);
        return sb.toString();
    }
}
