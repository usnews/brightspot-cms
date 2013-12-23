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
import com.kaltura.client.enums.KalturaEntryType;
import com.kaltura.client.enums.KalturaPlaylistType;
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
import com.kaltura.client.types.KalturaPlaylist;
import com.kaltura.client.types.KalturaUiConf;
import com.kaltura.client.types.KalturaUiConfListResponse;
import com.kaltura.client.services.KalturaPlaylistService;
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
         }
         public String createPlayList(String name,List<VideoContainer> items) {
         try {
             //Step1: Start kaltura session 
             KalturaConfiguration kalturaConfig = KalturaSessionUtils.getKalturaConfig();
             KalturaClient client= new KalturaClient(kalturaConfig);
             KalturaSessionUtils.startAdminSession(client, kalturaConfig);
             //Step2: Create a playlist
             KalturaPlaylist playlist = new KalturaPlaylist();
             playlist.name =name;
             playlist.type = KalturaEntryType.PLAYLIST;
             playlist.playlistType = KalturaPlaylistType.STATIC_LIST;
             StringBuffer playListContentIds= new StringBuffer();
             for ( VideoContainer vc : items) {
                 if (playListContentIds.length() > 0)  playListContentIds.append(",");
                 VideoContainer.Data videoData=vc.as(VideoContainer.Data.class);
                 playListContentIds.append(videoData.getExternalId());
             }
             playlist.playlistContent=playListContentIds.toString();
             KalturaPlaylist pl = client.getPlaylistService().add(playlist);
             //Step3: Close kaltura session
             KalturaSessionUtils.closeSession(client);
             return pl.id;
         }
         catch (Exception e ) {
             logger.error("createPlayList failed", e);
             return "";
         }
         }
         public boolean updatePlayList(String externalId,List<VideoContainer> items) {
         try {
             //Step1: Start kaltura session 
             KalturaConfiguration kalturaConfig = KalturaSessionUtils.getKalturaConfig();
             KalturaClient client= new KalturaClient(kalturaConfig);
             KalturaSessionUtils.startAdminSession(client, kalturaConfig);
             //Step2: Get the  playlist and update the order of items
             KalturaPlaylistService playListService = client.getPlaylistService()  ;
             KalturaPlaylist playList = playListService.get(externalId);
             //playlist.type = KalturaEntryType.PLAYLIST;
             //playlist.playlistType = KalturaPlaylistType.STATIC_LIST;
             StringBuffer playListContentIds= new StringBuffer();
             for ( VideoContainer vc : items) {
                 if (playListContentIds.length() > 0)  playListContentIds.append(",");
                 VideoContainer.Data videoData=vc.as(VideoContainer.Data.class);
                 playListContentIds.append(videoData.getExternalId());
             }
             
             //If there is a change..update kaltura
             if (!playList.playlistContent.equals(playListContentIds.toString())) {
                 KalturaPlaylist playlist = new KalturaPlaylist();
                 playlist.name =playList.name;
                 playlist.type = KalturaEntryType.PLAYLIST;
                 playlist.playlistType = KalturaPlaylistType.STATIC_LIST;
                 playlist.playlistContent=playListContentIds.toString(); 
                 client.getPlaylistService().update(externalId,playlist);
             }
             //Step3: Close kaltura session
             KalturaSessionUtils.closeSession(client);
             return true;
         }
         catch (Exception e ) {
             logger.error("updatePlayList failed", e);
             return false;
         }
         }
         public boolean deletePlayList(String externalId) {
         try {
             //Step1: Start kaltura session 
             KalturaConfiguration kalturaConfig = KalturaSessionUtils.getKalturaConfig();
             KalturaClient client= new KalturaClient(kalturaConfig);
             KalturaSessionUtils.startAdminSession(client, kalturaConfig);
             //Step2: Get the  playlist and update the order of items
             KalturaPlaylistService playListService = client.getPlaylistService()  ;
             playListService.delete(externalId);
             //Step3: Close kaltura session
             KalturaSessionUtils.closeSession(client);
             return true;
         }
         catch (Exception e ) {
             logger.error("updatePlayList failed", e);
             return false;
         }
         }
}
