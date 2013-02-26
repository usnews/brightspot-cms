package com.psddev.cms.tool;

import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.psddev.cms.db.Template;
import com.psddev.cms.db.ToolRole;
import com.psddev.dari.db.Record;
import com.psddev.dari.util.HtmlWriter;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.StorageItem;

/** Brightspot CMS. */
public class CmsTool extends Tool {

    private static final String ATTRIBUTE_PREFIX = CmsTool.class.getName() + ".";
    private static final String CSS_WRITTEN = ATTRIBUTE_PREFIX + ".cssWritten";

    private String companyName;
    private StorageItem companyLogo;
    private String environment;
    private ToolRole defaultRole;
    private Set<String> disabledPlugins;
    private String extraCss;
    private String extraJavaScript;
    private String defaultSiteUrl;
    private String defaultTextOverlayCss;
    private List<CssClassGroup> textCssClassGroups;
    private List<ResourceItem> resources;

    @Embedded
    private Template modulePreviewTemplate;

    @Embedded
    public static class CssClassGroup extends Record {

        @Required
        private String displayName;

        @Required
        private String internalName;

        private boolean dropDown;
        private List<CssClass> cssClasses;

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getInternalName() {
            return internalName;
        }

        public void setInternalName(String internalName) {
            this.internalName = internalName;
        }

        public boolean isDropDown() {
            return dropDown;
        }

        public void setDropDown(boolean dropDown) {
            this.dropDown = dropDown;
        }

        public List<CssClass> getCssClasses() {
            if (cssClasses == null) {
                cssClasses = new ArrayList<CssClass>();
            }
            return cssClasses;
        }

        public void setCssClasses(List<CssClass> cssClasses) {
            this.cssClasses = cssClasses;
        }
    }

    @Embedded
    public static class CssClass extends Record {

        @Required
        private String displayName;

        @Required
        private String internalName;

        private String tag;
        private String css;
        private String cmsOnlyCss;

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getInternalName() {
            return internalName;
        }

        public void setInternalName(String internalName) {
            this.internalName = internalName;
        }

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }

        public String getCss() {
            return css;
        }

        public void setCss(String css) {
            this.css = css;
        }

        public String getCmsOnlyCss() {
            return cmsOnlyCss;
        }

