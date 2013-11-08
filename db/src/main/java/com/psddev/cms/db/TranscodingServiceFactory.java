package com.psddev.cms.db; 
import java.lang.IllegalArgumentException; 
public class TranscodingServiceFactory {
         public static TranscodingService getTranscodingService(String transcodingProvider) {
             if (transcodingProvider.equals("kaltura"))
             return new KalturaTranscodingService();
             throw new IllegalArgumentException("Unsupported Transcoding provider:" + transcodingProvider);
         }
}

