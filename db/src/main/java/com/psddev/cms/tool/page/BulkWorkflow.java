package com.psddev.cms.tool.page;

import com.psddev.cms.db.Workflow;
import com.psddev.cms.db.WorkflowState;
import com.psddev.cms.db.WorkflowTransition;
import com.psddev.cms.tool.CmsTool;
import com.psddev.cms.tool.PageServlet;
import com.psddev.cms.tool.Search;
import com.psddev.cms.tool.SearchResultSelection;
import com.psddev.cms.tool.SearchResultSelectionItem;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.db.ObjectType;
import com.psddev.dari.db.Query;
import com.psddev.dari.db.State;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.RoutingFilter;
import com.psddev.dari.util.TypeReference;
import com.psddev.dari.util.UrlBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RoutingFilter.Path(application = "media", value = BulkWorkflow.PATH)
public class BulkWorkflow extends PageServlet {

    public static final String PATH = "/workflowBulk";

    public static final String WIDGET_STATE_PARAMETER = "bulkWorkflowState";
    public static final String SELECTION_ID_PARAMETER = "selectionId";
    public static final String TYPE_ID_PARAMETER = "typeId";
    public static final String WORKFLOW_STATE_PARAMETER = "workflowState";

    public static final String BUTTON_STATE = "button";
    public static final String POPUP_STATE = "popup";

    private static Logger LOGGER = LoggerFactory.getLogger(BulkWorkflow.class);

    @Override
    protected String getPermissionId() {
        return null;
    }

    public void doService(ToolPageContext page) throws IOException, ServletException {

        Search search = searchFromJson(page);

        if (search == null) {
            search = new Search(page);
        }

        doService(page, search);
    }

