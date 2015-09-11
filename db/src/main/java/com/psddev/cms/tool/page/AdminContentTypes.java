package com.psddev.cms.tool.page;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;

import com.psddev.cms.db.ContentField;
import com.psddev.cms.db.ContentType;
import com.psddev.cms.db.ToolUi;
import com.psddev.cms.tool.PageServlet;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.db.Database;
import com.psddev.dari.db.ObjectField;
import com.psddev.dari.db.ObjectType;
import com.psddev.dari.db.Query;
import com.psddev.dari.util.RoutingFilter;

@RoutingFilter.Path(application = "cms", value = "adminContentTypes")
@SuppressWarnings("serial")
public class AdminContentTypes extends PageServlet {

    @Override
    protected String getPermissionId() {
        return "area/admin/contentTypes";
    }

    @Override
    protected void doService(ToolPageContext page) throws IOException, ServletException {
        List<ObjectType> types = new ArrayList<ObjectType>(Database.Static.getDefault().getEnvironment().getTypes());
        UUID selectedId = page.param(UUID.class, "typeId");
        ObjectType selected = null;
        ContentType sct = null;

        for (ObjectType t : types) {
            if (t.getId().equals(selectedId)) {
                selected = t;
                break;
            }
        }

        if (selected != null) {
            sct = Query.from(ContentType.class).where("internalName = ?", selected.getInternalName()).first();

            if (page.isFormPost()) {
                if (sct == null) {
                    sct = new ContentType();
                    sct.getState().setId(page.param(UUID.class, "id"));
                }

                if (page.tryStandardUpdate(sct)) {
                    return;
                }

            } else if (sct == null) {
                sct = new ContentType();

                sct.setDisplayName(selected.getDisplayName());
                sct.setInternalName(selected.getInternalName());
            }

            for (ObjectField of : selected.getFields()) {
                ToolUi ui = of.as(ToolUi.class);

                if (!ui.isHidden()) {
                    boolean found = false;
                    for (ContentField cf : sct.getFields()) {
                        if (of.getInternalName().equals(cf.getInternalName())) {
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        ContentField cf = new ContentField();

                        cf.setTab(ui.getTab());
                        cf.setDisplayName(of.getDisplayName());
                        cf.setInternalName(of.getInternalName());
                        sct.getFields().add(cf);
                    }
                }
            }

            for (Iterator<ContentField> i = sct.getFields().iterator(); i.hasNext();) {
                ContentField cf = i.next();
                boolean found = false;

                for (ObjectField of : selected.getFields()) {
                    if (of.getInternalName().equals(cf.getInternalName())) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    i.remove();
                }
            }
        }

        Collections.sort(types);

        page.writeHeader();
            page.writeStart("div", "class", "withLeftNav");
                page.writeStart("div", "class", "leftNav");
                    page.writeStart("div", "class", "widget");
                        page.writeStart("h1", "class");
                            page.writeHtml(page.localize(AdminContentTypes.class, "title"));
                        page.writeEnd();

                        page.writeStart("ul", "class", "links");
                            for (ObjectType t : types) {
                                if (t.isAbstract()) {
                                    continue;
                                }

                                page.writeStart("li", "class", t.equals(selected) ? "selected" : null);
                                    page.writeStart("a", "href", page.url(null, "typeId", t.getId()));
                                        page.writeObjectLabel(t);
                                    page.writeEnd();
                                page.writeEnd();
                            }
                        page.writeEnd();
                    page.writeEnd();
                page.writeEnd();

                if (sct != null) {
                    page.writeStart("div", "class", "main");
                        page.writeStart("div", "class", "widget");
                            page.writeStandardForm(sct, false);
                        page.writeEnd();
                    page.writeEnd();
                }
            page.writeEnd();
        page.writeFooter();
    }
}
