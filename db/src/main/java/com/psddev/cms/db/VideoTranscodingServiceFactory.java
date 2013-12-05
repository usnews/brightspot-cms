package com.psddev.cms.db;
public class VideoTranscodingServiceFactory {
    public static VideoTranscodingService getTranscodingService(String transcodingProvider) {
        if (KalturaVideoTranscodingService.NAME.equals(transcodingProvider)) {
            return new KalturaVideoTranscodingService();
        }
        return null;
    }
}

