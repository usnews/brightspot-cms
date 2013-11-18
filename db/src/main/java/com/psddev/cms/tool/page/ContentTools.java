package com.psddev.cms.tool.page;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletException;

import com.psddev.cms.db.Content;
import com.psddev.cms.db.Directory;
import com.psddev.cms.db.Renderer;
import com.psddev.cms.db.ToolUser;
import com.psddev.cms.tool.CmsTool;
import com.psddev.cms.tool.PageServlet;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.db.Application;
import com.psddev.dari.db.ObjectType;
import com.psddev.dari.db.Query;
import com.psddev.dari.db.State;
import com.psddev.dari.util.CodeUtils;
import com.psddev.dari.util.DebugFilter;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.RoutingFilter;
import com.psddev.dari.util.StringUtils;

@RoutingFilter.Path(application = "cms", value = "contentTools")
@SuppressWarnings("serial")
public class ContentTools extends PageServlet {

    @Override
    protected String getPermissionId() {
        return null;
    }

    @Override
    protected void doService(ToolPageContext page) throws IOException, ServletException {
        Object object = Query.from(Object.class).where("_id = ?", page.param(UUID.class, "id")).first();
        State state = State.getInstance(object);
        String returnUrl = page.param(String.class, "returnUrl");

        page.writeHeader();
            page.writeStart("div", "class", "widget");
                page.writeStart("h1", "class", "icon icon-wrench");
                    page.writeHtml("Tools");
                page.writeEnd();

                if (object != null) {
                    Content.ObjectModification contentData = state.as(Content.ObjectModification.class);
                    Date publishDate = contentData.getPublishDate();
                    ToolUser publishUser = contentData.getPublishUser();
                    Date updateDate = contentData.getUpdateDate();
                    ToolUser updateUser = contentData.getUpdateUser();

                    page.writeStart("h2");
                        page.writeHtml("For Editors");
                    page.writeEnd();

                    page.writeStart("ul");
                        if (publishDate != null || publishUser != null) {
                            page.writeStart("li");
                                page.writeHtml("Published: ");

                                if (publishDate != null) {
                                    page.writeHtml(page.formatUserDateTime(publishDate));
                                }

                                if (publishUser != null) {
                                    page.writeHtml(publishDate != null ? " by " : "By ");
                                    page.writeObjectLabel(updateUser);
                                }
                            page.writeEnd();
                        }

                        if (updateDate != null || updateUser != null) {
                            page.writeStart("li");
                                page.writeHtml("Last Updated: ");

                                if (updateDate != null) {
                                    page.writeHtml(page.formatUserDateTime(updateDate));
                                }

                                if (updateUser != null) {
                                    page.writeHtml(updateDate != null ? " by " : "By ");
                                    page.writeObjectLabel(updateUser);
                                }
                            page.writeEnd();
                        }
                    page.writeEnd();
                }

                page.writeStart("h2");
                    page.writeHtml("For Developers");
                page.writeEnd();

                page.writeStart("ul");
                    if (object != null) {
                        page.writeStart("li");
                            page.writeStart("a",
                                    "target", "_blank",
                                    "href", page.objectUrl("/contentRaw", object));
                                page.writeHtml("View Raw Data");
                            page.writeEnd();
                        page.writeEnd();
                    }

                    if (!ObjectUtils.isBlank(returnUrl)) {
                        page.writeStart("li");
                            if (ObjectUtils.to(boolean.class, StringUtils.getQueryParameterValue(returnUrl, "deprecated"))) {
                                page.writeStart("a",
                                        "target", "_top",
                                        "href", StringUtils.addQueryParameters(returnUrl,
                                                "deprecated", null));
                                    page.writeHtml("Hide Deprecated Fields");
                                page.writeEnd();

                            } else {
                                page.writeStart("a",
                                        "target", "_top",
                                        "href", StringUtils.addQueryParameters(returnUrl,
                                                "deprecated", true));
                                    page.writeHtml("Show Deprecated Fields");
                                page.writeEnd();
                            }
                        page.writeEnd();
                    }

                    if (object != null) {
                        ObjectType type = state.getType();

                        if (type != null) {
                            String className = type.getObjectClassName();
                            File source = CodeUtils.getSource(className);

                            page.writeStart("li");
                                if (source != null) {
                                    page.writeStart("a",
                                            "target", "_blank",
                                            "href", DebugFilter.Static.getServletPath(page.getRequest(), "code",
                                                    "file", source));
                                        page.writeHtml("View Source Class: ");
                                        page.writeHtml(className);
                                    page.writeEnd();

                                } else {
                                    page.writeHtml("Source Class: ");
                                    page.writeHtml(className);
                                }
                            page.writeEnd();

                            String defaultPath = type.as(Renderer.TypeModification.class).getPath();
                            Map<String, String> paths = type.as(Renderer.TypeModification.class).getPaths();

                            if (!ObjectUtils.isBlank(defaultPath) || !ObjectUtils.isBlank(paths)) {
                                page.writeStart("li");
                                    page.writeHtml("Renderers:");

                                    page.writeStart("ul");
                                        if (!ObjectUtils.isBlank(defaultPath)) {
                                            page.writeStart("li");
                                                page.writeStart("a",
                                                        "target", "_blank",
                                                        "href", DebugFilter.Static.getServletPath(page.getRequest(), "code",
                                                                "action", "edit",
                                                                "type", "JSP",
                                                                "servletPath", defaultPath));
                                                    page.writeHtml("View Default: ");
                                                    page.writeHtml(defaultPath);
                                                page.writeEnd();
                                            page.writeEnd();
                                        }

                                        for (Map.Entry<String, String> entry : paths.entrySet()) {
                                            page.writeStart("li");
                                                page.writeStart("a",
                                                        "target", "_blank",
                                                        "href", DebugFilter.Static.getServletPath(page.getRequest(), "code",
                                                                "action", "edit",
                                                                "type", "JSP",
                                                                "servletPath", entry.getValue()));
                                                    page.writeHtml("View ");
                                                    page.writeHtml(entry.getKey());
                                                    page.writeHtml(": ");
                                                    page.writeHtml(entry.getValue());
                                                page.writeEnd();
                                            page.writeEnd();
                                        }
                                    page.writeEnd();
                                page.writeEnd();
                            }
                        }
                    }
                page.writeEnd();

                if (object != null) {
                    ObjectType type = state.getType();

                    if (type != null &&
                            !ObjectUtils.isBlank(type.as(Renderer.TypeModification.class).getEmbedPath())) {
                        String permalink = state.as(Directory.ObjectModification.class).getPermalink();

                        if (!ObjectUtils.isBlank(permalink)) {
                            String siteUrl = Application.Static.getInstance(CmsTool.class).getDefaultSiteUrl();
                            StringBuilder embedCode = new StringBuilder();

                            embedCode.append("<script type=\"text/javascript\" src=\"");
                            embedCode.append(StringUtils.addQueryParameters(
                                    StringUtils.removeEnd(siteUrl, "/") + permalink,
                                    "_embed", true,
                                    "_format", "js"));
                            embedCode.append("\"></script>");

                            page.writeHtml("Embed Code:");
                            page.writeTag("br");
                            page.writeStart("textarea",
                                    "class", "code",
                                    "data-expandable-class", "code",
                                    "readonly", "readonly",
                                    "onclick", "this.select();");
                                page.writeHtml(embedCode);
                            page.writeEnd();
                        }
                    }
                }
            page.writeEnd();
        page.writeFooter();
    }
}
