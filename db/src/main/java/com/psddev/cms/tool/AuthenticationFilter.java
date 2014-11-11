package com.psddev.cms.tool;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.psddev.cms.db.ToolUser;
import com.psddev.dari.db.Database;
import com.psddev.dari.db.ForwardingDatabase;
import com.psddev.dari.db.Query;
import com.psddev.dari.util.AbstractFilter;
import com.psddev.dari.util.DebugFilter;
import com.psddev.dari.util.JspUtils;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.Settings;
import sun.rmi.runtime.Log;

public class AuthenticationFilter extends AbstractFilter {

    private static final String ATTRIBUTE_PREFIX = AuthenticationFilter.class.getName() + ".";

    public static final String AUTHENTICATED_ATTRIBUTE = ATTRIBUTE_PREFIX + "authenticated";
    public static final String DATABASE_OVERRIDDEN_ATTRIBUTE = ATTRIBUTE_PREFIX + "databaseOverridden";
    public static final String USER_ATTRIBUTE = ATTRIBUTE_PREFIX + "user";
    public static final String USER_TOKEN = ATTRIBUTE_PREFIX + "token";
    public static final String USER_CHECKED_ATTRIBUTE = ATTRIBUTE_PREFIX + "userChecked";
    public static final String USER_SETTINGS_CHANGED_ATTRIBUTE = ATTRIBUTE_PREFIX + "userSettingsChanged";

    public static final String LOG_IN_PATH = "/logIn.jsp";
    public static final String RETURN_PATH_PARAMETER = "returnPath";
    public static final String USER_COOKIE = "cmsToolUser";

    // --- AbstractFilter support ---

    @Override
    protected void doRequest(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain)
            throws Exception {

        try {
            chain.doFilter(request, response);

        } catch (Exception error) {
            if (Static.isAuthenticated(request)) {
                DebugFilter.Static.writeError(request, response, error);

            } else {
                throw error;
            }

        } finally {
            if (Boolean.TRUE.equals(request.getAttribute(DATABASE_OVERRIDDEN_ATTRIBUTE))) {
                Database.Static.setIgnoreReadConnection(false);
                Database.Static.restoreDefault();
            }

            ToolUser user = Static.getUser(request);

            if (user != null && Boolean.TRUE.equals(request.getAttribute(USER_SETTINGS_CHANGED_ATTRIBUTE))) {
                user.save();
            }
        }
    }

    /** {@link AuthenticationFilter} utility methods. */
    public static final class Static {

        private Static() {
        }

        /** Logs in the given tool {@code user}. */
        public static void logIn(HttpServletRequest request, HttpServletResponse response, ToolUser user) {
            String token = (String) request.getAttribute(USER_TOKEN);
            if (token == null) {
                token = user.generateLoginToken();
            } else {
                user.refreshLoginToken(token);
            }

            Cookie cookie = new Cookie(USER_COOKIE, token);

            cookie.setPath("/");
            cookie.setSecure(JspUtils.isSecure(request));
            JspUtils.setSignedCookie(response, cookie);

            request.setAttribute(USER_ATTRIBUTE, user);
            request.setAttribute(USER_TOKEN, token);
            request.setAttribute(USER_CHECKED_ATTRIBUTE, Boolean.TRUE);
        }

        /** Logs out the current tool user. */
        public static void logOut(HttpServletRequest request, HttpServletResponse response) {
            if (request != null) {
                ToolUser user = getUser(request);
                String token = (String) request.getAttribute(USER_TOKEN);
                user.removeLoginToken(token);
            }

            Cookie cookie = new Cookie(USER_COOKIE, null);

            cookie.setMaxAge(0);
            cookie.setPath("/");

            response.addCookie(cookie);
        }

        @Deprecated
        public static void logOut(HttpServletResponse response) {
            logOut(null, response);
        }

