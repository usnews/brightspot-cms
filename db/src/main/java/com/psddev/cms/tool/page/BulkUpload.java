package com.psddev.cms.tool.page;

import java.io.IOException;

import javax.servlet.ServletException;

import com.psddev.cms.db.Content;
import com.psddev.cms.db.Site;
import com.psddev.cms.db.ToolUi;
import com.psddev.cms.tool.CmsTool;
import com.psddev.cms.tool.PageServlet;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.db.ObjectField;
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
        boolean hasUploadable = false;

        for (ObjectType t : ObjectType.getInstance(Content.class).as(ToolUi.class).findDisplayTypes()) {
            for (ObjectField field : t.getFields()) {
                if (ObjectField.FILE_TYPE.equals(field.getInternalItemType())) {
                    hasUploadable = true;
                    break;
                }
            }
        }

        CmsTool.BulkUploadSettings settings = null;
        Site site = page.getSite();

        if (site != null) {
            settings = site.getBulkUploadSettings();
        }

        if (settings == null) {
            settings = page.getCmsTool().getBulkUploadSettings();
        }

        ObjectType defaultType = settings != null ? settings.getDefaultType() : null;

        page.writeStart("div", "class", "widget uploadable");
            page.writeStart("h1", "class", "icon icon-action-upload").writeHtml("Bulk Upload").writeEnd();
            page.writeStart("div", "class", "message message-info");

                if (!hasUploadable) {
                    page.writeHtml("There aren't any content types that can be uploaded in bulk.");

                } else {
                    page.writeHtml("Drag and drop or ");
                    page.writeStart("a",
                            "class", "uploadableLink",
                            "target", "uploadFiles",
                            "href", page.url("/content/uploadFiles",
                                    "typeId", ObjectType.getInstance(Content.class).getId(),
                                    "type", defaultType != null ? defaultType.getId() : null));
                        page.writeHtml("select");
                    page.writeEnd();
                    page.writeHtml(" files.");
                }

            page.writeEnd();
        page.writeEnd();
    }
}
