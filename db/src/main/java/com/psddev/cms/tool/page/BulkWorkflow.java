package com.psddev.cms.tool.page;

import com.psddev.cms.db.Workflow;
import com.psddev.cms.db.WorkflowState;
import com.psddev.cms.db.WorkflowTransition;
import com.psddev.cms.tool.CmsTool;
import com.psddev.cms.tool.PageServlet;
import com.psddev.cms.tool.Search;
import com.psddev.cms.tool.SearchResultSelection;
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

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RoutingFilter.Path(application = "cms", value = BulkWorkflow.PATH)
public class BulkWorkflow extends PageServlet {

    public static final String PATH = "/workflowBulk";

    public static final String TARGET = "workflowBulk";

    private static final Logger LOGGER = LoggerFactory.getLogger(BulkWorkflow.class);

    @Override
    protected String getPermissionId() {
        return null;
    }

    // default public constructor
    public BulkWorkflow() { }

    public void doService(ToolPageContext page) throws IOException, ServletException {

        doService(new Context(page));
    }

    public void doService(ToolPageContext page, Search search, SearchResultSelection selection) throws IOException, ServletException {

        LOGGER.debug("doService, search: " + ObjectUtils.toJson(search));
        LOGGER.debug("doService, selection: " + ObjectUtils.toJson(selection));

        Context context = new Context(page, selection, search);

        LOGGER.debug("Finished building Context");
        doService(context);
    }

    private void doService(Context page) throws IOException, ServletException {

        LOGGER.debug("doService, Context");

        if (!page.hasAnyTransitions()) {
            return;
        }

        LOGGER.debug("page.getWidgetState(): " + page.getWidgetState());

        switch (page.getWidgetState()) {

        case BUTTON:

            page.writeStart("div", "class", "widget media-widget");
            page.writeStart("h1", "class", "icon icon-object-workflow");
            page.writeHtml("Workflow Options");
            page.writeEnd();

            page.writeButtonHtml();

            page.writeEnd(); // end .media-widget

            break;

        case POPUP:

            if (page.isFormPost()) {

                Search search = page.getSearch();

                if (search != null) {
                    if (search.getVisibilities() != null) {
                        search.getVisibilities().clear();
                    } else {
                        search.setVisibilities(new ArrayList<>());
                    }

                    search.getVisibilities().add("w." + page.param(String.class, Context.WORKFLOW_STATE_PARAMETER));
                }

                for (Object item : page.itemsQuery().selectAll()) {

                    page.tryWorkflowOnly(item);

                    // TODO: present Error dialog when workflow transition fails.
                    for (Throwable throwable : page.getErrors()) {
                        LOGGER.warn("Bulk Workflow Error: ", throwable);
                    }

                }
                page.writeStart("script").writeHtml("window.location.href = window.location.href;").writeEnd();
            } else {

                ObjectType transitionSourceType = ObjectType.getInstance(page.param(UUID.class, Context.TYPE_ID_PARAMETER));

                String workflowStateName = page.param(String.class, Context.WORKFLOW_STATE_PARAMETER);
                WorkflowState workflowState = null;

                Workflow workflow = Query.from(Workflow.class).where("_id = ?", page.param(String.class, Context.WORKFLOW_ID_PARAMETER)).first();

                for (WorkflowState workflowStateValue : workflow.getStates()) {
                    if (ObjectUtils.equals(workflowStateValue.getName(), workflowStateName)) {
                        workflowState = workflowStateValue;
                        break;
                    }
                }

                WorkflowTransition workflowTransition = page.getWorkflowTransition(workflow, transitionSourceType, workflowState, page.param(String.class, "action-workflow"));

                page.writeStart("p");
                page.writeHtml("Click \"Confirm\" below to perform the following workflow update.");
                page.writeEnd();

                page.writeStart("table", "class", "table-striped");

                page.writeStart("tr");
                page.writeStart("td").writeHtml("Type").writeEnd();
                page.writeStart("td").writeHtml(transitionSourceType.getDisplayName()).writeEnd();
                page.writeEnd(); // end row

                page.writeStart("tr");
                page.writeStart("td").writeHtml("Count").writeEnd();
                page.writeStart("td").writeHtml(page.getWorkflowStateCount(transitionSourceType, workflowStateName)).writeEnd();
                page.writeEnd(); // end row

                page.writeStart("tr");
                page.writeStart("td").writeHtml("Current State").writeEnd();
                page.writeStart("td").writeHtml(workflowTransition.getSource().getDisplayName()).writeEnd();
                page.writeEnd(); // end row

                page.writeStart("tr");
                page.writeStart("td").writeHtml("New State").writeEnd();
                page.writeStart("td").writeHtml(workflowTransition.getTarget().getDisplayName()).writeEnd();
                page.writeEnd(); // end row

                page.writeEnd(); // end table

                page.writeStart("form",
                        "method", "post",
                        "action", page.getButtonActionUrl(workflow, transitionSourceType, workflowState, WidgetState.POPUP));

                page.writeStart("button", "name", "action-workflow", "value", workflowTransition.getName());
                page.writeHtml("Confirm");
                page.writeEnd();
                page.writeEnd();
            }

            break;

        case DEFAULT:

        default:

            page.writeStart("div", "class", "searchResult-action-simple");
            page.writeStart("a",
                    "class", "button",
                    "target", TARGET,
                    "href", page.getButtonActionUrl(null, null, null, WidgetState.BUTTON));
            page.writeHtml("Bulk Workflow ");
            page.writeHtml(page.getSelection() != null ? "Selected" : "");
            page.writeEnd();
            page.writeEnd();

            break;
        }
    }

