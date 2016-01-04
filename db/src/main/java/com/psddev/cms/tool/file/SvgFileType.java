package com.psddev.cms.tool.file;

import java.io.IOException;

import javax.servlet.ServletException;

import com.psddev.cms.tool.FileContentType;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.db.State;
import com.psddev.dari.util.StorageItem;
import com.psddev.dari.util.StringUtils;

/**
 * Adds support for rendering SVG file previews.
 */
public class SvgFileType implements FileContentType {

    @Override
    public double getPriority(StorageItem storageItem) {
        String contentType = storageItem.getContentType();

        if (StringUtils.isBlank(contentType) || !contentType.startsWith("image/svg")) {
            return DEFAULT_PRIORITY_LEVEL - 1;
        }

        return DEFAULT_PRIORITY_LEVEL + 1;
    }

    @Override
    public void writePreview(ToolPageContext page, State state, StorageItem fieldValue) throws IOException, ServletException {

        page.writeStart("a",
                "href", page.h(fieldValue.getPublicUrl()),
                "target", "_blank");
            page.write(page.h(fieldValue.getContentType()));
            page.write(": ");
            page.write(page.h(fieldValue.getPath()));
        page.writeEnd();

        page.writeTag("img",
                "style", "display:block;",
                "src", fieldValue.getPublicUrl(),
                "width", "200");
    }
}
