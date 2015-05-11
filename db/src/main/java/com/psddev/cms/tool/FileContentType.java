package com.psddev.cms.tool;

import java.io.IOException;
import java.lang.reflect.Modifier;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import com.psddev.dari.db.ObjectField;
import com.psddev.dari.db.State;
import com.psddev.dari.util.ClassFinder;
import com.psddev.dari.util.StorageItem;
import com.psddev.dari.util.TypeDefinition;

public interface FileContentType {

    boolean isSupported(StorageItem storageItem);
    boolean isPreferred(StorageItem storageItem);
    void writePreview(ToolPageContext page, State state, StorageItem fieldValue) throws IOException, ServletException;

    static FileContentType getFileFieldWriter(StorageItem storageItem) {

        if (storageItem == null) {
            return null;
        }

        FileContentType fileContentType = null;

        for (Class<? extends FileContentType> contentTypeClass : ClassFinder.Static.findClasses(FileContentType.class)) {
            if (!contentTypeClass.isInterface() && !Modifier.isAbstract(contentTypeClass.getModifiers())) {
                FileContentType tester = TypeDefinition.getInstance(contentTypeClass).newInstance();
                if (tester.isSupported(storageItem)) {
                    fileContentType = tester;

                    if (tester.isPreferred(storageItem)) {
                        break;
                    }
                }
            }
        }

        return fileContentType;
    }

    static void writeFilePreview(ToolPageContext page, StorageItem fieldValue) throws IOException, ServletException {

        HttpServletRequest request = page.getRequest();
        State state = State.getInstance(request.getAttribute("object"));
        ObjectField field = (ObjectField) request.getAttribute("field");
        String fieldName = field != null ? field.getInternalName() : page.paramOrDefault(String.class, "fieldName", "");
        
        if (fieldValue == null) {
            fieldValue = (StorageItem) state.getValue(fieldName);
        }

        FileContentType fileContentType = FileContentType.getFileFieldWriter(fieldValue);
        if (fileContentType != null) {
            fileContentType.writePreview(page, state, fieldValue);
        } else {
            page.writeStart("a",
                    "href", page.h(fieldValue.getPublicUrl()),
                    "target", "_blank");
                page.writeHtml(page.h(fieldValue.getContentType()) + ":" + page.h(fieldValue.getPath()));
            page.writeEnd();
        }
    }
}
