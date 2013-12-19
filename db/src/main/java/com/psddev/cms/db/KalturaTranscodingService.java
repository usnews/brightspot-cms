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
public class KalturaTranscodingService implements TranscodingService {
         private static final Logger logger = LoggerFactory.getLogger(KalturaTranscodingService.class);
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
         /** Retrieves the list of flavors from kaltura and creates/updates in BrightSphot **/
         public boolean updateTranscodingFlavorsInCms() {
             try {
                List<KalturaFlavorParams> flavorParamList=getFlavorList();
                for (KalturaFlavorParams kalturaFlavor : flavorParamList) {
                  TranscodingFlavor transcodingFlavor=Query.from(TranscodingFlavor.class).where("externalId = " + kalturaFlavor.id).first();
                  if (transcodingFlavor != null ) updateTranscodingFlavorInCms(transcodingFlavor,kalturaFlavor);
                  else createTranscodingFlavorInCms(kalturaFlavor);
                }
                return true;
             } catch(Exception e) {
                 logger.error("updateTranscodingFlavorsInCms failed", e);
                 return false;
             }
         }
         //public List<TranscodingFlavor> getTranscodingFlavors();
         private void updateTranscodingFlavorInCms(TranscodingFlavor tf, KalturaFlavorParams  ktf) {
             //Check to see if there is an update to flavor data in Kaltura   
             TranscodingFlavor transcodingFlavorKaltura= new TranscodingFlavor();
             updateTranscodingFlavor(transcodingFlavorKaltura,ktf);
             //If there is an update in Kaltura..do the update in CMS
             if ( !transcodingFlavorKaltura.equals(tf)) {
                 updateTranscodingFlavor(tf,ktf);
                 Content.Static.publish(tf,null,Query.from(ToolUser.class).first());
             }
         }
         private void updateTranscodingFlavor(TranscodingFlavor tf,KalturaFlavorParams  ktf) {
             tf.setName(ktf.name);
             tf.setDescription(ktf.description);
             tf.setHeight(ktf.height);
             tf.setWidth(ktf.width);
             tf.setHeight(ktf.height);
             tf.setAudioBitRate(ktf.audioBitrate);
             tf.setVideoBitRate(ktf.videoBitrate);
             tf.setExternalId(ktf.id);
             //Updated video codec    
             if (ktf.videoCodec.equals(KalturaVideoCodec.NONE))
                 tf.setVideoCodec(TranscodingFlavor.VideoCodec.NONE); 
             else if (ktf.videoCodec.equals(KalturaVideoCodec.APCH))
                 tf.setVideoCodec(TranscodingFlavor.VideoCodec.APCH);
             else if (ktf.videoCodec.equals(KalturaVideoCodec.APCN))
                 tf.setVideoCodec(TranscodingFlavor.VideoCodec.APCN);
             else if (ktf.videoCodec.equals(KalturaVideoCodec.APCO))
                 tf.setVideoCodec(TranscodingFlavor.VideoCodec.APCO);
             else if (ktf.videoCodec.equals(KalturaVideoCodec.APCS))
                 tf.setVideoCodec(TranscodingFlavor.VideoCodec.APCS);
             else if (ktf.videoCodec.equals(KalturaVideoCodec.COPY))
                 tf.setVideoCodec(TranscodingFlavor.VideoCodec.COPY);
             else if (ktf.videoCodec.equals(KalturaVideoCodec.DNXHD))
                 tf.setVideoCodec(TranscodingFlavor.VideoCodec.DNXHD);
             else if (ktf.videoCodec.equals(KalturaVideoCodec.DV))
                 tf.setVideoCodec(TranscodingFlavor.VideoCodec.DV);
             else if (ktf.videoCodec.equals(KalturaVideoCodec.FLV))
                 tf.setVideoCodec(TranscodingFlavor.VideoCodec.FLV);
             else if (ktf.videoCodec.equals(KalturaVideoCodec.H263))
                 tf.setVideoCodec(TranscodingFlavor.VideoCodec.H263);
             else if (ktf.videoCodec.equals(KalturaVideoCodec.H264))
                 tf.setVideoCodec(TranscodingFlavor.VideoCodec.H264);
             else if (ktf.videoCodec.equals(KalturaVideoCodec.H264B))
                 tf.setVideoCodec(TranscodingFlavor.VideoCodec.H264B);
             else if (ktf.videoCodec.equals(KalturaVideoCodec.H264H))
                 tf.setVideoCodec(TranscodingFlavor.VideoCodec.H264H);
             else if (ktf.videoCodec.equals(KalturaVideoCodec.H264M))
                 tf.setVideoCodec(TranscodingFlavor.VideoCodec.H264M);
             else if (ktf.videoCodec.equals(KalturaVideoCodec.MPEG2))
                 tf.setVideoCodec(TranscodingFlavor.VideoCodec.MPEG2);
             else if (ktf.videoCodec.equals(KalturaVideoCodec.MPEG4))
                 tf.setVideoCodec(TranscodingFlavor.VideoCodec.MPEG4);
             else if (ktf.videoCodec.equals(KalturaVideoCodec.THEORA))
                 tf.setVideoCodec(TranscodingFlavor.VideoCodec.THEORA);
             else if (ktf.videoCodec.equals(KalturaVideoCodec.VP6))
                 tf.setVideoCodec(TranscodingFlavor.VideoCodec.VP6);
             else if (ktf.videoCodec.equals(KalturaVideoCodec.VP8))
                 tf.setVideoCodec(TranscodingFlavor.VideoCodec.VP8);
             else if (ktf.videoCodec.equals(KalturaVideoCodec.WMV2))
                 tf.setVideoCodec(TranscodingFlavor.VideoCodec.WMV2);
             else if (ktf.videoCodec.equals(KalturaVideoCodec.WMV3))
                 tf.setVideoCodec(TranscodingFlavor.VideoCodec.WMV3);
             else if (ktf.videoCodec.equals(KalturaVideoCodec.WVC1A))
                 tf.setVideoCodec(TranscodingFlavor.VideoCodec.WVC1A);
             //Update audio codec
             if (ktf.audioCodec.equals(KalturaAudioCodec.NONE))
                 tf.setAudioCodec(TranscodingFlavor.AudioCodec.NONE);
             else if (ktf.audioCodec.equals(KalturaAudioCodec.AAC))
                 tf.setAudioCodec(TranscodingFlavor.AudioCodec.AAC);
             else if (ktf.audioCodec.equals(KalturaAudioCodec.AACHE))
                 tf.setAudioCodec(TranscodingFlavor.AudioCodec.AACHE);
             else if (ktf.audioCodec.equals(KalturaAudioCodec.AC3))
                 tf.setAudioCodec(TranscodingFlavor.AudioCodec.AC3);
             else if (ktf.audioCodec.equals(KalturaAudioCodec.AMRNB))
                 tf.setAudioCodec(TranscodingFlavor.AudioCodec.AMRNB);
             else if (ktf.audioCodec.equals(KalturaAudioCodec.COPY))
                 tf.setAudioCodec(TranscodingFlavor.AudioCodec.COPY);
             else if (ktf.audioCodec.equals(KalturaAudioCodec.MP3))
                 tf.setAudioCodec(TranscodingFlavor.AudioCodec.MP3);
             else if (ktf.audioCodec.equals(KalturaAudioCodec.MPEG2))
                 tf.setAudioCodec(TranscodingFlavor.AudioCodec.MPEG2);
             else if (ktf.audioCodec.equals(KalturaAudioCodec.PCM))
                 tf.setAudioCodec(TranscodingFlavor.AudioCodec.PCM);
             else if (ktf.audioCodec.equals(KalturaAudioCodec.VORBIS))
                 tf.setAudioCodec(TranscodingFlavor.AudioCodec.VORBIS);
             else if (ktf.audioCodec.equals(KalturaAudioCodec.WMA))
                 tf.setAudioCodec(TranscodingFlavor.AudioCodec.WMA);
             else if (ktf.audioCodec.equals(KalturaAudioCodec.WMAPRO))
                 tf.setAudioCodec(TranscodingFlavor.AudioCodec.WMAPRO);
             //Update format 
             if (ktf.format.equals(KalturaContainerFormat._3GP))
                 tf.setFormat(TranscodingFlavor.VideoContainerFormat._3GP); 
             else if (ktf.format.equals(KalturaContainerFormat.APPLEHTTP))
                 tf.setFormat(TranscodingFlavor.VideoContainerFormat.APPLEHTTP);
             else if (ktf.format.equals(KalturaContainerFormat.AVI))
                 tf.setFormat(TranscodingFlavor.VideoContainerFormat.AVI);
             else if (ktf.format.equals(KalturaContainerFormat.BMP))
                 tf.setFormat(TranscodingFlavor.VideoContainerFormat.BMP);
             else if (ktf.format.equals(KalturaContainerFormat.COPY))
                 tf.setFormat(TranscodingFlavor.VideoContainerFormat.COPY);
             else if (ktf.format.equals(KalturaContainerFormat.FLV))
                 tf.setFormat(TranscodingFlavor.VideoContainerFormat.FLV);
             else if (ktf.format.equals(KalturaContainerFormat.ISMV))
                 tf.setFormat(TranscodingFlavor.VideoContainerFormat.ISMV);
             else if (ktf.format.equals(KalturaContainerFormat.JPG))
                 tf.setFormat(TranscodingFlavor.VideoContainerFormat.JPG);
             else if (ktf.format.equals(KalturaContainerFormat.MKV))
                 tf.setFormat(TranscodingFlavor.VideoContainerFormat.MKV);
             else if (ktf.format.equals(KalturaContainerFormat.MOV))
                 tf.setFormat(TranscodingFlavor.VideoContainerFormat.MOV);
             else if (ktf.format.equals(KalturaContainerFormat.MP3))
                 tf.setFormat(TranscodingFlavor.VideoContainerFormat.MP3);
             else if (ktf.format.equals(KalturaContainerFormat.MP4))
                 tf.setFormat(TranscodingFlavor.VideoContainerFormat.MP4);
             else if (ktf.format.equals(KalturaContainerFormat.MPEG))
                 tf.setFormat(TranscodingFlavor.VideoContainerFormat.MPEG);
             else if (ktf.format.equals(KalturaContainerFormat.MPEGTS))
                 tf.setFormat(TranscodingFlavor.VideoContainerFormat.MPEGTS);
             else if (ktf.format.equals(KalturaContainerFormat.OGG))
                 tf.setFormat(TranscodingFlavor.VideoContainerFormat.OGG);
             else if (ktf.format.equals(KalturaContainerFormat.OGV))
                 tf.setFormat(TranscodingFlavor.VideoContainerFormat.OGV);
             else if (ktf.format.equals(KalturaContainerFormat.PDF))
                 tf.setFormat(TranscodingFlavor.VideoContainerFormat.PDF);
             else if (ktf.format.equals(KalturaContainerFormat.SWF))
                 tf.setFormat(TranscodingFlavor.VideoContainerFormat.SWF);
             else if (ktf.format.equals(KalturaContainerFormat.WAV))
                 tf.setFormat(TranscodingFlavor.VideoContainerFormat.WAV);
             else if (ktf.format.equals(KalturaContainerFormat.WEBM))
                 tf.setFormat(TranscodingFlavor.VideoContainerFormat.WEBM);
             else if (ktf.format.equals(KalturaContainerFormat.WMA))
                 tf.setFormat(TranscodingFlavor.VideoContainerFormat.WMA);
             else if (ktf.format.equals(KalturaContainerFormat.WMV))
                 tf.setFormat(TranscodingFlavor.VideoContainerFormat.WMV);
             else if (ktf.format.equals(KalturaContainerFormat.WVM))
                 tf.setFormat(TranscodingFlavor.VideoContainerFormat.WVM);
         }

         private void createTranscodingFlavorInCms(KalturaFlavorParams ktf) {
             TranscodingFlavor tf= new TranscodingFlavor();
             tf.setExternalId(ktf.id);
             updateTranscodingFlavorInCms(tf,ktf);
         }

         public List<KalturaFlavorParams> getFlavorList()  {
             try {
                   KalturaConfiguration kalturaConfig= KalturaSessionUtils.getKalturaConfig();
                   KalturaClient client= new KalturaClient(kalturaConfig);
                   KalturaSessionUtils.startAdminSession(client,kalturaConfig);
                   List<KalturaFlavorParams> kfplist=client.getFlavorParamsService().list().objects;
                   KalturaSessionUtils.closeSession(client);
                   return kfplist;
                 } catch(Exception e) {
                    logger.error("getFlavorList failed", e);
                    return null;
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
    }

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
