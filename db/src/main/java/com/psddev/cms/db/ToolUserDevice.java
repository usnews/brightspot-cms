package com.psddev.cms.db;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import com.psddev.dari.db.Record;
import com.psddev.dari.util.ObjectUtils;

/**
 * Device used by a user to access the tool.
 */
@Record.Embedded
public class ToolUserDevice extends Record {

    @Indexed
    private UUID lookingGlassId;

    private String userAgent;
    private List<ToolUserAction> actions;

    public UUID getLookingGlassId() {
        return lookingGlassId;
    }

    public void setLookingGlassId(UUID lookingGlassId) {
        this.lookingGlassId = lookingGlassId;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    /**
     * Returns a descriptive label of the user agent suitable for display.
     *
     * @return Never {@code null}.
     */
    public String getUserAgentDisplay() {
        String ua = getUserAgent();

        if (ObjectUtils.isBlank(ua)) {
            return "Unknown Device";
        }

        ua = ua.toLowerCase(Locale.ENGLISH);

        if (ua.contains("iphone")) {
            return "iPhone";

        } else if (ua.contains("chrome/")) {
            return "Google Chrome";

        } else if (ua.contains("safari/")) {
            return "Apple Safari";

        } else if (ua.contains("firefox/")) {
            return "Mozilla Firefox";

        } else {
            return ua;
        }
    }

    /**
     * @return Never {@code null}. Mutable.
     */
    public List<ToolUserAction> getActions() {
        if (actions == null) {
            actions = new ArrayList<ToolUserAction>();
        }
        return actions;
    }

    /**
     * @param actions May be {@code null} to clear the list.
     */
    public void setActions(List<ToolUserAction> actions) {
        this.actions = actions;
    }

    /**
     * Adds the given {@code action} to this device.
     *
     * @param action Can't be {@code null}.
     */
    public void addAction(ToolUserAction action) {
        action.setTime(System.currentTimeMillis());

        List<ToolUserAction> actions = getActions();
        Map<String, Object> actionMap = action.getState().getSimpleValues();

        actionMap.remove("_id");
        actionMap.remove("time");

        for (Iterator<ToolUserAction> i = actions.iterator(); i.hasNext(); ) {
            Map<String, Object> currentMap = i.next().getState().getSimpleValues();

            currentMap.remove("_id");
            currentMap.remove("time");

            if (currentMap.equals(actionMap)) {
                i.remove();
            }
        }

        actions.add(0, action);

        while (actions.size() > 5) {
            actions.remove(actions.size() - 1);
        }
    }

    /**
     * Finds the last action from this device.
     *
     * @return May be {@code null}.
     */
    public ToolUserAction findLastAction() {
        for (ToolUserAction action : getActions()) {
            return action;
        }

        return null;
    }
}