    public static enum WidgetState {
        BUTTON,
        POPUP,
        DEFAULT
    }

    private static class Context extends ToolPageContext {

        public static final String WIDGET_STATE_PARAMETER = "bulkWorkflowState";
        public static final String SELECTION_ID_PARAMETER = "selectionId";
        public static final String TYPE_ID_PARAMETER = "sourceTypeId";
        public static final String WORKFLOW_ID_PARAMETER = "workflowId";
        public static final String WORKFLOW_STATE_PARAMETER = "sourceWorkflowState";
        public static final String SEARCH_PARAMETER = "search";

        private Search search;
        private SearchResultSelection selection;
        private WidgetState widgetState;

        private Map<Workflow, Map<ObjectType, Map<WorkflowState, Map<String, WorkflowTransition>>>> availableTransitionsMap = new LinkedHashMap<>();
        private Map<ObjectType, Map<String, Integer>> workflowStateCounts = new LinkedHashMap<>();
        private boolean hasAnyTransitions = false;

        public Context(PageContext pageContext) {
            this(pageContext.getServletContext(), (HttpServletRequest) pageContext.getRequest(), (HttpServletResponse) pageContext.getResponse(), null, null);
        }

        public Context(ToolPageContext page) {

            this(page.getServletContext(), page.getRequest(), page.getResponse(), null, null);
        }

        public Context(ToolPageContext page, SearchResultSelection selection, Search search) {

            this(page.getServletContext(), page.getRequest(), page.getResponse(), selection, search);
        }

        public Context(ServletContext servletContext, HttpServletRequest request, HttpServletResponse response, SearchResultSelection selection, Search search) {

            super(servletContext, request, response);

            String selectionId = param(String.class, SELECTION_ID_PARAMETER);

            this.widgetState = ObjectUtils.firstNonNull(param(WidgetState.class, WIDGET_STATE_PARAMETER), WidgetState.DEFAULT);

            if (selection != null) {
                LOGGER.debug("Received SearchResultSelection constructor object: " + ObjectUtils.toJson(selection));
                setSelection(selection);
            } else if (!ObjectUtils.isBlank(selectionId)) {
                LOGGER.debug("Found " + SELECTION_ID_PARAMETER + " query parameter with value: " + selectionId);
                SearchResultSelection queriedSelection = (SearchResultSelection) Query.fromAll().where("_id = ?", selectionId).first();

                LOGGER.debug("Queried SearchResultSelection by id: " + ObjectUtils.toJson(queriedSelection));

                if (queriedSelection == null) {
                    throw new IllegalArgumentException("No SearchResultSelection exists for id " + selectionId);
                }

                setSelection(queriedSelection);
            } else if (search != null) {

                LOGGER.debug("Received Search constructor object: " + ObjectUtils.toJson(search));
                setSearch(search);
            } else {

                Search searchFromJson = searchFromJson();

                LOGGER.debug("Tried to obtain Search object from JSON query parameter: " + ObjectUtils.toJson(searchFromJson));

                if (searchFromJson == null) {

                    LOGGER.debug("Could not objtain Search object from JSON query parameter");
                    searchFromJson = new Search();

                    LOGGER.debug("Tried to obtain Search object from CMS query parameters: " + ObjectUtils.toJson(searchFromJson));
                }

                setSearch(searchFromJson);
            }
        }

