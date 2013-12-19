package com.psddev.cms.db; 
import java.lang.IllegalArgumentException; 
import java.com.psddev.dari.util.VideoStorageItem;

public class TranscodingServiceFactory {
         public static TranscodingService getDefault() {
             if (VideoStorageItem.isDefaultStorageKaltura()) {
                 return KalturaTranscodingService();
             }
         }
         public static TranscodingService getTranscodingService(String transcodingProvider) {
             if (transcodingProvider.equals("kaltura"))
             return new KalturaTranscodingService();
             throw new IllegalArgumentException("Unsupported Transcoding provider:" + transcodingProvider);
         }
}

