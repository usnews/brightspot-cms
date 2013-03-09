<%@ page import="

com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.State
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);
State state = State.getInstance(request.getAttribute("object"));

// --- Presentation ---

%><div class="buttons">
    <button class="action action-save" name="action" value="Save">Save</button>
    <% if (state != null && !state.isNew()) { %>
        <button class="action action-delete action-pullRight link" name="action" value="Delete" onclick="return confirm('Are you sure you want to delete?');">Delete</button>
    <% } %>
</div>
