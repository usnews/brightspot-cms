package com.psddev.cms.db;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import org.joda.time.DateTime;
import java.util.UUID;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.psddev.dari.db.Query;
import com.psddev.dari.db.Database;
import com.psddev.dari.db.DistributedLock;
import com.psddev.dari.util.VideoStorageItem;
import com.psddev.dari.util.VideoStorageItem.TranscodingStatus;
import com.psddev.dari.util.VideoStorageItemListener;
import com.psddev.dari.util.RepeatingTask;

public  class VideoTranscodingStatusUpdateTask extends RepeatingTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(VideoTranscodingStatusUpdateTask.class);
    private static final String DEFAULT_TASK_NAME = "Video Transcoding Status Update Task";
    private static final long updateIntervalMillis = 60000L;

    @Override
    protected DateTime calculateRunTime(DateTime currentTime) {
        return everyMinute(currentTime);
    }

    public VideoTranscodingStatusUpdateTask() {
        super(null, DEFAULT_TASK_NAME);
    }

    @Override
    protected void doRepeatingTask(DateTime runTime) throws IOException {
        if(!shouldContinue())  return;
        DistributedLock disLock = null;
        LOGGER.debug("VideoTranscodingStatusUpdateTask starting....");
        try {
            if (Query.from(VideoContainer.class).where("cms.video.transcodingStatus = 'pending'").first() == null) {
                return;
            }

            disLock = DistributedLock.Static.getInstance(Database.Static.getDefault(), VideoTranscodingStatusUpdateTask.class.getName());
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
                    //Check to see if status was updated with in the update interval (currently one minute) ..if so skip it
                    if (videoData.getTranscodingStatusUpdatedAt() != null  &&
                            (System.currentTimeMillis() - videoData.getTranscodingStatusUpdatedAt().getTime()) < updateIntervalMillis) {
                        continue;
                    }

                    VideoStorageItem videoStorageItem = videoData.getFile();
                    //If there are listeners set..query using the ids and set it on the storage item
                    //before invoking pull method
                    List <UUID> videoStorageItemListenerIds=videoStorageItem.getVideoStorageItemListenerIds();
                    if (videoStorageItemListenerIds != null ) {
                        List <VideoStorageItemListener> videoStorageItemListeners = new ArrayList<VideoStorageItemListener>();
                        for (UUID listenerId : videoStorageItemListenerIds) {
                            VideoStorageItemListener listener = Query.findById(VideoStorageItemListener.class, listenerId);
                            if (listener != null) {
                                videoStorageItemListeners.add(listener);
                            }
                        }
                        videoStorageItem.setVideoStorageItemListeners(videoStorageItemListeners);
                    }

                    //Invoke pull method from storage to check  the status
                    videoStorageItem.pull();
                    boolean statusUpdated = false;
                    try {
                        TranscodingStatus videoTranscodingStatus = videoStorageItem.getTranscodingStatus();
                        // If transcoding is complete..update transcoding status on the video
                        // and length ..if failed..updated the error message
                        if (videoTranscodingStatus.equals(TranscodingStatus.SUCCEEDED)) {
                            videoData.setLength(videoStorageItem.getLength());
                            statusUpdated = true;
                        } else if (videoTranscodingStatus.equals(TranscodingStatus.FAILED)) {
                            videoData.setTranscodingError(videoStorageItem.getTranscodingError());
                            statusUpdated = true;
                        }
                        //Updated the transcodingStatus
                        try {
                            videoData.setTranscodingStatus(videoTranscodingStatus);
                        } catch (UnsupportedOperationException e) {
                            // Safe to ignore.
                        }
                    } catch (UnsupportedOperationException e) {
                        // Safe to ignore.
                    }
                    try {
                        videoData.setDurationType(videoStorageItem.getDurationType());
                    } catch (UnsupportedOperationException e) {
                        // Safe to ignore.
                    }
                    try {
                        List<Integer> videoFlavorIds = videoStorageItem.getTranscodingFlavorIds();
                        if (videoFlavorIds != null) {
                            List<VideoTranscodingFlavor> transcodingFlavors = new ArrayList<VideoTranscodingFlavor>(videoFlavorIds.size());
                            for (Integer flavorId : videoFlavorIds ) {
                                VideoTranscodingFlavor tf = Query.from(VideoTranscodingFlavor.class).where("externalId = ?", flavorId).first();
                                if (tf != null)  transcodingFlavors.add(tf);
                            }
                            videoData.setTranscodingFlavors(transcodingFlavors);
                        }
                    } catch (UnsupportedOperationException e) {
                        // Safe to ignore.
                    }
                    videoData.setTranscodingStatusUpdatedAt(new Date());
                    videoData.save();
                    //Sends a notification if it implement VideoTranscodingStatusListener
                    if (statusUpdated && videoStorageItemListenerIds != null) {
                        videoStorageItem.notifyVideoStorageItemListeners();
                    }
                } catch (Exception e) {
                    LOGGER.error("Transcoding status update failed for Video :"+ videoContainer.toString(), e);
                }
            }
            this.setProgress("All Done ....");
        } catch(Exception e) {
            LOGGER.error("Transcoding status update task failed ", e);
        } finally {
            if (disLock != null) {
                disLock.unlock();
            }
        }
    }
}
