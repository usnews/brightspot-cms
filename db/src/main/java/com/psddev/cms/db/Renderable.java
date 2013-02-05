package com.psddev.cms.db;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.psddev.dari.db.Modification;
import com.psddev.dari.db.ObjectField;
import com.psddev.dari.db.ObjectType;
import com.psddev.dari.db.Recordable;

public interface Renderable extends Recordable {

    @FieldInternalNamePrefix("cms.renderable.")
    public static class Data extends Modification<Object> {

        public Map<String, List<String>> listLayouts;

        public Map<String, List<String>> getListLayouts() {
            if (listLayouts == null) {
                listLayouts = new HashMap<String, List<String>>();
            }
            return listLayouts;
        }

        public void setListLayouts(Map<String, List<String>> listLayouts) {
            this.listLayouts = listLayouts;
        }
    }

    public static class FieldData extends Modification<ObjectField> {

        private Map<String, List<String>> listLayouts;

        public Map<String, List<String>> getListLayouts() {
            if (listLayouts == null) {
                listLayouts = new HashMap<String, List<String>>();
            }
            return listLayouts;
        }

        public void setListLayouts(Map<String, List<String>> listLayouts) {
            this.listLayouts = listLayouts;
        }
    }

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ListLayout {
        String name();
        Class<?>[] itemClasses();
    }

    @Documented
    @Inherited
    @ObjectField.AnnotationProcessorClass(ListLayoutsProcessor.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface ListLayouts {
        String[] value() default { };
        ListLayout[] map() default { };
    }
}

class ListLayoutsProcessor implements ObjectField.AnnotationProcessor<Renderable.ListLayouts> {
    @Override
    public void process(ObjectType type, ObjectField field, Renderable.ListLayouts annotation) {
        String[] value = annotation.value();
        Renderable.ListLayout[] map = annotation.map();

        Map<String, List<String>> listLayouts = field.as(Renderable.FieldData.class).getListLayouts();

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
