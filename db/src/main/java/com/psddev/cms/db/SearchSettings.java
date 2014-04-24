package com.psddev.cms.db;

import com.psddev.dari.db.Query;

import java.util.ArrayList;
import java.util.List;

public class SearchSettings extends Content {

    @ToolUi.Note("Do you want searches to do an exact match of the user's query? Unchecking this box will match based on any of the individual user terms")
    private boolean exactMatchTerms;
    private List<SearchQueryBuilder.Rule> rules = new ArrayList<SearchQueryBuilder.Rule>();

    private transient String searchString;

    public boolean isExactMatchTerms() {
        return exactMatchTerms;
    }

    public void setExactMatchTerms(boolean exactMatchTerms) {
        this.exactMatchTerms = exactMatchTerms;
    }

    public List<SearchQueryBuilder.Rule> getRules() {
        return rules;
    }

    public void setRules(List<SearchQueryBuilder.Rule> rules) {
        this.rules = rules;
    }

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    public Query<?> getQuery(){
        SearchQueryBuilder searchQuery = new SearchQueryBuilder();
        searchQuery.setExactMatchTerms(exactMatchTerms);
        searchQuery.setRules(getRules());
        return searchQuery.toQuery(searchString);
    }
}
