package com.psddev.cms.rtc;

import com.google.common.collect.ImmutableMap;
import com.psddev.cms.db.ToolUser;
import com.psddev.cms.tool.AuthenticationFilter;
import com.psddev.dari.db.Query;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.TypeDefinition;
import com.psddev.dari.util.UuidUtils;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.AtmosphereResourceEventListener;
import org.atmosphere.cpr.AtmosphereResourceEventListenerAdapter;
import org.atmosphere.cpr.AtmosphereResponse;
import org.atmosphere.handler.OnMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

class RtcHandler extends OnMessage<Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RtcHandler.class);

    private static final ConcurrentMap<UUID, UUID> USER_IDS = new ConcurrentHashMap<>();
    private static final AtmosphereResourceEventListener DISCONNECT_LISTENER = new AtmosphereResourceEventListenerAdapter.OnDisconnect() {

        @Override
        @SuppressWarnings("unchecked")
        public void onDisconnect(AtmosphereResourceEvent event) {
            UUID sessionId = resourceToSessionId(event.getResource());

            Query.from(RtcSession.class)
                    .where("_id = ?", sessionId)
                    .deleteAll();

            Query.from(RtcEvent.class)
                    .where("cms.rtc.event.sessionId = ?", sessionId)
                    .selectAll()
                    .forEach(RtcEvent::onDisconnect);
        }
    };

    private static UUID resourceToSessionId(AtmosphereResource resource) {
        String resourceUuid = resource.uuid();
        UUID resourceUuidUuid = ObjectUtils.to(UUID.class, resourceUuid);

        if (resourceUuidUuid == null) {
            resourceUuidUuid = UuidUtils.createVersion3Uuid(resourceUuid);
        }

        return resourceUuidUuid;
    }

    @Override
    public void onOpen(AtmosphereResource resource) throws IOException {
        resource.addEventListener(DISCONNECT_LISTENER);

        ToolUser user = AuthenticationFilter.Static.getUser(resource.getRequest().wrappedRequest());

        if (user != null) {
            UUID sessionId = resourceToSessionId(resource);
            RtcSession session = new RtcSession();

            session.getState().setId(sessionId);
            session.setUserId(user.getId());
            session.save();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onMessage(AtmosphereResponse response, Object message) throws IOException {
        try {
            AtmosphereResource resource = response.resource();
            UUID sessionId = resourceToSessionId(resource);
            UUID userId = USER_IDS.computeIfAbsent(sessionId, sid -> {
                RtcSession session = Query
                        .from(RtcSession.class)
                        .where("_id = ?", sid)
                        .first();

                return session != null ? session.getUserId() : null;
            });

            if (userId == null) {
                return;
            }

            if (message instanceof RtcBroadcastMessage) {
                RtcBroadcastMessage broadcastMessage = (RtcBroadcastMessage) message;

                writeBroadcast(
                        broadcastMessage.getBroadcast(),
                        broadcastMessage.getData(),
                        userId,
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
                            writeBroadcast(broadcast, data, userId, resource));
                }

                return;
            }

            String actionClassName = ObjectUtils.to(String.class, messageJson.get("action"));

            if (!ObjectUtils.isBlank(actionClassName)) {
                createInstance(RtcAction.class, actionClassName)
                        .execute((Map<String, Object>) messageJson.get("data"), userId, sessionId);
            }

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
}
