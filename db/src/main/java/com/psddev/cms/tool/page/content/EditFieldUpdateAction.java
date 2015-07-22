package com.psddev.cms.tool.page.content;

import com.psddev.cms.tool.page.RtcAction;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.TypeReference;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

class EditFieldUpdateAction implements RtcAction {

    private UUID userId;
    private final Set<UUID> contentIds = new HashSet<>();

    @Override
    public void initialize(UUID userId) {
        this.userId = userId;
    }

    @Override
    public void execute(Map<String, Object> data) {
        UUID contentId = ObjectUtils.to(UUID.class, data.get("contentId"));

        contentIds.add(contentId);

        EditFieldUpdate.save(
                userId,
                contentId,
                ObjectUtils.to(new TypeReference<Map<String, Set<String>>>() {
                }, data.get("fieldNamesByObjectId")));
    }

    @Override
    public void destroy() {
        contentIds.forEach(contentId -> EditFieldUpdate.delete(userId, contentId));
    }
}
