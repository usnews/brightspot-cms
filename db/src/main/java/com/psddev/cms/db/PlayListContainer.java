package com.psddev.cms.db;

import java.util.Date;
import java.util.List;

import com.psddev.dari.db.Modification;
import com.psddev.dari.db.Recordable;

/**
 * Interface used to create a playlist
 */
public interface PlayListContainer extends Recordable {

    public List<VideoContainer> getVideos();

    /**
     * Modification which add information specific to playlist
     */
    @Modification.FieldInternalNamePrefix("cms.playlist.")
    public static final class Data extends Modification<PlayListContainer> {
        @Indexed
        @ToolUi.ReadOnly
        private String externalId;

        public String getExternalId() {
            return externalId;
        }

        public void setExternalId(String externalId) {
            this.externalId = externalId;
        }

        @Override
        public void beforeSave() {
            VideoTranscodingService pls = VideoTranscodingServiceFactory.getDefault();
            PlayListContainer PlayListContainer = (PlayListContainer) getState().getOriginalObject();
            List<VideoContainer> videoContainers = PlayListContainer.getVideos();
            if (externalId == null) {
                if (videoContainers != null & videoContainers.size() > 0) {
                    externalId = pls.createPlayList(Long.toString(new Date().getTime()), videoContainers);
                }
            } else {
                pls.updatePlayList(externalId, videoContainers);
            }
        }

        @Override
        public void beforeDelete() {
            if (externalId != null) {
                VideoTranscodingService pls = VideoTranscodingServiceFactory.getDefault();
                pls.deletePlayList(externalId);
            }
        }
    }

}
