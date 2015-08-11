package com.psddev.cms.view;

import com.psddev.dari.util.TypeDefinition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface ViewRenderer {

    static final Logger LOGGER = LoggerFactory.getLogger(ViewRenderer.class);

    ViewResult renderObject(ViewMap viewMap);

    // TODO: move into the find renderer method.
    static List<Class<?>> getClassesToCheck(Class<?> viewClass) {

        List<Class<?>> classesToCheck = new ArrayList<>();

        classesToCheck.add(viewClass);

        // check super classes
        Class<?> superClass = viewClass.getSuperclass();

        while (superClass != null) {
            if (!Object.class.equals(superClass)) {
                classesToCheck.add(superClass);
            }

            superClass = superClass.getSuperclass();
        }

        // check interfaces
        classesToCheck.addAll(Arrays.asList(viewClass.getInterfaces()));

        return classesToCheck;
    }

    static ViewRenderer createRenderer(Object view) {

        if (view instanceof ViewMap) {
            view = ((ViewMap) view).getView();
        }

        // we expect a list of size 1
        List<ViewRenderer> renderers = new ArrayList<>();

        for (Class<?> viewClass : getClassesToCheck(view.getClass())) {

            SetClass rendererAnnotation = viewClass.getAnnotation(SetClass.class);
            if (rendererAnnotation != null) {
                Class<? extends ViewRenderer> rendererClass = rendererAnnotation.value();

                if (rendererClass != null) {

                    try {
                        ViewRenderer renderer = TypeDefinition.getInstance(rendererClass).newInstance();

                        if (renderer != null) {
                            renderers.add(renderer);
                        }

                    } catch (Exception e) {
                        LOGGER.warn("Unable to create instance of renderer of type ["
                                        + rendererClass.getName() + "]");
                    }
                }
            }

            // check for annotation processors.
            for (Annotation viewAnnotation : viewClass.getAnnotations()) {

                Class<?> annotationClass = viewAnnotation.annotationType();

                ViewRenderer.AnnotationProcessorClass annotation = annotationClass.getAnnotation(
                        ViewRenderer.AnnotationProcessorClass.class);

                if (annotation != null) {

                    Class<? extends ViewRenderer.AnnotationProcessor<? extends Annotation>> annotationProcessorClass = annotation.value();

                    if (annotationProcessorClass != null) {

                        @SuppressWarnings("unchecked")
                        ViewRenderer.AnnotationProcessor<Annotation> annotationProcessor
                                = (ViewRenderer.AnnotationProcessor<Annotation>) TypeDefinition.getInstance(annotationProcessorClass).newInstance();

                        ViewRenderer renderer = annotationProcessor.createRenderer(viewAnnotation);

                        if (renderer != null) {
                            renderers.add(renderer);
                        }
                    }
                }
            }
        }

        if (!renderers.isEmpty()) {

            if (renderers.size() == 1) {
                return renderers.get(0);

            } else {
                LOGGER.warn("Found multiple renderers for view of type [" + view.getClass().getName() + "]!");
                return null;
            }

        } else {
            return null;
        }
    }

    /**
     * Annotation for view types that specifies the Renderer type that should be
     * used to render the view.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface SetClass {
        Class<? extends ViewRenderer> value();
    }

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.ANNOTATION_TYPE)
    static @interface AnnotationProcessorClass {
        Class<? extends AnnotationProcessor<? extends Annotation>> value();
    }

    static interface AnnotationProcessor<A extends Annotation> {

        ViewRenderer createRenderer(A annotation);
    }
}
