package com.psddev.cms.db;

//import java.util.LinkedHashMap;
import java.util.Map;

import com.psddev.dari.db.Modification;
import com.psddev.dari.db.Recordable;

/**
* Interface used to add VideContainer Modification 
*/
public interface VideoContainer extends Recordable {
    
    /**
    * Modification which add information specific to video
    */
    @Modification.FieldInternalNamePrefix("cms.video.")
    public static final class VideoContainerModification extends Modification<VideoContainer> {
        //private Map<String, Object> metadata;
        @Indexed
        private String status;
        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        /** Sets the collection of metadata. */
        public void setMetadata(Map<String, Object> metadata) {
            // TODO: This needs to find the video StorageItem contained in this originalObject and proxy this call to it
        }

        public Map<String, Object> getMetadata() {
            // TODO: This needs to find the video StorageItem contained in this originalObject and proxy this call to it
            return null;
        }

        @Override
        public void beforeSave() {
        }
    }

}
