<%@ page session="false" import="

com.psddev.cms.db.Template,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.ObjectField,
com.psddev.dari.db.State,

java.util.ArrayList,
java.util.Collections,
java.util.List,

javax.script.ScriptEngineFactory,
javax.script.ScriptEngineManager
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);

State state = State.getInstance(request.getAttribute("object"));

ObjectField field = (ObjectField) request.getAttribute("field");
String fieldName = field.getInternalName();
String fieldValue = (String) state.getValue(fieldName);

String inputName = (String) request.getAttribute("inputName");

List<Option> engines = new ArrayList<Option>();
engines.add(new Option(Template.CSV_NORMALIZE_PATHS_ENGINE));
ScriptEngineManager manager = new ScriptEngineManager();
for (ScriptEngineFactory factory : manager.getEngineFactories()) {
    for (String name : factory.getNames()) {
        engines.add(new Option(name, factory.getLanguageName()));
        break;
    }
}
Collections.sort(engines);

if ((Boolean) request.getAttribute("isFormPost")) {
    state.putValue(fieldName, wp.param(inputName));
    return;
}

// --- Presentation ---

%><div class="inputSmall">
    <select id="<%= wp.getId() %>" name="<%= wp.h(inputName) %>">
        <option value=""></option>
        <% for (Option engine : engines) { %>
            <option<%= engine._name.equals(fieldValue) ? " selected" : "" %> value="<%= wp.h(engine._name) %>"><%= wp.h(engine._label) %></option>
        <% } %>
    </select>
</div><%!

// Script engine options.
private static class Option implements Comparable<Option> {

    public String _name;
    public String _label;

    public Option(String name, String label) {
        _name = name;
        _label = label;
    }

    public Option(String name) {
        this(name, name);
    }

    public int compareTo(Option other) {
        if (other == null) {
            throw new NullPointerException();
        } else {
            return _label.compareTo(other._label);
        }
    }
}
%>
