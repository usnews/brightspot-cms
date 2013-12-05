package com.psddev.cms.db;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import org.joda.time.DateTime;

import com.psddev.dari.db.Database;
import com.psddev.dari.db.DistributedLock;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.RepeatingTask;
import com.psddev.dari.util.Settings;
/***
* This task updates the transcoding flavors defined in the external transcoding system such as kaltura etc to Brightspot CMS
**/
public  class VideoTranscodingFlavorUpdateTask extends RepeatingTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(VideoTranscodingFlavorUpdateTask.class);
    private static final String DEFAULT_TASK_NAME = "Video Transcoding Flavor Update Task";

    @Override
    protected DateTime calculateRunTime(DateTime currentTime) {
        return everyMinute(currentTime);
    }

    public VideoTranscodingFlavorUpdateTask() {
        super(null, DEFAULT_TASK_NAME);
    }

    @Override
    protected void doRepeatingTask(DateTime runTime) throws IOException {
        if(!shouldContinue()) {
            return;
        }

        DistributedLock disLock = null;
        LOGGER.debug("VideoTranscodingFlavorUpdateTask starting....");

        try {
           String defaultVideoStorage = ObjectUtils.to(String.class, Settings.get("dari/defaultVideoStorage"));
           if (defaultVideoStorage == null) {
               return;
           }
           VideoTranscodingService ts = VideoTranscodingServiceFactory.getTranscodingService(defaultVideoStorage);
           if (ts == null) {
               return;
           }
           disLock = DistributedLock.Static.getInstance(Database.Static.getDefault(), VideoTranscodingStatusUpdateTask.class.getName());
           disLock.lock();
           this.setProgress("Starting ....");
           ts.updateTranscodingFlavorsInCms();
           this.setProgress("All Done ....");
        } catch(Exception e) {
            LOGGER.error("Transcoding flavor update task failed ", e);
        } finally {
            if (disLock != null) {
                disLock.unlock();
            }
        }
    }
}
