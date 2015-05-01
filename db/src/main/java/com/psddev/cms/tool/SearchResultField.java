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
    public default boolean isDefault(ObjectType type) {
        return false;
    }

    /**
     * @param page Can't be {@code null}.
     * @throws IOException if unable to write to the given {@code page}.
     */
    public default void writeHeaderCellText(ToolPageContext page) throws IOException {
        page.writeHtml(getDisplayName());
    }

    /**
     * @param page Can't be {@code null}.
     * @throws IOException if unable to write to the given {@code page}.
     */
    public default void writeTableHeaderCellHtml(ToolPageContext page) throws IOException {
        page.writeStart("th");
        writeHeaderCellText(page);
        page.writeEnd();
    }

    /**
     * @param page Can't be {@code null}.
     * @param item Can't be {@code null}.
     * @throws IOException if unable to write to the given {@code page}.
     */
    public void writeDataCellText(ToolPageContext page, Object item) throws IOException;

    /**
     * @param page Can't be {@code null}.
     * @param item Can't be {@code null}.
     * @throws IOException if unable to write to the given {@code page}.
     */
    public default void writeTableDataCellHtml(ToolPageContext page, Object item) throws IOException {
        page.writeStart("td");
        writeDataCellText(page, item);
        page.writeEnd();
    }
}
