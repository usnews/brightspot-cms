package com.psddev.cms.db;

import com.psddev.dari.db.ObjectType;
import com.psddev.dari.db.Query;
import com.psddev.dari.util.ObjectUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EditorialSearchSettings extends Content {

    @Embedded
    private SearchQueryBuilder.StopWords stopWords;
    @Embedded
    private SearchQueryBuilder.Synonyms synonyms;

    private transient String searchString;

    public EditorialSearchSettings() {
        setStopWords(new SearchQueryBuilder.StopWords());
    }

    public SearchQueryBuilder.StopWords getStopWords() {
        return stopWords;
    }

    public void setStopWords(SearchQueryBuilder.StopWords stopWords) {
        this.stopWords = stopWords;
    }

    public SearchQueryBuilder.Synonyms getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(SearchQueryBuilder.Synonyms synonyms) {
        this.synonyms = synonyms;
    }

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    public Query<?> getQuery() {
        //initialize the query for search
        SearchQueryBuilder searchQuery = new SearchQueryBuilder();

        //set the types that you will search against
        Set<ObjectType> types = new HashSet<ObjectType>();
        types.addAll(ObjectType.getInstance(Content.class).findConcreteTypes());
        searchQuery.setTypes(types);

        //set the initial rules, followed by the ones from the cms
        searchQuery.addRules(this.getEditorialRules());

        //return the query
        return searchQuery.toQuery(getSearchString());
    }

    public List<SearchQueryBuilder.Rule> getEditorialRules() {
        List<SearchQueryBuilder.Rule> rules = new ArrayList<SearchQueryBuilder.Rule>();
        if (!ObjectUtils.isBlank(stopWords)) {
            rules.add(stopWords);
        }
        if (!ObjectUtils.isBlank(synonyms)) {
            rules.add(synonyms);
        }
        return rules;
    }
}
