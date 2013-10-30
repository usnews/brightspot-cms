package com.psddev.cms.tool.page;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import javax.servlet.ServletException;

import com.psddev.cms.db.Directory;
import com.psddev.cms.db.Renderer;
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
        String returnUrl = page.param(String.class, "returnUrl");

        page.writeHeader();
            page.writeStart("div", "class", "widget");
                page.writeStart("h1", "class", "icon icon-wrench");
                    page.writeHtml("Tools");
                page.writeEnd();

                page.writeStart("ul", "class", "links");
                    if (object != null) {
                        ObjectType type = State.getInstance(object).getType();

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
                        }

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
                page.writeEnd();

                if (object != null) {
                    State state = State.getInstance(object);
                    ObjectType type = state.getType();

                    if (type != null &&
                            !ObjectUtils.isBlank(type.as(Renderer.TypeModification.class).getEmbedPath())) {
                        String permalink = State.getInstance(object).as(Directory.ObjectModification.class).getPermalink();

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
