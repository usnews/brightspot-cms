package com.psddev.cms.db;

import com.psddev.dari.db.ObjectField;
import com.psddev.dari.db.ObjectType;
import com.psddev.dari.db.Record;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

public abstract class RichTextElement extends Record {

    public abstract void fromAttributes(Map<String, String> attributes);

    public abstract Map<String, String> toAttributes();

    @Documented
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

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Exclusive {

    }

    @Documented
    @ObjectField.AnnotationProcessorClass(ParentProcessor.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Parent {

        Class<? extends RichTextElement> value();
    }

    private static class ParentProcessor implements ObjectField.AnnotationProcessor<Parent> {

        @Override
        public void process(ObjectType type, ObjectField field, Parent annotation) {
            Tag tagAnnotation = annotation.value().getAnnotation(Tag.class);

            if (tagAnnotation != null) {
                field.as(ToolUi.class).setRichTextElementParentTag(tagAnnotation.value());
            }
        }
    }
}
