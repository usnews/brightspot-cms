package com.psddev.cms.db;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.psddev.dari.db.Modification;
import com.psddev.dari.db.ObjectType;
import com.psddev.dari.db.Record;

public class Workflow extends Record {

    @Indexed(unique = true)
    @Required
    private String name;

    @Indexed(unique = true)
    private Set<ObjectType> contentTypes;

    @ToolUi.FieldDisplayType("workflowActions")
    private Map<String, Object> actions;

    /** Returns the name. */
    public String getName() {
        return name;
    }

    /** Sets the name. */
    public void setName(String name) {
        this.name = name;
    }

    public Set<ObjectType> getContentTypes() {
        if (contentTypes == null) {
            contentTypes = new LinkedHashSet<ObjectType>();
        }
        return contentTypes;
    }

    public void setContentTypes(Set<ObjectType> contentTypes) {
        this.contentTypes = contentTypes;
    }

    public Map<String, Object> getActions() {
        if (actions == null) {
            actions = new LinkedHashMap<String, Object>();
        }
        return actions;
    }

    public void setActions(Map<String, Object> actions) {
        this.actions = actions;
    }

    public Map<String, WorkflowTransition> getTransitions() {
        @SuppressWarnings({ "rawtypes", "unchecked" })
        Map<String, List<Map<String, Object>>> actions = (Map) getActions();
        Map<String, WorkflowState> states = new HashMap<String, WorkflowState>();
        WorkflowState state;

        for (Map<String, Object> s : actions.get("states")) {
            state = new WorkflowState();
            state.setName((String) s.get("name"));
            states.put((String) s.get("id"), state);
        }

        state = new WorkflowState();
        state.setName("New");
        states.put("initial", state);

        state = new WorkflowState();
        state.setName("Published");
        states.put("final", state);

        Map<String, WorkflowTransition> transitions = new HashMap<String, WorkflowTransition>();

        for (Map<String, Object> t : actions.get("transitions")) {
            WorkflowTransition transition = new WorkflowTransition();

            transition.setSource(states.get(t.get("source")));
            transition.setTarget(states.get(t.get("target")));
            transitions.put((String) t.get("name"), transition);
        }

        return transitions;
    }

    public Map<String, WorkflowTransition> getTransitionsFrom(String state) {
        Map<String, WorkflowTransition> transitions = getTransitions();

        if (state == null) {
            state = "New";
        }

        for (Iterator<Map.Entry<String, WorkflowTransition>> i = transitions.entrySet().iterator(); i.hasNext(); ) {
            WorkflowState source = i.next().getValue().getSource();

            if (source == null ||
                    !state.equals(source.getName())) {
                i.remove();
            }
        }

        return transitions;
    }

    @FieldInternalNamePrefix("cms.workflow.")
    public static class Data extends Modification<Object> {

        @Indexed(visibility = true)
        private String state;

        public String getWorkflowState() {
            return state;
        }

        public void setWorkflowState(String state) {
            this.state = state;
        }
    }
}
