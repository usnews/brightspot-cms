package com.psddev.cms.db;

/**
 * @deprecated No replacement.
 */
@Deprecated
@Rule.DisplayName("Match Always")
public class MatchAlwaysRule extends Rule {

    @Override
    public boolean evaluate(Variation variation, Profile profile, Object object) {
        return true;
    }
}
