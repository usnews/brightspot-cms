package com.psddev.cms.tool.file;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import com.psddev.dari.util.Settings;
import com.psddev.dari.util.SparseSet;
import com.psddev.dari.util.StorageItemUploadPart;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ContentTypeValidatorTest {

    private static final String RESOURCE_PATH_PREFIX = "com/psddev/cms/tool/file/ContentTypeValidator_Test/";

    @Mock
    File file;

    @Mock
    StorageItemUploadPart part;

    ContentTypeValidator validator;

    @Before
    public void before() {
        validator = new ContentTypeValidator();
        when(part.getFile()).thenReturn(file);
    }

    @Test
    public void nullPart() throws IOException {
        validator.beforeCreate(null);
        verify(part, times(0)).getContentType();
    }

    @Test
    public void nullContentType() throws IOException {
        when(part.getContentType()).thenReturn(null);
        validator.beforeCreate(part);
    }

    @Test(expected = IllegalStateException.class)
    public void invalidContentType() throws IOException, URISyntaxException {
        Settings.setOverride("cms/tool/fileContentTypeGroups", "+image/jpeg");
        when(part.getContentType()).thenReturn("image/png");
        validator.beforeCreate(part);
    }

    @Test
    public void validContentType() throws IOException, URISyntaxException {
        String fileContentType = "image/png";

        when(part.getContentType()).thenReturn("image/png");

        Set<String> contentTypeGroups = new SparseSet();
        contentTypeGroups.add(fileContentType);
        contentTypeGroups.add("text/html");
        Settings.setOverride("cms/tool/fileContentTypeGroups", contentTypeGroups.toString());

        validator.beforeCreate(part);
    }

    @Test(expected = IOException.class)
    public void disguisedHtmlFile() throws IOException, URISyntaxException {

        Settings.setOverride("cms/tool/fileContentTypeGroups", "+image/jpeg");

        String fileName = "html-image.jpg";
        when(part.getContentType()).thenReturn("image/jpeg");
        when(part.getFile()).thenReturn(new File(getClass().getClassLoader().getResource(RESOURCE_PATH_PREFIX + fileName).toURI()));

        validator.beforeCreate(part);
    }

    @Test
    public void validFile() throws IOException, URISyntaxException {
        Settings.setOverride("cms/tool/fileContentTypeGroups", "+image/png");

        String fileName = "test.png";
        when(part.getContentType()).thenReturn("image/png");
        when(part.getFile()).thenReturn(new File(getClass().getClassLoader().getResource(RESOURCE_PATH_PREFIX + fileName).toURI()));
        validator.beforeCreate(part);
    }
}