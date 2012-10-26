package com.psddev.cms.tool;

import com.psddev.cms.db.Content;
import com.psddev.cms.db.Draft;
import com.psddev.cms.db.History;
import com.psddev.cms.db.Site;
import com.psddev.cms.db.ToolFormWriter;
import com.psddev.cms.db.ToolUser;
import com.psddev.cms.db.Trash;

import com.psddev.dari.db.Application;
import com.psddev.dari.db.Database;
import com.psddev.dari.db.Query;
import com.psddev.dari.db.State;
import com.psddev.dari.db.StateStatus;
import com.psddev.dari.db.ObjectField;
import com.psddev.dari.db.ObjectType;
import com.psddev.dari.db.Predicate;
import com.psddev.dari.util.BuildDebugServlet;
import com.psddev.dari.util.JspUtils;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.Settings;
import com.psddev.dari.util.StringUtils;
import com.psddev.dari.util.WebPageContext;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * {@link WebPageContext} with extra methods that work well with
 * pages in {@link Tool}.
 */
public class ToolPageContext extends WebPageContext {

    public static final String TYPE_ID_PARAMETER = "typeId";
    public static final String OBJECT_ID_PARAMETER = "id";
    public static final String DRAFT_ID_PARAMETER = "draftId";
    public static final String ORIGINAL_DRAFT_VALUE = "original";
    public static final String HISTORY_ID_PARAMETER = "historyId";
    public static final String VARIATION_ID_PARAMETER = "variationId";
    public static final String RETURN_URL_PARAMETER = "returnUrl";

    private static final String ATTRIBUTE_PREFIX = ToolPageContext.class.getName() + ".";
    private static final String ERRORS_ATTRIBUTE = ATTRIBUTE_PREFIX + "errors";
    private static final String TOOL_ATTRIBUTE = ATTRIBUTE_PREFIX + "tool";
    private static final String TOOL_BY_CLASS_ATTRIBUTE = ATTRIBUTE_PREFIX + "toolByClass";
    private static final String TOOL_BY_PATH_ATTRIBUTE = ATTRIBUTE_PREFIX + "toolByPath";

    private static final String EXTRA_PREFIX = "cms.tool.";
    private static final String OVERLAID_DRAFT_EXTRA = EXTRA_PREFIX + "overlaidDraft";
    private static final String OVERLAID_HISTORY_EXTRA = EXTRA_PREFIX + "overlaidHistory";

    /** Creates an instance based on the given {@code pageContext}. */
    public ToolPageContext(PageContext pageContext) {
        super(pageContext);
    }

    /** Creates an instance based on the given servlet parameters. */
    public ToolPageContext(
            ServletContext servletContext,
            HttpServletRequest request,
            HttpServletResponse response) {

        super(servletContext, request, response);
    }

    /** @deprecated Use {@link ToolPageContext(ServletContext, HttpServletRequest, HttpServletResponse} instead. */
    @Deprecated
    public ToolPageContext(
            Servlet servlet,
            HttpServletRequest request,
            HttpServletResponse response) {

        super(servlet, request, response);
    }

    /**
     * Returns the parameter value as an instance of the given
     * {@code returnClass} associated with the given {@code name},
     * or if not found, either the {@linkplain ToolFilter#getPageSetting
     * page setting value} or the given {@code defaultValue}.
     */
    public <T> T pageParam(Class<T> returnClass, String name, T defaultValue) {
        HttpServletRequest request = getRequest();
        String valueString = request.getParameter(name);
        T value = ObjectUtils.to(returnClass, valueString);
        T userValue = ObjectUtils.to(returnClass, ToolFilter.getPageSetting(request, name));

        if (valueString == null) {
            return ObjectUtils.isBlank(userValue) ? defaultValue : userValue;

        } else {
            if (!ObjectUtils.equals(value, userValue)) {
                ToolFilter.putPageSetting(request, name, value);
            }
            return value;
        }
    }

    /** @deprecated Use the factory methods in {@link Database.Static} instead. */
    @Deprecated
    public Database getDatabase() {
        return Database.Static.getDefault();
    }

