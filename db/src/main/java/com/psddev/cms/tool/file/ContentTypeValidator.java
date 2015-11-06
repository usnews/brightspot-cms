package com.psddev.cms.tool.file;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.Settings;
import com.psddev.dari.util.SparseSet;
import com.psddev.dari.util.StorageItemBeforeCreate;
import com.psddev.dari.util.StorageItemUploadPart;

/**
 * Validates the content type of a file. Throws an error
 * if the content type is not accepted per the Application's
 * {@link Settings}, and also throws an error if the file is
 * an HTML file disguised as another content type.
 */
public class ContentTypeValidator implements StorageItemBeforeCreate {

    @Override
    public void beforeCreate(StorageItemUploadPart part) throws IOException {
        Preconditions.checkNotNull(part);
        Preconditions.checkNotNull(part.getFile());

        String fileContentType = part.getContentType();
        if (fileContentType == null) {
            return;
        }

        String groupsPattern = Settings.get(String.class, "cms/tool/fileContentTypeGroups");
        Set<String> contentTypeGroups = new SparseSet(ObjectUtils.isBlank(groupsPattern) ? "+/" : groupsPattern);

        Preconditions.checkState(contentTypeGroups.contains(fileContentType),
                "Invalid content type " + fileContentType + ". Must match the pattern " + contentTypeGroups + ".");

        // Disallow HTML disguising as other content types per:
        // http://www.adambarth.com/papers/2009/barth-caballero-song.pdf
        if (!contentTypeGroups.contains("text/html")) {
            try (InputStream input = new FileInputStream(part.getFile())) {
                byte[] buffer = new byte[1024];
                String data = new String(buffer, 0, input.read(buffer)).toLowerCase(Locale.ENGLISH);
                String ptr = data.trim();

                if (ptr.startsWith("<!")
                        || ptr.startsWith("<?")
                        || data.startsWith("<html")
                        || data.startsWith("<script")
                        || data.startsWith("<title")
                        || data.startsWith("<body")
                        || data.startsWith("<head")
                        || data.startsWith("<plaintext")
                        || data.startsWith("<table")
                        || data.startsWith("<img")
                        || data.startsWith("<pre")
                        || data.startsWith("text/html")
                        || data.startsWith("<a")
                        || ptr.startsWith("<frameset")
                        || ptr.startsWith("<iframe")
                        || ptr.startsWith("<link")
                        || ptr.startsWith("<base")
                        || ptr.startsWith("<style")
                        || ptr.startsWith("<div")
                        || ptr.startsWith("<p")
                        || ptr.startsWith("<font")
                        || ptr.startsWith("<applet")
                        || ptr.startsWith("<meta")
                        || ptr.startsWith("<center")
                        || ptr.startsWith("<form")
                        || ptr.startsWith("<isindex")
                        || ptr.startsWith("<h1")
                        || ptr.startsWith("<h2")
                        || ptr.startsWith("<h3")
                        || ptr.startsWith("<h4")
                        || ptr.startsWith("<h5")
                        || ptr.startsWith("<h6")
                        || ptr.startsWith("<b")
                        || ptr.startsWith("<br")) {
                    throw new IOException("Can't upload [" + fileContentType + "] file disguising as HTML!");
                }
            }
        }
    }
}
