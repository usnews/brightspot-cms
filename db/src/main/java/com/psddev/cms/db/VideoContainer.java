package com.psddev.cms.db; 
import java.util.Map;

import com.psddev.dari.db.Modification;
import com.psddev.dari.db.Recordable;
import com.psddev.dari.util.StorageItem;
import com.psddev.cms.db.ToolUi;

/**
* Interface used to add VideContainer Modification 
*/
public interface VideoContainer extends Recordable {
    
    public  enum  TranscodingStatus {
            PENDING,
            SUCCEEDED,
            FAILED
        }
    /**
    * Modification which add information specific to video
    */
    @Modification.FieldInternalNamePrefix("cms.video.")
    public static final class VideoContainerModification extends Modification<VideoContainer> {
        //private Map<String, Object> metadata;

	@Indexed
	private TranscodingStatus transcodingStatus;
	
	public TranscodingStatus getTranscodingStatus() {
		return transcodingStatus;
	}
	public void setTranscodingStatus(TranscodingStatus transcodingStatus) {
		this.transcodingStatus = transcodingStatus;
	}
        @Required
        @ToolUi.Note("Required Field")
        private StorageItem file;
        
	public StorageItem getFile() {
		return file;
	}

	public void setFile(StorageItem file) {
		this.file = file;
	}

        /** Sets the collection of metadata. */
        public void setMetadata(Map<String, Object> metadata) {
             file.setMetadata(metadata);
        }

        public Map<String, Object> getMetadata() {
            return file.getMetadata();
        }

        @Override
        public void beforeSave() {
            if (transcodingStatus == null)
              transcodingStatus=TranscodingStatus.PENDING;
        }
    }

}
