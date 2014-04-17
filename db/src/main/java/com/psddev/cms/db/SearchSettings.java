package com.psddev.cms.db;

import com.psddev.dari.db.*;

import java.util.*;

public class SearchSettings extends Content {

    private Set<ObjectType> types;
    @Embedded
    private List<SearchQueryBuilder.Rule> rules = new ArrayList<SearchQueryBuilder.Rule>(Arrays.asList(new StopWords()));

    public Set<ObjectType> getTypes() {
        if (types == null) {
            setTypes(new HashSet<ObjectType>());
        }
        return types;
    }

    public void setTypes(Set<ObjectType> types) {
        this.types = types;
    }

    public List<SearchQueryBuilder.Rule> getRules() {
        if (rules == null) {
            setRules(new ArrayList<SearchQueryBuilder.Rule>());
        }
        return rules;
    }

    public void setRules(List<SearchQueryBuilder.Rule> rules) {
        this.rules = rules;
    }


    public static class StopWords extends SearchQueryBuilder.Rule {

        @Embedded
        @CollectionMinimum(1)
        private Set<String> stopWords = new LinkedHashSet<String>(Arrays.asList(
                "a", "about", "an", "and", "are", "as", "at", "be", "but", "by", "com",
                "do", "for", "from", "he", "her", "him", "his", "her", "hers", "how", "I",
                "if", "in", "is", "it", "its", "me", "my", "of", "on", "or", "our", "ours",
                "that", "the", "they", "this", "to", "too", "us", "she", "was", "what", "when",
                "where", "who", "will", "with", "why", "www"));

        public String getLabel() {
            return "Common words that may be omitted from the search query to provide higher quality results";
        }

        public Set<String> getStopWords() {
            if (stopWords == null) {
                setStopWords(new LinkedHashSet<String>());
            }
            return stopWords;
        }

        public void setStopWords(Set<String> stopWords) {
            this.stopWords = stopWords;
        }

        public void apply(SearchQueryBuilder queryBuilder, Query query, List<String> queryTerms) {
            Set<String> stopWords = getStopWords();
            Set<String> removed = null;

            for (Iterator<String> i = queryTerms.iterator(); i.hasNext(); ) {
                String word = i.next();
                if (stopWords.contains(word)) {
                    i.remove();
                    if (removed == null) {
                        removed = new HashSet<String>();
                    }
                    removed.add(word);
                }
            }

            if (removed != null && !removed.isEmpty()) {
                query.sortRelevant(1.0, "_any matchesAll ?", removed);
            }
        }
    }

    public static class Synonyms extends SearchQueryBuilder.Rule {

        @Embedded
        @CollectionMinimum(1)
        private List<Synonym> synonyms = new ArrayList<Synonym>();

        public String getLabel() {
            return "Similar words or common misspellings that should be grouped together for the purposes of search";
        }

        public List<Synonym> getSynonyms() {
            return synonyms;
        }

        public void setSynonyms(List<Synonym> synonyms) {
            this.synonyms = synonyms;
        }

        public void apply(SearchQueryBuilder queryBuilder, Query query, List<String> queryTerms) {

        }

        public static class Synonym extends Record {

            @Embedded
            @Required
            @CollectionMinimum(2)
            private Set<String> words = new HashSet<String>();

            public String getLabel() {
                return words.toString();
            }

            public Set<String> getWords() {
                return words;
            }

            public void setWords(Set<String> words) {
                this.words = words;
            }
        }
    }

    public static class Spotlights extends SearchQueryBuilder.Rule {

        @Embedded
        @CollectionMinimum(1)
        private List<Spotlight> spotlights = new ArrayList<Spotlight>();

        public List<Spotlight> getSpotlights() {
            return spotlights;
        }

        public void setSpotlights(List<Spotlight> spotlights) {
            this.spotlights = spotlights;
        }

        public void apply(SearchQueryBuilder queryBuilder, Query query, List<String> queryTerms) {

        }

        public abstract class Spotlight<T extends Content> extends SearchQueryBuilder.Rule {
            private List<String> spotLightTerms;
            abstract List<T> getSpotlightContent();

            public String getLabel() {
                return spotLightTerms.toString();
            }

            public List<String> getSpotLightTerms() {
                return spotLightTerms;
            }

            public void setSpotLightTerms(List<String> spotLightTerms) {
                this.spotLightTerms = spotLightTerms;
            }

            public void apply(SearchQueryBuilder queryBuilder, Query query, List<String> queryTerms) {

            }
        }
    }

}
