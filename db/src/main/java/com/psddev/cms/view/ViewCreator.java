package com.psddev.cms.view;

import com.psddev.dari.db.ObjectType;
import com.psddev.dari.db.Recordable;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.TypeDefinition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public interface ViewCreator<M, V> {

    static final Logger LOGGER = LoggerFactory.getLogger(ViewCreator.class);

    V createView(M model, ViewContext context);

    static <M, V> ViewCreator<M, V> findCreator(M model, Class<V> viewClass) {

        if (model == null) {
            return null;
        }

        Class<?> modelClass = model.getClass();

        // we only expect to find one.
        List<Class<? extends ViewCreator>> creatorClasses = new ArrayList<>();

        Set<Class<? extends ViewCreator>> allCreatorClasses = new LinkedHashSet<>();

        for (Class<?> classToCheck : getClassesToCheck(modelClass)) {

            MainViewMapping mainViewMapping = classToCheck.getAnnotation(MainViewMapping.class);
            if (mainViewMapping != null) {
                allCreatorClasses.add(mainViewMapping.value());
            }

            allCreatorClasses.addAll(
                    Arrays.stream(classToCheck.getAnnotationsByType(ViewMapping.class))
                            .map(ViewMapping::value)
                            .collect(Collectors.toList()));
        }

        allCreatorClasses.forEach(creatorClass -> {

            Class<?> declaredViewClass = getGenericViewTypeArgument(creatorClass);
            Class<?> declaredModelClass = getGenericModelTypeArgument(creatorClass);

            if (declaredViewClass != null && viewClass.isAssignableFrom(declaredViewClass)
                    && declaredModelClass != null && declaredModelClass.isAssignableFrom(modelClass)) {

                creatorClasses.add(creatorClass);
            }
        });

        if (!creatorClasses.isEmpty()) {

            if (creatorClasses.size() == 1) {

                Class<? extends ViewCreator> creatorClass = creatorClasses.get(0);

                try {
                    @SuppressWarnings("unchecked")
                    ViewCreator<M, V> creator = (ViewCreator<M, V>) TypeDefinition.getInstance(creatorClass).newInstance();

                    return creator;
                } catch (Exception e) {
                    LOGGER.warn("Unable to create view creator of type [" + creatorClass.getName() + "]");
                }

            } else {
                LOGGER.warn("Found multiple conflicting mappings... " + creatorClasses);
            }
        }

        return null;
    }

    // returns classes in order of input class, followed by its super classes, followed by its interfaces.
    static List<Class<?>> getClassesToCheck(Class<?> klass) {

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

    static Class<?> getGenericModelTypeArgument(Class<? extends ViewCreator> viewCreatorClass) {
        return getGenericTypeArgument(viewCreatorClass, 0);
    }

    static Class<?> getGenericViewTypeArgument(Class<? extends ViewCreator> viewCreatorClass) {
        return getGenericTypeArgument(viewCreatorClass, 1);
    }

    static Class<?> getGenericTypeArgument(Class<? extends ViewCreator> viewCreatorClass, int argIndex) {

        for (Type interfaceClass : viewCreatorClass.getGenericInterfaces()) {

            if (interfaceClass instanceof ParameterizedType) {

                ParameterizedType pt = (ParameterizedType) interfaceClass;

                Type rt = pt.getRawType();

                if (rt instanceof Class
                        && ViewCreator.class.isAssignableFrom((Class<?>) rt)) {

                    Type[] args = pt.getActualTypeArguments();

                    if (args.length > argIndex) {
                        Type arg = args[argIndex];

                        if (arg instanceof Class) {
                            return (Class<?>) arg;
                        }
                    }
                }
            }
        }

        return null;
    }
}
