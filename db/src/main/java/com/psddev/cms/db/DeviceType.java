package com.psddev.cms.db;

/**
 * @deprecated No replacement.
 */
@Deprecated
public enum DeviceType {

    ANDROID("Android", new Evaluator() {
        @Override
        public boolean evaluate(Profile profile) {
            return profile.isUserAgentAndroid();
        }
    }),

    ANY_CHROME("Any Device using Chrome", new Evaluator() {
        @Override
        public boolean evaluate(Profile profile) {
            return profile.isUserAgentChrome();
        }
    }),

    ANY_FIREFOX("Any Device using Firefox", new Evaluator() {
        @Override
        public boolean evaluate(Profile profile) {
            return profile.isUserAgentFirefox();
        }
    }),

    ANY_MSIE("Any Device using Microsoft Internet Explorer", new Evaluator() {
        @Override
        public boolean evaluate(Profile profile) {
            return profile.isUserAgentMsie();
        }
    }),

    ANY_SAFARI("Any Device using Safari", new Evaluator() {
        @Override
        public boolean evaluate(Profile profile) {
            return profile.isUserAgentSafari();
        }
    }),

    IPAD("iPad", new Evaluator() {
        @Override
        public boolean evaluate(Profile profile) {
            return profile.isUserAgentIpad();
        }
    }),

    IPHONE("iPhone", new Evaluator() {
        @Override
        public boolean evaluate(Profile profile) {
            return profile.isUserAgentIphone();
        }
    }),

    MAC_CHROME("Mac OS using Chrome", new Evaluator() {
        @Override
        public boolean evaluate(Profile profile) {
            return profile.isUserAgentMac() && profile.isUserAgentChrome();
        }
    }),

    MAC_FIREFOX("Mac OS using Firefox", new Evaluator() {
        @Override
        public boolean evaluate(Profile profile) {
            return profile.isUserAgentMac() && profile.isUserAgentFirefox();
        }
    }),

    MAC_SAFARI("Mac OS using Safari", new Evaluator() {
        @Override
        public boolean evaluate(Profile profile) {
            return profile.isUserAgentMac() && profile.isUserAgentSafari();
        }
    }),

    WINDOWS_CHROME("Windows using Chrome", new Evaluator() {
        @Override
        public boolean evaluate(Profile profile) {
            return profile.isUserAgentWindows() && profile.isUserAgentChrome();
        }
    }),

    WINDOWS_FIREFOX("Windows using Firefox", new Evaluator() {

        @Override
        public boolean evaluate(Profile profile) {
            return profile.isUserAgentWindows() && profile.isUserAgentFirefox();
        }
    }),

    WINDOWS_MSIE("Windows using Microsoft Internet Explorer", new Evaluator() {
        @Override
        public boolean evaluate(Profile profile) {
            return profile.isUserAgentWindows() && profile.isUserAgentMsie();
        }
    }),

    WINDOWS_SAFARI("Windows using Safari", new Evaluator() {
        @Override
        public boolean evaluate(Profile profile) {
            return profile.isUserAgentWindows() && profile.isUserAgentSafari();
        }
    });

    private final String displayName;
    private final Evaluator evaluator;

    private DeviceType(String displayName, Evaluator evaluator) {
        this.displayName = displayName;
        this.evaluator = evaluator;
    }

    public boolean evaluate(Profile profile) {
        return evaluator.evaluate(profile);
    }

    @Override
    public String toString() {
        return displayName;
    }

    private static interface Evaluator {

        public boolean evaluate(Profile profile);
    }
}
