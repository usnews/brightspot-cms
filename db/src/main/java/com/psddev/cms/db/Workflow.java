package com.psddev.cms.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.psddev.dari.db.Modification;
import com.psddev.dari.db.ObjectType;
import com.psddev.dari.db.Query;
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
            String name = (String) t.get("name");

            transition.setName(name);
            transition.setSource(states.get(t.get("source")));
            transition.setTarget(states.get(t.get("target")));
            transitions.put(name, transition);
        }

        return transitions;
    }

    public Map<String, WorkflowTransition> getTransitionsFrom(String state) {
        Map<String, WorkflowTransition> transitions = getTransitions();

        if (state == null) {
            state = "New";
        }

        for (Iterator<Map.Entry<String, WorkflowTransition>> i = transitions.entrySet().iterator(); i.hasNext(); ) {
            WorkflowTransition transition = i.next().getValue();
            WorkflowState source = transition.getSource();

            if (source == null ||
                    !state.equals(source.getName()) ||
                    "Published".equals(transition.getTarget().getName())) {
                i.remove();
            }
        }

        return transitions;
    }

    @FieldInternalNamePrefix("cms.workflow.")
    public static class Data extends Modification<Object> {

        @Indexed(visibility = true)
        private String currentState;

        private List<Log> logs;

        public String getCurrentState() {
            return currentState;
        }

        /**
         * @param transition If {@code null}, makes the object visible.
         * @param user May be {@code null}.
         * @param comment May be {@code null}.
         * @return New workflow state. May be {@code null}.
         */
        public String changeState(WorkflowTransition transition, ToolUser user, String comment) {
            String transitionName;
            String transitionTarget;

            if (transition == null) {
                transitionName = "Publish";
                transitionTarget = null;

            } else {
                transitionName = transition.getName();
                transitionTarget = transition.getTarget().getName();

                if ("Published".equals(transitionTarget)) {
                    transitionTarget = null;
                }
            }

            currentState = transitionTarget;
            Log log = new Log();

            log.setDate(new Date());
            log.setTransition(transitionName);
            log.setUserId(user != null ? user.getId() : null);
            log.setComment(comment);
            getLogs().add(log);

            return currentState;
        }

        /**
         * @param state If {@code null}, makes the object visible.
         */
        public void revertState(String state) {
            this.currentState = state;

            List<Log> logs = getLogs();
            int logsSize = logs.size();

            if (logsSize > 0) {
                logs.remove(logsSize - 1);
            }
        }

        public List<Log> getLogs() {
            if (logs == null) {
                logs = new ArrayList<Log>();
            }
            return logs;
        }

        public void setLogs(List<Log> logs) {
            this.logs = logs;
        }

        public Log getLastLog() {
            List<Log> logs = getLogs();

            return logs.isEmpty() ? null : logs.get(logs.size() - 1);
        }
    }

    @Embedded
    public static class Log extends Record {

        private Date date;
        private String transition;
        private UUID userId;
        private String comment;

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public String getTransition() {
            return transition;
        }

        public void setTransition(String transition) {
            this.transition = transition;
        }

        public UUID getUserId() {
            return userId;
        }

        public void setUserId(UUID userId) {
            this.userId = userId;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public ToolUser getUser() {
            return Query.from(ToolUser.class).where("_id = ?", getUserId()).first();
        }
    }
}
