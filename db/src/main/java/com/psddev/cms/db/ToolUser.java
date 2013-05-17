package com.psddev.cms.db;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.psddev.dari.db.Record;
import com.psddev.dari.util.Password;

/** User that uses the CMS and other related tools. */
public class ToolUser extends Record {

    @Indexed
    @ToolUi.Note("If left blank, the user will have full access to everything.")
    private ToolRole role;

    @Indexed
    @Required
    private String name;

    @Indexed(unique = true)
    @Required
    private String email;

    @ToolUi.FieldDisplayType("timeZone")
    private String timeZone;

    @ToolUi.FieldDisplayType("password")
    private String password;

    private String phoneNumber;
    private NotificationMethod notifyVia;

    @Indexed
    @ToolUi.DropDown
    private Set<Notification> notifications;

    @ToolUi.Hidden
    private Map<String, Object> settings;

    private Site currentSite;

    @ToolUi.Hidden
    private Schedule currentSchedule;

    /** Returns the role. */
    public ToolRole getRole() {
        return role;
    }

    /** Sets the role. */
    public void setRole(ToolRole role) {
        this.role = role;
    }

    /** Returns the name. */
    public String getName() {
        return name;
    }

    /** Sets the name. */
    public void setName(String name) {
        this.name = name;
    }

    /** Returns the email. */
    public String getEmail() {
        return email;
    }

    /** Sets the email. */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the time zone.
     */
    public String getTimeZone() {
        return timeZone;
    }

    /**
     * Sets the time zone.
     */
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    /** Returns the password. */
    public Password getPassword() {
        return Password.valueOf(password);
    }

    /** Sets the password. */
    public void setPassword(Password password) {
        this.password = password.toString();
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public NotificationMethod getNotifyVia() {
        return notifyVia;
    }

    public void setNotifyVia(NotificationMethod notifyVia) {
        this.notifyVia = notifyVia;
    }

    public Set<Notification> getNotifications() {
        if (notifications == null) {
            notifications = new LinkedHashSet<Notification>();
        }
        return notifications;
    }

    public void setNotifications(Set<Notification> notifications) {
        this.notifications = notifications;
    }

    /** Returns the settings. */
    public Map<String, Object> getSettings() {
        if (settings == null) {
            settings = new LinkedHashMap<String, Object>();
        }
        return settings;
    }

    /** Sets the settings. */
    public void setSettings(Map<String, Object> settings) {
        this.settings = settings;
    }

    public Site getCurrentSite() {
        if ((currentSite == null &&
                hasPermission("site/global")) ||
                (currentSite != null &&
                hasPermission(currentSite.getPermissionId()))) {
            return currentSite;

        } else {
            for (Site s : Site.Static.findAll()) {
                if (hasPermission(s.getPermissionId())) {
                    return s;
                }
            }

            throw new IllegalStateException("No accessible site!");
        }
    }

    public void setCurrentSite(Site site) {
        this.currentSite = site;
    }

    public Schedule getCurrentSchedule() {
        return currentSchedule;
    }

    public void setCurrentSchedule(Schedule currentSchedule) {
        this.currentSchedule = currentSchedule;
    }

    /**
     * Returns {@code true} if this user is allowed access to the
     * resources identified by the given {@code permissionId}.
     */
    public boolean hasPermission(String permissionId) {
        ToolRole role = getRole();
        return role != null ? role.hasPermission(permissionId) : true;
    }
}