        public Search getSearch() {
            return search;
        }

        public void setSearch(Search search) {
            this.search = search;
            buildTransitionMap(search);
        }

        public SearchResultSelection getSelection() {
            return selection;
        }

        public void setSelection(SearchResultSelection selection) {
            this.selection = selection;
            buildTransitionMap(selection);
        }

        public WidgetState getWidgetState() {
            return widgetState;
        }

        public void setWidgetState(WidgetState widgetState) {
            this.widgetState = widgetState;
        }

        // Utility methods for navigating the availableTransitionsMap

        public Set<Workflow> workflows() {
            return availableTransitionsMap.keySet();
        }

        private Map<ObjectType, Map<WorkflowState, Map<String, WorkflowTransition>>> getTypeMap(Workflow workflow) {

            Map<ObjectType, Map<WorkflowState, Map<String, WorkflowTransition>>> typeMap = availableTransitionsMap.get(workflow);
            if (typeMap == null) {
                return new HashMap<>();
            }
            return typeMap;
        }

        public Set<ObjectType> workflowTypes(Workflow workflow) {

            return getTypeMap(workflow).keySet();
        }

        private Map<WorkflowState, Map<String, WorkflowTransition>> getStateMap(Workflow workflow, ObjectType workflowType) {

            Map<WorkflowState, Map<String, WorkflowTransition>> stateMap = getTypeMap(workflow).get(workflowType);

            if (stateMap == null) {
                return new HashMap<>();
            }

            return stateMap;
        }

        public Set<WorkflowState> workflowStates(Workflow workflow, ObjectType workflowType) {

            return getStateMap(workflow, workflowType).keySet();
        }

        private Map<String, WorkflowTransition> getTransitionMap(Workflow workflow, ObjectType workflowType, WorkflowState workflowState) {

            Map<String, WorkflowTransition> transitionMap = getStateMap(workflow, workflowType).get(workflowState);

            if (transitionMap == null) {
                return new HashMap<>();
            }

            return transitionMap;
        }

        public Set<String> transitionNames(Workflow workflow, ObjectType workflowType, WorkflowState workflowState) {

            return getTransitionMap(workflow, workflowType, workflowState).keySet();
        }

        public WorkflowTransition getWorkflowTransition(Workflow workflow, ObjectType workflowType, WorkflowState workflowState, String transitionName) {

            return getTransitionMap(workflow, workflowType, workflowState).get(transitionName);
        }

        public int getWorkflowStateCount(ObjectType workflowType, String workflowState) {

            Map<String, Integer> countMap = workflowStateCounts.get(workflowType);

            if (countMap == null) {
                return 0;
            }

            return ObjectUtils.to(int.class, countMap.get(workflowState));
        }

