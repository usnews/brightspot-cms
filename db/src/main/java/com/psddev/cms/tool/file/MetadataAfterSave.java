package com.psddev.cms.tool.file;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.psddev.dari.util.AbstractStorageItem;
import com.psddev.dari.util.AggregateException;
import com.psddev.dari.util.ImageMetadataMap;
import com.psddev.dari.util.IoUtils;
import com.psddev.dari.util.StorageItem;
import com.psddev.dari.util.StorageItemAfterSave;

public class MetadataAfterSave implements StorageItemAfterSave {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetadataAfterSave.class);

    @Override
    public void afterSave(StorageItem storageItem) {
        if (storageItem == null || !(storageItem instanceof AbstractStorageItem)) {
            return;
        }

        Map<String, Object> metadata = storageItem.getMetadata();
        ImageMetadataMap imageMetadata = null;
        InputStream inputStream = null;
        String contentType = storageItem.getContentType();

        try {
            if (!metadata.containsKey("width")
                    && !metadata.containsKey("height")
                    && contentType != null
                    && contentType.startsWith("image/")) {

                inputStream = storageItem.getData();
                imageMetadata = new ImageMetadataMap(inputStream);
                List<Throwable> errors = imageMetadata.getErrors();

                if (!errors.isEmpty()) {
                    LOGGER.debug("Can't read image metadata", new AggregateException(errors));
                }
            }

        } catch (IOException e) {
            LOGGER.debug("Can't read image metadata", e);
        } finally {
            IoUtils.closeQuietly(inputStream);
        }

        if (imageMetadata != null) {
            metadata.putAll(imageMetadata);
        }
    }
}
