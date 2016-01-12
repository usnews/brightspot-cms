<%@ page session="false" import="
    com.psddev.cms.tool.ToolPageContext,
    com.psddev.cms.db.ToolUser,

    java.util.ArrayList,
    java.util.List,
    java.util.Map,
    java.util.Collections
"%>

<%
    ToolPageContext wp = new ToolPageContext(pageContext);

    ToolUser user = wp.getUser();
    Map<String, String> savedSearches = user.getSavedSearches();

    if (savedSearches.isEmpty()) {
        wp.writeStart("div", "class", "message");
            wp.writeHtml("No saved searches yet.");
        wp.writeEnd();
    }
    else {
        List<String> savedSearchNames = new ArrayList<String>(savedSearches.keySet());

        Collections.sort(savedSearchNames, String.CASE_INSENSITIVE_ORDER);

        wp.writeStart("ul", "class", "links");
            for (String savedSearchName : savedSearchNames) {
                String savedSearch = savedSearches.get(savedSearchName);

                    wp.writeStart("li");
                        wp.writeStart("a",
                              "href", wp.cmsUrl("/misc/search.jsp") + "?" + savedSearch,
                              "target", "miscSearch");
                            wp.writeHtml(savedSearchName);
                        wp.writeEnd();
                    wp.writeEnd();
            }
        wp.writeEnd();
    }
    wp.writeStart("form", "action", wp.cmsUrl("/misc/savedSearches.jsp"), "name", "refreshSavedSearches");
    wp.writeEnd();

%>