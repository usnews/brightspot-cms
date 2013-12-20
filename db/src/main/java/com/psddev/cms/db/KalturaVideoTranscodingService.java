package com.psddev.cms.db;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kaltura.client.KalturaClient;
import com.kaltura.client.KalturaConfiguration;
import com.kaltura.client.types.KalturaFlavorParams;
import com.kaltura.client.enums.KalturaContainerFormat;
import com.kaltura.client.enums.KalturaVideoCodec;
import com.kaltura.client.enums.KalturaAudioCodec;
import com.psddev.dari.db.Query;
import com.psddev.cms.db.VideoContainer;
import com.kaltura.client.KalturaApiException;
import com.kaltura.client.enums.KalturaCuePointType;
import com.kaltura.client.services.KalturaCuePointService;
import com.kaltura.client.types.KalturaAnnotation;
import com.kaltura.client.types.KalturaCuePoint;
import com.kaltura.client.types.KalturaCuePointFilter;
import com.kaltura.client.types.KalturaCuePointListResponse;
import com.kaltura.client.types.KalturaFlavorParams;
import com.kaltura.client.types.KalturaMediaEntryFilter;
import com.kaltura.client.types.KalturaMediaListResponse;
import com.kaltura.client.types.KalturaMediaEntry;
import com.kaltura.client.types.KalturaUiConf;
import com.kaltura.client.types.KalturaUiConfListResponse;
import com.psddev.dari.util.KalturaSessionUtils;
import com.psddev.dari.util.VideoStorageItem;
import com.psddev.dari.util.KalturaStorageItem;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.Settings;
/**
* This class is used to retrieve information from kaltura 
* such as transcoding flavors/profiles/players etc from kaltura
* and it also has methods to update data from kaltura to BrightSpot
**/
public class KalturaVideoTranscodingService implements VideoTranscodingService {
         private static final Logger logger = LoggerFactory.getLogger(KalturaVideoTranscodingService.class);
         public boolean updateThumbnailsInCms() {
             try {
                KalturaMediaEntryFilter  kmef= new KalturaMediaEntryFilter();
                kmef.updatedAtGreaterThanOrEqual =  (int) ( (System.currentTimeMillis()/1000) -180);
                KalturaConfiguration kalturaConfig = KalturaSessionUtils.getKalturaConfig();
                KalturaClient client = new KalturaClient(kalturaConfig);
                KalturaSessionUtils.startAdminSession(client, kalturaConfig);
                KalturaMediaListResponse updatedMedia=client.getMediaService().list(kmef);
                for (KalturaMediaEntry mediaEntry : updatedMedia.objects) {
                  logger.info("updating thumbnail for video whose external id is:" + mediaEntry.id);
                  VideoContainer videoContainer=Query.from(VideoContainer.class).where("cms.video.externalId = " + mediaEntry.id).first();
                  if (videoContainer != null ) {
                      VideoContainer.Data videoData = videoContainer.as(VideoContainer.Data.class);
                      VideoStorageItem videoStorageItem = videoData.getFile();
                      logger.error("value of updated thumbnail is:" + mediaEntry.thumbnailUrl);
                      videoStorageItem.setThumbnailUrl(mediaEntry.thumbnailUrl);
                      videoData.save();
                  }
                }
                KalturaSessionUtils.closeSession(client);
                return true;
             } catch(Exception e) {
                 logger.error("updateThumbnailsInCms failed", e);
                 return false;
             }
         }

         public List<KalturaCuePoint> getKalturaCuePoints(String entryId)  throws KalturaApiException { 
             KalturaConfiguration kalturaConfig = KalturaSessionUtils.getKalturaConfig();
             KalturaClient client = new KalturaClient(kalturaConfig);
             KalturaSessionUtils.startAdminSession(client, kalturaConfig);
             KalturaCuePointService cuePointService=client.getCuePointService();
             KalturaCuePointFilter cuePointFilter= new KalturaCuePointFilter();
             cuePointFilter.entryIdEqual=entryId;
             KalturaCuePointListResponse response=cuePointService.list(cuePointFilter);
             KalturaSessionUtils.closeSession(client);
             return response.objects;
         }

