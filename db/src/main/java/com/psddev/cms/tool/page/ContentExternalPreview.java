package com.psddev.cms.tool.page;

import java.io.IOException;

import javax.servlet.ServletException;

import com.psddev.cms.tool.PageServlet;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.util.RoutingFilter;
import com.psddev.dari.util.StringUtils;

@RoutingFilter.Path(application = "cms", value = "/content/externalPreview")
@SuppressWarnings("serial")
public class ContentExternalPreview extends PageServlet {

    @Override
    protected String getPermissionId() {
        return null;
    }

    @Override
    protected void doService(final ToolPageContext page) throws IOException, ServletException {
        String frameId = page.createId();

        page.writeHeader();
            page.writeStart("div", "class", "widget");
                page.writeStart("h1",
                        "class", "icon icon-action-preview");
                    page.writeHtml("External Content Preview");
                page.writeEnd();

                page.writeStart("iframe",
                        "id", frameId,
                        "scrolling", "no",
                        "src", page.cmsUrl("/content/externalPreviewFrame", "url", page.param(String.class, "url")),
                        "style", page.cssString(
                                "border-style", "none",
                                "width", "100%"));
                page.writeEnd();

                page.writeStart("script", "type", "text/javascript");
                    page.writeRaw("(function(window, undefined) {");
                        page.writeRaw("setInterval(function() {");
                            page.writeRaw("var f = $('#");
                            page.writeRaw(StringUtils.escapeJavaScript(frameId));
                            page.writeRaw("')[0];");

                            page.writeRaw("f.style.height = f.contentDocument.body.scrollHeight + 'px';");
                        page.writeRaw("}, 500);");
                    page.writeRaw("})(window);");
                page.writeEnd();
            page.writeEnd();
        page.writeFooter();
    }
}