        /**
         * Returns {@code true} if a tool user is authenticated in the given
         * {@code request}.
         *
         * @param request Can't be {@code null}.
         */
        public static boolean isAuthenticated(HttpServletRequest request) {
            return Boolean.TRUE.equals(request.getAttribute(AUTHENTICATED_ATTRIBUTE));
        }

        public static boolean requireUser(ServletContext context, HttpServletRequest request, HttpServletResponse response) throws IOException {
            ToolUser user = getUser(request);

            if (user != null) {
                request.setAttribute(AUTHENTICATED_ATTRIBUTE, Boolean.TRUE);

                logIn(request, response, user);

                ForwardingDatabase db = new ForwardingDatabase() {
                    @Override
                    protected <T> Query<T> filterQuery(Query<T> query) {
                        return query.clone().master().resolveInvisible();
                    }
                };

                db.setDelegate(Database.Static.getDefault());
                Database.Static.setIgnoreReadConnection(true);
                Database.Static.overrideDefault(db);
                request.setAttribute(DATABASE_OVERRIDDEN_ATTRIBUTE, Boolean.TRUE);

            } else if (!JspUtils.getEmbeddedServletPath(context, request.getServletPath()).equals(LOG_IN_PATH)) {
                @SuppressWarnings("resource")
                ToolPageContext page = new ToolPageContext(context, request, response);
                String loginUrl = page.cmsUrl(LOG_IN_PATH, RETURN_PATH_PARAMETER, JspUtils.getAbsolutePath(request, ""));

                response.sendRedirect(loginUrl);

                return true;
            }

            return false;
        }

        /** Returns the tool user associated with the given {@code request}. */
        public static ToolUser getUser(HttpServletRequest request) {
            ToolUser user;

            if (Boolean.TRUE.equals(request.getAttribute(USER_CHECKED_ATTRIBUTE))) {
                user = (ToolUser) request.getAttribute(USER_ATTRIBUTE);

            } else {
                long sessionTimeout = Settings.getOrDefault(long.class, "cms/tool/sessionTimeout", 0L);
                String token = ObjectUtils.to(String.class, JspUtils.getSignedCookieWithExpiry(request, USER_COOKIE, sessionTimeout));
                user = ToolUser.Static.getByToken(token);

                request.setAttribute(USER_ATTRIBUTE, user);
                request.setAttribute(USER_TOKEN, token);
                request.setAttribute(USER_CHECKED_ATTRIBUTE, Boolean.TRUE);
            }

            return user;
        }

        /**
         * Returns the user setting value associated with the given
         * {@code key}.
         */
        public static Object getUserSetting(HttpServletRequest request, String key) {
            ToolUser user = getUser(request);

            return user != null ? user.getSettings().get(key) : null;
        }

        /**
         * Puts the given user setting {@code value} at the given {@code key}.
         * The user, along with the setting values, are saved once at the end
         * of the given {@code request}.
         */
        public static void putUserSetting(HttpServletRequest request, String key, Object value) {
            ToolUser user = getUser(request);

            if (user != null) {
                user.getSettings().put(key, value);
                request.setAttribute(USER_SETTINGS_CHANGED_ATTRIBUTE, Boolean.TRUE);
            }
        }

        // Returns the page setting key for use with the given {@code request}
        // and {@code key}.
        private static String getPageSettingKey(HttpServletRequest request, String key) {
            return "page" + request.getServletPath() + "/" + key;
        }

        /**
         * Returns the page setting value associated with the given
         * {@code request} and {@code key}.
         */
        public static Object getPageSetting(HttpServletRequest request, String key) {
            return getUserSetting(request, getPageSettingKey(request, key));
        }

        /**
         * Puts the given page setting {@code value} at the given
         * {@code request} and {@code key}. The user, along with the setting
         * values, are saved once at the end of the given {@code request}.
         */
        public static void putPageSetting(HttpServletRequest request, String key, Object value) {
            putUserSetting(request, getPageSettingKey(request, key), value);
        }
    }
}
