package com.psddev.cms.tool;

import com.psddev.cms.db.Template;

import com.psddev.dari.db.Record;
import com.psddev.dari.util.ObjectUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

/** Brightspot CMS. */
public class CmsTool extends Tool {

    private String companyName;
    private String extraCss;
    private String extraJavaScript;
    private String defaultSiteUrl;
    private String defaultTextOverlayCss;
    private List<CssClassGroup> textCssClassGroups;

    @Embedded
    private Template modulePreviewTemplate;

    @Embedded
    public static class CssClassGroup extends Record {

        @Required
        private String displayName;

        @Required
        private String internalName;

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

        @Required
        private String css;

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

        public String getCss() {
            return css;
        }

        public void setCss(String css) {
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

    // --- Tool support ---

    /*@Override
    public List<Plugin> getPlugins() {
        List<Plugin> plugins = new ArrayList<Plugin>();

        // Areas.
        plugins.add(createArea2("Pages & Content", "dashboard", "dashboard", "/"));
        plugins.add(createArea2("Admin", "admin", "admin", "/admin/"));
        plugins.add(createArea2("Settings", "adminSettings", "admin/adminSettings", "/admin/settings.jsp"));
        plugins.add(createArea2("Sites", "adminSites", "admin/adminSites", "/admin/sites.jsp"));
        plugins.add(createArea2("Templates & Sections", "adminTemplates", "admin/adminTemplates", "/admin/templates.jsp"));
        plugins.add(createArea2("Types", "adminTypes", "admin/adminTypes", "/admin/types.jsp"));
        plugins.add(createArea2("URLs", "adminUrls", "admin/adminUrls", "/admin/urls.jsp"));
        plugins.add(createArea2("Users & Roles", "adminUsers", "admin/adminUsers", "/admin/users.jsp"));
        plugins.add(createArea2("Variations & Profiles", "adminVariations", "admin/adminVariations", "/admin/variations.jsp"));
        plugins.add(createArea2("Workflows", "adminWorkflows", "admin/adminWorkflows", "/admin/workflows.jsp"));

        // Dashboard widgets.
        double dashboardColumn = 0.0;
        double dashboardRow = 0.0;

        plugins.add(createJspWidget("Site Map", "dashboard.siteMap", "/misc/siteMap.jsp", DASHBOARD_WIDGET_POSITION, dashboardColumn, dashboardRow ++));
        plugins.add(createJspWidget("Recent Activity", "dashboard.recentActivity", "/misc/recentActivity.jsp", DASHBOARD_WIDGET_POSITION, dashboardColumn, dashboardRow ++));

        dashboardColumn ++;
        dashboardRow = 0.0;

        plugins.add(createJspWidget("Page Builder", "dashboard.pageBuilder", "/misc/pageBuilder.jsp", DASHBOARD_WIDGET_POSITION, dashboardColumn, dashboardRow ++));
        plugins.add(createJspWidget("Schedules", "dashboard.scheduledEvents", "/misc/scheduledEvents.jsp", DASHBOARD_WIDGET_POSITION, dashboardColumn, dashboardRow ++));
        plugins.add(createJspWidget("Drafts", "dashboard.unpublishedDrafts", "/misc/unpublishedDrafts.jsp", DASHBOARD_WIDGET_POSITION, dashboardColumn, dashboardRow ++));

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
    }*/

    @Override
    public void initialize(Logger logger) throws Exception {

        // Pages & Content area.
        introducePlugin(createArea("Pages & Content", "dashboard", null, "/"));

        // Admin area.
        Area admin = createArea("Admin", "admin", null, "/admin/");
        introducePlugin(admin);

        introducePlugin(createArea("Settings", "adminSettings", admin, "/admin/settings.jsp"));
        introducePlugin(createArea("Sites", "adminSites", admin, "/admin/sites.jsp"));
        introducePlugin(createArea("Templates & Sections", "adminTemplates", admin, "/admin/templates.jsp"));
        introducePlugin(createArea("Types", "adminTypes", admin, "/admin/types.jsp"));
        introducePlugin(createArea("URLs", "adminUrls", admin, "/admin/urls.jsp"));
        introducePlugin(createArea("Users & Roles", "adminUsers", admin, "/admin/users.jsp"));
        introducePlugin(createArea("Variations & Profiles", "adminVariations", admin, "/admin/variations.jsp"));
        introducePlugin(createArea("Workflows", "adminWorkflows", admin, "/admin/workflows.jsp"));

        logger.info("Initialized the areas.");

        // Content right widgets.
        double rightColumn = 0.0;
        double rightRow = 0.0;

        JspWidget drafts = createWidget(JspWidget.class, "Drafts", "drafts", "table_multiple");
        drafts.setJsp("/WEB-INF/widget/drafts.jsp");
        drafts.addPosition(CONTENT_RIGHT_WIDGET_POSITION, rightColumn, rightRow ++);

        JspWidget schedules = createWidget(JspWidget.class, "Schedules", "schedules", "calendar");
        schedules.setJsp("/WEB-INF/widget/schedules.jsp");
        schedules.addPosition(CONTENT_RIGHT_WIDGET_POSITION, rightColumn, rightRow ++);

        JspWidget urls = createWidget(JspWidget.class, "URLs", "urls", "folder_page");
        urls.setJsp("/WEB-INF/widget/urls.jsp");
        urls.addPosition(CONTENT_RIGHT_WIDGET_POSITION, rightColumn, rightRow ++);

        JspWidget template = createWidget(JspWidget.class, "Template", "template", "layout");
        template.setJsp("/WEB-INF/widget/template.jsp");
        template.addPosition(CONTENT_RIGHT_WIDGET_POSITION, rightColumn, rightRow ++);

        JspWidget sites = createWidget(JspWidget.class, "Sites", "sites", "application_cascade");
        sites.setJsp("/WEB-INF/widget/sites.jsp");
        sites.addPosition(CONTENT_RIGHT_WIDGET_POSITION, rightColumn, rightRow ++);

        JspWidget history = createWidget(JspWidget.class, "History", "history", "table_multiple");
        history.setJsp("/WEB-INF/widget/history.jsp");
        history.addPosition(CONTENT_RIGHT_WIDGET_POSITION, rightColumn, rightRow ++);

        JspWidget references = createWidget(JspWidget.class, "References", "references", "table_multiple");
        references.setJsp("/WEB-INF/widget/references.jsp");
        references.addPosition(CONTENT_RIGHT_WIDGET_POSITION, rightColumn, rightRow ++);

        urls.getUpdateDependencies().add(template);

        introducePlugin(drafts);
        introducePlugin(schedules);
        introducePlugin(urls);
        introducePlugin(template);
        introducePlugin(sites);
        introducePlugin(history);
        introducePlugin(references);

        // Content bottom widgets.
        double bottomColumn = 0.0;
        double bottomRow = 0.0;

        JspWidget seo = createWidget(JspWidget.class, "Search Engine Optimization", "seo", "page_white_magnify");
        seo.setJsp("/WEB-INF/widget/seo.jsp");
        seo.addPosition(CONTENT_BOTTOM_WIDGET_POSITION, bottomColumn, bottomRow ++);

        introducePlugin(seo);

        logger.info("Initialized the widgets.");
    }
}
