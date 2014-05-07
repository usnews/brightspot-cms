package com.psddev.cms.db;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.language.Metaphone;

import com.psddev.dari.db.ComparisonPredicate;
import com.psddev.dari.db.CompoundPredicate;
import com.psddev.dari.db.ObjectType;
import com.psddev.dari.db.Predicate;
import com.psddev.dari.db.PredicateParser;
import com.psddev.dari.db.Query;
import com.psddev.dari.db.Record;
import com.psddev.dari.db.Recordable;
import com.psddev.dari.util.CollectionUtils;

public class Search extends Record {

    private static final Metaphone METAPHONE = new Metaphone();

    @Required
    private String displayName;

    @Indexed(unique = true)
    @Required
    private String internalName;

    private Set<ObjectType> types;
    private List<Rule> rules = new ArrayList<Rule>(Arrays.asList(new StopWords()));

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getInternalName() {
        return internalName;
    }

    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

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

    // --- Fluent methods ---

    public static Search named(String name) {
        return Query.from(Search.class).where("internalName = ?").first();
    }

    public Search addTypes(ObjectType... types) {
        if (types != null) {
            for (ObjectType type : types) {
                getTypes().add(type);
            }
        }
        return this;
    }

    public Search addTypes(Class<?>... classes) {
        if (classes != null) {
            for (Class<?> c : classes) {
                getTypes().add(ObjectType.getInstance(c));
            }
        }
        return this;
    }

    public Search addRule(Rule rule) {
        getRules().add(rule);
        return this;
    }

