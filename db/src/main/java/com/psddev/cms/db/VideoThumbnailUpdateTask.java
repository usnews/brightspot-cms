package com.psddev.cms.db;

import java.io.IOException;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.psddev.dari.db.Database;
import com.psddev.dari.db.DistributedLock;
import com.psddev.dari.util.RepeatingTask;

public class VideoThumbnailUpdateTask extends RepeatingTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(VideoThumbnailUpdateTask.class);
    private static final String DEFAULT_TASK_NAME = "Video Thumbnail  Update Task";

    @Override
    protected DateTime calculateRunTime(DateTime currentTime) {
        return everyMinute(currentTime);
    }

    public VideoThumbnailUpdateTask() {
        super(null, DEFAULT_TASK_NAME);
    }

    @Override
    protected void doRepeatingTask(DateTime runTime) throws IOException {
        if (!shouldContinue()) {
            return;
        }
        DistributedLock disLock = null;
        LOGGER.debug("VideoThumbnailUpdateTask starting....");
        try {
            disLock = DistributedLock.Static.getInstance(Database.Static.getDefault(), VideoThumbnailUpdateTask.class.getName());
            disLock.lock();
            this.setProgress("Starting ....");
            VideoTranscodingServiceFactory.getDefault().updateThumbnailsInCms();
            this.setProgress("All Done ....");
        } catch (Exception e) {
            LOGGER.error("VideoThumbnail  update task failed ", e);
        } finally {
            if (disLock != null) {
                disLock.unlock();
            }
        }
    }
}