    public void doService(ToolPageContext page, SearchResultSelection selection) throws IOException, ServletException {

        if (selection == null) {
            return;
        }

        Map<ObjectType, Map<String, Integer>> workflowStateCounts = new LinkedHashMap<>();

        Set<UUID> itemIds = new HashSet<>();

        for (SearchResultSelectionItem item : Query.
                from(SearchResultSelectionItem.class).
                where("selectionId = ?", selection.getId()).
                selectAll()) {

            itemIds.add(item.getItemId());
        }

        Set<ObjectType> itemTypes = new HashSet<>();

        for (Object item : Query.
                fromAll().
                where("_id = ?", itemIds).
                selectAll()) {

            State itemState = State.getInstance(item);
            ObjectType itemType = itemState.getType();

            if (workflowStateCounts.get(itemType) == null) {
                workflowStateCounts.put(itemType, new LinkedHashMap<>());
            }

            String itemWorkflowCurrentState = itemState.as(Workflow.Data.class).getCurrentState();

            if (!ObjectUtils.isBlank(itemWorkflowCurrentState)) {
                int count = workflowStateCounts.get(itemType).get(itemWorkflowCurrentState);
                workflowStateCounts.get(itemType).put(itemWorkflowCurrentState, count + 1);
            }

            itemTypes.add(itemType);
        }

        if (itemTypes.size() == 0) {
            return;
        }

        boolean hasAnyTransitions = false;

        Map<Workflow, Map<ObjectType, Map<WorkflowState, Map<String, WorkflowTransition>>>> availableTransitionsMap = new LinkedHashMap<>();

        for (Workflow workflow : Query.from(Workflow.class).where("contentTypes = ?", itemTypes).selectAll()) {

            for (ObjectType workflowType : workflow.getContentTypes()) {

                Map<ObjectType, Map<WorkflowState, Map<String, WorkflowTransition>>> stateTransitionsByType = availableTransitionsMap.get(workflow);
                if (stateTransitionsByType == null) {
                    stateTransitionsByType = new LinkedHashMap<>();
                    availableTransitionsMap.put(workflow, stateTransitionsByType);
                }

                for (WorkflowState workflowState : workflow.getStates()) {

                    Map<WorkflowState, Map<String, WorkflowTransition>> stateTransitions = stateTransitionsByType.get(workflowType);
                    if (stateTransitions == null) {
                        stateTransitions = new LinkedHashMap<>();
                        stateTransitionsByType.put(workflowType, stateTransitions);
                    }

                    if (ObjectUtils.to(int.class, workflowStateCounts.get(workflowType).get(workflowState.getName())) > 0) {

                        Map<String, WorkflowTransition> transitionsFrom = workflow.getTransitionsFrom(workflowState.getName());

                        if (transitionsFrom.size() > 0) {
                            hasAnyTransitions = true;
                        }

                        stateTransitions.put(workflowState, transitionsFrom);
                    }
                }
            }
        }

        if (!hasAnyTransitions) {
            return;
        }

        page.writeStart("div", "class", "widget media-widget");
        page.writeStart("h1", "class", "icon icon-object-workflow");
        page.writeHtml("Workflow Options");
        page.writeEnd();

        for (Workflow workflow : availableTransitionsMap.keySet()) {

            page.writeStart("div", "class", "media-workflowTransitionList");

            for (ObjectType workflowType : availableTransitionsMap.get(workflow).keySet()) {

                Map<WorkflowState, Map<String, WorkflowTransition>> stateTransitions = availableTransitionsMap.get(workflow).get(workflowType);

                for (WorkflowState workflowState : stateTransitions.keySet()) {

                    page.writeStart("h3", "class", "media-workflowTransitionTitle");
                    page.writeHtml(ObjectUtils.to(int.class, workflowStateCounts.get(workflowType).get(workflowState.getName())) + " " + workflowType.getDisplayName() + " ");
                    page.writeStart("span", "class", "visibilityLabel");
                    page.writeHtml(workflowState.getDisplayName());
                    page.writeEnd(); // end .visibilityLabel
                    page.writeEnd();

                    for (String transitionName : stateTransitions.get(workflowState).keySet()) {

                        page.writeStart("div", "class", "media-workflowTransition");
                        page.writeStart("form",
                                "class", "media-workflowTransitionForm",
                                "method", "get",
                                "target", "workflowBulk",
                                "action", new UrlBuilder(page.getRequest())
                                        .absolutePath(page.toolUrl(CmsTool.class, PATH))
                                        .parameter(SELECTION_ID_PARAMETER, selection.getId())
                                        .parameter(TYPE_ID_PARAMETER, workflowType.getId())
                                        .parameter(WORKFLOW_STATE_PARAMETER, workflowState.getName())
                                        .parameter(WIDGET_STATE_PARAMETER, POPUP_STATE));
                        page.writeStart("button",
                                "name", "action-workflow",
                                "value", transitionName);
                        page.writeHtml(stateTransitions.get(workflowState).get(transitionName).getDisplayName());
                        page.writeEnd(); // end button.action-workflow
                        page.writeEnd(); // end form.media-workflowTransitionForm
                        page.writeEnd(); // end div.media-workflowTransition
                    }
                }

            }

            page.writeEnd(); // end .media-workflowTransitionList
        }

        page.writeEnd(); // end .media-widget
    }

