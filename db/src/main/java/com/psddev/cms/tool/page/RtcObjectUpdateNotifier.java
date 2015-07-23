package com.psddev.cms.tool.page;

import com.psddev.dari.db.UpdateNotifier;
import org.atmosphere.cpr.Broadcaster;

class RtcObjectUpdateNotifier implements UpdateNotifier<Object> {

    private final Broadcaster broadcaster;

    public RtcObjectUpdateNotifier(Broadcaster broadcaster) {
        this.broadcaster = broadcaster;
    }

    @Override
    public void onUpdate(Object object) {
        if (!RtcBroadcast.forObject(object).isEmpty()) {
            broadcaster.broadcast(object);
        }
    }
}