        public Search searchFromJson() {

            Search search = null;

            String searchParam = param(String.class, SEARCH_PARAMETER);

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

        public boolean hasAnyTransitions() {
            LOGGER.debug("hasAnyTransitions: " + hasAnyTransitions);
            return hasAnyTransitions;
        }

        public Query itemsQuery() {

            if (getSearch() != null) {

                return getSearch().toQuery(getSite());
            } else if (getSelection() != null) {
                return getSelection().createItemsQuery();
            }

            throw new IllegalStateException("No Search or SearchResultsSelection populated.  Cannot create items Query.");
        }

        private void writeTransitionButton(Workflow workflow, ObjectType workflowType, WorkflowState workflowState, String transitionName) throws IOException {

            writeStart("div", "class", "media-workflowTransition");
            writeStart("form",
                    "class", "media-workflowTransitionForm",
                    "method", "get",
                    "target", "workflowBulk",
                    "action", getButtonActionUrl(workflow, workflowType, workflowState, WidgetState.POPUP));
            writeStart("button",
                    "name", "action-workflow",
                    "value", transitionName);
            writeHtml(getWorkflowTransition(workflow, workflowType, workflowState, transitionName).getDisplayName());
            writeEnd(); // end button.action-workflow
            writeEnd(); // end form.media-workflowTransitionForm
            writeEnd(); // end div.media-workflowTransition
        }

        public String getButtonActionUrl(Workflow workflow, ObjectType workflowType, WorkflowState workflowState, WidgetState widgetState) {

            UrlBuilder urlBuilder = new UrlBuilder(getRequest())
                    .absolutePath(toolUrl(CmsTool.class, PATH));

            if (getSearch() != null) {
                // Search uses current page parameters
                urlBuilder.currentParameters();

            } else if (getSelection() != null) {
                // SearchResultSelection uses an ID parameter
                urlBuilder.parameter(Context.SELECTION_ID_PARAMETER, getSelection().getId());
            }

            if (workflow != null) {
                urlBuilder.parameter(Context.WORKFLOW_ID_PARAMETER, workflow.getId());
            }

            if (workflowType != null) {
                urlBuilder.parameter(Context.TYPE_ID_PARAMETER, workflowType.getId());
            }

            if (workflowState != null) {
                urlBuilder.parameter(Context.WORKFLOW_STATE_PARAMETER, workflowState.getName());
            }

            if (widgetState != null) {
                urlBuilder.parameter(Context.WIDGET_STATE_PARAMETER, widgetState);
            }

            return urlBuilder.toString();
        }

        public void writeButtonHtml() throws IOException {

            for (Workflow workflow : workflows()) {

                writeStart("div", "class", "media-workflowTransitionList");

                for (ObjectType workflowType : workflowTypes(workflow)) {

                    for (WorkflowState workflowState : workflowStates(workflow, workflowType)) {

                        writeStart("h3", "class", "media-workflowTransitionTitle");
                        writeHtml(getWorkflowStateCount(workflowType, workflowState.getName()) + " " + workflowType.getDisplayName() + " ");
                        writeStart("span", "class", "visibilityLabel");
                        writeHtml(workflowState.getDisplayName());
                        writeEnd(); // end .visibilityLabel
                        writeEnd();

                        for (String transitionName : transitionNames(workflow, workflowType, workflowState)) {

                            writeTransitionButton(workflow, workflowType, workflowState, transitionName);
                        }
                    }

                }

                writeEnd(); // end .media-workflowTransitionList
            }
        }

        private void buildTransitionMap(SearchResultSelection selection) {

            if (selection == null) {

                return;
            }

            Set<ObjectType> itemTypes = new HashSet<>();

            for (Object item : selection.createItemsQuery().selectAll()) {

                State itemState = State.getInstance(item);

                ObjectType itemType = itemState.getType();

                if (itemType == null) {
                    continue;
                }

                if (workflowStateCounts.get(itemType) == null) {
                    workflowStateCounts.put(itemType, new LinkedHashMap<>());
                }

                String itemWorkflowCurrentState = itemState.as(Workflow.Data.class).getCurrentState();

                if (!ObjectUtils.isBlank(itemWorkflowCurrentState)) {
                    int count = ObjectUtils.to(int.class, workflowStateCounts.get(itemType).get(itemWorkflowCurrentState));
                    workflowStateCounts.get(itemType).put(itemWorkflowCurrentState, count + 1);
                }

                itemTypes.add(itemType);
            }

            for (ObjectType key : workflowStateCounts.keySet()) {
                LOGGER.debug("workflowStateCounts for " + key.getDisplayName() + ": " + ObjectUtils.toJson(workflowStateCounts.get(key)));
            }

            if (itemTypes.size() == 0) {

                return;
            }

            for (Workflow workflow : Query.from(Workflow.class).where("contentTypes = ?", itemTypes).selectAll()) {

                LOGGER.debug("Inspecting Workflow: " + workflow.getName());

                for (ObjectType workflowType : workflow.getContentTypes()) {

                    LOGGER.debug("Inspecting Workflow ObjectType: " + workflowType.getDisplayName());

                    Map<ObjectType, Map<WorkflowState, Map<String, WorkflowTransition>>> stateTransitionsByType = availableTransitionsMap.get(workflow);
                    if (stateTransitionsByType == null) {
                        stateTransitionsByType = new LinkedHashMap<>();
                        availableTransitionsMap.put(workflow, stateTransitionsByType);
                    }

                    for (WorkflowState workflowState : workflow.getStates()) {

                        LOGGER.debug("Inspecting Workflow State: " + workflowState.getDisplayName());

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

            LOGGER.debug("Finished building transition map");
        }

        private void buildTransitionMap(Search search) {

            if (search == null) {
                return;
            }

            Query<?> query = search.toQuery(getSite());

            ObjectType selectedType = search.getSelectedType();

            LOGGER.debug("selectedType: " + (selectedType == null ? null : selectedType.getDisplayName()));

            if (selectedType == null) {
                return;
            }

            workflowStateCounts.put(selectedType, new HashMap<>());

            // check that a single visibility (workflow) has been refined
            if (search.getVisibilities() == null || search.getVisibilities().size() == 1) {
                return;
            }

            LOGGER.debug("search.getVisibilities(): " + ObjectUtils.toJson(search.getVisibilities()));

            Set<String> refinedStates = new HashSet<>();

            for (String currentState : search.getVisibilities()) {
                if (currentState.startsWith("w.")) {
                    refinedStates.add(currentState.substring(2));
                } else {
                    refinedStates.add(currentState);
                }
            }

            LOGGER.debug("Refined States: " + ObjectUtils.toJson(refinedStates));

            for (Workflow workflow : Query.from(Workflow.class).where("contentTypes = ?", selectedType).selectAll()) {

                LOGGER.debug("Inspecting Workflow: " + workflow.getName());

                Map<ObjectType, Map<WorkflowState, Map<String, WorkflowTransition>>> stateTransitionsByType = availableTransitionsMap.get(workflow);
                if (stateTransitionsByType == null) {
                    stateTransitionsByType = new LinkedHashMap<>();
                    availableTransitionsMap.put(workflow, stateTransitionsByType);
                }

                for (WorkflowState workflowState : workflow.getStates()) {

                    LOGGER.debug("Inspecting Workflow State: " + workflowState.getDisplayName());

                    // skip workflow states that are not part of the current Search refinement
                    if (!refinedStates.contains(workflowState.getName()) && !refinedStates.contains("w")) {
                        continue;
                    }

                    Map<WorkflowState, Map<String, WorkflowTransition>> stateTransitions = stateTransitionsByType.get(selectedType);
                    if (stateTransitions == null) {
                        stateTransitions = new LinkedHashMap<>();
                        stateTransitionsByType.put(selectedType, stateTransitions);
                    }

                    long stateCount = Query.fromQuery(query).where("cms.workflow.currentState = ?", workflowState.getName()).count();

                    workflowStateCounts.get(selectedType).put(workflowState.getName(), ObjectUtils.to(Integer.class, stateCount));

                    if (stateCount > 0) {

                        Map<String, WorkflowTransition> transitionsFrom = workflow.getTransitionsFrom(workflowState.getName());

                        if (transitionsFrom.size() > 0) {
                            hasAnyTransitions = true;
                        }

                        stateTransitions.put(workflowState, transitionsFrom);
                    }
                }
            }
        }
    }
}

