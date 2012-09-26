package com.psddev.cms.db;

import java.util.ArrayList;
import java.util.List;

@Rule.DisplayName("Match Any")
public class MatchAnyRule extends Rule {

    private List<Rule> rules;

    public List<Rule> getRules() {
        if (rules == null) {
            rules = new ArrayList<Rule>();
        }
        return rules;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }

    // --- Rule support ---

    @Override
    public boolean evaluate(Variation variation, Profile profile, Object object) {
        for (Rule rule : getRules()) {
            if (rule.evaluate(variation, profile, object)) {
                return true;
            }
        }
        return false;
    }
}
