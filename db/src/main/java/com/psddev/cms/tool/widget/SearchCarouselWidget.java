package com.psddev.cms.tool.widget;

import com.psddev.cms.db.Site;
import com.psddev.cms.tool.CmsTool;
import com.psddev.cms.tool.Dashboard;
import com.psddev.cms.tool.DashboardWidget;
import com.psddev.cms.tool.Search;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.db.State;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.PaginatedResult;
import com.psddev.dari.util.StorageItem;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.UUID;

public class SearchCarouselWidget extends DashboardWidget {

    private transient ToolPageContext page;
    private transient PaginatedResult<?> result;

    @Override
    public void writeHtml(ToolPageContext page, Dashboard dashboard) throws IOException, ServletException {
        this.page = page;
        Search search = new Search(page);
        result = search.toQuery(page.getSite()).select(search.getOffset(), search.getLimit());

        page.writeStart("div", "class", "widget-searchCarousel",
                "data-next-page", result.hasNext() ? page.url("", Search.OFFSET_PARAMETER, result.getNextOffset()) : "",
                "data-prev-page", result.hasPrevious() ? page.url("", Search.OFFSET_PARAMETER, result.getPreviousOffset()) : "",
                "data-start-index", search.getOffset());
            writeItemsHtml();
        page.writeEnd();
    }

    protected void writeItemsHtml() throws IOException {
        UUID currentContentId = page.param(UUID.class, "id");

        for (Object item : result.getItems()) {
            State itemState = State.getInstance(item);
            UUID itemId = itemState.getId();

            page.writeStart("a",
                    "href", page.toolUrl(CmsTool.class, "/content/edit.jsp",
                            "id", State.getInstance(item).getId(),
                            "search", page.url("", Search.NAME_PARAMETER, null)),
                    "data-objectId", State.getInstance(item).getId(),
                    "target", "_top");

                page.writeStart("div", "class", "carousel-tile-content " + (ObjectUtils.equals(currentContentId, itemId) ? "carousel-tile-content-active" : ""));
                    writeContentPreview(item);
                page.writeEnd();

            page.writeEnd();
        }
    }


    private void writeContentPreview(Object item) throws IOException {

        StorageItem preview = State.getInstance(item).getPreview();

        if (preview != null) {
            String previewContentType = preview.getContentType();
            if (previewContentType != null && previewContentType.startsWith("image/")) {
                page.writeElement("img",
                        "class", "carousel-tile-content-image",
                        "src", page.getPreviewThumbnailUrl(item),
                        "alt", (page.getObjectLabel(State.getInstance(item).as(Site.ObjectModification.class).getOwner()) + ": ") +
                                (page.getTypeLabel(item) + ": ") + page.getObjectLabel(item));
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
}
