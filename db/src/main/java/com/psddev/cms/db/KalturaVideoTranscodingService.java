package com.psddev.cms.db;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kaltura.client.KalturaClient;
import com.kaltura.client.KalturaConfiguration;
import com.kaltura.client.types.KalturaFlavorParams;
import com.kaltura.client.enums.KalturaContainerFormat;
import com.kaltura.client.enums.KalturaVideoCodec;
import com.kaltura.client.enums.KalturaAudioCodec;
import com.psddev.dari.util.KalturaSessionUtils;
import com.psddev.dari.db.Query;
/**
* This class is used to retrieve information from kaltura
* such as transcoding flavors/profiles/players etc from kaltura
* and it also has methods to update data from kaltura to BrightSpot
**/
public class KalturaVideoTranscodingService implements VideoTranscodingService {
    public static final String NAME = "kaltura";
    private static final Logger LOGGER = LoggerFactory.getLogger(KalturaVideoTranscodingService.class);

    /** Retrieves the list of flavors from kaltura and creates/updates in BrightSphot **/
    public boolean updateTranscodingFlavorsInCms() {
        try {
            List<KalturaFlavorParams> flavorParamList = getFlavorList();
            if (flavorParamList != null) {
                for (KalturaFlavorParams kalturaFlavor : flavorParamList) {
                    VideoTranscodingFlavor transcodingFlavor = Query.from(VideoTranscodingFlavor.class).where("externalId = ?", kalturaFlavor.id).first();
                    if (transcodingFlavor != null ) {
                        updateTranscodingFlavorInCms(transcodingFlavor,kalturaFlavor);
                    } else {
                        createTranscodingFlavorInCms(kalturaFlavor);
                    }
                }
            }
            return true;
        } catch(Exception e) {
            LOGGER.error("updateTranscodingFlavorsInCms failed", e);
            return false;
        }
    }

    //public List<TranscodingFlavor> getTranscodingFlavors();
    private void updateTranscodingFlavorInCms(VideoTranscodingFlavor tf, KalturaFlavorParams  ktf) {
        //Check to see if there is an update to flavor data in Kaltura
        VideoTranscodingFlavor transcodingFlavorKaltura = new VideoTranscodingFlavor();
        updateTranscodingFlavor(transcodingFlavorKaltura, ktf);
        //If there is an update in Kaltura..do the update in CMS
        if (!transcodingFlavorKaltura.equals(tf)) {
            updateTranscodingFlavor(tf, ktf);
            Content.Static.publish(tf, null, null);
        }
    }

