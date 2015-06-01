package com.psddev.cms.tool;

import java.io.IOException;

import com.psddev.dari.db.ObjectField;

/**
 * An interface to extend the StorageItemField to allow
 * for adding front end uploaders
 */
public interface Uploader {

    public static final double DEFAULT_PRIORITY = 0;

    /**
     * Returns {@code double} as a priority rating for
     * this Uploader. The highest priority will be used
     * by {@code StorageItemField}. Return a value less
     * than zero if Uploader should not be supported.
     *
     * @param field Can't be {@code null}.
     */
    public double getPriority(ObjectField field);

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
