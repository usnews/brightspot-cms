package com.psddev.cms.db; 
public interface TranscodingService {
     public boolean updateTranscodingFlavorsInCms();
     public boolean updateThumbnailsInCms();
     public void updateEventData(VideoContainer.Data videoData);
     public void deleteEvent(String externalId);
}