        public void setCmsOnlyCss(String cmsOnlyCss) {
            this.cmsOnlyCss = cmsOnlyCss;
        }
    }

    /** Returns the company name. */
    public String getCompanyName() {
        return companyName;
    }

    /** Sets the company name. */
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    /** Returns the company logo. */
    public StorageItem getCompanyLogo() {
        return companyLogo;
    }

    /** Sets the company logo. */
    public void setCompanyLogo(StorageItem companyLogo) {
        this.companyLogo = companyLogo;
    }

    /** Returns the environment. */
    public String getEnvironment() {
        return environment;
    }

    /** Sets the environment. */
    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    /**
     * Returns the default role.
     *
     * @return May be {@code null}.
     */
    public ToolRole getDefaultRole() {
        return defaultRole;
    }

    /**
     * Sets the default role.
     *
     * @param defaultRole May be {@code null}.
     */
    public void setDefaultRole(ToolRole defaultRole) {
        this.defaultRole = defaultRole;
    }

    /**
     * Returns the set of disabled plugin names.
     *
     * @return Never {@code null}.
     */
    public Set<String> getDisabledPlugins() {
        if (disabledPlugins == null) {
            disabledPlugins = new HashSet<String>();
        }
        return disabledPlugins;
    }

    /** Sets the set of disabled plugin names. */
    public void setDisabledPlugins(Set<String> disabledPlugins) {
        this.disabledPlugins = disabledPlugins;
    }

    /** Returns the extra CSS. */
    public String getExtraCss() {
        return extraCss;
    }

    /** Sets the extra CSS. */
    public void setExtraCss(String css) {
        this.extraCss = css;
    }

    /** Returns the extra JavaScript. */
    public String getExtraJavaScript() {
        return extraJavaScript;
    }

    /** Sets the extra JavaScript. */
    public void setExtraJavaScript(String script) {
        this.extraJavaScript = script;
    }

    /** Returns the default site URL. */
    public String getDefaultSiteUrl() {
        return defaultSiteUrl;
    }

    /** Sets the default site URL. */
    public void setDefaultSiteUrl(String url) {
        this.defaultSiteUrl = url;
    }

    public String getDefaultTextOverlayCss() {
        return defaultTextOverlayCss;
    }

    public void setDefaultTextOverlayCss(String defaultTextOverlayCss) {
        this.defaultTextOverlayCss = defaultTextOverlayCss;
    }

    public List<CssClassGroup> getTextCssClassGroups() {
        if (textCssClassGroups == null) {
            textCssClassGroups = new ArrayList<CssClassGroup>();
        }
        return textCssClassGroups;
    }

    public void setTextCssClassGroups(List<CssClassGroup> textCssClassGroups) {
        this.textCssClassGroups = textCssClassGroups;
    }

    public List<ResourceItem> getResources() {
        if (resources == null) {
            resources = new ArrayList<ResourceItem>();
        }
        return resources;
    }

    public void setResources(List<ResourceItem> resources) {
        this.resources = resources;
    }

    public Template getModulePreviewTemplate() {
        return modulePreviewTemplate;
    }

    public void setModulePreviewTemplate(Template modulePreviewTemplate) {
        this.modulePreviewTemplate = modulePreviewTemplate;
    }

    /** Returns the preview URL. */
    public String getPreviewUrl() {
        String url = getDefaultSiteUrl();
        if (!ObjectUtils.isBlank(url)) {
            try {
                return new URI(url + "/").resolve("./_preview").toString();
            } catch (Exception ex) {
            }
        }
        return "/_preview";
    }

    /** Writes the custom CSS to the given {@code out}. */
    public void writeCss(HttpServletRequest request, Writer out) throws IOException {
        if (request.getAttribute(CSS_WRITTEN) != null) {
            return;
        }

        request.setAttribute(CSS_WRITTEN, Boolean.TRUE);

        @SuppressWarnings("all")
        HtmlWriter writer = new HtmlWriter(out);

        writer.start("style", "type", "text/css");
            writer.css(".cms-textAlign-left", "text-align", "left");
            writer.css(".cms-textAlign-center", "text-align", "center");
            writer.css(".cms-textAlign-right", "text-align", "right");

            for (CmsTool.CssClassGroup group : getTextCssClassGroups()) {
                String groupName = group.getInternalName();

                for (CmsTool.CssClass cssClass : group.getCssClasses()) {
                    String css = cssClass.getCss();

                    if (css != null) {
                        writer.write(".cms-");
                        writer.write(groupName);
                        writer.write("-");
                        writer.write(cssClass.getInternalName());
                        writer.write("{");
                        writer.write(cssClass.getCss());
                        writer.write("}");
                    }
                }
            }

        writer.end();
    }

    // --- Tool support ---

    @Override
    public List<Plugin> getPlugins() {
        List<Plugin> plugins = new ArrayList<Plugin>();

        // Areas.
        plugins.add(createArea2("Pages & Content", "dashboard", "dashboard", "/"));
        plugins.add(createArea2("Admin", "admin", "admin", "/admin/"));
        plugins.add(createArea2("Production Guides", "adminGuides", "admin/adminGuides", "/admin/guides.jsp"));
        plugins.add(createArea2("Settings", "adminSettings", "admin/adminSettings", "/admin/settings.jsp"));
        plugins.add(createArea2("Sites", "adminSites", "admin/adminSites", "/admin/sites.jsp"));
        plugins.add(createArea2("Templates & Sections", "adminTemplates", "admin/adminTemplates", "/admin/templates.jsp"));
        plugins.add(createArea2("URLs", "adminUrls", "admin/adminUrls", "/admin/urls.jsp"));
        plugins.add(createArea2("Users & Roles", "adminUsers", "admin/adminUsers", "/admin/users.jsp"));
        plugins.add(createArea2("Variations & Profiles", "adminVariations", "admin/adminVariations", "/admin/variations.jsp"));
        plugins.add(createArea2("Workflows", "adminWorkflows", "admin/adminWorkflows", "/admin/workflows.jsp"));

        // Dashboard widgets.
        double dashboardColumn = 0.0;
        double dashboardRow = 0.0;

        plugins.add(createJspWidget("Work Streams", "dashboard.workStreams", "/workStreams", DASHBOARD_WIDGET_POSITION, dashboardColumn, dashboardRow ++));
        plugins.add(createJspWidget("Site Map", "dashboard.siteMap", "/misc/siteMap.jsp", DASHBOARD_WIDGET_POSITION, dashboardColumn, dashboardRow ++));
        plugins.add(createJspWidget("Recent Activity", "dashboard.recentActivity", "/misc/recentActivity.jsp", DASHBOARD_WIDGET_POSITION, dashboardColumn, dashboardRow ++));

        dashboardColumn ++;
        dashboardRow = 0.0;

        plugins.add(createJspWidget("Create New", "dashboard.createNew", "/createNew", DASHBOARD_WIDGET_POSITION, dashboardColumn, dashboardRow ++));
        plugins.add(createJspWidget("Bulk Upload", "dashboard.bulkUpload", "/bulkUpload", DASHBOARD_WIDGET_POSITION, dashboardColumn, dashboardRow ++));
        plugins.add(createJspWidget("Schedules", "dashboard.scheduledEvents", "/misc/scheduledEvents.jsp", DASHBOARD_WIDGET_POSITION, dashboardColumn, dashboardRow ++));
        plugins.add(createJspWidget("Drafts", "dashboard.unpublishedDrafts", "/misc/unpublishedDrafts.jsp", DASHBOARD_WIDGET_POSITION, dashboardColumn, dashboardRow ++));
        plugins.add(createJspWidget("Resources", "dashboard.resources", "/resources", DASHBOARD_WIDGET_POSITION, dashboardColumn, dashboardRow ++));

        // Content right widgets.
        double rightColumn = 0.0;
        double rightRow = 0.0;
        JspWidget template, urls;

        plugins.add(createJspWidget("Drafts", "drafts", "/WEB-INF/widget/drafts.jsp", CONTENT_RIGHT_WIDGET_POSITION, rightColumn, rightRow ++));
        plugins.add(createJspWidget("Schedules", "schedules", "/WEB-INF/widget/schedules.jsp", CONTENT_RIGHT_WIDGET_POSITION, rightColumn, rightRow ++));
        plugins.add(urls = createJspWidget("URLs", "urls", "/WEB-INF/widget/urls.jsp", CONTENT_RIGHT_WIDGET_POSITION, rightColumn, rightRow ++));
        plugins.add(template = createJspWidget("Template", "template", "/WEB-INF/widget/template.jsp", CONTENT_RIGHT_WIDGET_POSITION, rightColumn, rightRow ++));
        plugins.add(createJspWidget("Sites", "sites", "/WEB-INF/widget/sites.jsp", CONTENT_RIGHT_WIDGET_POSITION, rightColumn, rightRow ++));
        plugins.add(createJspWidget("History", "history", "/WEB-INF/widget/history.jsp", CONTENT_RIGHT_WIDGET_POSITION, rightColumn, rightRow ++));
        plugins.add(createJspWidget("References", "references", "/WEB-INF/widget/references.jsp", CONTENT_RIGHT_WIDGET_POSITION, rightColumn, rightRow ++));

        urls.getUpdateDependencies().add(template);

        // Content bottom widgets.
        double bottomColumn = 0.0;
        double bottomRow = 0.0;

        plugins.add(createJspWidget("Search Engine Optimization", "seo", "/WEB-INF/widget/seo.jsp", CONTENT_BOTTOM_WIDGET_POSITION, bottomColumn, bottomRow ++));

        return plugins;
    }

    @Embedded
    public static abstract class ResourceItem extends Record {

        private String name;
        private boolean sameWindow;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isSameWindow() {
            return sameWindow;
        }

        public void setSameWindow(boolean sameWindow) {
            this.sameWindow = sameWindow;
        }

        public abstract String getUrl();
    }

    public static class ResourceLink extends ResourceItem {

        private String url;

        @Override
        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public static class ResourceFile extends ResourceItem {

        private StorageItem file;

        public StorageItem getFile() {
            return file;
        }

        public void setFile(StorageItem file) {
            this.file = file;
        }

        public String getUrl() {
            StorageItem file = getFile();
            return file != null ? file.getPublicUrl() : null;
        }
    }
}
