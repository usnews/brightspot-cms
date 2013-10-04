package com.psddev.cms.db; 
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kaltura.client.enums.KalturaEntryStatus;
import com.psddev.cms.db.Content;
import com.psddev.dari.db.Query;
import com.psddev.dari.db.Database;
import com.psddev.dari.db.DistributedLock;
import com.psddev.dari.util.VideoStorageItem;
import com.psddev.dari.util.VideoStorageItem.TranscodingStatus;
import com.psddev.dari.util.KalturaStorageItem;
import com.psddev.dari.util.VideoStorageItemListener;
import com.psddev.dari.util.StorageItem;
import com.psddev.dari.util.Task;

public  class VideoTranscodingStatusUpdateTask extends Task {
    private static final Logger LOGGER = LoggerFactory.getLogger(VideoTranscodingStatusUpdateTask.class);
    private static final String DEFAULT_TASK_NAME = "Video Transcoding Status Update Task";

    public VideoTranscodingStatusUpdateTask(String executor, String name) {
        super(executor, name);
    }

    public VideoTranscodingStatusUpdateTask(String name) {
        super(null, name);
    }

    public VideoTranscodingStatusUpdateTask() {
        super(null, DEFAULT_TASK_NAME);
    }

    @Override
    protected void doTask() throws Exception {
        if(!shouldContinue())  return;
        DistributedLock disLock=DistributedLock.Static.getInstance(Database.Static.getDefault(), VideoTranscodingStatusUpdateTask.class.getName());
        LOGGER.info("VideoTranscodingStatusUpdateTask starting....");
        try {
        disLock.lock();
        this.setProgress("Starting ....");
        List<VideoContainer> pendingVideoList = Query
                .from(VideoContainer.class)
                .where("cms.video.transcodingStatus = 'pending' ").selectAll();
        this.setProgress("Identified pending videos. Pending video count ...."
                + pendingVideoList.size());
        for (VideoContainer videoContainer : pendingVideoList) {
            try {
                VideoContainer.Data videoData = videoContainer.as(VideoContainer.Data.class);
                VideoStorageItem videoStorageItem = videoData.getFile();
                //If there are listeners set..query using the ids and set it on the storage item
                //before invoking pull method
                List <UUID> videoStorageItemListenerIds=videoStorageItem.getVideoStorageItemListenerIds();
                if (videoStorageItemListenerIds != null ) {
                   List <VideoStorageItemListener> videoStorageItemListeners= new ArrayList<VideoStorageItemListener>();
                   for (UUID listenerId : videoStorageItemListenerIds) {
                       VideoStorageItemListener listener=Query.findById(VideoStorageItemListener.class,listenerId);
                       if (listener != null ) videoStorageItemListeners.add (listener);
                   }
                   videoStorageItem.setVideoStorageItemListeners(videoStorageItemListeners);
                }

                //Invoke pull method from storage to check  the status
                videoStorageItem.pull();
                TranscodingStatus videoTranscodingStatus = videoStorageItem.getTranscodingStatus();  
                // If transcoding is complete..update transcoding status on the video
                // and length ..if failed..updated the error message
                boolean statusUpdated=false;
                if (videoTranscodingStatus.equals(TranscodingStatus.SUCCEEDED))
                 {
                        videoData.setLength(videoStorageItem.getLength());
                        statusUpdated=true;
                 } else if (videoTranscodingStatus.equals(TranscodingStatus.FAILED)) {
                        videoData.setTranscodingError(videoStorageItem.getTranscodingError());
                        statusUpdated=true;
                 }
                 //Updated the transcodingStatus
                 videoData.setTranscodingStatus(videoStorageItem.getTranscodingStatus());
                 videoData.setTranscodingStatusUpdatedAt(new Date());
                 videoData.save();
                 if (statusUpdated && videoStorageItemListenerIds != null) 
                     videoStorageItem.notifyVideoStorageItemListeners();
                 //Sends a notification if it implement VideoTranscodingStatusListener
                 //if (videoContainer instanceof  VideoTranscodingStatusUpdateListener)
                  // ((VideoTranscodingStatusUpdateListener)videoContainer).processTranscodingNotification(videoStorageItem);
            } catch (Exception e) {
                LOGGER.error("Transcoding status update failed for Video :"+ videoContainer.toString(), e);
            }
        }
        this.setProgress("All Done ....");
        } catch(Exception e) {
                LOGGER.error("Transcoding status update task failed ", e);
        } finally {
            if ( disLock != null) disLock.unlock();
        }
    }
}