    /** @deprecated Use {@link Query#from} instead. */
    @Deprecated
    public <T> Query<T> queryFrom(Class<T> objectClass) {
        Query<T> query = Query.from(objectClass);
        query.setDatabase(getDatabase());
        return query;
    }

    /**
     * Returns the singleton instance of the given {@code toolClass}.
     * Note that this method caches the result, so it'll return the
     * exact same object every time within a single request.
     */
    public <T extends Tool> T getToolByClass(Class<T> toolClass) {
        HttpServletRequest request = getRequest();

        @SuppressWarnings("unchecked")
        Map<Class<?>, Tool> tools = (Map<Class<?>, Tool>) request.getAttribute(TOOL_BY_CLASS_ATTRIBUTE);
        if (tools == null) {
            tools = new HashMap<Class<?>, Tool>();
            request.setAttribute(TOOL_BY_CLASS_ATTRIBUTE, tools);
        }

        Tool tool = tools.get(toolClass);
        if (!toolClass.isInstance(tool)) {
            tool = Application.Static.getInstance(toolClass);
            tools.put(toolClass, tool);
        }

        @SuppressWarnings("unchecked")
        T typedTool = (T) tool;
        return typedTool;
    }

    /**
     * Returns the CMS tool.
     *
     * @see #getToolByClass
     */
    public CmsTool getCmsTool() {
        return getToolByClass(CmsTool.class);
    }

    /** Returns all embedded tools, keyed by their context paths. */
    public Map<String, Tool> getEmbeddedTools() {
        HttpServletRequest request = getRequest();

        @SuppressWarnings("unchecked")
        Map<String, Tool> tools = (Map<String, Tool>) request.getAttribute(TOOL_BY_PATH_ATTRIBUTE);
        if (tools == null) {
            tools = new LinkedHashMap<String, Tool>();

            for (Map.Entry<String, Properties> entry :
                    JspUtils.getEmbeddedSettings(getServletContext()).entrySet()) {

                String toolClassName = entry.getValue().getProperty(Application.MAIN_CLASS_SETTING);
                Class<?> objectClass = ObjectUtils.getClassByName(toolClassName);

                if (objectClass != null &&
                        Tool.class.isAssignableFrom(objectClass)) {
                    @SuppressWarnings("unchecked")
                    Class<Tool> toolClass = (Class<Tool>) objectClass;
                    tools.put(entry.getKey(), getToolByClass(toolClass));
                }
            }

            if (!tools.containsKey("")) {
                Application app = Application.Static.getMain();
                if (app instanceof Tool) {
                    tools.put("", (Tool) app);
                }
            }

            request.setAttribute(TOOL_BY_PATH_ATTRIBUTE, tools);
        }

        return tools;
    }

    /** Returns the tool that's currently in use. */
    public Tool getTool() {
        ServletContext context = getServletContext();
        HttpServletRequest request = getRequest();

        Tool tool = (Tool) request.getAttribute(TOOL_ATTRIBUTE);
        if (tool == null) {

            String contextPath = JspUtils.getEmbeddedContextPath(context, request.getServletPath());
            tool = getEmbeddedTools().get(contextPath);
            request.setAttribute(TOOL_ATTRIBUTE, tool);
        }

        return tool;
    }

    /** Returns the area that's currently in use. */
    public Area getArea() {
        Tool tool = getTool();
        if (tool != null) {

            List<Area> areas = new ArrayList<Area>();
            for (Area area : tool.findPlugins(Area.class)) {
                if (area.getTool().equals(tool)) {
                    areas.add(area);
                }
            }

            Collections.sort(areas, new Comparator<Area>() {
                @Override
                public int compare(Area x, Area y) {
                    return y.getUrl().compareTo(x.getUrl());
                }
            });

            ServletContext context = getServletContext();
            HttpServletRequest request = getRequest();
            String path = JspUtils.getEmbeddedServletPath(context, request.getServletPath());
            for (Area area : areas) {
                if (path.startsWith(area.getUrl())) {
                    return area;
                }
            }
        }

        return null;
    }

