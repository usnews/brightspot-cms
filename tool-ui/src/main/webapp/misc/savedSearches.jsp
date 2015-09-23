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

  if (!savedSearches.isEmpty()) {
    List<String> savedSearchNames = new ArrayList<String>(savedSearches.keySet());

    Collections.sort(savedSearchNames, String.CASE_INSENSITIVE_ORDER);

    wp.writeStart("ul");
    for (String savedSearchName : savedSearchNames) {
      String savedSearch = savedSearches.get(savedSearchName);

      wp.writeStart("li");
          wp.writeStart("a",
                  //"href", wp.url(null) + "?" + savedSearch);
                  "href", "/cms/misc/search.jsp" + "?" + savedSearch,
                  "target", "miscSearch");
              wp.writeHtml(savedSearchName);
          wp.writeEnd();
          wp.writeStart("form", "action", "/cms/misc/savedSearches.jsp", "name", "refreshSavedSearches");
          wp.writeEnd();
      wp.writeEnd();
    }
    wp.writeEnd();
  }

%>