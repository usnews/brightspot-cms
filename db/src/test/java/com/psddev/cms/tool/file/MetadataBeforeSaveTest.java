package com.psddev.cms.tool.file;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import com.psddev.dari.util.AbstractStorageItem;
import com.psddev.dari.util.StorageItemUploadPart;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MetadataBeforeSaveTest {

    @Mock
    StorageItemUploadPart part;

    @Spy
    AbstractStorageItem item;

    @Before
    public void before() {
        item.setMetadata(null);
        when(item.getPart()).thenReturn(part);
    }

    @Test
    public void verifyOriginalFileName() {
        String originalFilename = "test";

        when(part.getName()).thenReturn(originalFilename);

        new MetadataBeforeSave().beforeSave(item);

        assertEquals(item.getMetadata().get("originalFilename"), originalFilename);
    }

    @Test
    public void verifyHttpHeaders() {
        long fileSize = 100;
        String fileContentType = "image/jpeg";

        when(part.getSize()).thenReturn(fileSize);
        when(part.getContentType()).thenReturn(fileContentType);

        new MetadataBeforeSave().beforeSave(item);

        Map<String, Object> httpHeaders = (Map<String, Object>) item.getMetadata().get("http.headers");

        assertEquals(((List<String>) httpHeaders.get("Cache-Control")).get(0), "public, max-age=31536000");
        assertEquals(((List<String>) httpHeaders.get("Content-Length")).get(0), String.valueOf(fileSize));
        assertEquals(((List<String>) httpHeaders.get("Content-Type")).get(0), fileContentType);
    }

}