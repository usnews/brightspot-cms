package com.psddev.cms.db;

import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.Settings;
import com.psddev.dari.util.StorageItem;

public class VideoTranscodingServiceFactory {
    public static VideoTranscodingService getDefault() {
        if (KalturaVideoTranscodingService.NAME.equals(ObjectUtils.to(String.class, Settings.get(StorageItem.DEFAULT_VIDEO_STORAGE_SETTING)))) {
            return new KalturaVideoTranscodingService();
        }
        return null;
    }

    public static VideoTranscodingService getTranscodingService(String transcodingProvider) {
        if (transcodingProvider.equals(KalturaVideoTranscodingService.NAME)) {
            return new KalturaVideoTranscodingService();
        }
        throw new IllegalArgumentException("Unsupported Transcoding provider:" + transcodingProvider);
    }
}
