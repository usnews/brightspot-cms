package com.psddev.cms.tool.page;

import java.io.IOException;

import javax.servlet.ServletException;

import com.psddev.cms.db.Content;
import com.psddev.cms.tool.PageServlet;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.db.ObjectType;
import com.psddev.dari.util.RoutingFilter;

@RoutingFilter.Path(application = "cms", value = "/bulkUpload")
@SuppressWarnings("serial")
public class BulkUpload extends PageServlet {

    @Override
    protected String getPermissionId() {
        return "area/dashboard";
    }

    @Override
    protected void doService(final ToolPageContext page) throws IOException, ServletException {
        page.getWriter();

        page.writeStart("div", "class", "widget upload-droppable");
            page.writeStart("h1", "class", "icon icon-action-upload").writeHtml("Bulk Upload").writeEnd();
            page.writeStart("div", "class", "message");
                page.writeHtml("Drag and drop or ");
                page.writeStart("a",
                        "class", "upload-link",
                        "href", page.url("/content/uploadFiles", "typeId", ObjectType.getInstance(Content.class).getId()),
                        "target", "uploadFiles");
                    page.writeHtml("select");
                page.writeEnd();
                page.writeHtml(" files.");
            page.writeEnd();
        page.writeEnd();
    }
}
