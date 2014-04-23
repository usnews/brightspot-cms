package com.psddev.cms.db;

import com.psddev.dari.db.*;
import com.psddev.dari.util.CollectionUtils;
import com.psddev.dari.util.ObjectUtils;
import org.apache.commons.codec.language.Metaphone;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//TODO: not sure I like that every method pretty much needs to be deprecated, leaning back toward new object and deprecating this one
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
        return Query.from(Search.class).where("internalName = ?",name).first();
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
            for (Iterator<Rule> i = getRules().iterator(); i.hasNext(); ) {
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

    public Search boostDirectoryItems(final double boost) {
        return addRule(new Rule() {
            public void apply(Search search, SearchQuery query, List<String> queryTerms) {
                query.sortRelevant(boost, Directory.Static.hasPathPredicate());
            }

            //public void applyRule(SearchQueryBuilder queryBuilder, Query query, List<String> queryTerms) {
            public void applyRule(Search queryBuilder, Query query, List<String> queryTerms) {
                query.sortRelevant(boost, Directory.Static.hasPathPredicate());
            }
        });
    }

    private List<String> normalizeTerms(Object... terms) {
        List<String> normalized = new ArrayList<String>();

        for (Object term : CollectionUtils.recursiveIterable(terms)) {
            if (term == null) {

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
                        for (++ i; i < length && Character.isWhitespace(letters[i]); ) {
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

    @Deprecated
    public SearchQuery toQuery(Object... terms) {
        List<String> queryTerms = normalizeTerms(terms);
        SearchQuery query = new SearchQuery();

        if (!queryTerms.isEmpty()) {
            for (Rule rule : getRules()) {
                rule.apply(this, query, queryTerms);
            }

            if (!queryTerms.isEmpty()) {
                query.or("_any matchesAll ?", queryTerms);
            }
        }

        Set<ObjectType> allTypes = new HashSet<ObjectType>();
        for (ObjectType type : getTypes()) {
            allTypes.addAll(type.as(ToolUi.class).findDisplayTypes());
        }
        query.and("_type = ?", allTypes);

        return query;
    }

    public Query getQuery(Object... terms) {
        List<String> queryTerms = normalizeTerms(terms);
        Query query = Query.from(Object.class);

        if (!queryTerms.isEmpty()) {
            for (Rule rule : getRules()) {
                rule.applyRule(this, query, queryTerms);
            }

            if (!queryTerms.isEmpty()) {
                query.or("_any matchesAll ?", queryTerms);
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
    public static abstract class Rule extends Record {

        @Deprecated
        public abstract void apply(Search search, SearchQuery query, List<String> queryTerms);
        //public void applyRule(SearchQueryBuilder queryBuilder, Query query, List<String> queryTerms) {
        public abstract void applyRule(Search queryBuilder, Query query, List<String> queryTerms);
    }

    public static class StopWords extends Rule {

        @CollectionMinimum(1)
        private Set<String> stopWords = new LinkedHashSet<String>(Arrays.asList(
                "a", "about", "an", "and", "are", "as", "at", "be", "but", "by", "com",
                "do", "for", "from", "he", "her", "him", "his", "her", "hers", "how", "I",
                "if", "in", "is", "it", "its", "me", "my", "of", "on", "or", "our", "ours",
                "that", "the", "they", "this", "to", "too", "us", "she", "was", "what", "when",
                "where", "who", "will", "with", "why", "www"));

        public Set<String> getStopWords() {
            if (stopWords == null) {
                setStopWords(new LinkedHashSet<String>());
            }
            return stopWords;
        }

        public void setStopWords(Set<String> stopWords) {
            this.stopWords = stopWords;
        }

        @Deprecated
        public void apply(Search search, SearchQuery query, List<String> queryTerms) {
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

        //public void applyRule(SearchQueryBuilder queryBuilder, Query query, List<String> queryTerms) {
        public void applyRule(Search queryBuilder, Query query, List<String> queryTerms) {
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

    public static abstract class BoostRule extends Rule {

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

        @Deprecated
        public void apply(Search search, SearchQuery query, List<String> queryTerms) {
            query.sortRelevant(getBoost(), "_type = ?", type.as(ToolUi.class).findDisplayTypes());
        }

        //public void applyRule(SearchQueryBuilder queryBuilder, Query query, List<String> queryTerms) {
        public void applyRule(Search queryBuilder, Query query, List<String> queryTerms) {
            query.sortRelevant(getBoost(), "_type = ?", type.as(ToolUi.class).findDisplayTypes());
        }
    }

    public static class BoostLabels extends BoostRule {

        @Deprecated
        public void apply(Search search, SearchQuery query, List<String> queryTerms) {
            double boost = getBoost();
            for (ObjectType type : search.getTypes()) {
                String prefix = type.getInternalName() + "/";
                for (String fieldName : type.getLabelFields()) {
                    query.sortRelevant(boost, prefix + fieldName + " matchesAll ?", queryTerms);
                }
            }
        }

        //public void applyRule(SearchQueryBuilder queryBuilder, Query query, List<String> queryTerms) {
        public void applyRule(Search queryBuilder, Query query, List<String> queryTerms) {
            double boost = getBoost();
            for (ObjectType type : queryBuilder.getTypes()) {
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

        @Deprecated
        public void apply(Search search, SearchQuery query, List<String> queryTerms) {
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

        //public void applyRule(SearchQueryBuilder queryBuilder, Query query, List<String> queryTerms) {
        public void applyRule(Search queryBuilder, Query query, List<String> queryTerms) {
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

        @Deprecated
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

        //public void applyRule(SearchQueryBuilder queryBuilder, Query query, List<String> queryTerms) {
        public void applyRule(Search queryBuilder, Query query, List<String> queryTerms) {
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

        @Deprecated
        public void apply(Search search, SearchQuery query, List<String> queryTerms) {
            List<ObjectType> types = getType().as(ToolUi.class).findDisplayTypes();

            for (Iterator<String> i = queryTerms.iterator(); i.hasNext(); ) {
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

        //public void applyRule(SearchQueryBuilder queryBuilder, Query query, List<String> queryTerms) {
        public void applyRule(Search queryBuilder, Query query, List<String> queryTerms) {
            List<ObjectType> types = getType().as(ToolUi.class).findDisplayTypes();

            for (Iterator<String> i = queryTerms.iterator(); i.hasNext(); ) {
                String word = i.next();
                String similar = findSimilar(word);

                if (similar != null) {
                    i.remove();
                    query.and("_type = ? or _any matchesAll ?", types, word);
                    query.sortRelevant(getBoost(), "_type = ?", types);

                    //TODO: add logic that was removed
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

        @Deprecated
        public void apply(Search search, SearchQuery query, List<String> queryTerms) {
            Set<String> terms = getTerms();

            for (Iterator<String> i = queryTerms.iterator(); i.hasNext(); ) {
                String queryTerm = i.next();

                if (terms.contains(queryTerm)) {
                    i.remove();
                }
            }

            query.or("_any matchesAny ?", terms);
            query.sortRelevant(getBoost(), "_any matchesAny ?", terms);
        }

        //public void applyRule(SearchQueryBuilder queryBuilder, Query query, List<String> queryTerms) {
        public void applyRule(Search queryBuilder, Query query, List<String> queryTerms) {
            Set<String> terms = getTerms();

            for (Iterator<String> i = queryTerms.iterator(); i.hasNext(); ) {
                String queryTerm = i.next();

                if (terms.contains(queryTerm)) {
                    i.remove();
                }
            }

            query.or("_any matchesAny ?", terms);
            query.sortRelevant(getBoost(), "_any matchesAny ?", terms);
        }
    }
}