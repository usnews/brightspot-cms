package com.psddev.cms.tool.page;

import com.google.common.base.Preconditions;
import com.psddev.cms.db.ToolUser;
import com.psddev.dari.db.Database;
import com.psddev.dari.db.Query;
import org.atmosphere.cache.UUIDBroadcasterCache;
import org.atmosphere.client.TrackMessageSizeInterceptor;
import org.atmosphere.container.JSR356AsyncSupport;
import org.atmosphere.cpr.AtmosphereFramework;
import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResponse;
import org.atmosphere.interceptor.AtmosphereResourceLifecycleInterceptor;
import org.atmosphere.interceptor.BroadcastOnPostAtmosphereInterceptor;
import org.atmosphere.interceptor.HeartbeatInterceptor;
import org.atmosphere.interceptor.SuspendTrackerInterceptor;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

/**
 * Endpoint that handles the real-time communication between the server
 * and the clients.
 *
 * @since 3.1
 */
@WebServlet(urlPatterns = { RtcServlet.PATH }, asyncSupported = true)
public class RtcServlet extends HttpServlet {

    public static final String PATH = "/_rtc";

    private static final String CURRENT_USER_ID_ATTRIBUTE = RtcServlet.class.getName() + ".currentUserId";

    private AtmosphereFramework framework;
    private RtcObjectUpdateNotifier notifier;

    /**
     * Returns the unique ID of the current user associated with the given
     * {@code resource}.
     *
     * @param resource
     *        Can't be {@code null}.
     *
     * @return Never {@code null}.
     *
     * @throws IllegalStateException
     *         If there's no user associated with the given {@code resource}.
     */
    public static UUID getCurrentUserId(AtmosphereResource resource) {
        Preconditions.checkNotNull(resource);

        AtmosphereRequest request = resource.getRequest();
        UUID currentUserId = (UUID) request.getAttribute(CURRENT_USER_ID_ATTRIBUTE);

        if (currentUserId == null) {
            String cookie = request.getHeader("Cookie");

            if (cookie != null) {
                for (String pair : cookie.trim().split("\\s*;\\s*")) {
                    int equalAt = pair.indexOf('=');

                    if (equalAt < 0) {
                        continue;
                    }

                    String name = pair.substring(0, equalAt);
                    String value = pair.substring(equalAt + 1);

                    if (!name.equals("bsp.tu")) {
                        continue;
                    }

                    value = value.substring(name.length());
                    int pipeAt = value.indexOf('|');

                    if (pipeAt < 0) {
                        continue;
                    }

                    ToolUser user = Query
                            .from(ToolUser.class)
                            .where("loginTokens/token = ?", value.substring(0, pipeAt))
                            .first();

                    if (user != null) {
                        currentUserId = user.getId();
                    }
                }
            }

            if (currentUserId == null) {
                throw new IllegalStateException();
            }
        }

        return currentUserId;
    }

    /**
     * Sets the unique ID of the current user associated with the given
     * {@code resource} to the given {@code currentUserId}.
     *
     * @param resource
     *        Can't be {@code null}.
     *
     * @param currentUserId
     *        May be {@code null}.
     */
    public static void setCurrentUserId(AtmosphereResource resource, UUID currentUserId) {
        Preconditions.checkNotNull(resource);

        resource.getRequest().setAttribute(CURRENT_USER_ID_ATTRIBUTE, currentUserId);
    }

    @Override
    public void init() throws ServletException {
        framework = new AtmosphereFramework();

        framework.init(getServletConfig());
        framework.setAsyncSupport(new JSR356AsyncSupport(framework.getAtmosphereConfig()));
        framework.setBroadcasterCacheClassName(UUIDBroadcasterCache.class.getName());
        framework.addAtmosphereHandler(
                PATH,
                new RtcHandler(),
                Arrays.asList(
                        new AtmosphereResourceLifecycleInterceptor(),
                        new BroadcastOnPostAtmosphereInterceptor(),
                        new HeartbeatInterceptor(),
                        new SuspendTrackerInterceptor(),
                        new TrackMessageSizeInterceptor()
                )
        );

        notifier = new RtcObjectUpdateNotifier(framework.getBroadcasterFactory().lookup(PATH));

        Database.Static.getDefault().addUpdateNotifier(notifier);
    }

    @Override
    public void destroy() {
        try {
            if (notifier != null) {
                try {
                    Database.Static.getDefault().removeUpdateNotifier(notifier);

                } finally {
                    notifier = null;
                }
            }

        } finally {
            try {
                framework.destroy();

            } finally {
                framework = null;
            }
        }
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        framework.doCometSupport(AtmosphereRequest.wrap(request), AtmosphereResponse.wrap(response));
    }
}
