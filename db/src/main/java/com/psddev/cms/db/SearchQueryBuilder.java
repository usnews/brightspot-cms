package com.psddev.cms.db;

import com.psddev.dari.db.ComparisonPredicate;
import com.psddev.dari.db.CompoundPredicate;
import com.psddev.dari.db.ObjectField;
import com.psddev.dari.db.ObjectType;
import com.psddev.dari.db.Predicate;
import com.psddev.dari.db.PredicateParser;
import com.psddev.dari.db.Query;
import com.psddev.dari.db.Record;
import com.psddev.dari.db.Recordable;
import com.psddev.dari.util.CollectionUtils;
import com.psddev.dari.util.ObjectUtils;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchQueryBuilder extends Record {

    private Set<ObjectType> types;
    private List<Rule> rules = new ArrayList<Rule>();

    public Set<ObjectType> getTypes() {
        if (types == null) {
            setTypes(new HashSet<ObjectType>());
        }
        return types;
    }

    public void setTypes(Set<ObjectType> types) {
        this.types = types;
    }

    public List<Rule> getRules() {
        if (rules == null) {
            setRules(new ArrayList<Rule>());
        }
        return rules;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }

    public SearchQueryBuilder addRule(Rule rule) {
        getRules().add(rule);
        return this;
    }

    public SearchQueryBuilder addRules(List<Rule> rules) {
        if (!ObjectUtils.isBlank(rules)) {
            getRules().addAll(rules);
        }
        return this;
    }

    public SearchQueryBuilder addTypes(ObjectType... types) {
        if (types != null) {
            for (ObjectType type : types) {
                getTypes().add(type);
            }
        }
        return this;
    }

    public SearchQueryBuilder addTypes(Class<?>... classes) {
        if (classes != null) {
            for (Class<?> c : classes) {
                getTypes().add(ObjectType.getInstance(c));
            }
        }
        return this;
    }

    public SearchQueryBuilder boostType(int boost, ObjectType type) {
        BoostType rule = new BoostType();
        rule.setBoost(boost);
        rule.setType(type);
        return addRule(rule);
    }

    public SearchQueryBuilder boostType(int boost, Class<?> objectClass) {
        return boostType(boost, ObjectType.getInstance(objectClass));
    }

    public SearchQueryBuilder boostFields(int boost, ObjectType type, String... fields) {
        BoostFields rule = new BoostFields();
        rule.setBoost(boost);
        rule.setType(type);
        Collections.addAll(rule.getFields(), fields);
        return addRule(rule);
    }

    public SearchQueryBuilder boostFields(int boost, Class<?> objectClass, String... fields) {
        return boostFields(boost, ObjectType.getInstance(objectClass), fields);
    }

    public SearchQueryBuilder boostPhrase(int boost, String pattern, ObjectType type, String predicate) {
        BoostPhrase rule = new BoostPhrase();
        rule.setBoost(boost);
        rule.setPattern(pattern);
        rule.setType(type);
        rule.setPredicate(predicate);
        return addRule(rule);
    }

    public SearchQueryBuilder boostPhrase(int boost, String pattern, Class<?> objectClass, String predicate) {
        return boostPhrase(boost, pattern, ObjectType.getInstance(objectClass), predicate);
    }

    public SearchQueryBuilder addStopWords(String... stopWords) {
        if (stopWords != null) {
            StopWords rule = null;

            for (Rule r : getRules()) {
                if (r instanceof StopWords) {
                    rule = (StopWords) r;
                    break;
                }
            }

            if (rule == null) {
                rule = new StopWords();
                addRule(rule);
            }

            Collections.addAll(rule.getStopWords(), stopWords);
        }

        return this;
    }

    public SearchQueryBuilder addSynonyms() {
        //TODO: implementation
        return this;
    }

    public SearchQueryBuilder addSpotlights() {
        //TODO implementation
        return this;
    }

    private List<String> normalizeTerms(Object... terms) {
        List<String> normalized = new ArrayList<String>();

        for (Object term : CollectionUtils.recursiveIterable(terms)) {
            if (term instanceof Recordable) {
                normalized.add(((Recordable) term).getState().getId().toString());

            } else if (term != null) {
                String termString = term.toString();
                char[] letters = termString.toCharArray();
                int lastEnd = 0;

                for (int i = 0, length = letters.length; i < length; ++i) {
                    char letter = letters[i];

                    if (Character.isWhitespace(letter)) {
                        int end = i;
                        for (++i; i < length && Character.isWhitespace(letters[i]);) {
                            ++i;
                        }

                        String word = termString.substring(lastEnd, end);
                        lastEnd = i;
                        normalized.add(word);
                    }
                }

                normalized.add(termString.substring(lastEnd));
            }
        }
        return normalized;
    }

    public Query toQuery(Object... terms) {
        List<String> queryTerms = normalizeTerms(terms);
        Query query = Query.from(Object.class);

        for (Rule rule : getRules()) {
            rule.apply(this, query, queryTerms);
        }

        if (!queryTerms.isEmpty()) {
            query.or("_any matchesAny ?", queryTerms);
            query.sortRelevant(100000.0, "_any matchesAll ?", terms);
        }

        Set<ObjectType> allTypes = new HashSet<ObjectType>();
        for (ObjectType type : getTypes()) {
            allTypes.addAll(type.as(ToolUi.class).findDisplayTypes());
        }
        query.and("_type = ?", allTypes);

        return query;
    }

    public abstract static class Rule extends Record {

        public abstract void apply(SearchQueryBuilder queryBuilder, Query query, List<String> queryTerms);
    }

    public static class OnlyPathed extends Rule {

        private boolean onlyReturnPathed;

        public OnlyPathed() {
        }

        public OnlyPathed(boolean isPathed) {
            onlyReturnPathed = isPathed;
        }

        public boolean isOnlyReturnPathed() {
            return onlyReturnPathed;
        }

        public void setOnlyReturnPathed(boolean onlyReturnPathed) {
            this.onlyReturnPathed = onlyReturnPathed;
        }

        public void apply(SearchQueryBuilder queryBuilder, Query query, List<String> queryTerms) {
            if (onlyReturnPathed) {
                query.and(Directory.Static.hasPathPredicate());
            }
        }
    }

    public static class StopWords extends Rule {

        @CollectionMinimum(1)
        @Embedded
        @ToolUi.Note("Common words that may be omitted from the search query to provide higher quality results")
        private Set<String> stopWords = new LinkedHashSet<String>(Arrays.asList(
                "a", "about", "an", "and", "are", "as", "at", "be", "but", "by", "com",
                "do", "for", "from", "he", "her", "him", "his", "her", "hers", "how", "I",
                "if", "in", "is", "it", "its", "me", "my", "of", "on", "or", "our", "ours",
                "that", "the", "they", "this", "to", "too", "us", "she", "was", "what", "when",
                "where", "who", "will", "with", "why", "www"));

        public String getLabel() {
            return "Stop Words " + stopWords;
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

            if (ObjectUtils.isBlank(queryTerms)) {
                return;
            }

            for (Iterator<String> i = queryTerms.iterator(); i.hasNext();) {
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
                query.sortRelevant(0.001, "_any matchesAll ?", removed);
            }
        }
    }

    public static class Synonyms extends Rule {

        @CollectionMinimum(1)
        @Embedded
        @ToolUi.Note("Similar words that should be used in the search query to enrich the experience")
        private Set<Synonym> synonyms = new HashSet<Synonym>();

        public String getLabel() {
            return synonyms.size() + " Synonym Groups";
        }

        public Set<Synonym> getSynonyms() {
            if (synonyms == null) {
                setSynonyms(new HashSet<Synonym>());
            }
            return synonyms;
        }

        public void setSynonyms(Set<Synonym> synonyms) {
            this.synonyms = synonyms;
        }

        public void apply(SearchQueryBuilder queryBuilder, Query query, List<String> queryTerms) {
            if (ObjectUtils.isBlank(queryTerms)) {
                return;
            }

            Set<String> newTerms = new HashSet<String>();
            for (String qt : queryTerms) {
                newTerms.add(qt.toLowerCase());
            }

            for (Synonym s : getSynonyms()) {
                for (String term : s.getWords()) {
                    if (newTerms.contains(term.toLowerCase())) {
                        newTerms.addAll(s.getWords());
                        break;
                    }
                }
            }

            queryTerms = new ArrayList<String>(newTerms);
        }

        public static class Synonym extends Record {

            @CollectionMinimum(2)
            @Embedded
            @Required
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

    public static class Spotlights extends Rule {

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
            //TODO: add logic
        }

        public abstract class Spotlight<T extends Content> {
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
        }
    }

    public abstract static class BoostRule extends Rule {

        @ToolUi.Note("Number between -2 and 5")
        @Minimum(-2)
        @Maximum(5)
        private int boost;

        public double getBoost() {
            return Math.pow(10, boost);
        }

        public void setBoost(int boost) {
            this.boost = boost;
        }
    }

    public static class BoostType extends BoostRule {

        @Required
        private ObjectType type;

        public String getLabel() {
            return "Boost: " + getBoost() + " Type: " + type.getDisplayName();
        }

        public ObjectType getType() {
            return type;
        }

        public void setType(ObjectType type) {
            this.type = type;
        }

        @Override
        public void apply(SearchQueryBuilder queryBuilder, Query query, List<String> queryTerms) {
            query.sortRelevant(getBoost(), "_type = ?", type);
        }
    }

    public static class BoostFields extends BoostRule {

        private ObjectType type;
        private Set<String> fields;

        public String getLabel() {
            return "Boost: " + getBoost() + " " + type.getDisplayName() + " Field(s): " + fields;
        }

        public ObjectType getType() {
            return type;
        }

        public void setType(ObjectType type) {
            this.type = type;
        }

        public Set<String> getFields() {
            if (fields == null) {
                setFields(new LinkedHashSet<String>());
            }
            return fields;
        }

        public void setFields(Set<String> fields) {
            this.fields = fields;
        }

        @Override
        public void apply(SearchQueryBuilder queryBuilder, Query query, List<String> queryTerms) {
            double boost = getBoost();
            String prefix = getType().getInternalName() + "/";
            for (String field : getFields()) {

                List<UUID> uuids = new ArrayList<UUID>();
                List<String> texts = new ArrayList<String>();

                if (queryTerms != null) {
                    for (String queryTerm : queryTerms) {
                        UUID uuid = ObjectUtils.to(UUID.class, queryTerm);
                        if (uuid != null) {
                            uuids.add(uuid);
                        } else {
                            texts.add(queryTerm);
                        }
                    }
                }

                if (ObjectField.RECORD_TYPE.equals(type.getField(field).getInternalItemType())) {
                    if (!uuids.isEmpty()) {
                        query.sortRelevant(boost, prefix + field + " matchesAll ?", uuids);
                    }

                } else {
                    if (!texts.isEmpty()) {
                        query.sortRelevant(boost, prefix + field + " matchesAll ?", texts);
                    }
                }
            }
        }
    }

    public static class BoostPhrase extends BoostRule {

        public String pattern;
        public ObjectType type;
        public String predicate;

        public String getLabel() {
            return "Boost: " + getBoost() + " Phrase: " + pattern;
        }

        public String getPattern() {
            return pattern;
        }

        public void setPattern(String pattern) {
            this.pattern = pattern;
        }

        public ObjectType getType() {
            return type;
        }

        public void setType(ObjectType type) {
            this.type = type;
        }

        public String getPredicate() {
            return predicate;
        }

        public void setPredicate(String predicate) {
            this.predicate = predicate;
        }

        @Override
        public void apply(SearchQueryBuilder queryBuilder, Query query, List<String> queryTerms) {
            StringBuilder queryTermsString = new StringBuilder();

            for (String term : queryTerms) {
                queryTermsString.append(term);
                queryTermsString.append(' ');
            }

            Pattern pattern = Pattern.compile(getPattern(), Pattern.CANON_EQ | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            Matcher matcher = pattern.matcher(queryTermsString.toString());

            while (matcher.find()) {
                int groupCount = matcher.groupCount();
                Object[] parameters = new Object[groupCount];

                for (int i = 0; i < groupCount; ++i) {
                    parameters[i] = matcher.group(i + 1);
                }

                Predicate predicate = PredicateParser.Static.parse(getPredicate(), parameters);
                predicate = addPrefix(getType().getInternalName() + "/", predicate);
                query.sortRelevant(getBoost(), predicate);
            }
        }

        private Predicate addPrefix(String prefix, Predicate predicate) {
            if (predicate instanceof CompoundPredicate) {
                CompoundPredicate compound = (CompoundPredicate) predicate;
                List<Predicate> children = new ArrayList<Predicate>();
                for (Predicate child : compound.getChildren()) {
                    children.add(addPrefix(prefix, child));
                }
                return new CompoundPredicate(compound.getOperator(), children);

            } else if (predicate instanceof ComparisonPredicate) {
                ComparisonPredicate comparison = (ComparisonPredicate) predicate;
                return new ComparisonPredicate(comparison.getOperator(),
                                              comparison.isIgnoreCase(),
                                              prefix + comparison.getKey(),
                                              comparison.getValues());

            } else {
                return predicate;
            }
        }
    }
}
