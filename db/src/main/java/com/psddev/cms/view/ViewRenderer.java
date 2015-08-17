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
import java.util.List;

/**
 * A renderer of views.
 */
public interface ViewRenderer {

    static final Logger LOGGER = LoggerFactory.getLogger(ViewRenderer.class);

    /**
     * Renders a view, storing the result.
     *
     * @param viewMap a Map implementation that wraps the view to be rendered.
     * @return the result of rendering a view.
     */
    ViewResult render(ViewMap viewMap);

    /**
     * Creates an appropriate ViewRenderer based on the specified view.
     *
     * @param view the view from which to create a view renderer.
     * @return the view renderer for the specified view.
     */
    static ViewRenderer createRenderer(Object view) {

        if (view instanceof ViewMap) {
            view = ((ViewMap) view).getView();
        }

        // we expect a list of size 1
        List<ViewRenderer> renderers = new ArrayList<>();

        for (Class<?> viewClass : ViewUtils.getAnnotatableClasses(view.getClass())) {

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
     * Annotation for view types that specifies the ViewRenderer type that
     * should be used to render the view.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface SetClass {
        Class<? extends ViewRenderer> value();
    }

    /**
     * An annotation that is placed on the annotations of views. Specifies the
     * class that that will read the view annotation and create a ViewRenderer
     * that will be used to render the view. This is a hook to create custom
     * annotations that define ViewRenderers as opposed to the more direct
     * {@link com.psddev.cms.view.ViewRenderer.SetClass} annotation.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.ANNOTATION_TYPE)
    static @interface AnnotationProcessorClass {

        /**
         * @return the class that will process annotation on which this
         * annotation lives.
         */
        Class<? extends AnnotationProcessor<? extends Annotation>> value();
    }

    /**
     * Processes an annotation in order to create a ViewRenderer.
     *
     * @param <A> the annotation type to process.
     */
    static interface AnnotationProcessor<A extends Annotation> {

        /**
         * Creates a ViewRenderer based on the specified annotation.
         *
         * @param annotation the annotation to process.
         * @return the ViewRenderer created based on the annotation.
         */
        ViewRenderer createRenderer(A annotation);
    }
}
