package com.psddev.cms.db;

import com.psddev.dari.db.Recordable;

public interface ToolEntity extends Recordable {

    /**
     * Sends a notification to this entity using the given {@code sender}.
     *
     * @param sender Can't be {@code null}.
     */
    public void sendNotification(NotificationSender sender);
}