    private Object[] pushToArray(Object[] array, Object... newItems) {
        int os = array.length;
        int ns = newItems.length;
        Object[] newArray = new Object[os + ns];
        if (os > 0) {
            System.arraycopy(array, 0, newArray, 0, os);
        }
        if (ns > 0) {
            System.arraycopy(newItems, 0, newArray, os, ns);
        }
        return newArray;
    }

    /**
     * Returns an absolute version of the given {@code path} in context
     * of the given {@code tool}, modified by the given {@code parameters}.
     */
    public String toolUrl(Tool tool, String path, Object... parameters) {
        String url = null;
        for (Map.Entry<String, Tool> entry : getEmbeddedTools().entrySet()) {
            if (entry.getValue().equals(tool)) {
                url = entry.getKey();
                break;
            }
        }

        if (url == null) {
            url = tool.getUrl();
            if (ObjectUtils.isBlank(url)) {
                return "javascript:alert('" + js(String.format(
                        "No tool URL for [%s]! (must be set under Admin/Settings)",
                        tool.getName())) + "');";
            }

        } else {
            url = getServletContext().getContextPath() + url;
        }

        if (!path.equals("") && !path.startsWith("/")) {
            url += "/";
        }
        url += path;

        return StringUtils.addQueryParameters(url, parameters);
    }

    /**
     * Returns an absolute version of the given {@code path} in context
     * of the CMS, modified by the given {@code parameters}.
     */
    public String cmsUrl(String path, Object... parameters) {
        return toolUrl(getCmsTool(), path, parameters);
    }

    public String typeUrl(String path, UUID typeId, Object... parameters) {
        return url(path, pushToArray(parameters,
                TYPE_ID_PARAMETER, typeId,
                OBJECT_ID_PARAMETER, null,
                DRAFT_ID_PARAMETER, null,
                HISTORY_ID_PARAMETER, null));
    }

    public String typeUrl(
            String path, Class<?> objectClass, Object... parameters) {
        UUID typeId = ObjectType.getInstance(objectClass).getId();
        return typeUrl(path, typeId, parameters);
    }

    public String objectUrl(
            String path, Object object, Object... parameters) {
        if (object instanceof Draft) {
            Draft draft = (Draft) object;
            parameters = pushToArray(parameters,
                    OBJECT_ID_PARAMETER, draft.getObjectId(),
                    DRAFT_ID_PARAMETER, draft.getId(),
                    HISTORY_ID_PARAMETER, null);
        } else if (object instanceof History) {
            History history = (History) object;
            parameters = pushToArray(parameters,
                    OBJECT_ID_PARAMETER, history.getObjectId(),
                    DRAFT_ID_PARAMETER, null,
                    HISTORY_ID_PARAMETER, history.getId());
        } else {
            UUID objectId = State.getInstance(object).getId();
            Draft draft = getOverlaidDraft(object);
            History history = getOverlaidHistory(object);
            parameters = pushToArray(parameters,
                    OBJECT_ID_PARAMETER, objectId,
                    DRAFT_ID_PARAMETER, draft != null ? draft.getId() : null,
                    HISTORY_ID_PARAMETER,
                    history != null ? history.getId() : null);
        }
        return url(path, parameters);
    }

    public String originalUrl(
            String path, Object object, Object... parameters) {
        return url(path, pushToArray(parameters,
                OBJECT_ID_PARAMETER, State.getInstance(object).getId(),
                DRAFT_ID_PARAMETER, ORIGINAL_DRAFT_VALUE,
                HISTORY_ID_PARAMETER, null));
    }

    /** Returns an URL for returning to the current page from the request
     *  at the given {@code path}, modified by the given {@code parameters}. */
    public String returnableUrl(String path, Object... parameters) {
        HttpServletRequest request = getRequest();
        return url(path, pushToArray(parameters,
                RETURN_URL_PARAMETER, JspUtils.getAbsolutePath(request, "")
                .substring(JspUtils.getEmbeddedContextPath(getServletContext(), request.getServletPath()).length())));
    }

