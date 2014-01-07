package com.psddev.cms.db;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.psddev.dari.db.Database;
import com.psddev.dari.db.DistributedLock;
import com.psddev.dari.db.Query;
import com.psddev.dari.util.RepeatingTask;
import com.psddev.dari.util.VideoStorageItem;
import com.psddev.dari.util.VideoStorageItem.TranscodingStatus;

/***
 *
 * This task performs a check for changes to transcoding status using the
 * storage item and updates the CMS if there is a change. Storage iteminternally
 * uses OVP API to retrieve information. Also updates thumbnail path of the
 * video if it's updated in the OVP's such as Kaltura/thePlatform etc
 *
 */
public class VideoServiceTask extends RepeatingTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(VideoServiceTask.class);
    private static final String DEFAULT_TASK_NAME = "Video Service Task";
    private static final long updateIntervalMillis = 60000L;

    @Override
    protected DateTime calculateRunTime(DateTime currentTime) {
        return everyMinute(currentTime);
    }

    public VideoServiceTask() {
        super(null, DEFAULT_TASK_NAME);
    }

    @Override
    protected void doRepeatingTask(DateTime runTime) throws IOException {
        if (!shouldContinue()) {
            return;
        }
        DistributedLock disLock = null;
        disLock = DistributedLock.Static.getInstance(Database.Static.getDefault(), VideoServiceTask.class.getName());
        disLock.lock();

        LOGGER.debug("VideoServiceTask starting....");
        try {
            // Update thumbnail paths if they are updated
            if (VideoTranscodingServiceFactory.getDefault() != null) {
                VideoTranscodingServiceFactory.getDefault().updateThumbnailsInCms();
            }

            // Update video transcoding status
            if (Query.from(VideoContainer.class).where("cms.video.transcodingStatus = 'pending'").first() == null) {
                return;
            }

            this.setProgress("Starting ....");
            List<VideoContainer> pendingVideoList = Query.from(VideoContainer.class).where("cms.video.transcodingStatus = 'pending' ").selectAll();
            this.setProgress("Identified pending videos. Pending video count ...." + pendingVideoList.size());
            for (VideoContainer videoContainer : pendingVideoList) {
                try {
                    VideoContainer.Data videoData = videoContainer.as(VideoContainer.Data.class);
                    // Check to see if status was updated with in the update
                    // interval (currently one minute) ..if so skip it
                    if (videoData.getTranscodingStatusUpdatedAt() != null && System.currentTimeMillis() - videoData.getTranscodingStatusUpdatedAt().getTime() < updateIntervalMillis) {
                        continue;
                    }
                    VideoStorageItem videoStorageItem = videoContainer.getVideo();
                    // Invoke pull method from storage to check the status
                    videoStorageItem.pull();
                    TranscodingStatus videoTranscodingStatus = videoStorageItem.getTranscodingStatus();
                    // If transcoding is complete..update transcoding status and length/duration
                    // if failed..updated the error message
                    boolean statusUpdated = false;
                    if (videoTranscodingStatus.equals(TranscodingStatus.SUCCEEDED)) {
                        videoData.setLength(videoStorageItem.getLength());
                        statusUpdated = true;
                    } else if (videoTranscodingStatus.equals(TranscodingStatus.FAILED)) {
                        videoData.setTranscodingError(videoStorageItem.getTranscodingError());
                        statusUpdated = true;
                    }

                    // Updated the transcodingStatus
                    videoData.setTranscodingStatus(videoStorageItem.getTranscodingStatus());
                    videoData.setDurationType(videoStorageItem.getDurationType());
                    // End update related to transcoding flavor
                    videoData.setTranscodingStatusUpdatedAt(new Date());
                    videoData.setExternalId(videoStorageItem.getExternalId());
                    videoData.save();
                    if (statusUpdated && videoStorageItem.getVideoStorageItemListenerIds() != null) {
                        videoStorageItem.notifyVideoStorageItemListeners();
                    }
                } catch (Exception e) {
                    LOGGER.error("Transcoding status update failed for Video :" + videoContainer.toString(), e);
                }

            }

            this.setProgress("All Done ....");
        } catch (Exception e) {
            LOGGER.error("Transcoding status update task failed ", e);
        } finally {
            if (disLock != null) {
                disLock.unlock();
            }
        }
    }
}
