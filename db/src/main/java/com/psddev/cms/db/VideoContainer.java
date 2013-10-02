package com.psddev.cms.db; 
import java.util.Map;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import com.psddev.dari.db.Modification;
import com.psddev.dari.db.ObjectField;
import com.psddev.dari.db.ObjectType;
import com.psddev.dari.db.Recordable;
import com.psddev.dari.util.VideoStorageItem;

/**
* Interface used to add VideContainer Modification 
*/
public interface VideoContainer extends Recordable {
    
    public enum TranscodingStatus {
            PENDING,
            SUCCEEDED,
            FAILED
    }

    /**
    * Modification which add information specific to video
    */
    @Modification.FieldInternalNamePrefix("cms.video.")
    public static final class Data extends Modification<VideoContainer> {

        //private static final Logger LOGGER = LoggerFactory.getLogger(Data.class);

        @Indexed
        private TranscodingStatus transcodingStatus;
        
        public TranscodingStatus getTranscodingStatus() {
            return transcodingStatus;
        }

        public void setTranscodingStatus(TranscodingStatus transcodingStatus) {
            this.transcodingStatus = transcodingStatus;
        }

        public ObjectField getVideoStorageItemField() {
            return getState().getType().as(TypeModification.class).getVideoStorageItemField();
        }

        public VideoStorageItem getFile() {
            return (getVideoStorageItemField() == null ? null : (VideoStorageItem) getState().getByPath(getVideoStorageItemField().getInternalName()));
        }

        /** Sets the collection of metadata. */
        public void setMetadata(Map<String, Object> metadata) {
            getFile().setMetadata(metadata);
        }

        public Map<String, Object> getMetadata() {
            return getFile().getMetadata();
        }

        @Override
        public void beforeSave() {
            if (transcodingStatus == null)
                transcodingStatus=TranscodingStatus.PENDING;
        }
    }

    public static final class TypeModification extends Modification<ObjectType> {
        //private static final Logger LOGGER = LoggerFactory.getLogger(TypeModification.class);

        private ObjectField videoStorageItemField;

        public ObjectField getVideoStorageItemField() {
            videoStorageItemField = null;
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
