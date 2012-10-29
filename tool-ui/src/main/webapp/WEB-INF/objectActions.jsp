<%@ page import="

com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.State
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);
State state = State.getInstance(request.getAttribute("object"));

// --- Presentation ---

%><div class="buttons">
    <input name="action" type="submit" value="Save" />
    <% if (state != null && !state.isNew()) { %>
        <input class="delete link" name="action" type="submit" value="Delete" onclick="return confirm('Are you sure you want to delete?');" />
    <% } %>
</div>
