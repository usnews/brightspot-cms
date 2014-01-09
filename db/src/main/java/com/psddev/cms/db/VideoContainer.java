package com.psddev.cms.db;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.psddev.dari.db.Modification;
import com.psddev.dari.db.Recordable;
import com.psddev.dari.util.VideoStorageItem;
import com.psddev.dari.util.VideoStorageItem.DurationType;
import com.psddev.dari.util.VideoStorageItem.TranscodingStatus;

/**
 * Interface used to add VideoContainer Modification
 */
public interface VideoContainer extends Recordable {
    public VideoStorageItem getVideo();

    /**
     * Modification which add information specific to video
     */
    @Modification.FieldInternalNamePrefix("cms.video.")
    public static final class Data extends Modification<VideoContainer> {
        private static final Logger LOGGER = LoggerFactory.getLogger(Data.class);
        /* Length of the video in seconds */
        @Indexed
        @ToolUi.ReadOnly
        @ToolUi.Tab("Video Detail")
        private Long length;

        @Indexed
        @ToolUi.Filterable
        @ToolUi.ReadOnly
        @ToolUi.Tab("Video Detail")
        private TranscodingStatus transcodingStatus;

        /*
         * Transcoding error contains information about why the transcoding
         * failed such as virus infected, no content etc
         */
        @ToolUi.ReadOnly
        @ToolUi.Tab("Video Detail")
        private String transcodingError;

        @Indexed
        @ToolUi.ReadOnly
        @ToolUi.Tab("Video Detail")
        private Date transcodingStatusUpdatedAt;

        @Indexed
        @ToolUi.Filterable
        @ToolUi.ReadOnly
        @ToolUi.Tab("Video Detail")
        private DurationType durationType;

        public Long getLength() {
            return length;
        }

        public void setLength(Long length) {
            this.length = length;
        }

        public TranscodingStatus getTranscodingStatus() {
            return transcodingStatus;
        }

        public void setTranscodingStatus(TranscodingStatus transcodingStatus) {
            this.transcodingStatus = transcodingStatus;
        }

        public String getTranscodingError() {
            return transcodingError;
        }

        public void setTranscodingError(String transcodingError) {
            this.transcodingError = transcodingError;
        }

        public Date getTranscodingStatusUpdatedAt() {
            return transcodingStatusUpdatedAt;
        }

        public void setTranscodingStatusUpdatedAt(Date transcodingStatusUpdatedAt) {
            this.transcodingStatusUpdatedAt = transcodingStatusUpdatedAt;
        }

        public DurationType getDurationType() {
            return durationType;
        }

        public void setDurationType(DurationType durationType) {
            this.durationType = durationType;
        }

        @Indexed
        @ToolUi.ReadOnly
        @ToolUi.Tab("Video Detail")
        private String externalId;

        public String getExternalId() {
            return externalId;
        }

        public void setExternalId(String externalId) {
            this.externalId = externalId;
        }

        @Override
        public void beforeDelete() {
            try {
                VideoContainer vidContainer = (VideoContainer) getState().getOriginalObject();
                if (vidContainer != null && vidContainer.getVideo() != null) {
                    vidContainer.getVideo().delete();
                }
            } catch (Exception e) {
                LOGGER.error("Failed to delete storage item", e);
            }
        }

        @Override
        public void beforeSave() {
            if (transcodingStatus == null) {
                transcodingStatus = TranscodingStatus.PENDING;
            }
        }
    }
}
