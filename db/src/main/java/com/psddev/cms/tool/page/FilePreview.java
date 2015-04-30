package com.psddev.cms.tool.page;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import com.psddev.cms.db.ToolUi;
import com.psddev.cms.tool.FileContentType;
import com.psddev.cms.tool.PageServlet;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.db.ObjectField;
import com.psddev.dari.db.State;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.RoutingFilter;
import com.psddev.dari.util.StorageItem;
import com.psddev.dari.util.StringUtils;

@RoutingFilter.Path(application = "cms", value = "filePreview")
public class FilePreview extends PageServlet {

    public static void reallyDoService(ToolPageContext page) throws IOException, ServletException {

        HttpServletRequest request = page.getRequest();
        State state = State.getInstance(request.getAttribute("object"));
        ObjectField field = (ObjectField) request.getAttribute("field");
        String inputName = ObjectUtils.firstNonBlank(page.param(String.class, "inputName"), (String) request.getAttribute("inputName"));
        String storageName = inputName + ".storage";
        String pathName = inputName + ".path";
        String contentTypeName = inputName + ".contentType";

        String fieldName = field != null ? field.getInternalName() : page.paramOrDefault(String.class, "fieldName", "");
        StorageItem fieldValue = (StorageItem) state.getValue(fieldName);

        if (fieldValue == null) {
            return;
        }

        String contentType = fieldValue.getContentType();

        page.writeStart("div",
                "class", StorageItemField.FILE_SELECTOR_EXISTING_CLASS + " " + StorageItemField.FILE_SELECTOR_ITEM_CLASS + " filePreview");
            page.writeTag("input",
                    "name", page.h(storageName),
                    "type", "hidden",
                    "value", page.h(fieldValue.getStorage()));
            page.writeTag("input",
                    "name", page.h(pathName),
                    "type", "hidden",
                    "value", page.h(fieldValue.getPath()));
            page.writeTag("input",
                    "name", page.h(contentTypeName),
                    "type", "hidden",
                    "value", page.h(fieldValue.getContentType()));

            if (field != null && field.as(ToolUi.class).getStoragePreviewProcessorPath() != null) {
                ToolUi ui = field.as(ToolUi.class);
                String processorPath = ui.getStoragePreviewProcessorPath();
                if (processorPath != null) {
                    page.include(RoutingFilter.Static.getApplicationPath(ui.getStoragePreviewProcessorApplication()) +
                            StringUtils.ensureStart(processorPath, "/"));
                }
            } else {

                FileContentType fileContentType = FileContentType.Static.getFileFieldWriter(fieldValue);
                if (fileContentType != null) {
                    fileContentType.writePreview(page);
                } else {
                    page.writeStart("a",
                            "href", page.h(fieldValue.getPublicUrl()),
                            "target", "_blank");
                        page.writeHtml(page.h(contentType) + ":" + page.h(fieldValue.getPath()));
                    page.writeEnd();
                }
            }
        page.writeEnd();
    }

    public static void setMetadata(ToolPageContext page, State state, StorageItem fieldValue, File file) throws IOException, ServletException {

        FileContentType fileContentType = FileContentType.Static.getFileFieldWriter(fieldValue);

        if (fileContentType == null) {
            return;
        }

        fileContentType.setMetadata(page, state, fieldValue, file);
    }

    @Override
    protected void doService(ToolPageContext page) throws IOException, ServletException {
        reallyDoService(page);
    }

    @Override
    protected String getPermissionId() {
        return null;
    }
}
