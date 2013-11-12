package com.psddev.cms.tool.page;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.ServletException;

import com.psddev.cms.db.PageFilter;
import com.psddev.cms.db.Schedule;
import com.psddev.cms.tool.PageServlet;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.db.Query;
import com.psddev.dari.db.State;
import com.psddev.dari.util.RoutingFilter;

@RoutingFilter.Path(application = "cms", value = "inlineEditor")
@SuppressWarnings("serial")
public class InlineEditor extends PageServlet {

    @Override
    protected String getPermissionId() {
        return null;
    }

    @Override
    protected void doService(final ToolPageContext page) throws IOException, ServletException {
        UUID mainId = page.param(UUID.class, "id");
        Object mainObject = Query.
                fromAll().
                where("_id = ?", mainId).
                first();

        if (mainObject == null) {
            throw new IllegalArgumentException(String.format(
                    "No object with ID! [%s]",
                    mainId));
        }

        Schedule currentSchedule = page.getUser().getCurrentSchedule();

        page.writeHeader();
            page.writeStart("style", "type", "text/css");
                page.writeCss(".toolBroadcast, .toolHeader, .toolFooter", "display", "none");
                page.writeCss("body, .toolContent", "background", "transparent");
                page.writeCss("body", "margin-top", "75px !important");
            page.writeEnd();

            page.writeStart("ul", "class", "inlineEditorControls inlineEditorControls-main");
                page.writeStart("li", "class", "inlineEditorLogo");
                    page.writeStart("a",
                            "target", "_blank",
                            "href", page.cmsUrl("/"));
                        page.writeTag("img",
                                "src", page.cmsUrl("/style/brightspot.png"),
                                "alt", "Brightspot",
                                "width", 104,
                                "height", 14);
                    page.writeEnd();
                page.writeEnd();

                if (currentSchedule != null) {
                    page.writeStart("li");
                        page.writeStart("a",
                                "class", "icon icon-action-schedule",
                                "target", "scheduleEdit",
                                "href", page.cmsUrl("/scheduleEdit", "id", currentSchedule.getId()));
                            page.writeHtml("Current Schedule: ");
                            page.writeObjectLabel(currentSchedule);
                        page.writeEnd();
                    page.writeEnd();
                }

                page.writeStart("li");
                    page.writeStart("a",
                            "class", "icon icon-action-edit",
                            "target", "contentEdit",
                            "href", page.cmsUrl("/content/edit.jsp", "id", State.getInstance(mainObject).getId()));
                        page.writeTypeObjectLabel(mainObject);
                    page.writeEnd();
                page.writeEnd();

                page.writeStart("li");
                    page.writeStart("a",
                            "class", "icon icon-action-delete icon-only",
                            "style", page.cssString("color", "#ff0e40"),
                            "onclick", "$(window.parent.document.body).find('.cms-inlineEditor').remove(); return false;");
                        page.writeHtml("Remove");
                    page.writeEnd();
                page.writeEnd();
            page.writeEnd();

            if (PageFilter.Static.isInlineEditingAllContents(page.getRequest())) {
                page.writeStart("script",
                        "type", "text/javascript",
                        "src", page.cmsUrl("/script/inlineeditor.js"));
                page.writeEnd();
            }
        page.writeFooter();
    }
}