    /** Returns an URL to the return to the page specified by a previous
     *  call to {@link #returnableUrl(String, Object...)}, modified by the
     *  given {@code parameters}. */
    public String returnUrl(Object... parameters) {
        String returnUrl = param(RETURN_URL_PARAMETER);
        if (ObjectUtils.isBlank(returnUrl)) {
            throw new IllegalArgumentException(String.format(
                    "The [%s] parameter is required!", RETURN_URL_PARAMETER));
        }
        return url(returnUrl, parameters);
    }

    /** Returns an HTML-escaped label, or the given {@code defaultLabel} if
     *  one can't be found, for the type of the given {@code object}. */
    public String typeLabel(Object object, String defaultLabel) {
        State state = State.getInstance(object);
        if (state != null) {
            ObjectType type = state.getType();
            if (type != null) {
                return objectLabel(type);
            }
        }
        return h(defaultLabel);
    }

    /** Returns an HTML-escaped label for the type of the given
     *  {@code object}. */
    public String typeLabel(Object object) {
        return typeLabel(object, "Unknown Type");
    }

    /** Returns an HTML-escaped label, or the given {@code defaultLabel} if
     *  one can't be found, for the given {@code object}. */
    public String objectLabel(Object object, String defaultLabel) {
        State state = State.getInstance(object);
        if (state != null) {
            String label = state.getLabel();
            if (ObjectUtils.to(UUID.class, label) == null) {
                return h(label);
            }
        }
        return h(defaultLabel);
    }

    /** Returns an HTML-escaped label for the given {@code object}. */
    public String objectLabel(Object object) {
        State state = State.getInstance(object);
        return state != null ? h(state.getLabel()) : "Not Available";
    }

    /** Returns a modifiable list of all the errors in this page. */
    public List<Throwable> getErrors() {
        @SuppressWarnings("unchecked")
        List<Throwable> errors = (List<Throwable>) getRequest().getAttribute(ERRORS_ATTRIBUTE);
        if (errors == null) {
            errors = new ArrayList<Throwable>();
            getRequest().setAttribute(ERRORS_ATTRIBUTE, errors);
        }
        return errors;
    }

    /**
     * Renders the form inputs appropriate for the given {@code field}
     * using the data from the given {@code object}.
     */
    public void renderField(Object object, ObjectField field) throws IOException {
        ToolFormWriter writer = new ToolFormWriter(this);
        writer.inputs(State.getInstance(object), field.getInternalName());
    }

    /**
     * Processes the form inputs for the given {@code field}, rendered in
     * {@link #renderField(Object, ObjectField)}, using the data from the
     * given {@code object}.
     */
    public void processField(Object object, ObjectField field) throws Throwable {
        ToolFormWriter writer = new ToolFormWriter(this);
        writer.update(State.getInstance(object), getRequest(), field.getInternalName());
    }

