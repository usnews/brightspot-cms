package com.psddev.cms.db;

import com.psddev.dari.db.ObjectType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SearchSettings extends Content {

    private Set<ObjectType> types;
    @Embedded
    //TODO: maybe these embedded objects are wrapped so that the rules can be used in more than one search
    //private List<SearchQueryBuilder.Rule> rules = new ArrayList<>();
    private List<Search.Rule> rules = new ArrayList<>();

    public Set<ObjectType> getTypes() {
        if (types == null) {
            setTypes(new HashSet<ObjectType>());
        }
        return types;
    }

    public void setTypes(Set<ObjectType> types) {
        this.types = types;
    }

    /*public List<SearchQueryBuilder.Rule> getRules() {
        if (rules == null) {
            setRules(new ArrayList<SearchQueryBuilder.Rule>());
        }
        return rules;
    }

    public void setRules(List<SearchQueryBuilder.Rule> rules) {
        this.rules = rules;
    } */

    public List<Search.Rule> getRules() {
        return rules;
    }

    public void setRules(List<Search.Rule> rules) {
        this.rules = rules;
    }
}
