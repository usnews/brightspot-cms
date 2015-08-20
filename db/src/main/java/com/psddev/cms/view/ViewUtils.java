package com.psddev.cms.view;

import com.psddev.dari.db.ObjectType;
import com.psddev.dari.db.Recordable;
import com.psddev.dari.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final class ViewUtils {

    private ViewUtils() {
    }

    // Returns classes in order of input class, followed by its super classes,
    // followed by its interfaces, followed by its modification classes.
    static List<Class<?>> getAnnotatableClasses(Class<?> klass) {

        List<Class<?>> classesToCheck = new ArrayList<>();

        classesToCheck.add(klass);

        // check super classes
        Class<?> superClass = klass.getSuperclass();

        while (superClass != null) {
            if (!Object.class.equals(superClass)) {
                classesToCheck.add(superClass);
            }

            superClass = superClass.getSuperclass();
        }

        // check interfaces
        classesToCheck.addAll(Arrays.asList(klass.getInterfaces()));

        // check modification classes
        if (Recordable.class.isAssignableFrom(klass)) {
            ObjectType type = ObjectType.getInstance(klass);
            if (type != null) {
                for (String modClassName : type.getModificationClassNames()) {
                    Class<?> modClass = ObjectUtils.getClassByName(modClassName);
                    if (modClass != null) {
                        classesToCheck.add(modClass);
                    }
                }
            }
        }

        return classesToCheck;
    }
}
