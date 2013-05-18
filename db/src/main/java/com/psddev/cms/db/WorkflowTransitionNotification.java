package com.psddev.cms.db;

import java.util.Date;

import com.psddev.cms.tool.CmsTool;
import com.psddev.dari.db.Application;
import com.psddev.dari.db.State;

public class WorkflowTransitionNotification extends Notification {

    @Indexed(unique = true)
    @Required
    private String transition;

    public String getTransition() {
        return transition;
    }

    public void setTransition(String transition) {
        this.transition = transition;
    }

    @Override
    protected String createMessage(Object object, ToolUser sender, Date date, ToolUser receiver) {
        State state = State.getInstance(object);
        StringBuilder message = new StringBuilder();

        message.append("User: ");
        message.append(sender.getLabel());
        message.append(", Workflow: ");
        message.append(getTransition());
        message.append(", Content: ");
        message.append(state.getLabel());
        message.append(" - ");
        message.append(Application.Static.getInstance(CmsTool.class).fullUrl("/content/edit.jsp", "id", state.getId()));

        return message.toString();
    }

    @Override
    public String getLabel() {
        StringBuilder label = new StringBuilder();

        label.append("When Someone ");
        label.append(getTransition());

        return label.toString();
    }
}
