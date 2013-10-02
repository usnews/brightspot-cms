package com.psddev.cms.db; 
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kaltura.client.enums.KalturaEntryStatus;
import com.psddev.cms.db.Content;
import com.psddev.dari.db.Query;
import com.psddev.dari.util.KalturaStorageItem;
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
		LOGGER.info("VideoTranscodingStatusUpdateTask starting...");
		this.setProgress("Starting ....");
		List<VideoContainer> pendingVideoList = Query
		        .from(VideoContainer.class)
		        .where("cms.video.transcodingStatus = 'pending' ").selectAll();
		this.setProgress("Identified pending videos. Pending video count ...."
		        + pendingVideoList.size());
		for (VideoContainer video : pendingVideoList) {
			try {
				VideoContainer.VideoContainerModification videoContainerModification = video
				        .as(VideoContainer.VideoContainerModification.class);
				StorageItem videoStorage = videoContainerModification.getFile();
				if (videoStorage instanceof KalturaStorageItem) {
					KalturaStorageItem kalturaStorageItem = (KalturaStorageItem) videoStorage;
					kalturaStorageItem.pull();
					KalturaEntryStatus kalturaEntryStatus = kalturaStorageItem.getStatus();  
					// If transcoding is complete..update status on the video
					// and save..
					if (kalturaEntryStatus.equals(com.kaltura.client.enums.KalturaEntryStatus.READY))
				        {
						videoContainerModification.setTranscodingStatus(VideoContainer.TranscodingStatus.SUCCEEDED);
						videoContainerModification.save();
					} else if (kalturaEntryStatus.equals(KalturaEntryStatus.ERROR_CONVERTING) ||
							   kalturaEntryStatus.equals(KalturaEntryStatus.SCAN_FAILURE) ||
							   kalturaEntryStatus.equals(KalturaEntryStatus.INFECTED) ||
							   kalturaEntryStatus.equals(KalturaEntryStatus.NO_CONTENT) ) {
						videoContainerModification.setTranscodingStatus(VideoContainer.TranscodingStatus.FAILED);
						videoContainerModification.save();
					}
				}
			} catch (Exception e) {
				LOGGER.error("Transcoding status update failed for Video :"+ video.toString(), e);
			}
		}
		this.setProgress("All Done ....");
        }
}

