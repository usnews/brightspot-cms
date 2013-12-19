package com.psddev.cms.db; 
import java.lang.IllegalArgumentException; 
import com.psddev.dari.util.KalturaStorageItem;

public class TranscodingServiceFactory {
         public static TranscodingService getDefault() {
             if (VideoStorageItem.isDefaultStorageKaltura()) {
                 return KalturaTranscodingService();
             }
             return null;
         }
         public static TranscodingService getTranscodingService(String transcodingProvider) {
             if (transcodingProvider.equals("kaltura"))
             return new KalturaTranscodingService();
             throw new IllegalArgumentException("Unsupported Transcoding provider:" + transcodingProvider);
         }
}

