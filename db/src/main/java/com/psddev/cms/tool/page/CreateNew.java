package com.psddev.cms.tool.page;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

import com.psddev.cms.db.Directory;
import com.psddev.cms.db.Template;
import com.psddev.cms.tool.PageServlet;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.db.ObjectType;
import com.psddev.dari.db.Query;
import com.psddev.dari.db.State;
import com.psddev.dari.util.RoutingFilter;

@RoutingFilter.Path(application = "cms", value = "/createNew")
@SuppressWarnings("serial")
public class CreateNew extends PageServlet {

    @Override
    protected String getPermissionId() {
        return "area/dashboard";
    }

    @Override
    protected void doService(final ToolPageContext page) throws IOException, ServletException {
        page.getWriter();

        Map<ObjectType, List<Template>> typeTemplates = new HashMap<ObjectType, List<Template>>();

        for (Template template : Query.
                from(Template.class).
                where(page.siteItemsPredicate()).
                sortAscending("name").
                selectAll()) {

            for (ObjectType type : template.getContentTypes()) {
                List<Template> templates = typeTemplates.get(type);

                if (templates ==  null) {
                    templates = new ArrayList<Template>();
                    typeTemplates.put(type, templates);
                }

                templates.add(template);
            }
        }

        List<ObjectType> types = new ArrayList<ObjectType>(typeTemplates.keySet());
        Collections.sort(types);

        page.writeStart("div", "class", "widget");
            page.writeStart("h1", "class", "icon icon-plus-sign").writeHtml("Create New").writeEnd();

            page.writeStart("ul", "class", "links pageThumbnails");
                for (ObjectType type : types) {
                    List<Template> templates = typeTemplates.get(type);

                    if (templates.size() == 1) {
                        writeListItem(page, type, templates.get(0), false);

                    } else {
                        for (Template template : templates) {
                            writeListItem(page, type, template, true);
                        }
                    }
                }

            page.writeEnd();
        page.writeEnd();
    }

    private void writeListItem(
            ToolPageContext page,
            ObjectType type,
            Template template,
            boolean multiple)
            throws IOException {

        State state = State.getInstance(Query.fromType(type).where("cms.template.default = ?", template).first());
        String permalink = null;

        if (state != null) {
            permalink = state.as(Directory.ObjectModification.class).getPermalink();
        }

        page.writeStart("li", "data-preview-url", permalink);
            page.writeStart("a",
                    "target", "_top",
                    "href", page.url("/content/edit.jsp",
                            "typeId", type.getId(),
                            "templateId", template.getId()));

                page.writeHtml(page.getObjectLabel(type));

                if (multiple) {
                    page.writeHtml(" - ");
                    page.writeHtml(page.getObjectLabel(template));
                }

            page.writeEnd();
        page.writeEnd();
    }
}
