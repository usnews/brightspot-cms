package com.psddev.cms.view;

import com.psddev.dari.db.ObjectType;
import com.psddev.dari.db.Recordable;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.StringUtils;
import com.psddev.dari.util.TypeDefinition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Creator of view objects from a model object.
 *
 * @param <M> the model type to create the view from.
 * @param <V> the view type to create.
 */
public interface ViewCreator<M, V> {

    /**
     * Creates a view based on the specified model in the current view context.
     *
     * @param model the backing model for the view to be created.
     * @param context the current view context.
     * @return the newly created view.
     */
    V createView(M model, ViewContext context);

    /**
     * Finds the view creator that should be used to create views of the
     * specified {@code viewClass} type based on the specified {@code model}.
     *
     * @param model the model from which the view should be created.
     * @param viewClass the class of the view that should be created.
     * @param <M> the model type the view creator creates from.
     * @param <V> the view type the view creator creates.
     * @return the view creator of model to view.
     */
    static <M, V> ViewCreator<M, V> findCreator(M model, Class<V> viewClass) {

        if (model == null) {
            return null;
        }

        Class<?> modelClass = model.getClass();

        // we only expect to find one.
        List<Class<? extends ViewCreator>> creatorClasses = new ArrayList<>();

        Set<Class<? extends ViewCreator>> allCreatorClasses = new LinkedHashSet<>();

        for (Class<?> annotatableClass : Static.getAnnotatableClasses(modelClass)) {

            allCreatorClasses.addAll(
                    Arrays.stream(annotatableClass.getAnnotationsByType(ViewMapping.class))
                            .map(ViewMapping::value)
                            .collect(Collectors.toList()));
        }

        allCreatorClasses.forEach(creatorClass -> {

            Class<?> declaredViewClass = Static.getGenericViewTypeArgument(creatorClass);
            Class<?> declaredModelClass = Static.getGenericModelTypeArgument(creatorClass);

            if (((declaredViewClass != null && viewClass.isAssignableFrom(declaredViewClass)) || viewClass.isAssignableFrom(creatorClass))
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
                    Static.LOGGER.warn("Unable to create view creator of type [" + creatorClass.getName() + "]");
                }

            } else {
                Static.LOGGER.warn("Found multiple conflicting view creator mappings... " + creatorClasses);
            }
        }

        return null;
    }

    /**
     * Private static utility methods
     */
    static final class Static {

        private static final Logger LOGGER = LoggerFactory.getLogger(ViewCreator.class);

        private Static() {
        }

