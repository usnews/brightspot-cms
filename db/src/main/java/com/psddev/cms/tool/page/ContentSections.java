package com.psddev.cms.tool.page;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.ServletException;

import com.psddev.cms.db.ContentSection;
import com.psddev.cms.db.Page;
import com.psddev.cms.db.Section;
import com.psddev.cms.tool.PageServlet;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.db.Query;
import com.psddev.dari.db.State;
import com.psddev.dari.util.RoutingFilter;

@RoutingFilter.Path(application = "cms", value = "/content/editableSections.jsp")
@SuppressWarnings("serial")
public class ContentSections extends PageServlet {

    @Override
    protected String getPermissionId() {
        return null;
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void doService(final ToolPageContext page) throws IOException, ServletException {
        Page mainObject = Query.findById(Page.class, page.param(UUID.class, "id"));

        page.writeHeader();
            page.writeStart("div", "class", "widget");
                page.writeStart("h1", "class", "icon icon-object-template").writeHtml("Editable Sections").writeEnd();

                page.writeStart("ul", "class", "links");
                    page.writeStart("li");
                        page.writeStart("a",
                                "href", page.returnUrl("sectionId", null),
                                "target", "_top");
                            page.writeHtml("Layout");
                        page.writeEnd();
                    page.writeEnd();

                    if (mainObject != null) {
                        for (Section section : mainObject.findSections()) {
                            if (section instanceof ContentSection) {
                                State content = State.getInstance(((ContentSection) section).getContent());

                                if (content != null) {
                                    page.writeStart("li");
                                        page.writeStart("a",
                                                "href", page.returnUrl("sectionId", section.getId(), "contentId", content.getId()),
                                                "target", "_top");
                                            page.writeHtml("Section: ");
                                            page.writeHtml(page.getObjectLabelOrDefault(section, "Unnamed"));
                                            page.writeHtml(" (");
                                            page.writeHtml(page.getTypeLabel(content));
                                            page.writeHtml(")");
                                        page.writeEnd();
                                    page.writeEnd();
                                }
                            }
                        }
                    }
                page.writeEnd();
            page.writeEnd();
        page.writeFooter();
    }
}
