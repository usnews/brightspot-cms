package com.psddev.cms.db;

@Rule.DisplayName("Match Never")
public class MatchNeverRule extends Rule {

    @Override
    public boolean evaluate(Variation variation, Profile profile, Object object) {
        return false;
    }
}
