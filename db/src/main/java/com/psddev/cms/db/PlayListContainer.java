package com.psddev.cms.db;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.psddev.dari.db.Modification;
import com.psddev.dari.db.ObjectField;
import com.psddev.dari.db.ObjectType;
import com.psddev.dari.db.Recordable;

/**
 * Interface used to create a playlist
 */
public interface PlayListContainer extends Recordable {
    /**
     * Modification which add information specific to playlist
     */
    @Modification.FieldInternalNamePrefix("cms.playlist.")
    public static final class Data extends Modification<PlayListContainer> {
        private static final Logger LOGGER = LoggerFactory.getLogger(Data.class);
        @Indexed
        @ToolUi.ReadOnly
        private String externalId;

        public String getExternalId() {
            return externalId;
        }

        public void setExternalId(String externalId) {
            this.externalId = externalId;
        }

        public ObjectField getVideoContainerListItemField() {
            return getState().getType().as(TypeModification.class).getVideoContainerListItemField();
        }

        public List<VideoContainer> getVideoContainers() {
            return getVideoContainerListItemField() == null ? null : (List<VideoContainer>) getState().getByPath(getVideoContainerListItemField().getInternalName());
        }

        @Override
        public void beforeSave() {
            VideoTranscodingService pls = VideoTranscodingServiceFactory.getDefault();
            List<VideoContainer> videoContainers = getVideoContainers();
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

    public static final class TypeModification extends Modification<ObjectType> {
        // private static final Logger LOGGER =
        // LoggerFactory.getLogger(TypeModification.class);

        private ObjectField videoStorageItemField;

        public ObjectField getVideoContainerListItemField() {
            videoStorageItemField = null;
            if (videoStorageItemField == null) {
                for (ObjectField field : getOriginalObject().getFields()) {
                    if (List.class.isAssignableFrom(field.getJavaField(getOriginalObject().getObjectClass()).getType())) {
                        videoStorageItemField = field;
                        break;
                    }
                }
            }
            return videoStorageItemField;
        }
    }

}
