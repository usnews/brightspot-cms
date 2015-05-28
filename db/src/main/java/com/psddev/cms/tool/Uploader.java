package com.psddev.cms.tool;

import java.io.IOException;

import com.psddev.dari.db.ObjectField;

/**
 * An interface to extend the StorageItemField to allow
 * for adding front end uploaders
 */
public interface Uploader {

    /**
     * Returns {@code true} if this uploader is supported
     * for uploading the given {@code field}
     *
     * @param field Can't be {@code null}.
     */
    public boolean isSupported(ObjectField field);

    /**
     * Returns {@code true} if this uploader is the preferred default for
     * uploading the given {@code field}.
     *
     * @param field Can't be {@code null}.
     */
    public boolean isPreferred(ObjectField field);

    /**
     * Returns class used by javascript to which DOM
     * elements the uploader requires
     */
    public String getClassIdentifier();

    /**
     * @param page   Can't be {@code null}.
     * @param field
     * @throws IOException if unable to write to the given {@code page}.
     */
    public void writeHtml(
            ToolPageContext page,
            ObjectField field)
            throws IOException;
}
