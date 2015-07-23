package com.psddev.cms.tool.page;

import com.psddev.dari.util.ClassFinder;
import com.psddev.dari.util.TypeDefinition;

import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * For capturing any updates to the instances of the given {@code <T>} type
 * and sending some data back to the client.
 *
 * <p>The broadcast can be received in JavaScript using:</p>
 *
 * <p><blockquote><pre>
 *     define([ 'v3/rtc' ], function(rtc) {
 *         rtc.receive('full.RtcBroadcastClassName', function(data) {
 *             // Do something with data.
 *         });
 *     });
 * </pre></blockquote></p>
 *
 * @param <T>
 *        The type of instances that the implementation should handle.
 *
 * @since 3.1
 */
public interface RtcBroadcast<T> {

    static <T> Set<RtcBroadcast<T>> forObject(T object) {
        Set<RtcBroadcast<T>> broadcasts = new HashSet<>();

        BROADCAST:
        for (Class<?> c : ClassFinder.Static.findClasses(RtcBroadcast.class)) {
            if (c.isInterface() || Modifier.isAbstract(c.getModifiers())) {
                continue;
            }

            for (Type broadcastInterface : c.getGenericInterfaces()) {
                if (broadcastInterface instanceof ParameterizedType) {
                    ParameterizedType pt = (ParameterizedType) broadcastInterface;
                    Type rt = pt.getRawType();

                    if (rt instanceof Class
                            && RtcBroadcast.class.isAssignableFrom((Class<?>) rt)) {

                        Type[] args = pt.getActualTypeArguments();

                        if (args.length > 0) {
                            Type arg = args[0];

                            if (arg instanceof Class
                                    && !((Class<?>) arg).isInstance(object)) {
                                continue BROADCAST;

                            } else {
                                break;
                            }
                        }
                    }
                }
            }

            @SuppressWarnings("unchecked")
            Class<RtcBroadcast<T>> broadcastClass = (Class<RtcBroadcast<T>>) c;

            broadcasts.add(TypeDefinition.getInstance(broadcastClass).newInstance());
        }

        return broadcasts;
    }

    /**
     * Creates the broadcast data based on the given {@code currentUserId}
     * and {@code object}.
     *
     * @param currentUserId
     *        Can't be {@code null}.
     *
     * @param object
     *        Can't be {@code null}.
     *
     * @return If {@code null}, doesn't send anything back to the client.
     */
    Map<String, Object> create(UUID currentUserId, T object);
}
