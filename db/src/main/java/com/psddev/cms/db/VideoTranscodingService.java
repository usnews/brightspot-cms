package com.psddev.cms.db;

import java.util.List;

public interface VideoTranscodingService {
    public String getSessionId();

    public boolean updateThumbnailsInCms();

    // public void updateEventData(VideoContainer.Data videoData);
    public void deleteEvent(String externalId);

    public String createPlayList(String name, List<VideoContainer> items);

    public boolean updatePlayList(String externalId, List<VideoContainer> items);

    public boolean deletePlayList(String externalId);
}