    /** Finds an existing object or reserve one. */
    public Object findOrReserve(Collection<ObjectType> validTypes) {

        UUID objectId = uuidParam(OBJECT_ID_PARAMETER);
        Object object = Query.findById(Object.class, objectId);

        if (object != null) {
            ObjectType objectType = State.getInstance(object).getType();
            if (!ObjectUtils.isBlank(validTypes)
                    && !validTypes.contains(objectType)) {
                StringBuilder tb = new StringBuilder();
                for (ObjectType type : validTypes) {
                    tb.append(type.getLabel());
                    tb.append(", ");
                }
                tb.setLength(tb.length() - 2);
                throw new IllegalArgumentException(String.format(
                        "Expected one of [%s] types for [%s] object"
                        + " but it is of [%s] type", tb, objectId,
                        objectType != null ? objectType.getLabel()
                        : "unknown"));
            }

        } else if (!ObjectUtils.isBlank(validTypes)) {
            ObjectType selectedType = ObjectType.getInstance(uuidParam(TYPE_ID_PARAMETER));
            if (selectedType == null) {
                for (ObjectType type : validTypes) {
                    selectedType = type;
                    break;
                }
            }
            if (selectedType != null) {
                object = selectedType.createObject(objectId);
                State.getInstance(object).as(Site.ObjectModification.class).setOwner(getSite());
            }
        }

        UUID draftId = uuidParam(DRAFT_ID_PARAMETER);
        if (object == null) {
            Draft draft = Query.findById(Draft.class, draftId);
            if (draft != null) {
                object = draft.getObject();
                State.getInstance(object)
                        .getExtras().put(OVERLAID_DRAFT_EXTRA, draft);
            }

        } else {
            State state = State.getInstance(object);
            History history = Query
                    .from(History.class)
                    .where("id = ?", uuidParam(HISTORY_ID_PARAMETER))
                    .and("objectId = ?", objectId)
                    .first();

            if (history != null) {
                state.getExtras().put(OVERLAID_HISTORY_EXTRA, history);
                state.setValues(history.getObjectOriginals());
                state.setStatus(StateStatus.SAVED);

            } else if (objectId != null) {
                Draft draft = Query
                        .from(Draft.class)
                        .where("id = ?", draftId)
                        .and("objectId = ?", objectId)
                        .first();

                if (draft == null
                        && !ORIGINAL_DRAFT_VALUE.equals(param(DRAFT_ID_PARAMETER))) {
                    for (Draft d : getDatabase().readAll(Query
                            .from(Draft.class)
                            .where("objectId = ?", objectId))) {
                        String name = d.getName();
                        if (d.getSchedule() == null
                                && (name == null || name.length() == 0)) {
                            draft = d;
                            break;
                        }
                    }
                }

                if (draft != null) {
                    state.getExtras().put(OVERLAID_DRAFT_EXTRA, draft);
                    state.getValues().putAll(draft.getObjectChanges());
                }
            }

            UUID variationId = uuidParam(VARIATION_ID_PARAMETER);
            if (variationId != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> variationValues = (Map<String, Object>)
                        state.getValue("variations/" + variationId.toString());
                if (variationValues != null) {
                    state.setValues(variationValues);
                }
            }
        }

        return object;
    }

    /** Finds an existing object or reserve one. */
    public Object findOrReserve(UUID... validTypeIds) {
        Set<ObjectType> validTypes = null;
        if (!ObjectUtils.isBlank(validTypeIds)) {
            validTypes = new LinkedHashSet<ObjectType>();
            for (UUID typeId : validTypeIds) {
                ObjectType type = ObjectType.getInstance(typeId);
                if (type != null) {
                    validTypes.add(type);
                }
            }
        }
        return findOrReserve(validTypes);
    }

    /** Finds an existing object or reserve one. */
    public Object findOrReserve(Class<?>... validObjectClasses) {
        Set<ObjectType> validTypes = null;
        if (!ObjectUtils.isBlank(validObjectClasses)) {
            validTypes = new LinkedHashSet<ObjectType>();
            for (Class<?> validObjectClass : validObjectClasses) {
                ObjectType type = ObjectType.getInstance(validObjectClass);
                if (type != null) {
                    validTypes.add(type);
                }
            }
        }
        return findOrReserve(validTypes);
    }

    /** Finds an existing object or reserve one. */
    public Object findOrReserve() {
        UUID selectedTypeId = uuidParam(TYPE_ID_PARAMETER);
        return findOrReserve(selectedTypeId != null
                ? new UUID[] { selectedTypeId }
                : new UUID[0]);
    }

    /**
     * Returns the draft that was overlaid on top of the given
     * {@code object}.
     */
    public Draft getOverlaidDraft(Object object) {
        return (Draft) State.getInstance(object).getExtra(OVERLAID_DRAFT_EXTRA);
    }

    /**
     * Returns the past revision that was overlaid on top of the
     * {@code object}.
     */
    public History getOverlaidHistory(Object object) {
        return (History) State.getInstance(object).getExtra(OVERLAID_HISTORY_EXTRA);
    }

    public Predicate siteItemsPredicate() {
        ToolUser user = getUser();
        if (user != null) {
            Site site = user.getCurrentSite();
            if (site != null) {
                return site.itemsPredicate();
            }
        }
        return null;
    }

