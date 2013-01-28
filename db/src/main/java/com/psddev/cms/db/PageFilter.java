package com.psddev.cms.db;

import com.psddev.cms.tool.AuthenticationFilter;
import com.psddev.cms.tool.CmsTool;
import com.psddev.cms.tool.RemoteWidgetFilter;

import com.psddev.dari.db.Application;
import com.psddev.dari.db.ApplicationFilter;
import com.psddev.dari.db.Database;
import com.psddev.dari.db.ObjectType;
import com.psddev.dari.db.Query;
import com.psddev.dari.db.Record;
import com.psddev.dari.db.Recordable;
import com.psddev.dari.db.State;
import com.psddev.dari.util.AbstractFilter;
import com.psddev.dari.util.CodeUtils;
import com.psddev.dari.util.DebugFilter;
import com.psddev.dari.util.HtmlFormatter;
import com.psddev.dari.util.HtmlWriter;
import com.psddev.dari.util.JspUtils;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.PageContextFilter;
import com.psddev.dari.util.Profiler;
import com.psddev.dari.util.PullThroughCache;
import com.psddev.dari.util.Settings;
import com.psddev.dari.util.StorageItem;
import com.psddev.dari.util.StringUtils;
import com.psddev.dari.util.TypeDefinition;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PageFilter extends AbstractFilter {

    public static final String WIREFRAME_PARAMETER = "_wireframe";

    private static final Logger LOGGER = LoggerFactory.getLogger(PageFilter.class);

    private static final String PARAMETER_PREFIX = "_cms.db.";

    public static final String DEBUG_PARAMETER = PARAMETER_PREFIX + "debug";
    public static final String OVERLAY_PARAMETER = PARAMETER_PREFIX + "overlay";
    public static final String PREVIEW_DATA_PARAMETER = PARAMETER_PREFIX + "previewData";
    public static final String PREVIEW_ID_PARAMETER = PARAMETER_PREFIX + "previewId";
    public static final String PREVIEW_TYPE_ID_PARAMETER = PARAMETER_PREFIX + "previewTypeId";
    public static final String PREVIEW_OBJECT_PARAMETER = "_previewObject";

    private static final String ATTRIBUTE_PREFIX = PageFilter.class.getName();
    private static final String FIXED_PATH_ATTRIBUTE = ATTRIBUTE_PREFIX + ".fixedPath";
    private static final String NEW_REQUEST_ATTRIBUTE = ATTRIBUTE_PREFIX + ".newRequest";
    private static final String PATH_ATTRIBUTE = ATTRIBUTE_PREFIX + ".path";
    private static final String PATH_MATCHES_ATTRIBUTE = ATTRIBUTE_PREFIX + ".matches";

    public static final String ABORTED_ATTRIBUTE = ATTRIBUTE_PREFIX + ".aborted";
    public static final String CURRENT_OBJECT_ATTRIBUTE = ATTRIBUTE_PREFIX + ".currentObject";
    public static final String CURRENT_SECTION_ATTRIBUTE = ATTRIBUTE_PREFIX + ".currentSection";
    public static final String MAIN_OBJECT_ATTRIBUTE = ATTRIBUTE_PREFIX + ".mainObject";
    public static final String MAIN_OBJECT_CHECKED_ATTRIBUTE = ATTRIBUTE_PREFIX + ".mainObjectChecked";
    public static final String PAGE_ATTRIBUTE = ATTRIBUTE_PREFIX + ".page";
    public static final String PAGE_CHECKED_ATTRIBUTE = ATTRIBUTE_PREFIX + ".pageChecked";
    public static final String PARENT_SECTIONS_ATTRIBUTE = ATTRIBUTE_PREFIX + ".parentSections";
    public static final String PROFILE_ATTRIBUTE = ATTRIBUTE_PREFIX + ".profile";
    public static final String PROFILE_CHECKED_ATTRIBUTE = ATTRIBUTE_PREFIX + ".profileChecked";
    public static final String RENDERED_OBJECTS_ATTRIBUTE = ATTRIBUTE_PREFIX + ".renderedObjects";
    public static final String SITE_ATTRIBUTE = ATTRIBUTE_PREFIX + ".site";
    public static final String SITE_CHECKED_ATTRIBUTE = ATTRIBUTE_PREFIX + ".siteChecked";
    public static final String SUBSTITUTIONS_ATTRIBUTE = ATTRIBUTE_PREFIX + ".substitutions";

    /**
     * Returns {@code true} if rendering the given {@code request} has
     * been aborted.
     */
    public static boolean isAborted(HttpServletRequest request) {
        return Boolean.TRUE.equals(request.getAttribute(ABORTED_ATTRIBUTE));
    }

    /** Aborts rendering the given {@code request}. */
    public static void abort(HttpServletRequest request) {
        request.setAttribute(ABORTED_ATTRIBUTE, Boolean.TRUE);
    }

    /**
     * Returns the object currently being rendered in the given
     * {@code request}.
     */
    public static Object getCurrentObject(HttpServletRequest request) {
        return request.getAttribute(CURRENT_OBJECT_ATTRIBUTE);
    }

    /**
     * Sets the object currently being rendered in the given
     * {@code request}.
     */
    public static void setCurrentObject(HttpServletRequest request, Object object) {
        request.setAttribute(CURRENT_OBJECT_ATTRIBUTE, object);
        request.setAttribute("object", object);
        request.setAttribute("record", object);
        request.setAttribute("content", object);
    }

    /**
     * Returns the section currently being rendered in the given
     * {@code request}.
     */
    public static Section getCurrentSection(HttpServletRequest request) {
        return (Section) request.getAttribute(CURRENT_SECTION_ATTRIBUTE);
    }

    /**
     * Sets the section currently being rendered in the given
     * {@code request}.
     */
    protected static void setCurrentSection(HttpServletRequest request, Section section) {
        request.setAttribute(CURRENT_SECTION_ATTRIBUTE, section);
        request.setAttribute("section", section);
    }

    /**
     * Returns an unmodifiable list of all the parent sections used to render
     * the given {@code request} so far.
     */
    public static List<Section> getParentSections(HttpServletRequest request) {
        @SuppressWarnings("unchecked")
        List<Section> parents = (List<Section>) request.getAttribute(PARENT_SECTIONS_ATTRIBUTE);
        return parents != null ? Collections.unmodifiableList(parents) : Collections.<Section>emptyList();
    }

    /**
     * Adds the given {@code section} to the list of sections used to render
     * the given {@code request} so far.
     */
    protected static void addParentSection(HttpServletRequest request, Section section) {
        @SuppressWarnings("unchecked")
        List<Section> parents = (List<Section>) request.getAttribute(PARENT_SECTIONS_ATTRIBUTE);
        if (parents == null) {
            parents = new ArrayList<Section>();
            request.setAttribute(PARENT_SECTIONS_ATTRIBUTE, parents);
        }
        parents.add(section);

        @SuppressWarnings("unchecked")
        Map<String, Boolean> isInside = (Map<String, Boolean>) request.getAttribute("inside");
        if (isInside == null) {
            isInside = new HashMap<String, Boolean>();
            request.setAttribute("inside", isInside);
        }
        isInside.put(section.getName(), Boolean.TRUE);
    }

    /**
     * Removes the last parent section used to render the given
     * {@code request} so far.
     */
    @SuppressWarnings("unchecked")
    protected static void removeLastParentSection(HttpServletRequest request) {
        List<Section> parents = (List<Section>) request.getAttribute(PARENT_SECTIONS_ATTRIBUTE);
        Section section = parents.remove(parents.size() - 1);
        ((Map<String, Boolean>) request.getAttribute("inside")).remove(section.getName());
    }

    /**
     * Returns a modifiable set of all the objects used to render the given
     * {@code request}.
     */
    public static Set<Object> getRenderedObjects(HttpServletRequest request) {
        @SuppressWarnings("unchecked")
        Set<Object> rendered = (Set<Object>) request.getAttribute(RENDERED_OBJECTS_ATTRIBUTE);
        if (rendered == null) {
            rendered = new LinkedHashSet<Object>();
            request.setAttribute(RENDERED_OBJECTS_ATTRIBUTE, rendered);
        }
        return rendered;
    }

    /**
     * Returns the map of object substitutions for the given
     * {@code request}.
     */
    public static Map<UUID, Object> getSubstitutions(HttpServletRequest request) {
        @SuppressWarnings("unchecked")
        Map<UUID, Object> substitutions = (Map<UUID, Object>) request.getAttribute(SUBSTITUTIONS_ATTRIBUTE);
        if (substitutions == null) {
            substitutions = new HashMap<UUID, Object>();
            request.setAttribute(SUBSTITUTIONS_ATTRIBUTE, substitutions);
        }
        return substitutions;
    }

    // --- AbstractFilter support ---

    @Override
    public Iterable<Class<? extends Filter>> dependencies() {
        List<Class<? extends Filter>> dependencies = new ArrayList<Class<? extends Filter>>();
        dependencies.add(ApplicationFilter.class);
        dependencies.add(RemoteWidgetFilter.class);
        dependencies.add(AuthenticationFilter.class);
        dependencies.add(com.psddev.cms.tool.ScheduleFilter.class);
        dependencies.add(com.psddev.dari.util.FrameFilter.class);
        dependencies.add(com.psddev.dari.util.RoutingFilter.class);
        dependencies.add(FieldAccessFilter.class);
        return dependencies;
    }

    private static boolean redirectIfFixedPath(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = (String) request.getAttribute(FIXED_PATH_ATTRIBUTE);
        if (path == null) {
            return false;
        } else {
            JspUtils.redirectPermanently(request, response, path);
            return true;
        }
    }

    @Override
    protected void doError(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        doForward(request, response, chain);
    }

    @Override
    protected void doForward(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        request.removeAttribute(MAIN_OBJECT_CHECKED_ATTRIBUTE);
        request.removeAttribute(PAGE_CHECKED_ATTRIBUTE);
        request.removeAttribute(PROFILE_CHECKED_ATTRIBUTE);
        request.removeAttribute(SITE_CHECKED_ATTRIBUTE);

        doRequest(request, response, chain);
    }

    @Override
    protected void doRequest(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        Profile profile = Static.getProfile(request);
        Variation.Static.applyAll(TypeDefinition.getInstance(Record.class).newInstance(), profile);

        if (redirectIfFixedPath(request, response)) {
            return;
        }

        VaryingDatabase varying = new VaryingDatabase();
        varying.setDelegate(Database.Static.getDefault());
        varying.setRequest(request);
        varying.setProfile(profile);
        Database.Static.overrideDefault(varying);

        try {
            String servletPath = request.getServletPath();

            // Serve a special robots.txt file for non-production.
            if (servletPath.equals("/robots.txt") && !Settings.isProduction()) {
                response.setContentType("text/plain");
                PrintWriter writer = response.getWriter();
                writer.println("User-agent: *");
                writer.println("Disallow: /");
                return;

            // Render a single section.
            } else if (servletPath.startsWith("/_render")) {
                UUID sectionId = ObjectUtils.to(UUID.class, request.getParameter("_sectionId"));
                Section section = Query.findById(Section.class, sectionId);
                if (section != null) {
                    writeSection(request, response, response.getWriter(), section);
                }
                return;

            // Strip the special directory suffix.
            } else if (servletPath.endsWith("/index")) {
                JspUtils.redirectPermanently(request, response, servletPath.substring(0, servletPath.length() - 5));
                return;
            }

            // Global prefix?
            String prefix = Settings.get(String.class, "cms/db/directoryPrefix");
            if (!ObjectUtils.isBlank(prefix)) {
                Static.setPath(request, StringUtils.ensureEnd(prefix, "/") + servletPath);
            }

            Site site = Static.getSite(request);
            if (redirectIfFixedPath(request, response)) {
                return;
            }

            Object mainObject = Static.getMainObject(request);
            if (redirectIfFixedPath(request, response)) {
                return;
            } else {
                HttpServletRequest newRequest = (HttpServletRequest) request.getAttribute(NEW_REQUEST_ATTRIBUTE);
                if (newRequest != null) {
                    request = newRequest;
                }
            }

            // Not handled by the CMS.
            if (mainObject == null) {
                chain.doFilter(request, response);
                return;
            }

            // If mainObject has a redirect path AND a permalink and the
            // current request is the redirect path, then redirect to the
            // permalink.
            String path = Static.getPath(request);
            Directory.Path redirectPath = null;
            boolean isRedirect = false;
            for (Directory.Path p : State.getInstance(mainObject).as(Directory.ObjectModification.class).getPaths()) {
                if (p.getType() == Directory.PathType.REDIRECT && path.equals(p.getPath())) {
                    isRedirect = true;
                } else if (p.getType() == Directory.PathType.PERMALINK) {
                    redirectPath = p;
                }
            }

            if (isRedirect && redirectPath != null) {
                JspUtils.redirectPermanently(request, response, site != null ?
                        site.getPrimaryUrl() + redirectPath.getPath() :
                        redirectPath.getPath());
                return;
            }

            Page page = Static.getPage(request);

            if (page == null) {
                State state = State.getInstance(mainObject);
                ObjectType type = state.getType();
                String script = type.as(Renderer.TypeModification.class).getScript();

                if (!ObjectUtils.isBlank(script)) {
                    page = Application.Static.getInstance(CmsTool.class).getModulePreviewTemplate();
                }

                if (page == null) {
                    if (Settings.isProduction()) {
                        chain.doFilter(request, response);
                        return;

                    } else {
                        throw new IllegalStateException(String.format(
                                "No template for [%s] [%s]! (ID: %s)",
                                mainObject.getClass().getName(),
                                state.getLabel(),
                                state.getId().toString().replaceAll("-", "")));
                    }
                }
            }

            // Set up a profile.
            PrintWriter writer = response.getWriter();
            debugObject(request, writer, "Main object is", mainObject);

            Map<String, Object> seo = new HashMap<String, Object>();
            seo.put("title", Seo.Static.findTitle(mainObject));
            seo.put("description", Seo.Static.findDescription(mainObject));
            Set<String> keywords = Seo.Static.findKeywords(mainObject);
            if (keywords != null) {
                seo.put("keywords", keywords);
                seo.put("keywordsString", keywords.toString());
            }
            request.setAttribute("seo", seo);

            // Try to set the right content type based on the extension.
            String contentType = URLConnection.getFileNameMap().getContentTypeFor(servletPath);
            response.setContentType((ObjectUtils.isBlank(contentType) ? "text/html" : contentType) + ";charset=UTF-8");

            // Render the page.
            if (Boolean.parseBoolean(request.getParameter(OVERLAY_PARAMETER))) {
                writer.write("<span class=\"cms-mainObject\" style=\"display: none;\">");
                Map<String, String> map = new HashMap<String, String>();
                State state = State.getInstance(mainObject);
                map.put("id", state.getId().toString());
                map.put("label", state.getLabel());
                map.put("typeLabel", state.getType().getLabel());
                writer.write(ObjectUtils.toJson(map));
                writer.write("</span>");
            }

            HtmlWriter html = new HtmlWriter(writer);
            html.putAllStandardDefaults();
            html.putOverride(Recordable.class, new RecordableFormatter());

            boolean wireframe = isWireframe(request);
            String id = null;

            if (wireframe) {
                id = writeWireframeWrapperBegin(request, html);
            }

            beginPage(request, response, html, page);

            if (!response.isCommitted()) {
                request.getSession();
            }

            String rendererPath = page.getRendererPath();

            if (ObjectUtils.isBlank(rendererPath)) {
                rendererPath = page.as(Renderer.TypeModification.class).getScript();
            }

            if (!ObjectUtils.isBlank(rendererPath)) {
                JspUtils.include(request, response, writer, StringUtils.ensureStart(rendererPath, "/"));

            } else {
                Page.Layout layout = page.getLayout();

                if (layout != null) {
                    renderSection(request, response, html, layout.getOutermostSection());
                }
            }

            endPage(request, response, html, page);

            if (wireframe) {
                writeWireframeWrapperEnd(request, html, id);
            }

        } finally {
            Database.Static.restoreDefault();
        }
    }

    /** Renders the beginning of the given {@code page}. */
    protected static void beginPage(
            HttpServletRequest request,
            HttpServletResponse response,
            Writer writer,
            Page page)
            throws IOException, ServletException {

        debugObject(request, writer, "Beginning page", page);
    }

    /** Renders the end of the given {@code page}. */
    protected static void endPage(
            HttpServletRequest request,
            HttpServletResponse response,
            Writer writer,
            Page page)
            throws IOException, ServletException {

        debugObject(request, writer, "Ending page", page);
    }

    /** Renders the given {@code section}. */
    public static void renderSection(
            HttpServletRequest request,
            HttpServletResponse response,
            Writer writer,
            Section section)
            throws IOException, ServletException {

        if (isAborted(request)) {
            return;
        }

        try {
            Profiler.Static.startThreadEvent("Render", section);

            if (section != null) {
                Object substitution = getSubstitutions(request).get(section.getId());
                if (substitution != null) {
                    if (substitution instanceof Section) {
                        section = (Section) substitution;
                    } else {
                        ContentSection substitutionSection = new ContentSection();
                        substitutionSection.setContent(substitution);
                        section = substitutionSection;
                    }
                }
            }

            long cacheDuration = section != null ? section.getCacheDuration() : 0;
            if (cacheDuration > 0) {
                SectionCacheKey key = new SectionCacheKey();
                key.sectionId = section.getId();
                key.cacheDuration = cacheDuration;
                key.request = request;
                key.response = response;
                key.section = section;
                writer.write(SECTION_CACHE.get(key));

            } else {
                Section previousSection = getCurrentSection(request);
                try {
                    setCurrentSection(request, section);
                    writeSection(request, response, writer, section);
                } finally {
                    setCurrentSection(request, previousSection);
                }
            }

        } finally {
            Profiler.Static.stopThreadEvent();
        }
    }

    /**
     * Processes and writes the given {@code section} to the given
     * {@code writer}.
     */
    @SuppressWarnings("all")
    private static void writeSection(
            final HttpServletRequest request,
            final HttpServletResponse response,
            Writer writer,
            Section section)
            throws IOException, ServletException {

        boolean wireframe = isWireframe(request);
        boolean containerRendered = false;

        if (wireframe) {
            writeWireframeSectionBegin(writer, section);
        }

        // Container section - begin, child sections, then end.
        if (section instanceof ContainerSection) {
            ContainerSection container = (ContainerSection) section;

            if (ObjectUtils.isBlank(container.getRendererPath())) {
                containerRendered = true;
                List<Section> children = container.getChildren();

                try {
                    addParentSection(request, container);
                    beginContainer(request, response, writer, container);

                    for (Section child : children) {
                        renderSection(request, response, writer, child);
                        if (isAborted(request)) {
                            return;
                        }
                    }

                    endContainer(request, response, writer, container);

                } finally {
                    removeLastParentSection(request);
                }
            }
        }

        // Script section may be associated with an object.
        if (!containerRendered &&
                section instanceof ScriptSection) {
            Object object;
            if (section instanceof MainSection) {
                object = getMainObject(request);
            } else if (section instanceof ContentSection) {
                object = ((ContentSection) section).getContent();
            } else {
                object = null;
            }
            renderObjectWithSection(request, response, writer, object, (ScriptSection) section);
        }

        if (wireframe) {
            writeWireframeSectionEnd(writer, section);
        }
    }

    private static boolean isWireframe(HttpServletRequest request) {
        return !Settings.isProduction() &&
                ObjectUtils.to(boolean.class, request.getParameter(WIREFRAME_PARAMETER));
    }

    private static String writeWireframeWrapperBegin(HttpServletRequest request, Writer writer) throws IOException {
        HtmlWriter html = (HtmlWriter) writer;
        String id = JspUtils.createId(request);

        html.start("div", "id", id);

        return id;
    }

    private static void writeWireframeWrapperEnd(HttpServletRequest request, Writer writer, String id) throws IOException {
        HtmlWriter html = (HtmlWriter) writer;

        html.end();

        html.start("script", "type", "text/javascript");
            html.write("(function() {");
                html.write("var f = document.createElement('iframe');");
                html.write("f.frameBorder = '0';");
                html.write("var fs = f.style;");
                html.write("fs.background = 'transparent';");
                html.write("fs.border = 'none';");
                html.write("fs.overflow = 'hidden';");
                html.write("fs.width = '100%';");
                html.write("f.src = '");
                html.write(JspUtils.getAbsolutePath(request, "/_resource/cms/section.html", "id", id));
                html.write("';");
                html.write("var a = document.getElementById('");
                html.write(id);
                html.write("');");
                html.write("a.parentNode.insertBefore(f, a.nextSibling);");
            html.write("})();");
        html.end();
    }

    private static void writeWireframeSectionBegin(Writer writer, Section section) throws IOException {
        HtmlWriter html = (HtmlWriter) writer;
        String sectionName = section.getName();

        StringBuilder className = new StringBuilder();
        className.append("cms-section cms-section-transform");
        className.append((int) (Math.random() * 4));

        if (section instanceof ContainerSection) {
            className.append(" cms-section-container");

            if (section instanceof HorizontalContainerSection) {
                className.append(" cms-section-horizontal");
            }
        }

        html.start("div", "class", className);
        html.start("h2");

            if (ObjectUtils.isBlank(sectionName)) {
                html.html("Unnamed ");
                html.html(section.getState().getType().getLabel());

            } else {
                html.html(sectionName);
            }

        html.end();

        if (section instanceof HorizontalContainerSection) {
            html.start("div", "class", "cms-section-horizontal-table");
        }
    }

    private static void writeWireframeSectionEnd(Writer writer, Section section) throws IOException {
        HtmlWriter html = (HtmlWriter) writer;

        if (section instanceof HorizontalContainerSection) {
            html.end();
        }

        html.end();
    }

    private static void writeWireframeSection(HttpServletRequest request, HtmlWriter html, String script) throws Exception {
        if (!ObjectUtils.isBlank(script)) {
            html.start("p");
                html.html("Rendered using ");
                html.start("code").html(script).end();
                html.html(".");
            html.end();

        } else {
            Object object = getCurrentObject(request);

            if (object != null) {
                String className = object.getClass().getName();
                File source = CodeUtils.getSource(className);

                html.start("p", "class", "alert alert-error");
                    html.html("No renderer! Add ");
                    html.start("code").html("@Renderer.Script").end();
                    html.html(" to the ");

                    if (source == null) {
                        html.html(className);

                    } else {
                        html.start("a",
                                "href", DebugFilter.Static.getServletPath(request, "code", "file", source),
                                "target", "code");
                            html.html(className);
                        html.end();
                    }

                    html.html(" class.");
                html.end();

            } else {
                Page page = getPage(request);

                if (ObjectUtils.isBlank(script)) {
                    html.start("p", "class", "alert alert-error");
                        html.html("No renderer! Specify it in the ");
                        html.start("a",
                                "href", StringUtils.addQueryParameters("/cms/content/edit.jsp", "id", page.getId()),
                                "target", "cms");
                            html.html(page.getName());
                        html.end();
                        html.html(" ").html(page.getClass().getSimpleName().toLowerCase()).html(".");
                    html.end();
                }
            }
        }

        String classId = JspUtils.createId(request);
        Map<String, String> names = new TreeMap<String, String>();

        for (
                @SuppressWarnings("unchecked")
                Enumeration<String> e = request.getAttributeNames();
                e.hasMoreElements(); ) {
            String name = e.nextElement();
            if (!name.contains(".")) {
                names.put(name, JspUtils.createId(request));
            }
        }

        names.remove("mainObject");
        names.remove("mainRecord");
        names.remove("object");
        names.remove("record");

        html.start("form");
            html.start("select", "name", "name", "onchange", "$('." + classId + "').hide(); $('#' + $(this).find(':selected').data('jstl-id')).show();");
                html.start("option", "value", "").html("Available JSTL Expressions").end();
                for (Map.Entry<String, String> entry : names.entrySet()) {
                    String name = entry.getKey();
                    html.start("option", "value", name, "data-jstl-id", entry.getValue());
                        html.html("${").html(name).html("}");
                    html.end();
                }
            html.end();

            for (Map.Entry<String, String> entry : names.entrySet()) {
                String name = entry.getKey();
                Object value = request.getAttribute(name);

                html.start("div",
                        "class", classId,
                        "id", entry.getValue(),
                        "style", "display: none;");

                    html.start("h3").html(value.getClass().getName()).end();

                    html.start("dl");

                        if (value instanceof Map) {
                            for (Map.Entry<?, ?> entry2 : ((Map<?, ?>) value).entrySet()) {
                                html.start("dt").start("code").html("${").html(name).html("['").html(entry2.getKey()).html("']}").end().end();
                                html.start("dd").object(entry2.getValue()).end();
                            }

                        } else if (value instanceof List) {
                            List<?> valueList = (List<?>) value;

                            for (int i = 0, size = valueList.size(); i < size; ++ i) {
                                html.start("dt").start("code").html("${").html(name).html("[").html(i).html("]}").end().end();
                                html.start("dd").object(valueList.get(i)).end();
                            }

                        } else {
                            for (PropertyDescriptor propDesc : Introspector.getBeanInfo(value.getClass()).getPropertyDescriptors()) {
                                String getterName = propDesc.getName();
                                Method getterMethod = propDesc.getReadMethod();

                                if (getterMethod == null ||
                                        "class".equals(getterName) ||
                                        "state".equals(getterName) ||
                                        "modifications".equals(getterName) ||
                                        getterMethod.isAnnotationPresent(Deprecated.class)) {
                                    continue;
                                }

                                html.start("dt").start("code").html("${").html(name).html(".").html(getterName).html("}").end().end();
                                html.start("dd").object(getterMethod.invoke(value)).end();
                            }
                        }

                    html.end();

                html.end();
            }
        html.end();
    }

    /**
     * Key for {@link #SECTION_CACHE} that contains extra information
     * like the request object.
     */
    private static class SectionCacheKey {

        public UUID sectionId;
        public long cacheDuration;
        public HttpServletRequest request;
        public HttpServletResponse response;
        public Section section;

        @Override
        public boolean equals(Object other) {
            return this == other || (
                    other instanceof SectionCacheKey &&
                    sectionId.equals(((SectionCacheKey) other).sectionId));
        }

        @Override
        public int hashCode() {
            return sectionId.hashCode();
        }
    }

    /** Cache that contains output of each sections. */
    private static final PullThroughCache<SectionCacheKey, String>
            SECTION_CACHE = new PullThroughCache<SectionCacheKey, String>() {

        @Override
        protected boolean isExpired(SectionCacheKey key, Date lastProduced) {
            return System.currentTimeMillis() - lastProduced.getTime() > key.cacheDuration;
        }

        @Override
        protected String produce(SectionCacheKey key) throws IOException, ServletException {
            try {
                StringWriter writer = new StringWriter();
                writeSection(key.request, key.response, writer, key.section);
                return writer.toString();

            } finally {
                key.request = null;
                key.response = null;
                key.section = null;
            }
        }
    };

    /** Renders the given {@code object}. */
    public static void renderObject(
            HttpServletRequest request,
            HttpServletResponse response,
            Writer writer,
            Object object)
            throws IOException, ServletException {

        if (object instanceof Section) {
            renderSection(request, response, writer, (Section) object);

        } else {
            renderObjectWithSection(request, response, writer, object, null);
        }
    }

    /** Renders the given {@code object} using the given {@code section}. */
    @SuppressWarnings("all")
    private static void renderObjectWithSection(
            HttpServletRequest request,
            HttpServletResponse response,
            Writer writer,
            Object object,
            ScriptSection section)
            throws IOException, ServletException {

        String engine;
        String script;
        if (section != null) {
            engine = section.getEngine();
            script = section.getRendererPath();
        } else {
            engine = null;
            script = null;
        }

        if (object != null) {
            Object substitution = getSubstitutions(request).get(State.getInstance(object).getId());
            if (substitution != null) {
                object = substitution;
            }

            getRenderedObjects(request).add(object);

            // Engine not specified on section so fall back to the one
            // specified in the object type definition.
            if (ObjectUtils.isBlank(script)) {
                ObjectType type = State.getInstance(object).getType();
                if (type != null) {
                    Renderer.TypeModification typeRenderer = type.as(Renderer.TypeModification.class);
                    engine = typeRenderer.getEngine();
                    script = typeRenderer.getScript();
                }
            }
        }

        debugObject(request, writer, "Rendering", object);

        Object previousObject = getCurrentObject(request);
        boolean isOverlay = Boolean.parseBoolean(request.getParameter(OVERLAY_PARAMETER));
        try {

            setCurrentObject(request, object);
            if (isOverlay) {
                writer.write("<span class=\"cms-overlayBegin\" style=\"display: none;\">");
                Map<String, String> map = new HashMap<String, String>();

                if (section != null) {
                    map.put("sectionName", section.getName());
                }

                if (object != null) {
                    State state = State.getInstance(object);
                    map.put("id", state.getId().toString());
                    map.put("label", state.getLabel());
                    map.put("typeLabel", state.getType().getLabel());
                }

                writer.write(ObjectUtils.toJson(map));
                writer.write("</span>");
            }

            renderScript(request, response, writer, engine, script);

        } finally {
            setCurrentObject(request, previousObject);
            if (isOverlay) {
                writer.write("<span class=\"cms-overlayEnd\" style=\"display: none;\"></span>");
            }
        }
    }

    // Renders the given script using the given engine.
    private static void renderScript(
            HttpServletRequest request,
            HttpServletResponse response,
            Writer writer,
            String engine,
            String script)
            throws IOException, ServletException {

        long startTime = System.nanoTime();

        try {
            debugMessage(request, writer, "Engine is [%s]", engine);
            debugMessage(request, writer, "Script is [%s]", script);

            boolean wireframe = isWireframe(request);

            if (!wireframe) {
                if ("RawText".equals(engine)) {
                    writer.write(script);
                    return;

                } else if (!ObjectUtils.isBlank(script)) {
                    JspUtils.include(request, response, writer, StringUtils.ensureStart(script, "/"));
                    return;
                }

                if (Settings.isProduction()) {
                    return;
                }
            }

            Section section = getCurrentSection(request);

            if (!(section instanceof ScriptSection)) {
                return;
            }

            if (section instanceof ContainerSection) {
                return;
            }

            if (!(writer instanceof HtmlWriter)) {
                return;
            }

            HtmlWriter html = (HtmlWriter) writer;

            if (wireframe) {
                writeWireframeSection(request, html, script);

            } else {
                String id = writeWireframeWrapperBegin(request, writer);
                    writeWireframeSectionBegin(writer, section);
                        writeWireframeSection(request, html, script);
                    writeWireframeSectionEnd(writer, section);
                writeWireframeWrapperEnd(request, writer, id);
            }

        // Always catch the error so the page never looks broken
        // in production.
        } catch (Throwable ex) {
            if (Settings.isProduction()) {
                LOGGER.warn(String.format("Can't render [%s]!", script), ex);

            } else if (ex instanceof IOException) {
                throw (IOException) ex;
            } else if (ex instanceof ServletException) {
                throw (ServletException) ex;
            } else if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            } else if (ex instanceof Error) {
                throw (Error) ex;
            } else {
                throw new RuntimeException(ex);
            }

        } finally {
            debugMessage(request, writer,
                    "Rendering [%s: %s] took [%s] milliseconds",
                    engine,
                    script,
                    (System.nanoTime() - startTime) / 1000000.0);
        }
    }

    private static class RecordableFormatter implements HtmlFormatter<Recordable> {

        @Override
        public void format(HtmlWriter writer, Recordable recordable) throws IOException {
            State state = recordable.getState();
            String permalink = state.as(Directory.ObjectModification.class).getPermalink();
            ObjectType type = state.getType();
            StringBuilder label = new StringBuilder();

            if (type != null) {
                label.append(type.getLabel());
                label.append(": ");
            }
            label.append(state.getLabel());

            if (ObjectUtils.isBlank(permalink)) {
                writer.html(label);

            } else {
                writer.start("a",
                        "href", StringUtils.addQueryParameters(permalink, "_wireframe", true),
                        "target", "cms");
                    writer.html(label);
                writer.end();
            }

            if (!type.isEmbedded()) {
                writer.html(" - ");

                writer.start("a",
                        "href", StringUtils.addQueryParameters("/cms/content/edit.jsp", "id", state.getId()),
                        "target", "cms");
                    writer.html("Edit");
                writer.end();
            }
        }
    }

    /**
     * Writes the given debug {@code message} to both the log and the given
     * {@code response}.
     */
    private static void debugMessage(
            HttpServletRequest request,
            Writer writer,
            String message,
            Object... arguments) throws IOException {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format(message, arguments));
        }

        if (Boolean.parseBoolean(request.getParameter(DEBUG_PARAMETER))) {
            writer.write("<!--CMS: ");
            writer.write(String.format(message, arguments).replace("--", "- -"));
            writer.write("-->");
        }
    }

    /**
     * Writes the given debug {@code message} with a short description
     * about the given {@code object} to both the log and the given
     * {@code response}.
     */
    private static void debugObject(
            HttpServletRequest request,
            Writer writer,
            String message,
            Object object) throws IOException {

        if (object == null) {
            debugMessage(request, writer, message);

        } else {
            State state = State.getInstance(object);
            debugMessage(request, writer, "%s [%s #%s] [%s]",
                    message,
                    object.getClass().getSimpleName(),
                    state.getId(),
                    state.getLabel());
        }
    }

    // ---

    /** {@link PageFilter} utility methods. */
    public static final class Static {

        private Static() {
        }

        /** Returns the path used to find the main object. */
        public static String getPath(HttpServletRequest request) {
            getSite(request);
            String path = (String) request.getAttribute(PATH_ATTRIBUTE);
            return path != null ? path : request.getServletPath();
        }

        /** Sets the path used to find the main object. */
        public static void setPath(HttpServletRequest request, String path) {
            request.setAttribute(PATH_ATTRIBUTE, path);
        }

        private static void fixPath(HttpServletRequest request, String path) {
            request.setAttribute(FIXED_PATH_ATTRIBUTE, path);
        }

        /** Returns the site associated with the given {@code request}. */
        public static Site getSite(HttpServletRequest request) {
            if (Boolean.TRUE.equals(request.getAttribute(SITE_CHECKED_ATTRIBUTE))) {
                return (Site) request.getAttribute(SITE_ATTRIBUTE);
            }

            request.setAttribute(SITE_CHECKED_ATTRIBUTE, Boolean.TRUE);

            Site site = null;
            String servletPath = request.getServletPath();
            String absoluteUrl = JspUtils.getAbsoluteUrl(request, servletPath);
            Map.Entry<String, Site> entry = Site.Static.findByUrl(absoluteUrl);

            if (entry != null) {
                String path = absoluteUrl.substring(entry.getKey().length() - 1);
                if (path.length() == 0) {
                    fixPath(request, servletPath + "/");
                }

                site = Query.from(Site.class).where("_id = ?", entry.getValue()).first();
                setSite(request, site);
                setPath(request, path);
            }

            return site;
        }

        /** Sets the site associated with the given {@code request}. */
        public static void setSite(HttpServletRequest request, Site site) {
            request.setAttribute(SITE_CHECKED_ATTRIBUTE, Boolean.TRUE);
            request.setAttribute(SITE_ATTRIBUTE, site);
            request.setAttribute("site", site);
        }

        /** Returns the main object associated with the given {@code request}. */
        public static Object getMainObject(HttpServletRequest request) {
            if (Boolean.TRUE.equals(request.getAttribute(MAIN_OBJECT_CHECKED_ATTRIBUTE))) {
                return request.getAttribute(MAIN_OBJECT_ATTRIBUTE);
            }

            VaryingDatabase varying = new VaryingDatabase();
            varying.setDelegate(Database.Static.getDefault());
            varying.setRequest(request);
            varying.setProfile(getProfile(request));
            Database.Static.overrideDefault(varying);

            try {
                request.setAttribute(MAIN_OBJECT_CHECKED_ATTRIBUTE, Boolean.TRUE);

                Object mainObject = null;
                String servletPath = request.getServletPath();
                String path = getPath(request);
                Site site = getSite(request);

                // On preview request, manually create the main object based on
                // the post data.
                if (path.startsWith("/_preview")) {
                    UUID previewId = ObjectUtils.to(UUID.class, request.getParameter(PREVIEW_ID_PARAMETER));
                    if (previewId != null) {

                        String[] objectStrings = request.getParameterValues(PREVIEW_OBJECT_PARAMETER);
                        Map<UUID, Object> substitutions = getSubstitutions(request);
                        if (objectStrings != null) {
                            for (String objectString : objectStrings) {
                                if (!ObjectUtils.isBlank(objectString)) {
                                    @SuppressWarnings("unchecked")
                                    Map<String, Object> objectMap = (Map<String, Object>) ObjectUtils.fromJson(objectString.trim());
                                    ObjectType type = ObjectType.getInstance(ObjectUtils.to(UUID.class, objectMap.remove("_typeId")));
                                    if (type != null) {
                                        Object object = type.createObject(ObjectUtils.to(UUID.class, objectMap.remove("_id")));
                                        State objectState = State.getInstance(object);
                                        objectState.setValues(objectMap);
                                        substitutions.put(objectState.getId(), object);
                                    }
                                }
                            }
                        }

                        Object preview = Query.findById(Object.class, previewId);

                        if (preview instanceof Draft) {
                            mainObject = ((Draft) preview).getObject();

                        } else if (preview instanceof History) {
                            mainObject = ((History) preview).getObject();

                        } else if (preview instanceof Preview) {
                            mainObject = ((Preview) preview).getObject();

                        } else {
                            mainObject = substitutions.get(previewId);

                            if (mainObject == null) {
                                mainObject = preview;
                            }
                        }
                    }

                    if (mainObject != null) {
                        setSite(request, State.getInstance(mainObject).as(Site.ObjectModification.class).getOwner());
                    }

                } else {
                    mainObject = Directory.Static.findObject(site, path);
                    if (mainObject != null) {

                        // Directories should have a trailing slash and objects
                        // should not.
                        if (mainObject instanceof Directory) {
                            if (!path.endsWith("/")) {
                                fixPath(request, servletPath + "/");
                            }
                            mainObject = Directory.Static.findObject(site, path + "/index");

                        } else if (path.endsWith("/")) {
                            fixPath(request, servletPath.substring(0, servletPath.length() - 1));
                        }
                    }
                }

                // Case-insensitive path look-up.
                for (int i = 0, length = path.length(); i < length; ++ i) {
                    if (Character.isUpperCase(path.charAt(i))) {
                        String pathLc = path.toLowerCase(Locale.ENGLISH);
                        if (Directory.Static.findObject(site, pathLc) != null) {
                            fixPath(request, pathLc);
                        }
                        break;
                    }
                }

                // Special fallback names. For example, given /path/to/file,
                // the following are checked:
                //
                // - /path/to/file/*
                // - /path/to/file/**
                // - /path/to/*
                // - /path/to/**
                // - /path/**
                // - /**
                String checkPath;
                int endMarker;

                if (path.endsWith("/")) {
                    checkPath = path;
                    endMarker = 0;

                } else {
                    checkPath = path + "/";
                    endMarker = 1;
                }

                for (int i = 0; mainObject == null; ++ i) {
                    int slashAt = checkPath.lastIndexOf("/");

                    if (slashAt < 0) {
                        break;
                    } else {
                        checkPath = checkPath.substring(0, slashAt);
                    }

                    if (i <= endMarker) {
                        mainObject = Directory.Static.findObject(site, checkPath + "/*");
                    }

                    if (mainObject == null) {
                        mainObject = Directory.Static.findObject(site, checkPath + "/**");
                    }

                    if (mainObject instanceof Directory) {
                        mainObject = null;
                    }

                    if (mainObject != null) {
                        final String pathInfo = path.substring(checkPath.length());
                        if (pathInfo.length() < 1) {
                            fixPath(request, servletPath + "/");
                        }

                        request.setAttribute(NEW_REQUEST_ATTRIBUTE, new HttpServletRequestWrapper(request) {
                            @Override
                            public String getPathInfo() {
                                return pathInfo;
                            }
                        });
                    }
                }

                setMainObject(request, mainObject);
                return mainObject;

            } finally {
                Database.Static.restoreDefault();
            }
        }

        /** Sets the main object associated with the given {@code request}. */
        public static void setMainObject(HttpServletRequest request, Object mainObject) {
            request.setAttribute(MAIN_OBJECT_CHECKED_ATTRIBUTE, Boolean.TRUE);
            request.setAttribute(MAIN_OBJECT_ATTRIBUTE, mainObject);
            request.setAttribute("mainObject", mainObject);
            request.setAttribute("mainRecord", mainObject);
            request.setAttribute("mainContent", mainObject);
        }

        /** Returns the page used to render the given {@code request}. */
        public static Page getPage(HttpServletRequest request) {
            if (Boolean.TRUE.equals(request.getAttribute(PAGE_CHECKED_ATTRIBUTE))) {
                return (Page) request.getAttribute(PAGE_ATTRIBUTE);
            }

            request.setAttribute(PAGE_CHECKED_ATTRIBUTE, Boolean.TRUE);

            Page page = null;
            Object mainObject = getMainObject(request);

            if (mainObject instanceof Page) {
                page = (Page) mainObject;
            } else {
                page = Template.Static.findRenderable(mainObject, getSite(request));
            }

            setPage(request, page);
            return page;
        }

        /** Sets the page used to render the given {@code request}. */
        public static void setPage(HttpServletRequest request, Page page) {
            request.setAttribute(PAGE_CHECKED_ATTRIBUTE, Boolean.TRUE);
            request.setAttribute(PAGE_ATTRIBUTE, page);
            request.setAttribute("template", page);
        }

        /** Returns the profile used to process the given {@code request}. */
        public static Profile getProfile(HttpServletRequest request) {
            if (Boolean.TRUE.equals(request.getAttribute(PROFILE_CHECKED_ATTRIBUTE))) {
                return (Profile) request.getAttribute(PROFILE_ATTRIBUTE);
            }

            request.setAttribute(PROFILE_CHECKED_ATTRIBUTE, Boolean.TRUE);

            Profile profile = new Profile();
            profile.setUserAgent(request.getHeader("User-Agent"));

            setProfile(request, profile);
            return profile;
        }

        /** Sets the profile used to process the given {@code request}. */
        public static void setProfile(HttpServletRequest request, Profile profile) {
            request.setAttribute(PROFILE_CHECKED_ATTRIBUTE, Boolean.TRUE);
            request.setAttribute(PROFILE_ATTRIBUTE, profile);
            request.setAttribute("profile", profile);
        }

        // --- EL functions ---

        /**
         * Returns the plain {@linkplain StorageItem CDN} URL associated
         * with the given {@code servletPath}.
         */
        public static String getPlainResource(String servletPath) {
            if (ObjectUtils.coalesce(
                    Settings.get(Boolean.class, "cms/isResourceInStorage"),
                    Settings.isProduction())) {

                ServletContext servletContext = null;
                HttpServletRequest request = null;
                try {
                    servletContext = PageContextFilter.Static.getServletContext();
                    request = PageContextFilter.Static.getRequest();
                } catch (IllegalStateException ex) {
                }

                if (servletContext != null && request != null) {
                    StorageItem item = StorageItem.Static.getPlainResource(null, servletContext, servletPath);
                    if (item != null) {
                        return request.isSecure() ?
                                item.getSecurePublicUrl() :
                                item.getPublicUrl();
                    }
                }
            }

            return servletPath;
        }

        /**
         * Returns the plain or gzipped {@linkplain StorageItem CDN} URL
         * associated with the given {@code servletPath}.
         */
        public static String getResource(String servletPath) {
            if (ObjectUtils.coalesce(
                    Settings.get(Boolean.class, "cms/isResourceInStorage"),
                    Settings.isProduction())) {

                ServletContext servletContext = null;
                HttpServletRequest request = null;
                try {
                    servletContext = PageContextFilter.Static.getServletContext();
                    request = PageContextFilter.Static.getRequest();
                } catch (IllegalStateException ex) {
                }

                if (servletContext != null && request != null) {
                    String encodings = request.getHeader("Accept-Encoding");
                    StorageItem item = ObjectUtils.isBlank(encodings) || !encodings.contains("gzip") ?
                            StorageItem.Static.getPlainResource(null, servletContext, servletPath) :
                            StorageItem.Static.getGzippedResource(null, servletContext, servletPath);
                    if (item != null) {
                        return request.isSecure() ?
                                item.getSecurePublicUrl() :
                                item.getPublicUrl();
                    }
                }
            }

            return servletPath;
        }

        /** Returns the section with the given {@code internalName}. */
        public static Section getSection(String internalName) {
            return Query.from(Section.class).where("internalName = ?", internalName).first();
        }
    }

    public static class PathPattern extends Rule {

        private String pattern;

        public String getPattern() {
            return pattern;
        }

        public void setPattern(String pattern) {
            this.pattern = pattern;
        }

        // --- Rule support ---

        @Override
        public boolean evaluate(Variation variation, Profile profile, Object object) {
            HttpServletRequest request = PageContextFilter.Static.getRequest();
            if (request == null) {
                return false;
            }

            String path = request.getServletPath();
            Matcher matcher = Pattern.compile(getPattern()).matcher(path);

            if (!matcher.matches()) {
                return false;
            }

            List<String> matches = new ArrayList<String>();
            StringBuilder pathBuilder = new StringBuilder();
            int lastEnd = 0;

            for (int i = 1, count = matcher.groupCount(); i <= count; ++ i) {
                matches.add(matcher.group(i));
                pathBuilder.append(path.substring(lastEnd, matcher.start(i)));
                lastEnd = matcher.end(i);
            }

            request.setAttribute(PATH_MATCHES_ATTRIBUTE, matches);
            request.setAttribute("pathMatches", matches);

            pathBuilder.append(path.substring(lastEnd));
            PageFilter.Static.setPath(request, pathBuilder.toString());
            return true;
        }
    }

    // --- Deprecated ---

    /** @deprecated No replacement. */
    @Deprecated
    public static final String EXCEPTION_CSS_INJECTED_ATTRIBUTE = ATTRIBUTE_PREFIX + ".exceptionCssInjected";

    /** @deprecated Use {@link Static#getSite} instead. */
    @Deprecated
    public static Site getSite(HttpServletRequest request) {
        return Static.getSite(request);
    }

    /** @deprecated Use {@link Static#setSite} instead. */
    @Deprecated
    public static void setSite(HttpServletRequest request, Site site) {
        Static.setSite(request, site);
    }

    /** @deprecated Use {@link Static#getMainObject} instead. */
    @Deprecated
    public static Object getMainObject(HttpServletRequest request) {
        return Static.getMainObject(request);
    }

    /** @deprecated Use {@link Static#setMainObject} instead. */
    @Deprecated
    public static void setMainObject(HttpServletRequest request, Object mainObject) {
        Static.setMainObject(request, mainObject);
    }

    /**
     * @deprecated Use {@link Static#getPage} instead. To maintain
     *             backward compatibility, this method will return a
     *             {@code null} instead of looking for the most
     *             appropriate page to render with, if it's called
     *             before {@link #doRequest}.
     */
    @Deprecated
    public static Page getPage(HttpServletRequest request) {
        return (Page) request.getAttribute(PAGE_ATTRIBUTE);
    }

    /** @deprecated Use {@link Static#setPage} instead. */
    @Deprecated
    public static void setPage(HttpServletRequest request, Page page) {
        Static.setPage(request, page);
    }

    /** @deprecated Use {@link Static#getProfile} instead. */
    @Deprecated
    public static Profile getProfile(HttpServletRequest request) {
        return Static.getProfile(request);
    }

    /** @deprecated Use {@link Static#setProfile} instead. */
    @Deprecated
    public static void setProfile(HttpServletRequest request, Profile profile) {
        Static.setProfile(request, profile);
    }

    /**
     * @deprecated Use {@link Query#from} and {@link Site#itemsPredicate}
     *             together instead.
     */
    @Deprecated
    public static <T> Query<T> queryFrom(HttpServletRequest request, Class<T> objectClass) {
        return Query.from(objectClass).where(Site.OWNER_FIELD + " = ?", getSite(request));
    }

    /**
     * @deprecated You should let the exception propagate up naturally
     *             instead of catching it and using this method.
     */
    @Deprecated
    public static void writeException(HttpServletRequest request, Writer writer, Exception exception) throws IOException {
        if (exception instanceof RuntimeException) {
            throw (RuntimeException) exception;
        } else if (exception instanceof IOException) {
            throw (IOException) exception;
        } else {
            throw new RuntimeException(exception);
        }
    }

    /** @deprecated Use {@link Static#getPlainResource} instead. */
    @Deprecated
    public static String getPlainResource(String servletPath) {
        return Static.getPlainResource(servletPath);
    }

    /** @deprecated Use {@link Static#getResource} instead. */
    @Deprecated
    public static String getResource(String servletPath) {
        return Static.getResource(servletPath);
    }

    /** @deprecated Use {@link Static#getSection} instead. */
    @Deprecated
    public static Section getSection(String internalName) {
        return Static.getSection(internalName);
    }

    /** Renders the beginning of the given {@code container}. */
    @Deprecated
    protected static void beginContainer(
            HttpServletRequest request,
            HttpServletResponse response,
            Writer writer,
            ContainerSection container)
            throws IOException, ServletException {

        debugObject(request, writer, "Beginning container", container);
        renderScript(request, response, writer, container.getBeginEngine(), container.getBeginScript());
    }

    /** Renders the end of the given {@code container}. */
    @Deprecated
    protected static void endContainer(
            HttpServletRequest request,
            HttpServletResponse response,
            Writer writer,
            ContainerSection container)
            throws IOException, ServletException {

        renderScript(request, response, writer, container.getEndEngine(), container.getEndScript());
        debugObject(request, writer, "Ending container", container);
    }
}