    private void updateTranscodingFlavor(VideoTranscodingFlavor tf, KalturaFlavorParams ktf) {
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
            tf.setVideoCodec(VideoTranscodingFlavor.VideoCodec.NONE);
        else if (ktf.videoCodec.equals(KalturaVideoCodec.APCH))
            tf.setVideoCodec(VideoTranscodingFlavor.VideoCodec.APCH);
        else if (ktf.videoCodec.equals(KalturaVideoCodec.APCN))
            tf.setVideoCodec(VideoTranscodingFlavor.VideoCodec.APCN);
        else if (ktf.videoCodec.equals(KalturaVideoCodec.APCO))
            tf.setVideoCodec(VideoTranscodingFlavor.VideoCodec.APCO);
        else if (ktf.videoCodec.equals(KalturaVideoCodec.APCS))
            tf.setVideoCodec(VideoTranscodingFlavor.VideoCodec.APCS);
        else if (ktf.videoCodec.equals(KalturaVideoCodec.COPY))
            tf.setVideoCodec(VideoTranscodingFlavor.VideoCodec.COPY);
        else if (ktf.videoCodec.equals(KalturaVideoCodec.DNXHD))
            tf.setVideoCodec(VideoTranscodingFlavor.VideoCodec.DNXHD);
        else if (ktf.videoCodec.equals(KalturaVideoCodec.DV))
            tf.setVideoCodec(VideoTranscodingFlavor.VideoCodec.DV);
        else if (ktf.videoCodec.equals(KalturaVideoCodec.FLV))
            tf.setVideoCodec(VideoTranscodingFlavor.VideoCodec.FLV);
        else if (ktf.videoCodec.equals(KalturaVideoCodec.H263))
            tf.setVideoCodec(VideoTranscodingFlavor.VideoCodec.H263);
        else if (ktf.videoCodec.equals(KalturaVideoCodec.H264))
            tf.setVideoCodec(VideoTranscodingFlavor.VideoCodec.H264);
        else if (ktf.videoCodec.equals(KalturaVideoCodec.H264B))
            tf.setVideoCodec(VideoTranscodingFlavor.VideoCodec.H264B);
        else if (ktf.videoCodec.equals(KalturaVideoCodec.H264H))
            tf.setVideoCodec(VideoTranscodingFlavor.VideoCodec.H264H);
        else if (ktf.videoCodec.equals(KalturaVideoCodec.H264M))
            tf.setVideoCodec(VideoTranscodingFlavor.VideoCodec.H264M);
        else if (ktf.videoCodec.equals(KalturaVideoCodec.MPEG2))
            tf.setVideoCodec(VideoTranscodingFlavor.VideoCodec.MPEG2);
        else if (ktf.videoCodec.equals(KalturaVideoCodec.MPEG4))
            tf.setVideoCodec(VideoTranscodingFlavor.VideoCodec.MPEG4);
        else if (ktf.videoCodec.equals(KalturaVideoCodec.THEORA))
            tf.setVideoCodec(VideoTranscodingFlavor.VideoCodec.THEORA);
        else if (ktf.videoCodec.equals(KalturaVideoCodec.VP6))
            tf.setVideoCodec(VideoTranscodingFlavor.VideoCodec.VP6);
        else if (ktf.videoCodec.equals(KalturaVideoCodec.VP8))
            tf.setVideoCodec(VideoTranscodingFlavor.VideoCodec.VP8);
        else if (ktf.videoCodec.equals(KalturaVideoCodec.WMV2))
            tf.setVideoCodec(VideoTranscodingFlavor.VideoCodec.WMV2);
        else if (ktf.videoCodec.equals(KalturaVideoCodec.WMV3))
            tf.setVideoCodec(VideoTranscodingFlavor.VideoCodec.WMV3);
        else if (ktf.videoCodec.equals(KalturaVideoCodec.WVC1A))
            tf.setVideoCodec(VideoTranscodingFlavor.VideoCodec.WVC1A);
        //Update audio codec
        if (ktf.audioCodec.equals(KalturaAudioCodec.NONE))
            tf.setAudioCodec(VideoTranscodingFlavor.AudioCodec.NONE);
        else if (ktf.audioCodec.equals(KalturaAudioCodec.AAC))
            tf.setAudioCodec(VideoTranscodingFlavor.AudioCodec.AAC);
        else if (ktf.audioCodec.equals(KalturaAudioCodec.AACHE))
            tf.setAudioCodec(VideoTranscodingFlavor.AudioCodec.AACHE);
        else if (ktf.audioCodec.equals(KalturaAudioCodec.AC3))
            tf.setAudioCodec(VideoTranscodingFlavor.AudioCodec.AC3);
        else if (ktf.audioCodec.equals(KalturaAudioCodec.AMRNB))
            tf.setAudioCodec(VideoTranscodingFlavor.AudioCodec.AMRNB);
        else if (ktf.audioCodec.equals(KalturaAudioCodec.COPY))
            tf.setAudioCodec(VideoTranscodingFlavor.AudioCodec.COPY);
        else if (ktf.audioCodec.equals(KalturaAudioCodec.MP3))
            tf.setAudioCodec(VideoTranscodingFlavor.AudioCodec.MP3);
        else if (ktf.audioCodec.equals(KalturaAudioCodec.MPEG2))
            tf.setAudioCodec(VideoTranscodingFlavor.AudioCodec.MPEG2);
        else if (ktf.audioCodec.equals(KalturaAudioCodec.PCM))
            tf.setAudioCodec(VideoTranscodingFlavor.AudioCodec.PCM);
        else if (ktf.audioCodec.equals(KalturaAudioCodec.VORBIS))
            tf.setAudioCodec(VideoTranscodingFlavor.AudioCodec.VORBIS);
        else if (ktf.audioCodec.equals(KalturaAudioCodec.WMA))
            tf.setAudioCodec(VideoTranscodingFlavor.AudioCodec.WMA);
        else if (ktf.audioCodec.equals(KalturaAudioCodec.WMAPRO))
            tf.setAudioCodec(VideoTranscodingFlavor.AudioCodec.WMAPRO);
        //Update format
        if (ktf.format.equals(KalturaContainerFormat._3GP))
            tf.setFormat(VideoTranscodingFlavor.VideoContainerFormat._3GP);
        else if (ktf.format.equals(KalturaContainerFormat.APPLEHTTP))
            tf.setFormat(VideoTranscodingFlavor.VideoContainerFormat.APPLEHTTP);
        else if (ktf.format.equals(KalturaContainerFormat.AVI))
            tf.setFormat(VideoTranscodingFlavor.VideoContainerFormat.AVI);
        else if (ktf.format.equals(KalturaContainerFormat.BMP))
            tf.setFormat(VideoTranscodingFlavor.VideoContainerFormat.BMP);
        else if (ktf.format.equals(KalturaContainerFormat.COPY))
            tf.setFormat(VideoTranscodingFlavor.VideoContainerFormat.COPY);
        else if (ktf.format.equals(KalturaContainerFormat.FLV))
            tf.setFormat(VideoTranscodingFlavor.VideoContainerFormat.FLV);
        else if (ktf.format.equals(KalturaContainerFormat.ISMV))
            tf.setFormat(VideoTranscodingFlavor.VideoContainerFormat.ISMV);
        else if (ktf.format.equals(KalturaContainerFormat.JPG))
            tf.setFormat(VideoTranscodingFlavor.VideoContainerFormat.JPG);
        else if (ktf.format.equals(KalturaContainerFormat.MKV))
            tf.setFormat(VideoTranscodingFlavor.VideoContainerFormat.MKV);
        else if (ktf.format.equals(KalturaContainerFormat.MOV))
            tf.setFormat(VideoTranscodingFlavor.VideoContainerFormat.MOV);
        else if (ktf.format.equals(KalturaContainerFormat.MP3))
            tf.setFormat(VideoTranscodingFlavor.VideoContainerFormat.MP3);
        else if (ktf.format.equals(KalturaContainerFormat.MP4))
            tf.setFormat(VideoTranscodingFlavor.VideoContainerFormat.MP4);
        else if (ktf.format.equals(KalturaContainerFormat.MPEG))
            tf.setFormat(VideoTranscodingFlavor.VideoContainerFormat.MPEG);
        else if (ktf.format.equals(KalturaContainerFormat.MPEGTS))
            tf.setFormat(VideoTranscodingFlavor.VideoContainerFormat.MPEGTS);
        else if (ktf.format.equals(KalturaContainerFormat.OGG))
            tf.setFormat(VideoTranscodingFlavor.VideoContainerFormat.OGG);
        else if (ktf.format.equals(KalturaContainerFormat.OGV))
            tf.setFormat(VideoTranscodingFlavor.VideoContainerFormat.OGV);
        else if (ktf.format.equals(KalturaContainerFormat.PDF))
            tf.setFormat(VideoTranscodingFlavor.VideoContainerFormat.PDF);
        else if (ktf.format.equals(KalturaContainerFormat.SWF))
            tf.setFormat(VideoTranscodingFlavor.VideoContainerFormat.SWF);
        else if (ktf.format.equals(KalturaContainerFormat.WAV))
            tf.setFormat(VideoTranscodingFlavor.VideoContainerFormat.WAV);
        else if (ktf.format.equals(KalturaContainerFormat.WEBM))
            tf.setFormat(VideoTranscodingFlavor.VideoContainerFormat.WEBM);
        else if (ktf.format.equals(KalturaContainerFormat.WMA))
            tf.setFormat(VideoTranscodingFlavor.VideoContainerFormat.WMA);
        else if (ktf.format.equals(KalturaContainerFormat.WMV))
            tf.setFormat(VideoTranscodingFlavor.VideoContainerFormat.WMV);
        else if (ktf.format.equals(KalturaContainerFormat.WVM))
            tf.setFormat(VideoTranscodingFlavor.VideoContainerFormat.WVM);
    }

    private void createTranscodingFlavorInCms(KalturaFlavorParams ktf) {
        VideoTranscodingFlavor tf = new VideoTranscodingFlavor();
        tf.setExternalId(ktf.id);
        updateTranscodingFlavorInCms(tf, ktf);
    }

    public List<KalturaFlavorParams> getFlavorList()  {
        try {
             KalturaConfiguration kalturaConfig = KalturaSessionUtils.getKalturaConfig();
             KalturaClient client = new KalturaClient(kalturaConfig);
             KalturaSessionUtils.startAdminSession(client, kalturaConfig);
             List<KalturaFlavorParams> kfplist = client.getFlavorParamsService().list().objects;
             KalturaSessionUtils.closeSession(client);
             return kfplist;
        } catch(Exception e) {
             LOGGER.error("getFlavorList failed", e);
             return null;
        }
    }
}

