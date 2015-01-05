package com.psddev.cms.tool;

import com.psddev.dari.db.ObjectType;

import java.io.IOException;

public interface SearchResultField {

    /**
     * @return Never {@code null}.
     */
    public String getDisplayName();

    /**
     * @param type Can't be {@code null}.
     * @return Never {@code null}.
     */
    public boolean isSupported(ObjectType type);

    /**
     * @param type Can't be {@code null}.
     * @return Never {@code null}.
     */
    public boolean isDefault(ObjectType type);

    /**
     * @param page Can't be {@code null}.
     * @throws IOException if unable to write to the given {@code page}.
     */
    public void writeTableHeaderCellHtml(ToolPageContext page) throws IOException;

    /**
     * @param page Can't be {@code null}.
     * @param item Can't be {@code null}.
     * @throws IOException if unable to write to the given {@code page}.
     */
    public void writeTableDataCellHtml(ToolPageContext page, Object item) throws IOException;
}
