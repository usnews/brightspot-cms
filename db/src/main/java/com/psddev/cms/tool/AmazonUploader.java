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
    public boolean isSupported(ObjectField field) {
        String storageItemClassName = Settings.getOrDefault(String.class, StorageItem.SETTING_PREFIX + "/" + StorageItemField.getStorageSetting(field) + "/class", "");

        if (StringUtils.isBlank(storageItemClassName)) {
            return false;
        }

        Class<?> storageItemClass = ObjectUtils.getClassByName(storageItemClassName);

        return storageItemClass != null && StorageItem.class.isAssignableFrom(storageItemClass);
    }

    @Override
    public boolean isPreferred(ObjectField field) {
        return false;
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

        page.writeStart("script", "type", "text/javascript");
            page.writeRaw("require([ 'evaporate' ], function(evaporate) { " +
                    "var _e_ = new Evaporate(");
            page.write(ObjectUtils.toJson(ImmutableMap.of(
                    "signerUrl", "/cms/s3auth",
                    "aws_key", Settings.get(String.class, StorageItem.SETTING_PREFIX + "/" + storageSetting + "/access"),
                    "bucket", Settings.get(String.class, StorageItem.SETTING_PREFIX + "/" + storageSetting + "/bucket"))));
            page.writeRaw(");})");
        page.writeEnd();
    }
}