    /** Writes the tool header. */
    public void writeHeader() throws IOException {
        if (isAjaxRequest() || boolParam("_isFrame")) {
            return;
        }

        write("<!doctype html>");
        write("<html>");
        write("<head>");

        CmsTool cmsTool = getCmsTool();
        String companyName = cmsTool.getCompanyName();
        write("<title>", h(companyName), " CMS</title>");

        for (String href : new String[] {
                "/style/global.less",
                "/style/local.less" }) {
            write("<link href=\"", cmsUrl(href), "\" rel=\"stylesheet\" type=\"text/less\">");
        }

        write("<script src=\"", cmsUrl("/script/less-1.1.3.min.js"), "\" type=\"text/javascript\"></script>");

        for (String href : new String[] {
                "/style/icon.jsp" }) {
            write("<link href=\"", cmsUrl(href), "\" rel=\"stylesheet\" type=\"text/css\">");
        }

        String extraCss = cmsTool.getExtraCss();
        if (!ObjectUtils.isBlank(extraCss)) {
            write("<style type=\"text/css\">", extraCss, "</style>");
        }

        write("<script type=\"text/javascript\">var CONTEXT_PATH = '", cmsUrl("/"), "';</script>");

        for (String src : new String[] {
                "/script/jquery-1.4.2.min.js",
                "/script/jquery.livequery.js",
                "/script/jquery.extra.js",
                "/script/jquery.autosubmit.js",
                "/script/jquery.calendar.js",
                "/script/jquery.expandable.js",
                "/script/jquery.popup.js",
                "/script/jquery.frame.js",
                "/script/jquery.layout.js",
                "/script/jquery.repeatable.js",
                "/script/jquery.toggleable.js",
                "/script/jquery.tree.js",
                "/script/jquery.widthaware.js",
                "/script/jquery-ui-1.8.4.custom.min.js",
                "/script/json2.min.js",
                "/script/ckeditor/ckeditor.js",
                "/script/ckeditor/adapters/jquery.js",
                "/script/jquery.editor.js",
                "/script/wysihtml5.min.js",
                "/script/jquery.rte.js" }) {
            write("<script src=\"", cmsUrl(src), "\" type=\"text/javascript\"></script>");
        }

        if (getCmsTool().isWysihtml5Rte()) {
            write("<script type=\"text/javascript\">");
            write("jQuery.prototype.editor = jQuery.prototype.rte;");
            write("</script>");
        }

        for (String src : new String[] {
                "/script/global.js",
                "/script/local.js" }) {
            write("<script src=\"", cmsUrl(src), "\" type=\"text/javascript\"></script>");
        }

        String extraJavaScript = cmsTool.getExtraJavaScript();
        if (!ObjectUtils.isBlank(extraJavaScript)) {
            write("<script type=\"text/javascript\">", extraJavaScript, "</script>");
        }

        write("</head>");
        write("<body>");

        write("<div class=\"toolHat\">");

        ToolUser user = getUser();
        if (user != null) {
            write("<ul class=\"piped profile\">");
            write("<li>Hello, ", objectLabel(user), "</li>");

            if (!Site.Static.findAll().isEmpty()) {
                write("<li>Site: <a href=\"");
                write(cmsUrl("/misc/sites.jsp"));
                write("\" target=\"misc\">");
                Site currentSite = user.getCurrentSite();
                write(currentSite != null ? h(currentSite.getLabel()) : "All Sites");
                write("</a></li>");
            }

            write("<li><a href=\"", cmsUrl("/misc/settings.jsp"), "\" target=\"misc\">Settings</a></li>");
            write("<li><a href=\"", cmsUrl("/misc/moreTools.jsp"), "\" target=\"misc\">More Tools</a></li>");
            write("<li><a href=\"", cmsUrl("/misc/logOut.jsp"), "\">Log Out</a></li>");
            write("</ul>");
        }

        write("</div>");

        write("<div class=\"toolHeader\">");

        write("<h1 class=\"title\">");
        write("<a href=\"", cmsUrl("/"), "\">");
        write("<span class=\"companyName\">", h(companyName), "</span> CMS");
        write("</a>");
        write("</h1>");

        if (user != null) {
            write("<form action=\"", cmsUrl("/misc/search.jsp"), "\" class=\"search\" method=\"get\" target=\"miscSearch\">");
            write("<span class=\"searchInput\">");
            write("<label for=\"", createId(), "\">Search</label>");
            write("<input id=\"", getId(), "\" name=\"", Search.QUERY_STRING_PARAMETER, "\" type=\"text\">");
            write("<input type=\"submit\" value=\"Go\">");
            write("</span>");
            write("</form>");

            write("<ul class=\"mainNav\">");
            String servletPath = JspUtils.getEmbeddedServletPath(getServletContext(), getRequest().getServletPath());
            for (Area top : cmsTool.findTopAreas()) {
                if (hasPermission(top.getPermissionId())) {
                    write("<li class=\"", top.hasChildren() ? " isNested" : "", top.isSelected(getTool(), servletPath) ? " selected" : "", "\">");
                    write("<a href=\"", toolUrl(top.getTool(), top.getUrl()), "\">", objectLabel(top), "</a>");
                    if (top.hasChildren()) {
                        write("<ul>");
                        for (Area child : top.findChildren()) {
                            if (hasPermission(child.getPermissionId())) {
                                write("<li", child.isSelected(getTool(), servletPath) ? " class=\"selected\"" : "", ">");
                                write("<a href=\"", toolUrl(child.getTool(), child.getUrl()), "\">", objectLabel(child), "</a>");
                                write("</li>");
                            }
                        }
                        write("</ul>");
                    }
                    write("</li>");
                }
            }
            write("</ul>");
        }

        write("</div>");
    }

