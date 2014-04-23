package com.psddev.cms.db;

import java.util.ArrayList;
import java.util.List;

public class SearchSettings extends Content {

    private List<SearchQueryBuilder.Rule> rules = new ArrayList<SearchQueryBuilder.Rule>();
    /*@Embedded
    private List<QueryRule> rules = new ArrayList<QueryRule>();
    */

    public List<SearchQueryBuilder.Rule> getRules() {
        return rules;
    }

    public void setRules(List<SearchQueryBuilder.Rule> rules) {
        this.rules = rules;
    }

    /*public List<SearchQueryBuilder.Rule> getRules() {
        List<SearchQueryBuilder.Rule> ruleList = new ArrayList<SearchQueryBuilder.Rule>();
        for(QueryRule queryRule:this.rules){
            ruleList.add(queryRule.getRule());
        }
        return ruleList;
    }

    public void setRules(List<SearchQueryBuilder.Rule> rules) {
        if(this.rules == null) {
            this.rules = new ArrayList<QueryRule>();
        }
        for(SearchQueryBuilder.Rule rule:rules){
            this.rules.add(new QueryRule(rule));
        }
    }

    public static class QueryRule extends Record {

        SearchQueryBuilder.Rule rule;

        public QueryRule(){}

        public QueryRule(SearchQueryBuilder.Rule rule){
            this.rule = rule;
        }

        public String getLabel(){
            if(!ObjectUtils.isBlank(rule)) {
                return rule.getClass().getSimpleName();
            }
            return "";
        }

        public SearchQueryBuilder.Rule getRule() {
            return rule;
        }

        public void setRule(SearchQueryBuilder.Rule rule) {
            this.rule = rule;
        }
    }*/
}
