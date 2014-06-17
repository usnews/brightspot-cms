package com.psddev.cms.db;

import java.util.ArrayList;
import java.util.List;

import com.psddev.dari.util.ObjectUtils;

public abstract class EditorialSearchSettings extends Content {

    @Embedded
    private SearchQueryBuilder.StopWords stopWords;
    @Embedded
    private SearchQueryBuilder.Synonyms synonyms;

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