         public void deleteEvent(String externalId)  { 
             try {
             KalturaConfiguration kalturaConfig = KalturaSessionUtils.getKalturaConfig();
             KalturaClient client = new KalturaClient(kalturaConfig);
             KalturaSessionUtils.startAdminSession(client, kalturaConfig);
             KalturaCuePointService cuePointService=client.getCuePointService();
             cuePointService.delete(externalId);
             KalturaSessionUtils.closeSession(client);
             } catch (Exception e) {
                 logger.error("Video Metadata/CuePoint delete failed", e);
             }
         }
        
         /* 
         public void updateEventData(VideoContainer.Data videoData)  {
             //VideoContainer.Data videoData=videoContainer.as(VideoContainer.Data.class);
             List<VideoEvent> videoEvents=videoData.getEvents();
             logger.error("Dump of number of events:", videoEvents.size());
             for (VideoEvent videoEvent : videoEvents) {
                 try {
                     logger.error("Dump of videoEvent data", videoEvent);
                     videoEvent.setEntryId(videoData.getExternalId());
                     if (videoEvent.getExternalId() == null ) {
                         addCuePoint(videoEvent);
                     } else {
                         updateCuePoint(videoEvent);
                     }
                 }catch (Exception e) {
                     logger.error("Video Metadata/CuePoint update Failed", e);
                 }
             }
         } */
     
         private KalturaCuePoint addCuePoint(VideoEvent videoEvent)  throws KalturaApiException { 
             logger.info("control in addCuePoint");
             KalturaAnnotation kalturaAnnotation= new KalturaAnnotation();
             updateCuePointDataFromEvent(kalturaAnnotation,videoEvent);
             KalturaConfiguration kalturaConfig = KalturaSessionUtils.getKalturaConfig();
             KalturaClient client = new KalturaClient(kalturaConfig);
             KalturaSessionUtils.startAdminSession(client, kalturaConfig);
             KalturaCuePointService cuePointService=client.getCuePointService();
             KalturaCuePoint kcp=cuePointService.add(kalturaAnnotation);
             videoEvent.setExternalId(kcp.id);
             videoEvent.save();
             KalturaSessionUtils.closeSession(client);
             return kcp;
         }
         
         private KalturaCuePoint updateCuePoint(VideoEvent videoEvent)  throws KalturaApiException {
             logger.info("control in updateCuePoint");
             KalturaAnnotation kalturaAnnotation= new KalturaAnnotation();
             updateCuePointDataFromEvent(kalturaAnnotation,videoEvent);
             KalturaConfiguration kalturaConfig = KalturaSessionUtils.getKalturaConfig();
             KalturaClient client = new KalturaClient(kalturaConfig);
             KalturaSessionUtils.startAdminSession(client, kalturaConfig);
             KalturaCuePointService cuePointService=client.getCuePointService();
             KalturaCuePoint kcp=cuePointService.update(videoEvent.getExternalId().toString(), kalturaAnnotation);
             KalturaSessionUtils.closeSession(client);
             return kcp;
          }
         
         private void updateCuePointDataFromEvent(KalturaAnnotation kalturaCuePoint,VideoEvent videoEvent) {
             kalturaCuePoint.cuePointType=KalturaCuePointType.ANNOTATION;
             kalturaCuePoint.text=videoEvent.getName();
             Map<String,Object> settings=(Map<String,Object>) Settings.get(KalturaStorageItem.KALTURA_SETTINGS_PREFIX);
             kalturaCuePoint.partnerId=ObjectUtils.to(Integer.class,settings.get(KalturaStorageItem.KALTURA_PARTNER_ID_SETTING));
             kalturaCuePoint.tags="chaptering";
             kalturaCuePoint.startTime=videoEvent.getStartTime();
             kalturaCuePoint.endTime=videoEvent.getEndTime();
             kalturaCuePoint.entryId=videoEvent.getEntryId();
             //kalturaCuePoint.entryId="1_mrsidaot";
             //JSONObject jsonObject= JSONObject.fromObject(videoEvent.getMetadata());
             //kalturaCuePoint.partnerData=jsonObject.toString();
             //kalturaAnnotation.partnerData="{\"desc\":\"FIRST CHAPTER  VIA API\"}";
         }
         
}
