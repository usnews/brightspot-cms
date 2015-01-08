package com.psddev.cms.tool.search;

import com.psddev.cms.db.Site;
import com.psddev.cms.tool.Search;
import com.psddev.dari.db.State;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.PaginatedResult;
import com.psddev.dari.util.StorageItem;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

public class CarouselSearchResultView extends ListSearchResultView {

    @Override
    public boolean isSupported(Search search) {
        return search.isShowCarousel();
    }

    @Override
    public boolean isPreferred(Search search) {
        return isSupported(search);
    }

    @Override
    protected void writeItemsHtml(Collection<?> items) throws IOException {

        UUID currentContentId = page.param(UUID.class, "id");

        page.writeStart("div", "class", "searchResult-carousel",
            "data-next-page", result.hasNext() ? page.url("", Search.OFFSET_PARAMETER, result.getNextOffset()) : "",
            "data-prev-page", result.hasPrevious() ? page.url("", Search.OFFSET_PARAMETER, result.getPreviousOffset()) : "");
        for (Object item : items) {

            State itemState = State.getInstance(item);
            UUID itemId = itemState.getId();

            itemWriter.writeBeforeHtml(page, search, item);

            page.writeStart("div", "class", "carousel-tile-content " + (ObjectUtils.equals(currentContentId, itemId) ? "carousel-tile-content-active" : ""));
                writeContentPreview(item);
            page.writeEnd();

            itemWriter.writeAfterHtml(page, search, item);
        }
        page.writeEnd();
    }

    private void writeContentPreview(Object item) throws IOException {

        StorageItem preview = State.getInstance(item).getPreview();

        if (preview != null) {
            String previewContentType = preview.getContentType();
            if (previewContentType != null && previewContentType.startsWith("image/")) {
                page.writeElement("img",
                        "class", "carousel-tile-content-image",
                        "src", page.getPreviewThumbnailUrl(item),
                        "alt", (showSiteLabel ? page.getObjectLabel(State.getInstance(item).as(Site.ObjectModification.class).getOwner()) + ": " : "") +
                                (showTypeLabel ? page.getTypeLabel(item) + ": " : "") +
                                page.getObjectLabel(item));
            }
            //TODO: handle video preview

            page.writeStart("div", "class", "carousel-tile-content-label");
                page.writeObjectLabel(item);
            page.writeEnd();
        } else {
            page.writeStart("div", "class", "carousel-tile-content-text");
                page.writeStart("div");
                    page.writeObjectLabel(item);
                page.writeEnd();
                page.writeStart("div", "class", "text");
                    page.writeTypeLabel(item);
                page.writeEnd();
            page.writeEnd();
        }
    }

    @Override
    protected void writeFieldsHtml() throws IOException {

    }

    @Override
    protected void writeSortsHtml() throws IOException {

    }

    @Override
    protected void writePaginationHtml(PaginatedResult<?> result) throws IOException {

    }

    @Override
    public boolean displayViews() {
        return false;
    }

    @Override
    public boolean displayActions() {
        return false;
    }
}
