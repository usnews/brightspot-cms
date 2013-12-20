package com.psddev.cms.db; 
import java.lang.IllegalArgumentException; 
import com.psddev.dari.util.KalturaStorageItem;
import com.psddev.dari.util.VideoStorageItem;

public class VideoTranscodingServiceFactory {
         public static VideoTranscodingService getDefault() {
             if (VideoStorageItem.isDefaultVideoStorageKaltura()) {
                 return new KalturaVideoTranscodingService();
             }
             return null;
         }
         public static VideoTranscodingService getTranscodingService(String transcodingProvider) {
             if (transcodingProvider.equals("kaltura"))
             return new KalturaVideoTranscodingService();
             throw new IllegalArgumentException("Unsupported Transcoding provider:" + transcodingProvider);
         }
}