        /**
         * Returns classes in order of input class, followed by its super
         * classes, followed by its interfaces, followed by its modification
         * classes.
         *
         * @param klass the class to traverse.
         * @return the list of classes that should be checked for annotations.
         */
        private static List<Class<?>> getAnnotatableClasses(Class<?> klass) {

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

        /**
         * Finds the actual view class generic of the ViewCreator interface in
         * context of the specified view creator class.
         *
         * @param viewCreatorClass the view creator class to inspect.
         * @return the view class generic for the given view creator class.
         */
        private static Class<?> getGenericModelTypeArgument(Class<? extends ViewCreator> viewCreatorClass) {
            return Static.getGenericTypeArgumentClass(viewCreatorClass, ViewCreator.class, 0);
        }

        /**
         * Finds the actual model class generic of the ViewCreator interface in
         * context of the specified view creator class.
         *
         * @param viewCreatorClass the view creator class to inspect.
         * @return the model class generic for the given view creator class.
         */
        private static Class<?> getGenericViewTypeArgument(Class<? extends ViewCreator> viewCreatorClass) {
            return Static.getGenericTypeArgumentClass(viewCreatorClass, ViewCreator.class, 1);
        }

        /**
         *
         * @param clazz the class that extends/implements {@code baseClass} whose
         *              actual generic type argument class should be fetched.
         * @param baseClass the class defining the generics.
         * @param baseClassGenericTypeArgumentIndex the index of the generic to fetch.
         * @return the generic type argument class for the given {@code clazz}.
         */
        private static Class<?> getGenericTypeArgumentClass(Class<?> clazz, Class<?> baseClass, int baseClassGenericTypeArgumentIndex) {

            List<ClassInfo> hierarchy = getClassInfoHierarchy(clazz, baseClass);

            int argIndex = baseClassGenericTypeArgumentIndex;

            for (ClassInfo classInfo : hierarchy) {

                List<Type> superClassTypeVars = classInfo.getSuperClassTypeParameters();

                if (superClassTypeVars.size() > argIndex) {

                    Type superClassTypeVar = superClassTypeVars.get(argIndex);

                    if (superClassTypeVar instanceof Class) {
                        return (Class<?>) superClassTypeVar;

                    } else if (superClassTypeVar instanceof TypeVariable) {

                        String superClassTypeVarName = ((TypeVariable) superClassTypeVar).getName();

                        int index = 0;
                        for (TypeVariable classTypeVar : classInfo.getClassTypeParameters()) {

                            if (superClassTypeVarName.equals(classTypeVar.getName())) {
                                argIndex = index;
                                break;
                            }
                            index++;
                        }
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            }

            return null;
        }

        /**
         * Returns the list of ClassInfo objects in the class hierarchy from
         * {@code clazz} class to {@code baseClass} class, where the first
         * element in the list is {@code clazz}. If the two classes are not in
         * the same hierarchy, an empty list returned
         *
         * @param clazz the bottom starting class in the hierarchy.
         * @param baseClass the top most parent class or interface in the hierarchy.
         * @return the ClassInfo hierarchy list between the two class arguments.
         */
        private static List<ClassInfo> getClassInfoHierarchy(Class<?> clazz, Class<?> baseClass) {

            List<Class<?>> classes = getClassHierarchy(clazz, baseClass);

            List<Class<?>> classesReversed = new ArrayList<>(classes);
            Collections.reverse(classesReversed);
            classesReversed.add(baseClass);

            List<ClassInfo> hierarchy = new ArrayList<>();

            Class<?> prevClass = null;
            for (Class<?> nextClass : classesReversed) {

                if (prevClass != null) {
                    Type superType = getGenericSuperType(prevClass, nextClass);

                    if (superType != null) {
                        hierarchy.add(new ClassInfo(prevClass, superType));
                    }
                }

                prevClass = nextClass;
            }

            Collections.reverse(hierarchy);

            return hierarchy;
        }

        /**
         * Returns the list of classes in the class hierarchy from {@code clazz}
         * class to {@code baseClass} class, where the first element in the
         * list is {@code clazz}. If the two classes are not in the same
         * hierarchy, {@code null} is returned.
         *
         * @param clazz the bottom starting class in the hierarchy.
         * @param baseClass the top most parent class or interface in the hierarchy.
         * @return the class hierarchy list between the two class arguments.
         */
        private static List<Class<?>> getClassHierarchy(Class<?> clazz, Class<?> baseClass) {

            if (Object.class.equals(clazz)) {
                return null;

            } else if (baseClass.equals(clazz)) {
                return new ArrayList<>();
            }

            List<Class<?>> superTypes = new ArrayList<>();

            Class<?> superClass = clazz.getSuperclass();
            if (superClass != null) {
                superTypes.add(superClass);
            }

            List<Class<?>> interfaceClasses = Arrays.asList(clazz.getInterfaces());
            superTypes.addAll(interfaceClasses);

            for (Class<?> superType : superTypes) {

                List<Class<?>> hierarchy = getClassHierarchy(superType, baseClass);

                if (hierarchy != null) {

                    List<Class<?>> clazzes = new ArrayList<>();

                    clazzes.addAll(hierarchy);
                    clazzes.add(clazz);

                    return clazzes;
                }
            }

            return null;
        }

        /**
         * Returns the generic super type of the for the given {@code clazz}
         * matching the specified {@code superType}.
         *
         * @param clazz the class containing a generic super type.
         * @param superType the non-generic superType of {@code clazz}.
         * @return the generic super type of {@code clazz}.
         */
        private static Type getGenericSuperType(Class<?> clazz, Class<?> superType) {

            if (superType.isInterface()) {
                for (Type genericInterface : clazz.getGenericInterfaces()) {

                    if (genericInterface instanceof Class) {
                        if (superType.equals(genericInterface)) {
                            return genericInterface;
                        }

                    } else if (genericInterface instanceof ParameterizedType) {

                        Type rawType = ((ParameterizedType) genericInterface).getRawType();
                        if (superType.equals(rawType)) {
                            return genericInterface;
                        }
                    }
                }

            } else {
                return clazz.getGenericSuperclass();
            }

            return null;
        }

        /**
         * Represents a class and one if its generic super classes or interfaces.
         */
        private static final class ClassInfo {

            private Class<?> clazz;

            private Type genericSuperClazz;

            private ClassInfo(Class<?> clazz, Type genericSuperClazz) {
                this.clazz = clazz;
                this.genericSuperClazz = genericSuperClazz;
            }

            private List<TypeVariable> getClassTypeParameters() {
                return Arrays.asList(clazz.getTypeParameters());
            }

            private List<Type> getSuperClassTypeParameters() {
                if (genericSuperClazz instanceof ParameterizedType) {
                    ParameterizedType pt = (ParameterizedType) genericSuperClazz;
                    return Arrays.asList(pt.getActualTypeArguments());
                } else {
                    return new ArrayList<>();
                }
            }

            @Override
            public String toString() {

                List<TypeVariable> classTypeParameters = getClassTypeParameters();

                String classTypeParamsString = StringUtils.join(classTypeParameters.stream().map(Object::toString).collect(Collectors.toList()), ", ");
                if (!StringUtils.isBlank(classTypeParamsString)) {
                    classTypeParamsString = "<" + classTypeParamsString + ">";
                }

                return clazz + classTypeParamsString + " <-- " + genericSuperClazz;
            }
        }
    }
}
