package com.psddev.cms.tool;

import com.psddev.cms.db.ToolUser;

import com.psddev.dari.util.AbstractFilter;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Use {@link RemoteWidgetFilter} and {@link AuthenticationFilter} instead. */
@Deprecated
public class ToolFilter extends AbstractFilter {

    /** @deprecated Use {@link AuthenticationFilter#AUTHENTICATED_ATTRIBUTE} instead. */
    @Deprecated
    public static final String AUTHENTICATED_ATTRIBUTE = AuthenticationFilter.AUTHENTICATED_ATTRIBUTE;

    /** @deprecated Use {@link AuthenticationFilter#USER_SETTINGS_CHANGED_ATTRIBUTE} instead. */
    @Deprecated
    public static final String IS_USER_SETTINGS_CHANGED_ATTRIBUTE = AuthenticationFilter.USER_SETTINGS_CHANGED_ATTRIBUTE;

    /** @deprecated Use {@link AuthenticationFilter#USER_ATTRIBUTE} instead. */
    @Deprecated
    public static final String USER_ATTRIBUTE = AuthenticationFilter.USER_ATTRIBUTE;

    /** @deprecated Use {@link AuthenticationFilter#LOG_IN_PATH} instead. */
    @Deprecated
    public static final String LOG_IN_PATH = AuthenticationFilter.LOG_IN_PATH;

    /** @deprecated Use {@link AuthenticationFilter#RETURN_PATH_PARAMETER} instead. */
    @Deprecated
    public static final String RETURN_PATH_PARAMETER = AuthenticationFilter.RETURN_PATH_PARAMETER;

    /** @deprecated Use {@link AuthenticationFilter#USER_COOKIE} instead. */
    @Deprecated
    public static final String USER_COOKIE = AuthenticationFilter.USER_COOKIE;

    /** @deprecated Use {@link AuthenticationFilter.Static#logIn} instead. */
    @Deprecated
    public static void logIn(HttpServletRequest request, HttpServletResponse response, ToolUser user) {
        AuthenticationFilter.Static.logIn(request, response, user);
    }

    /** @deprecated Use {@link AuthenticationFilter.Static#logOut} instead. */
    @Deprecated
    public static void logOut(HttpServletResponse response) {
        AuthenticationFilter.Static.logOut(response);
    }

    /** @deprecated Use {@link AuthenticationFilter.Static#getUser} instead. */
    @Deprecated
    public static ToolUser getUser(HttpServletRequest request) {
        return AuthenticationFilter.Static.getUser(request);
    }

    /** @deprecated Use {@link AuthenticationFilter.Static#getUserSetting} instead. */
    @Deprecated
    public static Object getUserSetting(HttpServletRequest request, String key) {
        return AuthenticationFilter.Static.getUserSetting(request, key);
    }

    /** @deprecated Use {@link AuthenticationFilter.Static#putUserSetting} instead. */
    @Deprecated
    public static void putUserSetting(HttpServletRequest request, String key, Object value) {
        AuthenticationFilter.Static.putUserSetting(request, key, value);
    }

    /** @deprecated Use {@link AuthenticationFilter.Static#getPageSetting} instead. */
    @Deprecated
    public static Object getPageSetting(HttpServletRequest request, String key) {
        return AuthenticationFilter.Static.getPageSetting(request, key);
    }

    /** @deprecated Use {@link AuthenticationFilter.Static#putPageSetting} instead. */
    @Deprecated
    public static void putPageSetting(HttpServletRequest request, String key, Object value) {
        AuthenticationFilter.Static.putPageSetting(request, key, value);
    }

    // --- AbstractFilter support ---

    @Override
    protected Iterable<Class<? extends Filter>> dependencies() {
        List<Class<? extends Filter>> dependencies = new ArrayList<Class<? extends Filter>>();
        dependencies.add(RemoteWidgetFilter.class);
        dependencies.add(AuthenticationFilter.class);
        return dependencies;
    }

    /** @deprecated Use {@link AuthenticationFilter.Static} instead. */
    @Deprecated
    public static final class Static {

        private Static() {
        }

        /** @deprecated Use {@link AuthenticationFilter.Static#getUser} instead. */
        @Deprecated
        public static boolean isAuthenticated(HttpServletRequest request) {
            return AuthenticationFilter.Static.getUser(request) != null;
        }
    }
}
