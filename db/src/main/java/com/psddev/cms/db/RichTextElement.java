package com.psddev.cms.db;

import com.psddev.dari.db.ObjectField;
import com.psddev.dari.db.ObjectType;
import com.psddev.dari.db.Record;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class RichTextElement extends Record {

    public abstract void fromAttributes(Map<String, String> attributes);

    public abstract Map<String, String> toAttributes();

    @Documented
    @ObjectType.AnnotationProcessorClass(TagProcessor.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Tag {

        String value();
        boolean block() default false;
        boolean empty() default false;
        boolean root() default false;
        Class<?>[] children() default { };
        String menu() default "";
        String tooltip() default "";
    }

    private static class TagProcessor implements ObjectType.AnnotationProcessor<Tag> {

        @Override
        public void process(ObjectType type, Tag annotation) {
            type.as(ToolUi.class).setRichTextElementTagName(annotation.value());
        }
    }

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Exclusive {

    }

    @Documented
    @ObjectField.AnnotationProcessorClass(TagsProcessor.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Tags {

        Class<?>[] value();
    }

    private static class TagsProcessor implements ObjectField.AnnotationProcessor<Tags> {

        @Override
        public void process(ObjectType type, ObjectField field, Tags annotation) {
            field.as(ToolUi.class).setRichTextElementClassNames(
                    Stream.of(annotation.value())
                            .map(Class::getName)
                            .collect(Collectors.toCollection(LinkedHashSet::new)));
        }
    }
}
