package com.psddev.cms.tool;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
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

import org.joda.time.DateTime;

import com.psddev.cms.db.Content;
import com.psddev.cms.db.Draft;
import com.psddev.cms.db.History;
import com.psddev.cms.db.LayoutTag;
import com.psddev.cms.db.Page;
import com.psddev.cms.db.Renderer;
import com.psddev.cms.db.Site;
import com.psddev.cms.db.Template;
import com.psddev.cms.db.ToolFormWriter;
import com.psddev.cms.db.ToolUi;
import com.psddev.cms.db.ToolUser;
import com.psddev.cms.db.Trash;
import com.psddev.cms.db.WorkStream;
import com.psddev.dari.db.Application;
import com.psddev.dari.db.CompoundPredicate;
import com.psddev.dari.db.Database;
import com.psddev.dari.db.ObjectField;
import com.psddev.dari.db.ObjectFieldComparator;
import com.psddev.dari.db.ObjectType;
import com.psddev.dari.db.Predicate;
import com.psddev.dari.db.PredicateParser;
import com.psddev.dari.db.Query;
import com.psddev.dari.db.State;
import com.psddev.dari.db.StateStatus;
import com.psddev.dari.util.BuildDebugServlet;
import com.psddev.dari.util.ErrorUtils;
import com.psddev.dari.util.JspUtils;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.Settings;
import com.psddev.dari.util.StorageItem;
import com.psddev.dari.util.StringUtils;
import com.psddev.dari.util.TypeReference;
import com.psddev.dari.util.WebPageContext;

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

    /**
     * Returns the parameter value as an instance of the given
     * {@code returnClass} associated with the given {@code name}, or if not
     * found, either the {@linkplain #getPageSetting page setting value} or
     * the given {@code defaultValue}.
     */
    public <T> T pageParam(Class<T> returnClass, String name, T defaultValue) {
        HttpServletRequest request = getRequest();
        String valueString = request.getParameter(name);
        T value = ObjectUtils.to(returnClass, valueString);
        T userValue = ObjectUtils.to(returnClass, AuthenticationFilter.Static.getPageSetting(request, name));

        if (valueString == null) {
            return ObjectUtils.isBlank(userValue) ? defaultValue : userValue;

        } else {
            if (!ObjectUtils.equals(value, userValue)) {
                AuthenticationFilter.Static.putPageSetting(request, name, value);
            }

            return value;
        }
    }

    /**
     * Returns a label, or the given {@code defaultLabel} if one can't be
     * found, for the given {@code object}.
     */
    public String getObjectLabelOrDefault(Object object, String defaultLabel) {
        return Static.getObjectLabelOrDefault(object, defaultLabel);
    }

    /** Returns a label for the given {@code object}. */
    public String getObjectLabel(Object object) {
        return Static.getObjectLabel(object);
    }

    /**
     * Returns a label, or the given {@code defaultLabel} if one can't be
     * found, for the type of the given {@code object}.
     */
    public String getTypeLabelOrDefault(Object object, String defaultLabel) {
        return Static.getTypeLabelOrDefault(object, defaultLabel);
    }

    /** Returns a label for the type of the given {@code object}. */
    public String getTypeLabel(Object object) {
        return Static.getTypeLabel(object);
    }

    /** Returns {@code true} is the given {@code object} is previewable. */
    public boolean isPreviewable(Object object) {
        if (object != null) {
            if (object.getClass() == Page.class) {
                return true;

            } else {
                State state = State.getInstance(object);
                ObjectType type = state.getType();

                if (type != null) {
                    return Template.Static.findUsedTypes(getSite()).contains(type) ||
                            !ObjectUtils.isBlank(type.as(Renderer.TypeModification.class).getModulePath());
                }
            }
        }

        return false;
    }

    /**
     * Returns the singleton instance of the given {@code toolClass}.
     * Note that this method caches the result, so it'll return the
     * exact same object every time within a single request.
     */
    @SuppressWarnings("unchecked")
    public <T extends Tool> T getToolByClass(Class<T> toolClass) {
        HttpServletRequest request = getRequest();
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

        return (T) tool;
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
    @SuppressWarnings("unchecked")
    public Map<String, Tool> getEmbeddedTools() {
        HttpServletRequest request = getRequest();
        Map<String, Tool> tools = (Map<String, Tool>) request.getAttribute(TOOL_BY_PATH_ATTRIBUTE);

        if (tools == null) {
            tools = new LinkedHashMap<String, Tool>();

            for (Map.Entry<String, Properties> entry : JspUtils.getEmbeddedSettings(getServletContext()).entrySet()) {
                String toolClassName = entry.getValue().getProperty(Application.MAIN_CLASS_SETTING);
                Class<?> objectClass = ObjectUtils.getClassByName(toolClassName);

                if (objectClass != null &&
                        Tool.class.isAssignableFrom(objectClass)) {
                    tools.put(entry.getKey(), getToolByClass((Class<Tool>) objectClass));
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

            for (Area area : Tool.Static.getPluginsByClass(Area.class)) {
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

    public String typeUrl(String path, Class<?> objectClass, Object... parameters) {
        UUID typeId = ObjectType.getInstance(objectClass).getId();

        return typeUrl(path, typeId, parameters);
    }

    public String objectUrl(String path, Object object, Object... parameters) {
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

    public String originalUrl(String path, Object object, Object... parameters) {
        return url(path, pushToArray(parameters,
                OBJECT_ID_PARAMETER, State.getInstance(object).getId(),
                DRAFT_ID_PARAMETER, ORIGINAL_DRAFT_VALUE,
                HISTORY_ID_PARAMETER, null));
    }

    /**
     * Returns an URL for returning to the current page from the request
     * at the given {@code path}, modified by the given {@code parameters}.
     */
    public String returnableUrl(String path, Object... parameters) {
        HttpServletRequest request = getRequest();

        return url(path, pushToArray(parameters,
                RETURN_URL_PARAMETER, JspUtils.getAbsolutePath(request, "")
                .substring(JspUtils.getEmbeddedContextPath(getServletContext(), request.getServletPath()).length())));
    }

    /**
     * Returns an URL to the return to the page specified by a previous
     * call to {@link #returnableUrl(String, Object...)}, modified by the
     * given {@code parameters}.
     */
    public String returnUrl(Object... parameters) {
        String returnUrl = param(String.class, RETURN_URL_PARAMETER);

        if (ObjectUtils.isBlank(returnUrl)) {
            throw new IllegalArgumentException(String.format(
                    "The [%s] parameter is required!", RETURN_URL_PARAMETER));
        }

        return url(returnUrl, parameters);
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
        @SuppressWarnings("all")
        ToolFormWriter writer = new ToolFormWriter(this);

        writer.inputs(State.getInstance(object), field.getInternalName());
    }

    /**
     * Processes the form inputs for the given {@code field}, rendered in
     * {@link #renderField(Object, ObjectField)}, using the data from the
     * given {@code object}.
     */
    public void processField(Object object, ObjectField field) throws Throwable {
        @SuppressWarnings("all")
        ToolFormWriter writer = new ToolFormWriter(this);

        writer.update(State.getInstance(object), getRequest(), field.getInternalName());
    }

    /** Finds an existing object or reserve one. */
    public Object findOrReserve(Collection<ObjectType> validTypes) {
        UUID objectId = param(UUID.class, OBJECT_ID_PARAMETER);
        Object object;
        WorkStream workStream = Query.findById(WorkStream.class, param(UUID.class, "workStreamId"));

        if (workStream != null) {
            object = workStream.next(getUser());

        } else {
            object = Query.findById(Object.class, objectId);
        }

        if (object != null) {
            ObjectType objectType = State.getInstance(object).getType();

            if (!ObjectUtils.isBlank(validTypes) &&
                    !validTypes.contains(objectType)) {
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
            ObjectType selectedType = ObjectType.getInstance(param(UUID.class, TYPE_ID_PARAMETER));

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

        UUID draftId = param(UUID.class, DRAFT_ID_PARAMETER);

        if (object == null) {
            Draft draft = Query.findById(Draft.class, draftId);

            if (draft != null) {
                object = draft.getObject();

                State.getInstance(object).getExtras().put(OVERLAID_DRAFT_EXTRA, draft);
            }

        } else {
            State state = State.getInstance(object);

            History history = Query.
                    from(History.class).
                    where("id = ?", param(UUID.class, HISTORY_ID_PARAMETER)).
                    and("objectId = ?", objectId).
                    first();

            if (history != null) {
                state.getExtras().put(OVERLAID_HISTORY_EXTRA, history);
                state.setValues(history.getObjectOriginals());
                state.setStatus(StateStatus.SAVED);

            } else if (objectId != null) {
                Draft draft = Query.
                        from(Draft.class).
                        where("id = ?", draftId).
                        and("objectId = ?", objectId).
                        first();

                if (draft == null &&
                        !ORIGINAL_DRAFT_VALUE.equals(param(String.class, DRAFT_ID_PARAMETER))) {
                    for (Draft d : getDatabase().readAll(Query.
                            from(Draft.class).
                            where("objectId = ?", objectId))) {
                        String name = d.getName();

                        if (d.getSchedule() == null &&
                                (name == null || name.length() == 0)) {
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

            UUID variationId = param(UUID.class, VARIATION_ID_PARAMETER);

            if (variationId != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> variationValues = (Map<String, Object>) state.getValue("variations/" + variationId.toString());

                if (variationValues != null) {
                    state.setValues(variationValues);
                }
            }
        }

        Template template = Query.from(Template.class).where("_id = ?", param(UUID.class, "templateId")).first();

        if (template != null) {
            if (object == null) {
                Set<ObjectType> contentTypes = template.getContentTypes();

                if (!contentTypes.isEmpty()) {
                    object = contentTypes.iterator().next().createObject(objectId);
                    State.getInstance(object).as(Site.ObjectModification.class).setOwner(getSite());
                }
            }

            if (object != null) {
                State.getInstance(object).as(Template.ObjectModification.class).setDefault(template);
            }

        } else if (object != null) {
            State state = State.getInstance(object);

            if (state.isNew()) {
                List<Template> templates = Template.Static.findUsable(object);

                if (!templates.isEmpty()) {
                    state.as(Template.ObjectModification.class).setDefault(templates.iterator().next());
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
        UUID selectedTypeId = param(UUID.class, TYPE_ID_PARAMETER);

        return findOrReserve(selectedTypeId != null ?
                new UUID[] { selectedTypeId } :
                new UUID[0]);
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

    public Predicate siteItemsSearchPredicate() {
        Predicate predicate = siteItemsPredicate();

        if (predicate != null) {
            predicate = CompoundPredicate.combine(
                    PredicateParser.AND_OPERATOR,
                    predicate,
                    PredicateParser.Static.parse("* matches *"));
        }

        return predicate;
    }

    private String cmsResource(String path, Object... parameters) {
        ServletContext context = getServletContext();
        path = cmsUrl(path);
        long lastModified = 0;

        try {
            URL resource = context.getResource(path);

            if (resource != null) {
                URLConnection resourceConnection = resource.openConnection();
                InputStream resourceInput = resourceConnection.getInputStream();

                try {
                    lastModified = resourceConnection.getLastModified();
                } finally {
                    resourceInput.close();
                }
            }

        } catch (IOException error) {
        }

        if (lastModified == 0) {
            lastModified = (long) (Math.random() * Long.MAX_VALUE);
        }

        return StringUtils.addQueryParameters(
                StringUtils.addQueryParameters(path, parameters),
                "_", lastModified);
    }

    /** Writes the tool header. */
    public void writeHeader() throws IOException {
        if (requireUser()) {
            throw new IllegalStateException();
        }

        if (isAjaxRequest() || param(boolean.class, "_isFrame")) {
            return;
        }

        CmsTool cms = getCmsTool();
        String companyName = cms.getCompanyName();
        StorageItem companyLogo = cms.getCompanyLogo();
        String environment = cms.getEnvironment();
        String extraCss = cms.getExtraCss();
        String extraJavaScript = cms.getExtraJavaScript();
        ToolUser user = getUser();

        if (ObjectUtils.isBlank(companyName)) {
            companyName = "Brightspot";
        }

        writeTag("!doctype html");
        writeTag("html");
            writeStart("head");

                writeStart("title");
                    writeHtml(companyName);
                writeEnd();

                writeTag("meta", "name", "robots", "content", "noindex");

                for (String href : new String[] {
                        "/style/cms.less" }) {
                    writeTag("link", "rel", "stylesheet", "type", "text/less", "href", cmsResource(href));
                }

                writeStart("script", "type", "text/javascript");
                    write("window.less = window.less || { }; window.less.env = 'development'; window.less.poll = 500;");
                writeEnd();

                writeStart("script", "type", "text/javascript", "src", cmsResource("/script/less-1.3.3.min.js"));
                writeEnd();

                if (!ObjectUtils.isBlank(extraCss)) {
                    writeStart("style", "type", "text/css");
                        write(extraCss);
                    writeEnd();
                }

                List<Map<String, Object>> cssClassGroups  = new ArrayList<Map<String, Object>>();

                for (CmsTool.CssClassGroup group : cms.getTextCssClassGroups()) {
                    Map<String, Object> groupDef = new HashMap<String, Object>();
                    cssClassGroups.add(groupDef);

                    groupDef.put("internalName", group.getInternalName());
                    groupDef.put("displayName", group.getDisplayName());
                    groupDef.put("dropDown", group.isDropDown());

                    List<Map<String, String>> cssClasses = new ArrayList<Map<String, String>>();
                    groupDef.put("cssClasses", cssClasses);

                    for (CmsTool.CssClass cssClass : group.getCssClasses()) {
                        Map<String, String> cssDef = new HashMap<String, String>();
                        cssClasses.add(cssDef);

                        cssDef.put("internalName", cssClass.getInternalName());
                        cssDef.put("displayName", cssClass.getDisplayName());
                        cssDef.put("tag", cssClass.getTag());
                    }
                }

                writeStart("script", "type", "text/javascript");
                    write("var CONTEXT_PATH = '", cmsUrl("/"), "';");
                    write("var CSS_CLASS_GROUPS = ", ObjectUtils.toJson(cssClassGroups), ";");
                writeEnd();

                for (String src : new String[] {
                        "/script/jquery-1.8.3.min.js",
                        "/script/jquery.extra.js",
                        "/script/jquery.autosubmit.js",
                        "/script/jquery.calendar.js",
                        "/script/jquery.dropdown.js",
                        "/script/jquery.expandable.js",
                        "/script/jquery.popup.js",
                        "/script/jquery.frame.js",
                        "/script/jquery.imageeditor.js",
                        "/script/jquery.objectid.js",
                        "/script/jquery.pagelayout.js",
                        "/script/jquery.pagethumbnails.js",
                        "/script/jquery.repeatable.js",
                        "/script/jquery.sortable.js",
                        "/script/jquery.toggleable.js",
                        "/script/json2.min.js",
                        "/script/pixastic/pixastic.core.js",
                        "/script/pixastic/actions/brightness.js",
                        "/script/pixastic/actions/crop.js",
                        "/script/pixastic/actions/desaturate.js",
                        "/script/pixastic/actions/fliph.js",
                        "/script/pixastic/actions/flipv.js",
                        "/script/pixastic/actions/invert.js",
                        "/script/pixastic/actions/rotate.js",
                        "/script/pixastic/actions/sepia.js",
                        "/script/html5slider.js",
                        "/script/wysihtml5.min.js",
                        "/script/jquery.rte.js",
                        "/script/cms.js" }) {
                    writeStart("script", "type", "text/javascript", "src", cmsResource(src));
                    writeEnd();
                }

                if (!ObjectUtils.isBlank(extraJavaScript)) {
                    writeStart("script", "type", "text/javascript");
                        write(extraJavaScript);
                    writeEnd();
                }

            writeEnd();
            writeTag("body");

                writeStart("div", "class", "toolHeader" + (!ObjectUtils.isBlank(environment) ? " toolHeader-hasEnvironment" : ""));

                    writeStart("h1", "class", "toolTitle");
                        writeStart("a", "href", cmsUrl("/"));
                            if (companyLogo != null) {
                                writeTag("img", "src", companyLogo.getPublicUrl(), "alt", companyName);
                            } else {
                                writeHtml(companyName);
                            }
                        writeEnd();
                    writeEnd();

                    if (!ObjectUtils.isBlank(environment)) {
                        writeStart("div", "class", "toolEnv");
                            writeHtml(environment);
                        writeEnd();
                    }

                    if (user != null) {
                        int nowHour = new DateTime().getHourOfDay();

                        writeStart("div", "class", "toolProfile");
                            writeHtml("Good ");
                            writeHtml(nowHour >= 2 && nowHour < 12 ? "Morning" : (nowHour >= 12 && nowHour < 18 ? "Afternoon" : "Evening"));
                            writeHtml(", ");
                            writeHtml(getObjectLabel(user));

                            writeStart("ul");
                                if (!Site.Static.findAll().isEmpty()) {
                                    Site currentSite = user.getCurrentSite();

                                    writeStart("li");
                                        writeHtml("Site: ");
                                        writeStart("a", "href", cmsUrl("/misc/sites.jsp"), "target", "misc");
                                            writeHtml(currentSite != null ? currentSite.getLabel() : "Global");
                                        writeEnd();
                                    writeEnd();
                                }

                                writeStart("li");
                                    writeStart("a",
                                            "class", "icon icon-object-toolUser",
                                            "href", cmsUrl("/misc/settings.jsp"),
                                            "target", "misc");
                                        writeHtml("Profile");
                                    writeEnd();
                                writeEnd();

                                writeStart("li");
                                    writeStart("a",
                                            "class", "action-logOut",
                                            "href", cmsUrl("/misc/logOut.jsp"));
                                        writeHtml("Log Out");
                                    writeEnd();
                                writeEnd();
                            writeEnd();
                        writeEnd();
                    }

                    if (hasPermission("area/dashboard")) {
                        writeStart("form",
                                "class", "toolSearch",
                                "method", "get",
                                "action", cmsUrl("/misc/search.jsp"),
                                "target", "miscSearch");

                            writeTag("input", "type", "hidden", "name", Search.NAME_PARAMETER, "value", "global");

                            writeStart("span", "class", "searchInput");
                                writeStart("label", "for", createId()).writeHtml("Search").writeEnd();
                                writeTag("input", "type", "text", "id", getId());
                                writeStart("button").writeHtml("Go").writeEnd();
                            writeEnd();

                        writeEnd();
                    }

                    if (user != null) {
                        String servletPath = JspUtils.getEmbeddedServletPath(getServletContext(), getRequest().getServletPath());

                        writeStart("ul", "class", "toolNav");
                            for (Area top : Tool.Static.getTopAreas()) {
                                if (!hasPermission(top.getPermissionId())) {
                                    continue;
                                }

                                writeStart("li",
                                        "class", (top.hasChildren() ? " isNested" : "") + (top.isSelected(getTool(), servletPath) ? " selected" : ""));
                                    writeStart("a", "href", toolUrl(top.getTool(), top.getUrl()));
                                        writeHtml(getObjectLabel(top));
                                    writeEnd();

                                    if (top.hasChildren()) {
                                        writeStart("ul");
                                            for (Area child : top.getChildren()) {
                                                if (!hasPermission(child.getPermissionId())) {
                                                    continue;
                                                }

                                                writeStart("li", "class", child.isSelected(getTool(), servletPath) ? "selected" : null);
                                                    writeStart("a", "href", toolUrl(child.getTool(), child.getUrl()));
                                                        writeHtml(getObjectLabel(child));
                                                    writeEnd();
                                                writeEnd();
                                            }
                                        writeEnd();
                                    }
                                writeEnd();
                            }
                        writeEnd();
                    }

                writeEnd();
    }

    /** Writes the tool footer. */
    public void writeFooter() throws IOException {
        if (isAjaxRequest() || param(boolean.class, "_isFrame")) {
            return;
        }

        Properties build = BuildDebugServlet.getProperties(getServletContext());
        String version = build.getProperty("version");
        String buildNumber = build.getProperty("buildNumber");

        if (ObjectUtils.isBlank(version)) {
            version = "Unknown";
        }

        if (ObjectUtils.isBlank(buildNumber)) {
            buildNumber = "?";
        }

                writeStart("div", "class", "toolFooter");
                    writeStart("div", "class", "toolBuild");
                        writeStart("span", "class", "version");
                            writeHtml(version);
                        writeEnd();
                        writeHtml(" ");
                        writeStart("span", "class", "buildNumber");
                            writeHtml("Build #");
                            writeHtml(buildNumber);
                        writeEnd();
                    writeEnd();
                writeEnd();
            writeTag("/body");
        writeTag("/html");
    }

    /**
     * Writes a {@code <select>} tag that allows the user to pick a content
     * type.
     *
     * @param types Types that the user is allowed to select from.
     * If {@code null}, all content types will be available.
     * @param selectedType Type that should be initially selected.
     * @param allLabel Label for the option that selects all types.
     * If {@code null}, the option won't be available.
     * @param attributes Attributes for the {@code <select>} tag.
     */
    public void writeTypeSelect(
            Iterable<ObjectType> types,
            ObjectType selectedType,
            String allLabel,
            Object... attributes) throws IOException {

        if (types == null) {
            types = Database.Static.getDefault().getEnvironment().getTypes();
        }

        List<ObjectType> typesList = ObjectUtils.to(new TypeReference<List<ObjectType>>() { }, types);

        for (Iterator<ObjectType> i = typesList.iterator(); i.hasNext(); ) {
            ObjectType type = i.next();

            if (!type.isConcrete()) {
                i.remove();
            }
        }

        Map<String, List<ObjectType>> typeGroups = new LinkedHashMap<String, List<ObjectType>>();
        List<ObjectType> mainTypes = Template.Static.findUsedTypes(getSite());

        mainTypes.retainAll(typesList);
        typesList.removeAll(mainTypes);
        typeGroups.put("Main Content Types", mainTypes);
        typeGroups.put("Misc Content Types", typesList);

        for (Iterator<List<ObjectType>> i = typeGroups.values().iterator(); i.hasNext(); ) {
            List<ObjectType> typeGroup = i.next();

            if (typeGroup.isEmpty()) {
                i.remove();

            } else {
                Collections.sort(typeGroup);
            }
        }

        PageWriter writer = getWriter();

        writer.writeStart("select", attributes);

            if (allLabel != null) {
                writer.writeStart("option", "value", "").writeHtml(allLabel).writeEnd();
            }

            if (typeGroups.size() == 1) {
                typeSelectGroup(writer, selectedType, typeGroups.values().iterator().next());

            } else {
                for (Map.Entry<String, List<ObjectType>> entry : typeGroups.entrySet()) {
                    writer.writeStart("optgroup", "label", entry.getKey());
                        typeSelectGroup(writer, selectedType, entry.getValue());
                    writer.writeEnd();
                }
            }

        writer.writeEnd();
    }

    private static void typeSelectGroup(PageWriter writer, ObjectType selectedType, List<ObjectType> types) throws IOException {
        String previousLabel = null;

        for (ObjectType type : types) {
            String label = Static.getObjectLabel(type);

            writer.writeStart("option",
                    "selected", type.equals(selectedType) ? "selected" : null,
                    "value", type.getId());
                writer.writeHtml(label);
                if (label.equals(previousLabel)) {
                    writer.writeHtml(" (");
                    writer.writeHtml(type.getInternalName());
                    writer.writeHtml(")");
                }
            writer.writeEnd();

            previousLabel = label;
        }
    }

    /**
     * Writes a {@code <select>} or {@code <input>} tag that allows the user
     * to pick a content.
     *
     * @param field Can't be {@code null}.
     * @param value Initial value. May be {@code null}.
     * @param attributes Extra attributes for the HTML tag.
     */
    public void writeObjectSelect(ObjectField field, Object value, Object... attributes) throws IOException {
        ErrorUtils.errorIfNull(field, "field");

        ToolUi ui = field.as(ToolUi.class);
        PageWriter writer = getWriter();

        if (isObjectSelectDropDown(field)) {
            List<?> items = new Search(field).toQuery().selectAll();
            Collections.sort(items, new ObjectFieldComparator("_label", false));

            writer.writeStart("select",
                    "data-searchable", "true",
                    attributes);
                writer.writeStart("option", "value", "").writeEnd();
                for (Object item : items) {
                    State itemState = State.getInstance(item);
                    writer.writeStart("option",
                            "selected", item.equals(value) ? "selected" : null,
                            "value", itemState.getId());
                        writer.objectLabel(item);
                    writer.writeEnd();
                }
            writer.writeEnd();

        } else {
            State state = State.getInstance(value);
            StorageItem preview = value != null ? state.getPreview() : null;
            StringBuilder typeIds = new StringBuilder();

            for (ObjectType type : field.getTypes()) {
                typeIds.append(type.getId());
                typeIds.append(',');
            }

            if (typeIds.length() > 0) {
                typeIds.setLength(typeIds.length() - 1);
            }

            writer.writeTag("input",
                    "type", "text",
                    "class", "objectId",
                    "data-additional-query", field.getPredicate(),
                    "data-label", value != null ? getObjectLabel(value) : null,
                    "data-pathed", ToolUi.isOnlyPathed(field),
                    "data-preview", preview != null ? preview.getPublicUrl() : null,
                    "data-searcher-path", ui.getInputSearcherPath(),
                    "data-suggestions", ui.isEffectivelySuggestions(),
                    "data-typeIds", typeIds,
                    "value", value != null ? state.getId() : null,
                    attributes);
        }
    }

    /**
     * Returns {@code true} if the {@code <select>} tag would be used to allow
     * the user to pick a content for the given {@code field}.
     *
     * @param field Can't be {@code null}.
     */
    public boolean isObjectSelectDropDown(ObjectField field) {
        ErrorUtils.errorIfNull(field, "field");

        return field.as(ToolUi.class).isDropDown() &&
                !new Search(field).toQuery().hasMoreThan(Settings.getOrDefault(long.class, "cms/tool/dropDownMaximum", 250L));
    }

    /** Writes all grid CSS, or does nothing if it's already written. */
    public ToolPageContext writeGridCssOnce() throws IOException {
        LayoutTag.Static.writeGridCss(this, getServletContext(), getRequest());
        return this;
    }

    /**
     * Tries to delete the given {@code object} if the user has ask for it
     * in the current request.
     *
     * @return {@code true} if the delete is tried.
     */
    public boolean tryDelete(Object object) {
        if (!(isFormPost() &&
                (param(String.class, "action-delete") != null ||
                "delete".equalsIgnoreCase(param(String.class, "action"))))) {
            return false;
        }

        try {
            Draft draft = getOverlaidDraft(object);

            if (draft != null) {
                draft.delete();
                redirect("", "discarded", System.currentTimeMillis());

            } else {
                deleteSoftly(object);
                redirect("", "id", null, "saved", null);
            }

        } catch (Exception error) {
            getErrors().add(error);
        }

        return true;
    }

    // --- AuthenticationFilter bridge ---

    /** @see AuthenticationFilter.Static#requireUser */
    public boolean requireUser() throws IOException {
        return AuthenticationFilter.Static.requireUser(getServletContext(), getRequest(), getResponse());
    }

    /**
     * Returns the current user accessing the tool.
     *
     * @see AuthenticationFilter.Static#getUser
     */
    public ToolUser getUser() {
        return AuthenticationFilter.Static.getUser(getRequest());
    }

    /**
     * Returns the current tool user setting value associated with the given
     * {@code key}.
     *
     * @see AuthenticationFilter.Static#getUserSetting
     */
    public Object getUserSetting(String key) {
        return AuthenticationFilter.Static.getUserSetting(getRequest(), key);
    }

    /**
     * Puts the given setting {@code value} at the given {@code key} for
     * the current tool user.
     *
     * @see AuthenticationFilter.Static#putUserSetting
     */
    public void putUserSetting(String key, Object value) {
        AuthenticationFilter.Static.putUserSetting(getRequest(), key, value);
    }

    /**
     * Returns the page setting value associated with the given {@code key}.
     *
     * @see AuthenticationFilter.Static#getPageSetting
     */
    public Object getPageSetting(String key) {
        return AuthenticationFilter.Static.getPageSetting(getRequest(), key);
    }

    /**
     * Puts the page setting {@code value} at the given {@code key}.
     *
     * @see AuthenticationFilter.Static#putPageSetting
     */
    public void putPageSetting(String key, Object value) {
        AuthenticationFilter.Static.putPageSetting(getRequest(), key, value);
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
     * Returns {@code true} if the {@linkplain #getUser current user}
     * is allowed access to the resources identified by the given
     * {@code permissionId}.
     *
     * @param If {@code null}, returns {@code true}.
     */
    public boolean hasPermission(String permissionId) {
        ToolUser user = getUser();

        return user != null &&
                (permissionId == null ||
                user.hasPermission(permissionId));
    }

    public boolean requirePermission(String permissionId) throws IOException {
        if (requireUser()) {
            return true;

        } else {
            if (hasPermission(permissionId)) {
                return false;

            } else {
                getResponse().sendError(Settings.isProduction() ?
                        HttpServletResponse.SC_NOT_FOUND :
                        HttpServletResponse.SC_FORBIDDEN);
                return true;
            }
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

    // --- WebPageContext support ---

    private PageWriter pageWriter;

    @Override
    public PageWriter getWriter() throws IOException {
        if (pageWriter == null) {
            pageWriter = new PageWriter(super.getWriter());
        }

        return pageWriter;
    }

    /** {@link ToolPageContext} utility methods. */
    public static final class Static {

        private Static() {
        }

        private static String notTooShort(String word) {
            char[] letters = word.toCharArray();
            StringBuilder not = new StringBuilder();
            int index = 0;
            int length = letters.length;

            for (; index < 5 && index < length; ++ index) {
                char letter = letters[index];

                if (Character.isWhitespace(letter)) {
                    not.append('\u00a0');
                } else {
                    not.append(letter);
                }
            }

            if (index < length) {
                not.append(letters, index, length - index);
            }

            return not.toString();
        }

        /**
         * Returns a label, or the given {@code defaultLabel} if one can't be
         * found, for the given {@code object}.
         */
        public static String getObjectLabelOrDefault(Object object, String defaultLabel) {
            State state = State.getInstance(object);

            if (state != null) {
                String label = state.getLabel();

                if (ObjectUtils.to(UUID.class, label) == null) {
                    return notTooShort(label);
                }
            }

            return notTooShort(defaultLabel);
        }

        /** Returns a label for the given {@code object}. */
        public static String getObjectLabel(Object object) {
            State state = State.getInstance(object);

            return notTooShort(state != null ? state.getLabel() : "Not Available");
        }

        /**
         * Returns a label, or the given {@code defaultLabel} if one can't be
         * found, for the type of the given {@code object}.
         */
        public static String getTypeLabelOrDefault(Object object, String defaultLabel) {
            State state = State.getInstance(object);

            if (state != null) {
                ObjectType type = state.getType();

                if (type != null) {
                    return getObjectLabel(type);
                }
            }

            return notTooShort(defaultLabel);
        }

        /** Returns a label for the type of the given {@code object}. */
        public static String getTypeLabel(Object object) {
            return getTypeLabelOrDefault(object, "Unknown Type");
        }
    }

    // --- Deprecated ---

    /** @deprecated Use {@link ToolPageContext(ServletContext, HttpServletRequest, HttpServletResponse} instead. */
    @Deprecated
    public ToolPageContext(
            Servlet servlet,
            HttpServletRequest request,
            HttpServletResponse response) {

        super(servlet, request, response);
    }

    /** @deprecated Use {@link Database.Static#getDefault} instead. */
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
     * Returns an HTML-escaped label, or the given {@code defaultLabel} if
     * one can't be found, for the given {@code object}.
     *
     * @deprecated Use {@link #getObjectLabelOrDefault} and {@link #h} instead.
     */
    @Deprecated
    public String objectLabel(Object object, String defaultLabel) {
        return h(getObjectLabelOrDefault(object, defaultLabel));
    }

    /**
     * Returns an HTML-escaped label for the given {@code object}.
     *
     * @deprecated Use {@link getObjectLabel} and {@link #h} instead.
     */
    @Deprecated
    public String objectLabel(Object object) {
        return h(getObjectLabel(object));
    }

    /**
     * Returns an HTML-escaped label, or the given {@code defaultLabel} if
     * one can't be found, for the type of the given {@code object}.
     *
     * @deprecated Use {@link #getTypeLabelOrDefault} and {@link #h} instead.
     */
    @Deprecated
    public String typeLabel(Object object, String defaultLabel) {
        return h(getTypeLabelOrDefault(object, defaultLabel));
    }

    /**
     * Returns an HTML-escaped label for the type of the given
     * {@code object}.
     *
     * @deprecated Use {@link #getTypeLabel} and {@link #h} instead.
     */
    @Deprecated
    public String typeLabel(Object object) {
        return h(getTypeLabel(object));
    }

    /** @deprecated Use {@link writeTypeSelect} instead. */
    @Deprecated
    public void typeSelect(
            Iterable<ObjectType> types,
            ObjectType selectedType,
            String allLabel,
            Object... attributes) throws IOException {

        writeTypeSelect(types, selectedType, allLabel, attributes);
    }

    /** @deprecated Use {@link writeObjectSelect} instead. */
    @Deprecated
    public void objectSelect(ObjectField field, Object value, Object... attributes) throws IOException {
        writeObjectSelect(field, value, attributes);
    }
}
