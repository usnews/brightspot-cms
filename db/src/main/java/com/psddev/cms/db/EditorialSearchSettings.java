package com.psddev.cms.db;

import java.util.List;

public class EditorialSearchSettings extends Content {

    @ToolUi.Note("Do you want searches to do an exact match of the user's query? Unchecking this box will match based on any of the individual user terms")
    private boolean exactMatchSearchTerms;
    @Embedded
    private SearchQueryBuilder.StopWords stopWords = new SearchQueryBuilder.StopWords();
    @Embedded
    private SearchQueryBuilder.Synonyms synonyms;
    @Embedded
    private SearchQueryBuilder.Spotlights spotlights;
    //TODO: how do we want to surface rules?

    public boolean isExactMatchSearchTerms() {
        return exactMatchSearchTerms;
    }

    public void setExactMatchSearchTerms(boolean exactMatchSearchTerms) {
        this.exactMatchSearchTerms = exactMatchSearchTerms;
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

    public List<SearchQueryBuilder.Rule> toQuery(){
         return null;
    }

    public SearchQueryBuilder.Spotlights getSpotlights() {
        return spotlights;
    }

    public void setSpotlights(SearchQueryBuilder.Spotlights spotlights) {
        this.spotlights = spotlights;
    }
}
