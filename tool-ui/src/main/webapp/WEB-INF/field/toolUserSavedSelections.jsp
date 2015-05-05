<%@ page session="false" import="

com.psddev.cms.db.ToolUser,
com.psddev.cms.tool.ToolPageContext,

java.util.ArrayList,
java.util.Collections,
java.util.List,
java.util.Map
" %><%

    ToolPageContext wp = new ToolPageContext(pageContext);
    ToolUser user = (ToolUser) request.getAttribute("object");
    String inputName = (String) request.getAttribute("inputName");
    Map<String, String> savedSelections = user.getSavedSelections();

    if (Boolean.TRUE.equals(request.getAttribute("isFormPost"))) {
        for (String name : wp.params(String.class, inputName)) {
            savedSelections.remove(name);
        }
    }

    List<String> names = new ArrayList<String>(savedSelections.keySet());

    Collections.sort(names, String.CASE_INSENSITIVE_ORDER);

    wp.writeStart("div", "class", "inputSmall");
    if (names.isEmpty()) {
        wp.writeStart("div", "class", "message message-info");
        wp.writeHtml("No saved selections yet.");
        wp.writeEnd();

    } else {
        wp.writeStart("ul", "style", "margin-bottom:0;");
        for (String name : names) {
            wp.writeStart("li");
            wp.writeHtml(name);

            wp.writeHtml(" ");

            wp.writeStart("span", "style", "float:right;");
            wp.writeElement("input",
                    "type", "checkbox",
                    "id", wp.createId(),
                    "name", inputName,
                    "value", name);

            wp.writeStart("label", "for", wp.getId());
            wp.writeHtml(" Remove");
            wp.writeEnd();
            wp.writeEnd();
            wp.writeEnd();
        }
        wp.writeEnd();
    }
    wp.writeEnd();
%>
