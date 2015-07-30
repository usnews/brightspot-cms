package com.psddev.cms.tool.page.content;

import com.psddev.cms.tool.page.RtcState;
import com.psddev.dari.db.Database;
import com.psddev.dari.db.Query;
import org.joda.time.DateTime;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

class EditFieldUpdateState implements RtcState {

    @Override
    public Iterable<? extends Object> create(Map<String, Object> data) {
        List<EditFieldUpdate> updates = Query.from(EditFieldUpdate.class)
                .where("contentId = ?", data.get("contentId"))
                .selectAll();

        Database database = Database.Static.getDefault();
        DateTime expire = new DateTime(database.now()).minusSeconds(60);

        database.beginIsolatedWrites();

        try {
            for (Iterator<EditFieldUpdate> it = updates.iterator(); it.hasNext();) {
                EditFieldUpdate update = it.next();
                long updateTime = update.getTime();

                if (updateTime == 0 || new DateTime(updateTime).isBefore(expire)) {
                    it.remove();
                    update.delete();
                }
            }

            database.commitWrites();

        } finally {
            database.endWrites();
        }

        return updates;
    }
}
