package com.psddev.cms.tool;

import java.io.IOException;

import com.google.common.collect.ImmutableMap;
import com.psddev.cms.tool.page.StorageItemField;
import com.psddev.dari.db.ObjectField;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.Settings;
import com.psddev.dari.util.StorageItem;
import com.psddev.dari.util.StringUtils;

public class AmazonUploader implements Uploader {

    @Override
    public double getPriority(ObjectField field) {

        String storageItemClassName = Settings.getOrDefault(String.class, StorageItem.SETTING_PREFIX + "/" + StorageItemField.getStorageSetting(field) + "/class", "");

        if (StringUtils.isBlank(storageItemClassName)) {
            return -1;
        }

        Class<?> storageItemClass = ObjectUtils.getClassByName(storageItemClassName);

        if (storageItemClass == null || !StorageItem.class.isAssignableFrom(storageItemClass)) {
            return -1;
        }

        return DEFAULT_PRIORITY;
    }

    @Override
    public String getClassIdentifier() {
        return "evaporate";
    }

    @Override
    public void writeHtml(ToolPageContext page, ObjectField field) throws IOException {
        String storageSetting = StorageItemField.getStorageSetting(field);

        if (StringUtils.isBlank(storageSetting)) {
            return;
        }

        page.writeTag("meta",
                "name", "evaporateSettings",
                "content", ObjectUtils.toJson(ImmutableMap.of(
                                "signerUrl", page.cmsUrl("/amazonAuth"),
                                "aws_key", Settings.get(String.class, StorageItem.SETTING_PREFIX + "/" + storageSetting + "/access"),
                                "bucket", Settings.get(String.class, StorageItem.SETTING_PREFIX + "/" + storageSetting + "/bucket")
                )),
                "data-path-start", StorageItemField.createStoragePathPrefix(),
                "data-field-name", field.getInternalName(),
                "data-storage", StorageItemField.getStorageSetting(field),
                "data-type-id", field.getParentType().getId());
    }
}
