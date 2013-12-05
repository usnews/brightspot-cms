package com.psddev.cms.db;
import java.util.Map;
import java.util.Date;
import java.util.List;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import com.psddev.dari.db.Modification;
import com.psddev.dari.db.ObjectField;
import com.psddev.dari.db.ObjectType;
import com.psddev.dari.db.Recordable;
import com.psddev.dari.util.VideoStorageItem;
import com.psddev.dari.util.VideoStorageItem.TranscodingStatus;
import com.psddev.dari.util.VideoStorageItem.DurationType;

/**
* Interface used to add VideContainer Modification
*/
public interface VideoContainer extends Recordable {

    /**
    * Modification which add information specific to video
    */
    @Modification.FieldInternalNamePrefix("cms.video.")
    public static final class Data extends Modification<VideoContainer> {
        //private static final Logger LOGGER = LoggerFactory.getLogger(Data.class);
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

        /* Trascoding error contains information about why the transcoding failed such as
           virus infected, no content etc */
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

        @Indexed
        @ToolUi.Filterable
        @ToolUi.ReadOnly
        @ToolUi.Tab("Video Detail")
        private List<VideoTranscodingFlavor> transcodingFlavors;

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
            this.transcodingStatusUpdatedAt=transcodingStatusUpdatedAt;
        }

        public DurationType getDurationType() {
            return durationType;
        }
        public void setDurationType(DurationType durationType) {
            this.durationType=durationType;
        }

        public List<VideoTranscodingFlavor>  getTranscodingFlavors() {
            return transcodingFlavors;
        }

        public void setTranscodingFlavors(List<VideoTranscodingFlavor> transcodingFlavors) {
            this.transcodingFlavors=transcodingFlavors;
        }

        public ObjectField getVideoStorageItemField() {
            return getState().getType().as(TypeModification.class).getVideoStorageItemField();
        }

        public VideoStorageItem getFile() {
            return (getVideoStorageItemField() == null ? null : (VideoStorageItem) getState().getByPath(getVideoStorageItemField().getInternalName()));
        }

        public String getExternalId() {
            return getFile().getExternalId();
        }

        @Override
        public void beforeSave() {
            if (transcodingStatus == null) {
                transcodingStatus = TranscodingStatus.PENDING;
            }
        }
    }

    public static final class TypeModification extends Modification<ObjectType> {
        //private static final Logger LOGGER = LoggerFactory.getLogger(TypeModification.class);

        private ObjectField videoStorageItemField;

        public ObjectField getVideoStorageItemField() {
            if (videoStorageItemField == null) {
                for (ObjectField field : getOriginalObject().getFields()) {
                    if (VideoStorageItem.class.isAssignableFrom(field.getJavaField(getOriginalObject().getObjectClass()).getType())) {
                        videoStorageItemField = field;
                        break;
                    }
                }
            }
            return videoStorageItemField;
        }
    }

}