    public Search addStopWords(String... stopWords) {
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

    public Search boostType(double boost, ObjectType type) {
        BoostType rule = new BoostType();
        rule.setBoost(boost);
        rule.setType(type);
        return addRule(rule);
    }

    public Search boostType(double boost, Class<?> objectClass) {
        return boostType(boost, ObjectType.getInstance(objectClass));
    }

    public Search boostLabels(double boost) {
        if (boost != 1.0) {
            for (Rule rule : getRules()) {
                if (rule instanceof BoostLabels) {
                    ((BoostLabels) rule).setBoost(boost);
                    return this;
                }
            }

            BoostLabels rule = new BoostLabels();
            rule.setBoost(boost);
            addRule(rule);

        } else {
            for (Iterator<Rule> i = getRules().iterator(); i.hasNext();) {
                Rule rule = i.next();
                if (rule instanceof BoostLabels) {
                    i.remove();
                }
            }
        }

        return this;
    }

    public Search boostFields(double boost, ObjectType type, String... fields) {
        BoostFields rule = new BoostFields();
        rule.setBoost(boost);
        rule.setType(type);
        Collections.addAll(rule.getFields(), fields);
        return addRule(rule);
    }

    public Search boostFields(double boost, Class<?> objectClass, String... fields) {
        return boostFields(boost, ObjectType.getInstance(objectClass), fields);
    }

    public Search boostPhrase(double boost, String pattern, ObjectType type, String predicate) {
        BoostPhrase rule = new BoostPhrase();
        rule.setBoost(boost);
        rule.setPattern(pattern);
        rule.setType(type);
        rule.setPredicate(predicate);
        return addRule(rule);
    }

    public Search boostPhrase(double boost, String pattern, Class<?> objectClass, String predicate) {
        return boostPhrase(boost, pattern, ObjectType.getInstance(objectClass), predicate);
    }

    public Search addTypeKeywords(double boost, ObjectType type, String... keywords) {
        if (keywords != null) {
            TypeKeywords rule = new TypeKeywords();
            rule.setBoost(boost);
            rule.setType(type);
            Collections.addAll(rule.getKeywords(), keywords);
            addRule(rule);
        }
        return this;
    }

    public Search addTypeKeywords(double boost, Class<?> objectClass, String... keywords) {
        return addTypeKeywords(boost, ObjectType.getInstance(objectClass), keywords);
    }

    private List<String> normalizeTerms(Object... terms) {
        List<String> normalized = new ArrayList<String>();

        for (Object term : CollectionUtils.recursiveIterable(terms)) {
            if (term == null) {
                continue;

            } else if (term instanceof Recordable) {
                normalized.add(((Recordable) term).getState().getId().toString());

            } else {
                String termString = term.toString();
                char[] letters = termString.toCharArray();
                int lastEnd = 0;

                for (int i = 0, length = letters.length; i < length; ++ i) {
                    char letter = letters[i];

                    if (Character.isWhitespace(letter)) {
                        int end = i;
                        for (++ i; i < length && Character.isWhitespace(letters[i]);) {
                            ++ i;
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

    public Search addOptionalTerms(double boost, Object... terms) {
        OptionalTerms rule = new OptionalTerms();
        rule.setBoost(boost);
        rule.setTerms(new HashSet<String>(normalizeTerms(terms)));
        return addRule(rule);
    }

    public SearchQuery toQuery(Object... terms) {
        List<String> queryTerms = normalizeTerms(terms);
        SearchQuery query = new SearchQuery();

        if (!queryTerms.isEmpty()) {
            for (Rule rule : getRules()) {
                rule.apply(this, query, queryTerms);
            }
            if (!queryTerms.isEmpty()) {
                query.and("_any matchesAll ?", queryTerms);
            }
        }

        Set<ObjectType> allTypes = new HashSet<ObjectType>();
        for (ObjectType type : getTypes()) {
            allTypes.addAll(type.as(ToolUi.class).findDisplayTypes());
        }

        query.and("_type = ?", allTypes);
        return query;
    }

    @Embedded
    public abstract static class Rule extends Record {

        public abstract void apply(Search search, SearchQuery query, List<String> queryTerms);
    }

    public static class StopWords extends Rule {

        private Set<String> stopWords = new LinkedHashSet<String>(Arrays.asList(
                "a", "about", "an", "are", "as", "at", "be", "by", "com",
                "for", "from", "how", "in", "is", "it", "of", "on", "or",
                "that", "the", "this", "to", "was", "what", "when", "where",
                "who", "will", "with", "the", "www"));

        public Set<String> getStopWords() {
            if (stopWords == null) {
                setStopWords(new LinkedHashSet<String>());
            }
            return stopWords;
        }

        public void setStopWords(Set<String> stopWords) {
            this.stopWords = stopWords;
        }

        @Override
        public void apply(Search search, SearchQuery query, List<String> queryTerms) {
            Set<String> stopWords = getStopWords();
            Set<String> removed = null;

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
                query.sortRelevant(1.0, "_any matchesAll ?", removed);
            }
        }
    }

    public abstract static class BoostRule extends Rule {

        private double boost;

        public double getBoost() {
            return boost;
        }

        public void setBoost(double boost) {
            this.boost = boost;
        }
    }

    public static class BoostType extends BoostRule {

        private ObjectType type;

        public ObjectType getType() {
            return type;
        }

        public void setType(ObjectType type) {
            this.type = type;
        }

        @Override
        public void apply(Search search, SearchQuery query, List<String> queryTerms) {
            query.sortRelevant(getBoost(), "_type = ?", type.as(ToolUi.class).findDisplayTypes());
        }
    }

    public static class BoostLabels extends BoostRule {

        @Override
        public void apply(Search search, SearchQuery query, List<String> queryTerms) {
            double boost = getBoost();
            for (ObjectType type : search.getTypes()) {
                String prefix = type.getInternalName() + "/";
                for (String fieldName : type.getLabelFields()) {
                    query.sortRelevant(boost, prefix + fieldName + " matchesAll ?", queryTerms);
                }
            }
        }
    }

    public static class BoostFields extends BoostRule {

        private ObjectType type;
        private Set<String> fields;

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
        public void apply(Search search, SearchQuery query, List<String> queryTerms) {
            double boost = getBoost();
            String prefix = getType().getInternalName() + "/";
            for (String field : getFields()) {
                query.sortRelevant(boost, prefix + field + " matchesAll ?", queryTerms);
            }
        }
    }

    public static class BoostPhrase extends BoostRule {

        public String pattern;
        public ObjectType type;
        public String predicate;

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
        public void apply(Search search, SearchQuery query, List<String> queryTerms) {
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

                for (int i = 0; i < groupCount; ++ i) {
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
                return new ComparisonPredicate(
                        comparison.getOperator(),
                        comparison.isIgnoreCase(),
                        prefix + comparison.getKey(),
                        comparison.getValues());

            } else {
                return predicate;
            }
        }
    }

    public static class TypeKeywords extends BoostRule {

        private ObjectType type;
        private Set<String> keywords;

        public ObjectType getType() {
            return type;
        }

        public void setType(ObjectType type) {
            this.type = type;
        }

        public Set<String> getKeywords() {
            if (keywords == null) {
                setKeywords(new LinkedHashSet<String>());
            }
            return keywords;
        }

        public void setKeywords(Set<String> keywords) {
            this.keywords = keywords;
        }

        @Override
        public void apply(Search search, SearchQuery query, List<String> queryTerms) {
            List<ObjectType> types = getType().as(ToolUi.class).findDisplayTypes();

            for (Iterator<String> i = queryTerms.iterator(); i.hasNext();) {
                String word = i.next();
                String similar = findSimilar(word);

                if (similar != null) {
                    i.remove();
                    query.and("_type = ? or _any matchesAll ?", types, word);
                    query.sortRelevant(getBoost(), "_type = ?", types);

                    if (!similar.equalsIgnoreCase(word)) {
                        query.getSubstitutions().put(word, similar);
                    }
                }
            }
        }

        private String findSimilar(String term) {
            ObjectType type = getType();
            String encodedTerm = METAPHONE.encode(term);

            String displayName = type.getDisplayName();
            if (encodedTerm.equals(METAPHONE.encode(displayName))) {
                return displayName;
            }

            for (String keyword : getKeywords()) {
                if (encodedTerm.equals(METAPHONE.encode(keyword))) {
                    return keyword;
                }
            }

            return null;
        }
    }

    public static class OptionalTerms extends BoostRule {

        private Set<String> terms;

        public Set<String> getTerms() {
            if (terms == null) {
                terms = new HashSet<String>();
            }
            return terms;
        }

        public void setTerms(Set<String> terms) {
            this.terms = terms;
        }

        @Override
        public void apply(Search search, SearchQuery query, List<String> queryTerms) {
            Set<String> terms = getTerms();

            for (Iterator<String> i = queryTerms.iterator(); i.hasNext();) {
                String queryTerm = i.next();

                if (terms.contains(queryTerm)) {
                    i.remove();
                }
            }

            query.and("_any matchesAny ?", terms);
            query.sortRelevant(getBoost(), "_any matchesAll ?", queryTerms);
        }
    }
}