    /** Writes the tool footer. */
    public void writeFooter() throws IOException {
        if (isAjaxRequest() || boolParam("_isFrame")) {
            return;
        }

        write("<div class=\"toolFooter\">");

        Properties build = BuildDebugServlet.getProperties(getServletContext());

        write("<div class=\"build\">");

        String version = build.getProperty("version");
        if (ObjectUtils.isBlank(version)) {
            version = InetAddress.getLocalHost().getHostName();
            if (ObjectUtils.isBlank(version)) {
                version = "Unknown";
            }
        }

        write(" <span class=\"version\">");
        write(h(version));
        write("</span>");

        String buildNumber = build.getProperty("buildNumber");
        if (ObjectUtils.isBlank(buildNumber)) {
            buildNumber = "?";
        }

        write(" <span class=\"buildNumber\">Build #");
        write(h(buildNumber));
        write("</span>");

        write("</div>");

        write("</div></body></html>");
    }

    // --- ToolFilter bridge ---

    /**
     * Returns the current user accessing the tool.
     *
     * @see ToolFilter#getUser
     */
    public ToolUser getUser() {
        return ToolFilter.getUser(getRequest());
    }

    /** @see ToolFilter#getUserSetting */
    public Object getUserSetting(String key) {
        return ToolFilter.getUserSetting(getRequest(), key);
    }

    /** @see ToolFilter#putUserSetting */
    public void putUserSetting(String key, Object value) {
        ToolFilter.putUserSetting(getRequest(), key, value);
    }

    /**
     * Returns the site that the {@linkplain #getUser current user}
     * is accessing.
     */
    public Site getSite() {
        ToolUser user = getUser();
        return user != null ? user.getCurrentSite() : null;
    }

    /**
     * Returns {@code true} if the {@linkplain #getUser() current user}
     * is allowed access to the resources identified by the given
     * {@code permissionId}.
     */
    public boolean hasPermission(String permissionId) {
        return getUser().hasPermission(permissionId);
    }

    public boolean requirePermission(String permissionId) throws IOException {
        if (hasPermission(permissionId)) {
            return false;
        } else {
            getResponse().sendError(HttpServletResponse.SC_FORBIDDEN);
            return true;
        }
    }

    // --- Content.Static bridge ---

    /** @see Content.Static#deleteSoftly */
    public Trash deleteSoftly(Object object) {
        return Content.Static.deleteSoftly(object, getSite(), getUser());
    }

    /** @see Content.Static#publish */
    public History publish(Object object) {
        return Content.Static.publish(object, getSite(), getUser());
    }

    /** @see Content.Static#purge */
    public void purge(Object object) {
        Content.Static.purge(object, getSite(), getUser());
    }
}
