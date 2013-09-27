package com.psddev.cms.video;

import java.util.LinkedHashMap;
import java.util.Map;

import com.psddev.dari.db.Modification;
/**
 * Modification which add information specific to video
 */
public class VideoContainerModification extends Modification<VideoContainer> {
	private Map<String, Object> metadata;
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
		this.metadata = metadata;
	}

	public Map<String, Object> getMetadata() {
		if (metadata == null) {
			metadata = new LinkedHashMap<String, Object>();
		}
		return metadata;
	}
}
