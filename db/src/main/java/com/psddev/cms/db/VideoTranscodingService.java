package com.psddev.cms.db; 
public interface VideoTranscodingService {
     public boolean updateThumbnailsInCms();
//     public void updateEventData(VideoContainer.Data videoData);
     public void deleteEvent(String externalId);
}

