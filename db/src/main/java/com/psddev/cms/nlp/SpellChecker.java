package com.psddev.cms.nlp;

import com.google.common.base.Preconditions;
import com.psddev.dari.util.ClassFinder;
import com.psddev.dari.util.Lazy;
import com.psddev.dari.util.TypeDefinition;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Spell checker.
 */
public interface SpellChecker {

    /**
     * Creates a list of dictionary names based on the given {@code baseName}
     * and {@code locales}.
     *
     * <p>This method should be used by the implementation class to select
     * the most appropriate dictionary using the same logic as resource
     * bundle loading order.</p>
     *
     * @param baseName
     *        Can't be {@code null}.
     *
     * @param locale
     *        Can't be {@code null}.
     *
     * @return Never {@code null}.
     *
     * @see ResourceBundle.Control#getCandidateLocales(String, Locale)
     */
    static List<String> createDictionaryNames(String baseName, Locale locale) {
        Preconditions.checkNotNull(baseName);
        Preconditions.checkNotNull(locale);

        ResourceBundle.Control control = ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_CLASS);
        Stream<String> names = control
                .getCandidateLocales(baseName, locale)
                .stream()
                .filter(l -> !Locale.ROOT.equals(l))
                .map(l -> control.toBundleName(baseName, l));

        if (baseName.length() == 0) {
            names = names.map(name -> name.substring(1));
        }

        return names.collect(Collectors.toList());
    }

    /**
     * Returns an instance that supports spell checking words in the given
     * {@code locale}.
     *
     * @param locale
     *        Can't be {@code null}.
     *
     * @return {@code null} if no spell checker instance supports spell
     *         checking words in the given {@code locale}.
     */
    static SpellChecker getInstance(Locale locale) {
        Preconditions.checkNotNull(locale);

        List<SpellChecker> instances = SpellCheckerPrivate.INSTANCES.get();

        return instances.stream()
                .filter(c -> c.isPreferred(locale))
                .findFirst()
                .orElseGet(() -> instances.stream()
                        .filter(c -> c.isSupported(locale))
                        .findFirst()
                        .orElse(null));
    }

    /**
     * Returns {@code true} if this spell checker supports the given
     * {@code locale}.
     *
     * @param locale
     *        Can't be {@code null}.
     */
    boolean isSupported(Locale locale);

    /**
     * Returns {@code true} if this spell checker is preferred for the given
     * {@code locale}.
     *
     * @param locale
     *        Can't be {@code null}.
     */
    boolean isPreferred(Locale locale);

    /**
     * Suggests possible correct spellings of the given {@code word} in the
     * given {@code locale}.
     *
     * @param locale
     *        Can't be {@code null}.
     *
     * @param word
     *        Can't be {@code null}.
     *
     * @return {@code null} if the given {@code word} is spelled correctly.
     */
    List<String> suggest(Locale locale, String word);
}

class SpellCheckerPrivate {

    public static final Lazy<List<SpellChecker>> INSTANCES = new Lazy<List<SpellChecker>>() {

        @Override
        protected List<SpellChecker> create() throws Exception {
            return ClassFinder.findConcreteClasses(SpellChecker.class)
                    .stream()
                    .sorted(Comparator.comparing(Class::getName))
                    .map(c -> TypeDefinition.getInstance(c).newInstance())
                    .collect(Collectors.toList());
        }
    };
}
