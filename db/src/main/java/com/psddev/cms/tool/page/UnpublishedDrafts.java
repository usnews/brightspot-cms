package com.psddev.cms.tool.page;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import javax.servlet.ServletException;

import com.psddev.cms.db.Content;
import com.psddev.cms.db.Draft;
import com.psddev.cms.db.Workflow;
import com.psddev.cms.db.WorkflowState;
import com.psddev.cms.tool.PageServlet;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.db.Query;
import com.psddev.dari.db.State;
import com.psddev.dari.util.PaginatedResult;
import com.psddev.dari.util.RoutingFilter;

@RoutingFilter.Path(application = "cms", value = "/unpublishedDrafts")
@SuppressWarnings("serial")
public class UnpublishedDrafts extends PageServlet {

    private static final int[] LIMITS = { 10, 20, 50 };

    @Override
    protected String getPermissionId() {
        return "area/dashboard";
    }

    @Override
    protected void doService(ToolPageContext page) throws IOException, ServletException {
        Query<Workflow> workflowQuery = Query.from(Workflow.class);
        Map<String, String> stateLabels = new TreeMap<String, String>();

        stateLabels.put("draft", "Draft");

        for (Workflow w : workflowQuery.iterable(0)) {
            for (WorkflowState s : w.getStates()) {
                stateLabels.put("ws." + s.getName(), s.getName());
            }
        }

        String state = page.pageParam(String.class, "state", null);
        Query<?> draftsQuery;

        if ("draft".equals(state)) {
            draftsQuery = Query.
                    from(Object.class).
                    where("_type = ? or cms.content.draft = true", Draft.class);

        } else if (state != null && state.startsWith("ws.")) {
            draftsQuery = Query.
                    from(Object.class).
                    where("cms.workflow.currentState = ?", state.substring(3));

        } else {
            draftsQuery = Query.
                    from(Object.class).
                    where("_type = ? or cms.content.draft = true or cms.workflow.currentState != missing", Draft.class);
        }

        int limit = page.pageParam(int.class, "limit", 20);
        PaginatedResult<?> drafts = draftsQuery.
                and(Content.UPDATE_DATE_FIELD + " != missing").
                and(page.siteItemsPredicate()).
                sortDescending(Content.UPDATE_DATE_FIELD).
                select(page.param(long.class, "offset"), limit);

        page.writeStart("div", "class", "widget widget-unpublishedDrafts");
            page.writeStart("h1", "class", "icon icon-object-draft");
                page.writeHtml("Unpublished Drafts");
            page.writeEnd();

            page.writeStart("form",
                    "method", "get",
                    "action", page.url(null));
                if (stateLabels.size() > 1) {
                    page.writeStart("select",
                            "class", "autoSubmit",
                            "name", "state");
                        page.writeStart("option", "value", "");
                            page.writeHtml("All");
                        page.writeEnd();

                        for (Map.Entry<String, String> entry : stateLabels.entrySet()) {
                            String key = entry.getKey();

                            page.writeStart("option",
                                    "selected", key.equals(state) ? "selected" : null,
                                    "value", key);
                                page.writeHtml(entry.getValue());
                            page.writeEnd();
                        }
                    page.writeEnd();
                }
            page.writeEnd();

            if (drafts.getItems().isEmpty()) {
                String label = stateLabels.get(state);

                page.writeStart("div", "class", "message message-info");
                    page.writeHtml("No ");
                    page.writeHtml(label != null ? label : "Matching");
                    page.writeHtml(" Items.");
                page.writeEnd();

            } else {
                page.writeStart("ul", "class", "pagination");
                    if (drafts.hasPrevious()) {
                        page.writeStart("li", "class", "first");
                            page.writeStart("a", "href", page.url("", "offset", drafts.getFirstOffset()));
                                page.writeHtml("Newest");
                            page.writeEnd();
                        page.writeEnd();

                        page.writeStart("li", "class", "previous");
                            page.writeStart("a", "href", page.url("", "offset", drafts.getPreviousOffset()));
                                page.writeHtml("Newer ");
                                page.writeHtml(drafts.getLimit());
                            page.writeEnd();
                        page.writeEnd();
                    }

                    page.writeStart("li");
                        page.writeStart("form",
                                "class", "autoSubmit",
                                "method", "get",
                                "action", page.url(null));
                            page.writeStart("select", "name", "limit");
                                for (int l : LIMITS) {
                                    page.writeStart("option",
                                            "value", l,
                                            "selected", limit == l ? "selected" : null);
                                        page.writeHtml("Show ");
                                        page.writeHtml(l);
                                    page.writeEnd();
                                }
                            page.writeEnd();
                        page.writeEnd();
                    page.writeEnd();

                    if (drafts.hasNext()) {
                        page.writeStart("li", "class", "next");
                            page.writeStart("a", "href", page.url("", "offset", drafts.getNextOffset()));
                                page.writeHtml("Older ");
                                page.writeHtml(drafts.getLimit());
                            page.writeEnd();
                        page.writeEnd();
                    }
                page.writeEnd();

                page.writeStart("table", "class", "links table-striped pageThumbnails");
                    page.writeStart("tbody");
                        for (Object item : drafts.getItems()) {
                            if (item instanceof Draft) {
                                Draft draft = (Draft) item;
                                item = draft.getObject();

                                if (item == null) {
                                    continue;
                                }

                                State itemState = State.getInstance(item);

                                if (!itemState.isVisible() &&
                                        draft.getObjectChanges().isEmpty()) {
                                    continue;
                                }

                                UUID draftId = draft.getId();

                                page.writeStart("tr", "data-preview-url", "/_preview?_cms.db.previewId=" + draftId);
                                    page.writeStart("td");
                                        page.writeHtml(page.getTypeLabel(item));
                                    page.writeEnd();

                                    page.writeStart("td", "data-preview-anchor", "");
                                        page.writeStart("a",
                                                "target", "_top",
                                                "href", page.url("/content/edit.jsp",
                                                        "id", itemState.getId(),
                                                        "draftId", draftId));
                                            page.writeStart("span", "class", "visibilityLabel");
                                                page.writeHtml("Draft");
                                            page.writeEnd();
                                            page.writeHtml(" ");
                                            page.writeObjectLabel(item);
                                        page.writeEnd();
                                    page.writeEnd();

                                    page.writeStart("td");
                                        page.writeObjectLabel(draft.as(Content.ObjectModification.class).getUpdateUser());
                                    page.writeEnd();
                                page.writeEnd();

                            } else {
                                State itemState = State.getInstance(item);
                                UUID itemId = itemState.getId();

                                page.writeStart("tr", "data-preview-url", "/_preview?_cms.db.previewId=" + itemId);
                                    page.writeStart("td");
                                        page.writeHtml(page.getTypeLabel(item));
                                    page.writeEnd();

                                    page.writeStart("td", "data-preview-anchor", "");
                                        page.writeStart("a", "href", page.url("/content/edit.jsp", "id", itemId), "target", "_top");
                                            page.writeObjectLabel(item);
                                        page.writeEnd();
                                    page.writeEnd();

                                    page.writeStart("td");
                                        page.writeObjectLabel(itemState.as(Content.ObjectModification.class).getUpdateUser());
                                    page.writeEnd();
                                page.writeEnd();
                            }
                        }
                    page.writeEnd();
                page.writeEnd();
            }
        page.writeEnd();
    }
}
