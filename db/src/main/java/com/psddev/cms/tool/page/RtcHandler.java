package com.psddev.cms.tool.page;

import com.google.common.collect.ImmutableMap;
import com.psddev.cms.db.ToolUser;
import com.psddev.cms.tool.AuthenticationFilter;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.TypeDefinition;
import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceSession;
import org.atmosphere.cpr.AtmosphereResourceSessionFactory;
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

    private static final String CURRENT_USER_ID_ATTRIBUTE = "currentUserId";
    private static final String ACTIONS_ATTRIBUTE = "actions";

    private AtmosphereResourceSessionFactory sessionFactory;

    public RtcHandler(AtmosphereResourceSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void onOpen(AtmosphereResource resource) throws IOException {
        ToolUser user = AuthenticationFilter.Static.getUser(resource.getRequest().wrappedRequest());

        if (user != null) {
            sessionFactory.getSession(resource).setAttribute(CURRENT_USER_ID_ATTRIBUTE, user.getId());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onMessage(AtmosphereResponse response, Object message) throws IOException {
        try {
            AtmosphereResource resource = response.resource();
            AtmosphereResourceSession session = sessionFactory.getSession(resource);
            UUID currentUserId = (UUID) session.getAttribute(CURRENT_USER_ID_ATTRIBUTE);

            if (currentUserId == null) {
                return;
            }

            if (message instanceof RtcBroadcastMessage) {
                RtcBroadcastMessage broadcastMessage = (RtcBroadcastMessage) message;

                writeBroadcast(
                        broadcastMessage.getBroadcast(),
                        broadcastMessage.getData(),
                        currentUserId,
                        resource);

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
                    RtcBroadcast.forEachBroadcast(object, (broadcast, data) ->
                            writeBroadcast(broadcast, data, currentUserId, resource));
                }

                return;
            }

            String actionClassName = ObjectUtils.to(String.class, messageJson.get("action"));
            AtmosphereRequest request = response.request();
            Map<String, RtcAction> actions = (Map<String, RtcAction>) session.getAttribute(ACTIONS_ATTRIBUTE);

            if (actions == null) {
                actions = new HashMap<>();
                session.setAttribute(ACTIONS_ATTRIBUTE, actions);
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

    private void writeBroadcast(RtcBroadcast<Object> broadcast, Map<String, Object> data, UUID currentUserId, AtmosphereResource resource) {
        if (broadcast.shouldBroadcast(data, currentUserId)) {
            resource.write(ObjectUtils.toJson(ImmutableMap.of(
                    "broadcast", broadcast.getClass().getName(),
                    "data", data)));
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
        AtmosphereResource resource = response.resource();
        AtmosphereResourceSession session = sessionFactory.getSession(resource);
        Map<String, RtcAction> actions = (Map<String, RtcAction>) session.getAttribute(ACTIONS_ATTRIBUTE);

        if (actions != null) {
            actions.values().forEach(RtcAction::destroy);
        }
    }
}
