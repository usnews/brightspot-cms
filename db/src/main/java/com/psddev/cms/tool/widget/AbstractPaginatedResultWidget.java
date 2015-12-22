package com.psddev.cms.tool.widget;

import java.io.IOException;

import javax.servlet.ServletException;

import com.google.common.collect.ImmutableMap;
import com.psddev.cms.db.Content;
import com.psddev.cms.tool.Dashboard;
import com.psddev.cms.tool.DashboardWidget;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.db.Query;
import com.psddev.dari.db.Record;
import com.psddev.dari.util.PaginatedResult;

/**
 * Provides an extensible base implementation of a {@link DashboardWidget} displaying a {@link PaginatedResult}.
 *
 * Minimal implementation requires overriding {@link #getQuery(ToolPageContext)} and {@link #getTitle(ToolPageContext)}. For further
 * customization, optionally override {@link #writeFilters(ToolPageContext)}, {@link #writeResults(ToolPageContext, PaginatedResult)}, or
 * {@link #writeResultsItem(ToolPageContext, Record)}.
 *
 * @param <T> type of {@link PaginatedResult} to be rendered.
 */
public abstract class AbstractPaginatedResultWidget<T extends Record> extends DashboardWidget {

    private static final int[] LIMITS = {10, 20, 50};
    public static final String OFFSET_PARAMETER = "offset";
    public static final String LIMIT_PARAMETER = "limit";

    /**
     * It is recommended that implementations of this method produce
     * a localized title.
     *
     * @param page Used to write {@link DashboardWidget} HTML.
     * @return the title
     * @throws IOException
     */
    abstract String getTitle(ToolPageContext page) throws IOException;

    /**
     * Implementations should produce a Query for the type
     * to be displayed.
     *
     * @param page Used for access to request parameters.
     * @return the {@link Query} to produce the {@link PaginatedResult}.
     */
    abstract Query<T> getQuery(ToolPageContext page);

    /**
     * Optionally override for more control over the creation of
     * the {@link PaginatedResult}.
     *
     * @param page Used for access to request parameters.
     * @return the {@link PaginatedResult} to be displayed by the widget.
     */
    public PaginatedResult<T> getPaginatedResult(ToolPageContext page) {
        return getQuery(page).select(page.param(long.class, OFFSET_PARAMETER), page.paramOrDefault(int.class, LIMIT_PARAMETER, LIMITS[0]));
    }

    /**
     * Optionally override to provide filters for the PaginatedResult displayed
     * by the widget.
     *
     * @param page Used to write {@link DashboardWidget} HTML.
     * @throws IOException
     */
    public void writeFilters(ToolPageContext page) throws IOException {
        // Default implementation has no filters
    }

    /**
     * Optionally override to customize the container of the results.
     *
     * @param page Used to write {@link DashboardWidget} HTML.
     * @param result A {@link PaginatedResult} from {@link #getQuery(ToolPageContext)}
     * @throws IOException
     */
    public void writeResults(ToolPageContext page, PaginatedResult<T> result) throws IOException {

        page.writeStart("table", "class", "links table-striped pageThumbnails").writeStart("tbody");

        for (T record : result.getItems()) {
            writeResultsItem(page, record);
        }

        page.writeEnd().writeEnd();

    }

    public void writeEmpty(ToolPageContext page) throws IOException {
        page.writeStart("div", "class", "message message-info");
            page.writeStart("p");
                page.writeHtml(page.localize(AbstractPaginatedResultWidget.class, "message.noResults"));
            page.writeEnd();
        page.writeEnd();
    }

    /**
     * Optionally override to customize appearance of a row in the table.
     *
     * @param page Used to write {@link DashboardWidget} HTML.
     * @param record A {@link Record} from the items produced by the {@link PaginatedResult} used by {@link #writeResults(ToolPageContext, PaginatedResult)}
     * @throws IOException
     */
    public void writeResultsItem(ToolPageContext page, T record) throws IOException {

        page.writeStart("tr");
            page.writeStart("td");
                page.writeTypeLabel(record);
            page.writeEnd();

            page.writeStart("td");

                if (record instanceof Content) {
                    page.writeStart("a",
                            "target", "_top",
                            "href", page.objectUrl("/content/edit.jsp", record));
                }

                    page.writeObjectLabel(record);

                if (record instanceof Content) {
                    page.writeEnd();
                }

            page.writeEnd();
        page.writeEnd();
    }

    @Override
    public void writeHtml(ToolPageContext page, Dashboard dashboard) throws IOException, ServletException {

        page.writeStart("div", "class", "widget");

            page.writeStart("h1");
                page.writeHtml(getTitle(page));
            page.writeEnd();

            page.writeStart("div", "class", "widget-filters");
                page.writeStart("form",
                        "method", "get",
                        "action", page.url(null));
                    writeFilters(page);
                page.writeEnd();
            page.writeEnd();

            PaginatedResult<T> result = getPaginatedResult(page);

            writePaginationHtml(page, result, 0);

            if (result.hasPages()) {
                writeResults(page, result);
            } else {
                writeEmpty(page);
            }

        page.writeEnd();
    }

    private void writePaginationHtml(ToolPageContext page, PaginatedResult<T> result, int limit) throws IOException {

        // Pagination
        page.writeStart("ul", "class", "pagination");

            if (result.hasPrevious()) {
                page.writeStart("li", "class", "first");
                    page.writeStart("a", "href", page.url("", OFFSET_PARAMETER, result.getFirstOffset()));
                        page.writeHtml(page.localize(WorkStreamsWidget.class, "pagination.newest"));
                    page.writeEnd();
                page.writeEnd();

                page.writeStart("li", "class", "previous");
                    page.writeStart("a", "href", page.url("", OFFSET_PARAMETER, result.getPreviousOffset()));
                        page.writeHtml(page.localize(ImmutableMap.of("count", limit), "pagination.newerCount"));
                    page.writeEnd();
                page.writeEnd();
            }

            if (result.getOffset() > 0 || result.hasNext() || result.getItems().size() > LIMITS[0]) {
                page.writeStart("li");
                    page.writeStart("form",
                            "data-bsp-autosubmit", "",
                            "method", "get",
                            "action", page.url(null));
                        page.writeStart("select", "name", LIMIT_PARAMETER);
                        for (int l : LIMITS) {
                            page.writeStart("option",
                                    "value", l,
                                    "selected", limit == l ? "selected" : null);
                                page.writeHtml(page.localize(WorkStreamsWidget.class, ImmutableMap.of("count", l), "option.showCount"));
                            page.writeEnd();
                        }
                        page.writeEnd();
                    page.writeEnd();
                page.writeEnd();
            }

            if (result.hasNext()) {
                page.writeStart("li", "class", "next");
                    page.writeStart("a", "href", page.url("", "offset", result.getNextOffset()));
                        page.writeHtml(page.localize(WorkStreamsWidget.class, ImmutableMap.of("count", limit), "pagination.olderCount"));
                    page.writeEnd();
                page.writeEnd();
            }

        page.writeEnd();

    }
}
