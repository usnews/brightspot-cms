package com.psddev.cms.tool.page;

import com.google.common.collect.ImmutableMap;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.TypeDefinition;
import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResponse;
import org.atmosphere.handler.OnMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

class RtcHandler extends OnMessage<Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RtcHandler.class);

    @Override
    @SuppressWarnings("unchecked")
    public void onMessage(AtmosphereResponse response, Object message) throws IOException {
        try {
            AtmosphereResource resource = response.resource();
            UUID currentUserId = RtcFilter.getCurrentUserId(resource);

            if (!(message instanceof String)) {
                writeBroadcast(message, currentUserId, resource);
                return;
            }

            Map<String, Object> messageJson = (Map<String, Object>) ObjectUtils.fromJson((String) message);

            if (!resource.uuid().equals(ObjectUtils.to(String.class, messageJson.get("resource")))) {
                return;
            }

            String stateClassName = ObjectUtils.to(String.class, messageJson.get("state"));

            if (!ObjectUtils.isBlank(stateClassName)) {
                RtcState state = createInstance(RtcState.class, stateClassName);

                for (Object object : state.create((Map<String, Object>) messageJson.get("data"))) {
                    writeBroadcast(object, currentUserId, resource);
                }

                return;
            }

            String actionClassName = ObjectUtils.to(String.class, messageJson.get("action"));
            AtmosphereRequest request = response.request();
            String currentUserIdString = currentUserId.toString();
            Map<String, RtcAction> actions = (Map<String, RtcAction>) request.getAttribute(currentUserIdString);

            if (actions == null) {
                actions = new HashMap<>();
                request.setAttribute(currentUserIdString, actions);
            }

            RtcAction action = actions.get(actionClassName);

            if (action == null) {
                action = createInstance(RtcAction.class, actionClassName);

                action.initialize(currentUserId);
                actions.put(actionClassName, action);
            }

            action.execute((Map<String, Object>) messageJson.get("data"));

        } catch (Exception error) {
            error.printStackTrace();
        }
    }

    private void writeBroadcast(Object object, UUID currentUserId, AtmosphereResource resource) {
        for (RtcBroadcast<Object> broadcast : RtcBroadcast.forObject(object)) {
            Map<String, Object> data = broadcast.create(currentUserId, object);

            if (data != null) {
                resource.write(ObjectUtils.toJson(ImmutableMap.of(
                        "broadcast", broadcast.getClass().getName(),
                        "data", data)));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T createInstance(Class<T> returnClass, String className) {
        Class<?> c = ObjectUtils.getClassByName(className);

        if (c == null) {
            throw new IllegalArgumentException(String.format(
                    "[%s] isn't a valid class name!",
                    className));

        } else if (!returnClass.isAssignableFrom(c)) {
            throw new IllegalArgumentException(String.format(
                    "[%s] isn't assignable from [%s]!",
                    returnClass.getName(),
                    c.getName()));
        }

        return (T) TypeDefinition.getInstance(c).newInstance();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onDisconnect(AtmosphereResponse response) throws IOException {
        AtmosphereRequest request = response.request();
        UUID userId = RtcFilter.getCurrentUserId(response.resource());
        Map<String, RtcAction> actions = (Map<String, RtcAction>) request.getAttribute(userId.toString());

        if (actions != null) {
            actions.values().forEach(RtcAction::destroy);
        }
    }
}
