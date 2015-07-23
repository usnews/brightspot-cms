package com.psddev.cms.tool.page.content;

import com.google.common.collect.ImmutableMap;
import com.psddev.cms.db.ToolUser;
import com.psddev.cms.tool.page.RtcBroadcast;
import com.psddev.dari.db.Query;

import java.util.Map;
import java.util.UUID;

class EditFieldUpdateBroadcast implements RtcBroadcast<EditFieldUpdate> {

    @Override
    public Map<String, Object> create(UUID currentUserId, EditFieldUpdate update) {
        ToolUser user = Query.from(ToolUser.class).where("_id = ?", update.getUserId()).first();

        if (user == null) {
            return null;
        }

        UUID userId = user.getId();

        if (userId.equals(currentUserId)) {
            return null;
        }

        return ImmutableMap.of(
                "userId", userId.toString(),
                "userName", user.getName(),
                "contentId", update.getContentId().toString(),
                "fieldNamesByObjectId", update.getFieldNamesByObjectId()
        );
    }
}
