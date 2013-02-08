package com.psddev.cms.db;

import com.psddev.dari.db.Modification;
import com.psddev.dari.db.Recordable;
import com.psddev.dari.db.State;
import com.psddev.dari.db.ObjectType;
import com.psddev.dari.db.ReferentialText;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** SEO-related classes. */
public final class Seo {

    /** @deprecated Use {@link Static#findTitle} instead. */
    @Deprecated
    public static String findTitle(Object object) {
        return Static.findTitle(object);
    }

    /** @deprecated Use {@link Static#findDescription} instead. */
    @Deprecated
    public static String findDescription(Object object) {
        return Static.findDescription(object);
    }

    /** @deprecated Use {@link Static#findKeywords} instead. */
    @Deprecated
    public static Set<String> findKeywords(Object object) {
        return Static.findKeywords(object);
    }

    /** Static utility methods. */
    public static final class Static {

        private Static() {
        }

        /** Finds the page title for the given {@code object}. */
        public static String findTitle(Object object) {
            if (object == null) {
                return null;

            } else {
                State state = State.getInstance(object);
                String title = state.as(ObjectModification.class).getTitle();
                return ObjectUtils.isBlank(title) ? state.getLabel() : title;
            }
        }

        /** Finds the meta description for the given {@code object}. */
        public static String findDescription(Object object) {
            if (object != null) {
                State state = State.getInstance(object);

                String description = state.as(ObjectModification.class).getDescription();
                if (!ObjectUtils.isBlank(description)) {
                    return description;
                }

                ObjectType type = state.getType();
                if (type != null) {
                    for (String field : type.as(TypeModification.class).getDescriptionFields()) {
                        Object fieldDescription = state.getValue(field);
                        if (fieldDescription != null) {
                            description = toMetaTagString(fieldDescription);
                            if (!description.isEmpty()) {
                                return description;
                            }
                        }
                    }
                }
            }

            return null;
        }

        /** Finds all meta keywords for the given {@code object}. */
        public static Set<String> findKeywords(Object object) {
            if (object != null) {

                Set<String> keywords = new LinkedHashSet<String>() {
                    @Override
                    public String toString() {
                        return org.apache.commons.lang.StringUtils.join(this, ",");
                    }
                };

                State state = State.getInstance(object);
                keywords.addAll(state.as(ObjectModification.class).getKeywords());

                ObjectType type = state.getType();
                if (type != null) {
                    for (String field : type.as(TypeModification.class).getKeywordsFields()) {
                        Iterable<Object> fieldKeywords = ObjectUtils.to(Iterable.class, state.getValue(field));
                        if (fieldKeywords != null) {
                            for (Object item : fieldKeywords) {
                                if (item != null) {
                                    keywords.add(toMetaTagString(item));
                                }
                            }
                        }
                    }
                }

                if (!keywords.isEmpty()) {
                    return keywords;
                }
            }

            return null;
        }

        /**
         * Converts the given {@code object} to a plain string that's usable
         * inside a meta tag.
         */
        private static String toMetaTagString(Object object) {
            String string;

            if (object instanceof ReferentialText) {
                StringBuilder sb = new StringBuilder();
                for (Object item : (ReferentialText) object) {
                    if (item instanceof String) {
                        sb.append((String) item);
                    }
                }
                string = StringUtils.unescapeHtml(
                        StringUtils.replaceAll(
                                sb.toString(), "<[^>]+>", ""));

            } else if (object instanceof Recordable) {
                string = ((Recordable) object).getState().getLabel();

            } else {
                string = object.toString();
            }

            return string == null ? "" : string.trim();
        }
    }

    public enum RobotsValue {
        NOINDEX,
        NOFOLLOW,
        NOARCHIVE,
        NOSNIPPET,
        NOODP,
        NOTRANSLATE,
        NOIMAGEINDEX;
    }

    /** Modification of an object for specifying SEO-related overrides. */
    @Modification.FieldInternalNamePrefix("cms.seo.")
    public static final class ObjectModification extends Modification<Object> {

        private String title;
        private String description;
        private Set<String> keywords;

        @ToolUi.NoteHtml("See <a href=\"https://developers.google.com/webmasters/control-crawl-index/docs/robots_meta_tag\" target=\"_blank\">robots meta tag documentation</a> for more information.")
        private Set<RobotsValue> robots;

        public ObjectModification() {
        }

        /** Returns the page title override. */
        public String getTitle() {
            return title;
        }

        /** Sets the page title override. */
        public void setTitle(String title) {
            this.title = title;
        }

        /** Returns the meta description override. */
        public String getDescription() {
            return description;
        }

        /** Sets the meta description override. */
        public void setDescription(String description) {
            this.description = description;
        }

        /** Returns the meta keywords override. */
        public Set<String> getKeywords() {
            if (keywords == null) {
                keywords = new LinkedHashSet<String>();
            }
            return keywords;
        }

        /** Sets the meta keywords override. */
        public void setKeywords(Set<String> keywords) {
            this.keywords = keywords;
        }

        public Set<RobotsValue> getRobots() {
            if (robots == null) {
                robots = new LinkedHashSet<RobotsValue>();
            }
            return robots;
        }

        public void setRobots(Set<RobotsValue> robots) {
            this.robots = robots;
        }
    }

    /**
     * Modification of a type for specifying various fields that are
     * checked to get SEO-related data.
     */
    public static final class TypeModification extends Modification<ObjectType> {

        private List<String> cms$seo$descriptionFields;
        private List<String> cms$seo$keywordsFields;

        public TypeModification() {
        }

        /**
         * Returns the fields that are checked to get the meta
         * description.
         */
        public List<String> getDescriptionFields() {
            if (cms$seo$descriptionFields == null) {
                cms$seo$descriptionFields = new ArrayList<String>();
            }
            return cms$seo$descriptionFields;
        }

        /** Sets the fields that are checked to get the meta description. */
        public void setDescriptionFields(List<String> descriptionFields) {
            this.cms$seo$descriptionFields = descriptionFields;
        }

        /** Returns the fields that are checked to get the meta keywords. */
        public List<String> getKeywordsFields() {
            if (cms$seo$keywordsFields == null) {
                cms$seo$keywordsFields = new ArrayList<String>();
            }
            return cms$seo$keywordsFields;
        }

        /** Sets the fields that are checked to get the meta keywords. */
        public void setKeywordsFields(List<String> keywordsFields) {
            this.cms$seo$keywordsFields = keywordsFields;
        }
    }

    /**
     * Specifies an array of fields that are checked to get the meta
     * description from an instance of the target type.
     */
    @Documented
    @Inherited
    @ObjectType.AnnotationProcessorClass(DescriptionFieldsProcessor.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface DescriptionFields {
        String[] value();
    }

    private static class DescriptionFieldsProcessor implements ObjectType.AnnotationProcessor<DescriptionFields> {
        @Override
        public void process(ObjectType type, DescriptionFields annotation) {
            type.as(TypeModification.class).setDescriptionFields(Arrays.asList(annotation.value()));
        }
    }

    /**
     * Specifies an array of fields that are checked to get the meta
     * keywords from an instance of the target type.
     */
    @Documented
    @Inherited
    @ObjectType.AnnotationProcessorClass(KeywordsFieldsProcessor.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface KeywordsFields {
        String[] value();
    }

    private static class KeywordsFieldsProcessor implements ObjectType.AnnotationProcessor<KeywordsFields> {
        @Override
        public void process(ObjectType type, KeywordsFields annotation) {
            type.as(TypeModification.class).setKeywordsFields(Arrays.asList(annotation.value()));
        }
    }
}
