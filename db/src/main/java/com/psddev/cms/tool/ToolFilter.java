package com.psddev.cms.tool;

import com.psddev.cms.db.ToolUser;

import com.psddev.dari.db.Database;
import com.psddev.dari.db.ObjectType;
import com.psddev.dari.db.Query;
import com.psddev.dari.db.State;
import com.psddev.dari.util.AbstractFilter;
import com.psddev.dari.util.JspUtils;
import com.psddev.dari.util.MultipartRequestFilter;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.Settings;
import com.psddev.dari.util.StringUtils;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ToolFilter extends AbstractFilter {

    private static final String ATTRIBUTE_PREFIX = ToolFilter.class.getName() + ".";

    public static final String AUTHENTICATED_ATTRIBUTE = ATTRIBUTE_PREFIX + "authenticated";
    public static final String IS_USER_SETTINGS_CHANGED_ATTRIBUTE = ATTRIBUTE_PREFIX + "isUserSettingsChanged";
    public static final String LOG_IN_PATH = "/logIn.jsp";
    public static final String RETURN_PATH_PARAMETER = "returnPath";
    public static final String USER_ATTRIBUTE = ATTRIBUTE_PREFIX + "user";
    public static final String USER_COOKIE = "cmsToolUser";

    /** Logs in the given tool {@code user}. */
    public static void logIn(HttpServletRequest request, HttpServletResponse response, ToolUser user) {
        Cookie cookie = new Cookie(USER_COOKIE, user.getId().toString());
        cookie.setPath("/");
        cookie.setSecure(JspUtils.isSecureRequest(request));
        JspUtils.setSignedCookie(response, cookie);
        request.setAttribute(USER_ATTRIBUTE, user);
    }

    /** Logs out the current tool user. */
    public static void logOut(HttpServletResponse response) {
        Cookie cookie = new Cookie(USER_COOKIE, null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    /** Returns the tool user associated with the given {@code request}. */
    public static ToolUser getUser(HttpServletRequest request) {
        Object user = request.getAttribute(USER_ATTRIBUTE);
        return user instanceof ToolUser ? (ToolUser) user : null;
    }

    /**
     * Returns the user setting value associated with the given {@code key}.
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
            request.setAttribute(IS_USER_SETTINGS_CHANGED_ATTRIBUTE, Boolean.TRUE);
        }
    }

    /**
     * Returns the page setting key for use with the given {@code request}
     * and {@code key}.
     */
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
     * Puts the given page setting {@code value} at the given {@code request}
     * and {@code key}. The user, along with the setting values, are saved
     * once at the end of the given {@code request}.
     */
    public static void putPageSetting(HttpServletRequest request, String key, Object value) {
        putUserSetting(request, getPageSettingKey(request, key), value);
    }

    // --- AbstractFilter support ---

    @Override
    protected Iterable<Class<? extends Filter>> dependencies() {
        List<Class<? extends Filter>> dependencies = new ArrayList<Class<? extends Filter>>();
        dependencies.add(com.psddev.dari.util.Utf8Filter.class);
        dependencies.add(MultipartRequestFilter.class);
        return dependencies;
    }

    /**
     * Creates an object that originates from the given {@code database}
     * based on the given {@code json} string.
     */
    private Object createObject(Database database, String json) {
        @SuppressWarnings("unchecked")
        Map<String, Object> jsonValues = (Map<String, Object>) ObjectUtils.fromJson(json);
        UUID id = ObjectUtils.to(UUID.class, jsonValues.remove("_id"));
        UUID typeId = ObjectUtils.to(UUID.class, jsonValues.remove("_typeId"));

        ObjectType type = database.getEnvironment().getTypeById(typeId);
        if (type == null) {
            throw new IllegalArgumentException(String.format(
                    "[%s] is not a valid type ID!", typeId));

        } else {
            Object object = type.createObject(id);
            State.getInstance(object).setValues(jsonValues);
            return object;
        }
    }

    @Override
    protected void doRequest(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        String path = request.getServletPath();
        String embeddedPath = JspUtils.getEmbeddedServletPath(getServletContext(), path);

        if (StringUtils.matches(path,
                ".*/(?:css|js|styles?|scripts?)/.*|" +
                ".*\\.(?:css|js|less|jpe?g|gif|png)")) {
            chain.doFilter(request, response);
            return;
        }

        try {

            // Bridge to JSP widgets from other tools.
            Boolean isUpdating = null;
            if (embeddedPath.startsWith(JspWidget.REMOTE_DISPLAY_API)) {
                isUpdating = Boolean.FALSE;
            } else if (embeddedPath.startsWith(JspWidget.REMOTE_UPDATE_API)) {
                isUpdating = Boolean.TRUE;
            }
            
            // Instruct compliant browsers not to display the page in a remote frame
            response.setHeader("X-Frame-Options", "SAMEORIGIN");

            ToolPageContext wp = new ToolPageContext(getServletContext(), request, response);
            if (isUpdating != null) {
                Database database = wp.getTool().getState().getDatabase();
                try {

                    ToolUser user = Query.findById(ToolUser.class, wp.uuidParam(RemoteWidget.USER_ID_PARAMETER));
                    if (user != null) {
                        logIn(request, response, user);
                    }

                    JspWidget widget = (JspWidget) createObject(database, wp.param(RemoteWidget.WIDGET_PARAMETER));
                    Object object = createObject(database, wp.param(RemoteWidget.OBJECT_PARAMETER));
                    response.setCharacterEncoding("UTF-8");
                    Writer writer = response.getWriter();
                    if (isUpdating) {
                        widget.update(wp, object);
                        response.setContentType("application/json");
                        writer.write(ObjectUtils.toJson(State.getInstance(object).getSimpleValues()));
                    } else {
                        writer.write(widget.display(wp, object));
                    }
                    return;

                } catch (IOException ex) {
                    throw (IOException) ex;
                } catch (ServletException ex) {
                    throw (ServletException) ex;
                } catch (RuntimeException ex) {
                    throw (RuntimeException) ex;
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }

            if (wp.getArea() == null) {
                chain.doFilter(request, response);
                return;
            }

            // Authenticate the tool user.
            ToolUser user = Query.findById(ToolUser.class, ObjectUtils.to(
                    UUID.class, JspUtils.getSignedCookieWithExpiry(
                            request, USER_COOKIE, Settings.get(
                                    long.class, "cms/tool/sessionTimeout", 0L))));

            if (user instanceof ToolUser) {
                logIn(request, response, user);
                request.setAttribute(AUTHENTICATED_ATTRIBUTE, Boolean.TRUE);
                try {
                    Database.Static.setIgnoreReadConnection(true);
                    chain.doFilter(request, response);
                } finally {
                    Database.Static.setIgnoreReadConnection(false);
                }
                return;
            }

            if (JspUtils.getEmbeddedServletPath(getServletContext(), path).equals(LOG_IN_PATH)) {
                chain.doFilter(request, response);
                return;
            }

            response.sendRedirect(response.encodeRedirectURL(
                    wp.cmsUrl(LOG_IN_PATH,
                            RETURN_PATH_PARAMETER, JspUtils.getAbsolutePath(request, ""))));

        } finally {
            ToolUser user = getUser(request);
            if (user != null && Boolean.TRUE.equals(request.getAttribute(IS_USER_SETTINGS_CHANGED_ATTRIBUTE))) {
                user.save();
            }
        }
    }

    /** {@link ToolFilter} utility methods. */
    public static final class Static {

        private Static() {
        }

        /**
         * Returns {@code true} if the tool user is authenticated in
         * the given {@code request}.
         */
        public static boolean isAuthenticated(HttpServletRequest request) {
            return Boolean.TRUE.equals(request.getAttribute(AUTHENTICATED_ATTRIBUTE));
        }
    }
}
