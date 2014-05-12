package com.psddev.cms.db;

import com.psddev.dari.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

public class EditorialSearchSettings extends Content {

    @Embedded
    private SearchQueryBuilder.StopWords stopWords;
    @Embedded
    private SearchQueryBuilder.Synonyms synonyms;
    @Embedded
    private SearchQueryBuilder.Spotlights spotlights;
    //TODO: how do we want to surface rules?

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

    public List<SearchQueryBuilder.Rule> toQuery() {
        return null;
    }

    public SearchQueryBuilder.Spotlights getSpotlights() {
        return spotlights;
    }

    public void setSpotlights(SearchQueryBuilder.Spotlights spotlights) {
        this.spotlights = spotlights;
    }

    public List<SearchQueryBuilder.Rule> getEditorialRules() {
        List<SearchQueryBuilder.Rule> rules = new ArrayList<SearchQueryBuilder.Rule>();
        if (!ObjectUtils.isBlank(stopWords)) {
            rules.add(stopWords);
        }
        if (!ObjectUtils.isBlank(synonyms)) {
            rules.add(synonyms);
        }
        if (!ObjectUtils.isBlank(spotlights)) {
            rules.add(spotlights);
        }
        return rules;
    }
}
