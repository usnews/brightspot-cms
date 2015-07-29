package com.psddev.cms.tool.page;

import com.psddev.dari.db.Database;
import org.atmosphere.cache.UUIDBroadcasterCache;
import org.atmosphere.client.TrackMessageSizeInterceptor;
import org.atmosphere.cpr.AtmosphereFramework;
import org.atmosphere.cpr.AtmosphereFrameworkInitializer;
import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResponse;
import org.atmosphere.interceptor.AtmosphereResourceLifecycleInterceptor;
import org.atmosphere.interceptor.BroadcastOnPostAtmosphereInterceptor;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;

/**
 * Filter that handles the real-time communication between the server
 * and the clients.
 *
 * @since 3.1
 */
public class RtcFilter implements Filter {

    public static final String PATH = "/_rtc";

    private static final String CURRENT_USER_ID_ATTRIBUTE = RtcFilter.class.getName() + ".currentUserId";

    private AtmosphereFrameworkInitializer initializer;
    private RtcObjectUpdateNotifier notifier;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        ServletConfig servletConfig = new ServletConfig() {

            @Override
            public String getServletName() {
                return filterConfig.getFilterName();
            }

            @Override
            public ServletContext getServletContext() {
                return filterConfig.getServletContext();
            }

            @Override
            public String getInitParameter(String name) {
                return filterConfig.getInitParameter(name);
            }

            @Override
            public Enumeration<String> getInitParameterNames() {
                return filterConfig.getInitParameterNames();
            }
        };

        initializer = new AtmosphereFrameworkInitializer(false, true);

        initializer.configureFramework(servletConfig);

        AtmosphereFramework framework = initializer.framework();

        framework.setBroadcasterCacheClassName(UUIDBroadcasterCache.class.getName());
        framework.addAtmosphereHandler(
                PATH,
                new RtcHandler(framework.sessionFactory()),
                Arrays.asList(
                        new AtmosphereResourceLifecycleInterceptor(),
                        new BroadcastOnPostAtmosphereInterceptor(),
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
                initializer.destroy();

            } finally {
                initializer = null;
            }
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        initializer.framework().doCometSupport(
                AtmosphereRequest.wrap((HttpServletRequest) request),
                AtmosphereResponse.wrap((HttpServletResponse) response));
    }
}
