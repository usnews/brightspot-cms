package com.psddev.cms.db;
import com.psddev.cms.db.Content;
import com.psddev.cms.db.Site;
import com.psddev.dari.util.StringUtils;
/**
Stores attributes of a transcoding flavor used to transcode a video 
**/
public class TranscodingFlavor extends Content {
    public enum VideoContainerFormat{_3GP,APPLEHTTP,AVI,BMP,COPY,FLV,ISMV,JPG,MKV,MOV,MP3,MP4,MPEG,MPEGTS,OGG,OGV,PDF,PNG,SWF,WAV,WEBM,WMA,WMV,WVM};
    public enum VideoCodec{ NONE,APCH,APCN,APCO,APCS,COPY,DNXHD,DV,FLV,H263,H264,H264B,H264H,H264M,MPEG2,MPEG4,THEORA,VP6,VP8,WMV2,WMV3,WVC1A};
    public enum AudioCodec{ NONE,AAC,AACHE,AC3,AMRNB,COPY,MP3,MPEG2,PCM,VORBIS,WMA,WMAPRO};
    @Indexed 
    private Integer externalId;
    private String  name;
    private String  description;
    private VideoContainerFormat format;
    private VideoCodec videoCodec;
    private AudioCodec audioCodec;
    private int videoBitRate;
    private int audioBitRate;
    private int width;
    private int height;
    public boolean equals(Object obj) {
        if (! (obj instanceof TranscodingFlavor)) return false;
        TranscodingFlavor tf=(TranscodingFlavor) obj;
        if ( name.equals(tf.getName()) &&
             description.equals(tf.getDescription())  &&
             externalId.equals(tf.getExternalId())  &&
             format.equals(tf.getFormat())  &&
             videoCodec.equals(tf.getVideoCodec())  &&
             audioCodec.equals(tf.getAudioCodec())  &&
             (videoBitRate == tf.videoBitRate) &&
             (audioBitRate==tf.audioBitRate) &&
             (width ==tf.width) &&
             (height==tf.height))
            return true;
        return false;    
    }
    public Integer getExternalId() {
        return externalId;
    }
    public void setExternalId(Integer externalId) {
        this.externalId = externalId;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    
    public VideoContainerFormat  getFormat() {
        return format;
    }
    public void setFormat(VideoContainerFormat format) {
        this.format = format;
    }


    public AudioCodec getAudioCodec() {
        return audioCodec;
    }
    public void setAudioCodec(AudioCodec audioCodec) {
        this.audioCodec = audioCodec;
    }
    public VideoCodec getVideoCodec() {
        return videoCodec;
    }
    public void setVideoCodec(VideoCodec videoCodec) {
        this.videoCodec = videoCodec;
    }

    public Integer getVideoBitRate() {
        return videoBitRate;
    }
    public void setVideoBitRate(Integer videoBitRate) {
        this.videoBitRate = videoBitRate;
    }

    public Integer getAudioBitRate() {
        return audioBitRate;
    }
    public void setAudioBitRate(Integer audioBitRate) {
        this.audioBitRate = audioBitRate;
    }

    public Integer getWidth() {
        return width;
    }
    public void setWidth(Integer width) {
        this.width = width;
    }
    public Integer getHeight() {
        return height;
    }
    public void setHeight(Integer height) {
        this.height = height;
    }
}

