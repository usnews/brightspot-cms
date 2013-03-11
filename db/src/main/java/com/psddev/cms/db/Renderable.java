package com.psddev.cms.db;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.psddev.dari.db.ObjectField;
import com.psddev.dari.db.ObjectType;
import com.psddev.dari.db.Recordable;

/** @deprecated Use {@link Renderer} instead. */
@Deprecated
public interface Renderable extends Recordable {

    /** @deprecated Use {@link Renderer.ListLayout} instead. */
    @Deprecated
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ListLayout {
        String name();
        Class<?>[] itemClasses();
    }

    /** @deprecated Use {@link Renderer.ListLayouts} instead. */
    @Deprecated
    @Documented
    @Inherited
    @ObjectField.AnnotationProcessorClass(RenderableListLayoutsProcessor.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface ListLayouts {
        String[] value() default { };
        ListLayout[] map() default { };
    }
}

@Deprecated
class RenderableListLayoutsProcessor implements ObjectField.AnnotationProcessor<Renderable.ListLayouts> {
    @Override
    public void process(ObjectType type, ObjectField field, Renderable.ListLayouts annotation) {
        String[] value = annotation.value();
        Renderable.ListLayout[] map = annotation.map();

        Map<String, List<String>> listLayouts = field.as(Renderer.FieldData.class).getListLayouts();

        for (String layoutName : value) {
            listLayouts.put(layoutName, new ArrayList<String>());
        }

        for (Renderable.ListLayout layout : map) {
            List<String> layoutItems = new ArrayList<String>();
            listLayouts.put(layout.name(), layoutItems);

            for (Class<?> itemClass : layout.itemClasses()) {
                layoutItems.add(itemClass.getName());
            }
        }
    }
}