    public void doService(ToolPageContext page, Search search) throws IOException, ServletException {

        if (search == null) {
            return;
        }

        Query<?> query = search.toQuery(page.getSite());

        ObjectType selectedType = search.getSelectedType();

        LOGGER.debug("selectedType: " + (selectedType == null ? null : selectedType.getDisplayName()));

        if (selectedType == null) {
            return;
        }

        Workflow workflow = Query.from(Workflow.class).where("contentTypes = ?", selectedType).first();

        if (workflow == null) {
            return;
        }

        // check that a single visibility (workflow) has been refined
        if (search.getVisibilities() != null && search.getVisibilities().size() != 1) {
            return;
        }

        String currentState = search.getVisibilities().iterator().next();

        if (currentState.startsWith("w.")) {

            currentState = currentState.substring(2);
        }

        LOGGER.debug("currentState: " + currentState);

        if (ObjectUtils.isBlank(currentState)) {
            return;
        }

        Map<String, String> transitionNames = new LinkedHashMap<>();

        for (Map.Entry<String, WorkflowTransition> entry : workflow.getTransitionsFrom(currentState).entrySet()) {
            String transitionName = entry.getKey();

            if (page.hasPermission("type/" + selectedType.getId() + "/" + transitionName)) {
                transitionNames.put(transitionName, entry.getValue().getDisplayName());
            }
        }

        if (transitionNames.isEmpty()) {
            return;
        }

        String widgetState = page.paramOrDefault(String.class, WIDGET_STATE_PARAMETER, BUTTON_STATE);

        if (BUTTON_STATE.equals(widgetState)) {

            for (Map.Entry<String, String> entry : transitionNames.entrySet()) {

                LOGGER.debug("writing workflow button for: " + entry.getValue());

                page.writeStart("form",
                        "class", "media-searchResultActionForm",
                        "method", "get",
                        "target", "workflowBulk",
                        "action", new UrlBuilder(page.getRequest())
                                .absolutePath(page.toolUrl(CmsTool.class, PATH))
                                .currentParameters()
                                .parameter(WIDGET_STATE_PARAMETER, POPUP_STATE));
                page.writeStart("button",
                        "name", "action-workflow",
                        "value", entry.getKey());
                page.writeHtml("Status: " + entry.getValue());
                page.writeEnd();
                page.writeEnd();
            }

        } else if (POPUP_STATE.equals(widgetState)) {

            if (page.isFormPost()) {
                for (Object item : query.selectAll()) {

                    page.tryWorkflow(item, false);
                    page.writeStart("script").writeHtml("window.location.href = window.location.href;").writeEnd();
                }
            } else {

                page.writeStart("p");
                page.writeHtml("Click \"Confirm\" below to perform the following workflow update.");
                page.writeEnd();

                page.writeStart("table", "class", "table-striped");

                page.writeStart("tr");
                page.writeStart("td").writeHtml("Type").writeEnd();
                page.writeStart("td").writeHtml(search.getSelectedType().getDisplayName()).writeEnd();
                page.writeEnd(); // end row

                page.writeStart("tr");
                page.writeStart("td").writeHtml("Count").writeEnd();
                page.writeStart("td").writeHtml(query.count()).writeEnd();
                page.writeEnd(); // end row

                page.writeStart("tr");
                page.writeStart("td").writeHtml("Current State").writeEnd();
                page.writeStart("td").writeHtml(currentState).writeEnd();
                page.writeEnd(); // end row

                page.writeStart("tr");
                page.writeStart("td").writeHtml("New State").writeEnd();
                page.writeStart("td").writeHtml(transitionNames.get(page.param(String.class, "action-workflow"))).writeEnd();
                page.writeEnd(); // end row

                page.writeEnd(); // end table

                page.writeStart("form",
                        "method", "post",
                        "action", new UrlBuilder(page.getRequest())
                                .absolutePath(page.toolUrl(CmsTool.class, PATH))
                                .currentParameters()
                                .parameter(WIDGET_STATE_PARAMETER, POPUP_STATE));

                page.writeStart("button", "name", "action-workflow", "value", page.param(String.class, "action-workflow"));
                page.writeHtml("Confirm");
                page.writeEnd();
                page.writeEnd();
            }
        }
    }

    public static Search searchFromJson(ToolPageContext page) {

        Search search = null;

        String searchParam = page.param(String.class, "search");

        if (searchParam != null) {

            try {

                Map<String, Object> searchJson = ObjectUtils.to(new TypeReference<Map<String, Object>>() {
                }, ObjectUtils.fromJson(searchParam));
                search = new Search();
                search.getState().setValues(searchJson);

            } catch (Exception ignore) {

                // Ignore.  Search will be constructed below using ToolPageContext
            }
        }

        return search;
    }
}

