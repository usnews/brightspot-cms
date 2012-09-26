package com.psddev.cms.tool;

import com.psddev.cms.db.ToolUser;

import com.psddev.dari.db.State;
import com.psddev.dari.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

/** Widget at a remote location that is accessible through web APIs. */
public class RemoteWidget extends Widget {

    public static final String OBJECT_PARAMETER = "_object";
    public static final String USER_ID_PARAMETER = "_userId";
    public static final String WIDGET_PARAMETER = "_widget";

    private String displayApi;
    private String updateApi;

    /** Creates a JSON string that represents the given {@code object}. */
    private static String getJson(Object object) {
        State state = State.getInstance(object);
        Map<String, Object> values = state.getSimpleValues();
        values.put("_id", state.getId().toString());
        values.put("_typeId", state.getTypeId().toString());
        return ObjectUtils.toJson(values);
    }

    /** Displays the given {@code widget}. */
    public static String displayWidget(Widget widget, String api, ToolPageContext page, Object object) throws Exception {

        HttpClient client = new DefaultHttpClient();
        try {

            HttpPost post = new HttpPost(widget.getTool().getUrl() + api);
            List<NameValuePair> pairs = new ArrayList<NameValuePair>();
            for (String name : page.paramNames()) {
                for (String value : page.params(name)) {
                    pairs.add(new BasicNameValuePair(name, value));
                }
            }

            ToolUser user = page.getUser();
            if (user != null) {
                pairs.add(new BasicNameValuePair(USER_ID_PARAMETER, user.getId().toString()));
            }

            pairs.add(new BasicNameValuePair(OBJECT_PARAMETER, getJson(object)));
            pairs.add(new BasicNameValuePair(WIDGET_PARAMETER, getJson(widget)));
            post.setEntity(new UrlEncodedFormEntity(pairs, HTTP.UTF_8));
            return client.execute(post, new BasicResponseHandler());

        } finally {
            client.getConnectionManager().shutdown();
        }
    }

    /** Updates with the given {@code widget}. */
    public static void updateWithWidget(Widget widget, String api, ToolPageContext page, Object object) throws Exception {
        String json = displayWidget(widget, api, page, object);
        Map<String, Object> values = (Map<String, Object>) ObjectUtils.fromJson(json);
        State.getInstance(object).setValues(values);
    }

    /** Returns the display API location. */
    public String getDisplayApi() {
        return displayApi;
    }

    /** Sets the display API location. */
    public void setDisplayApi(String api) {
        this.displayApi = api;
    }

    /** Returns the update API location. */
    public String getUpdateApi() {
        return updateApi;
    }

    /** Sets the update API location. */
    public void setUpdateApi(String api) {
        this.updateApi = api;
    }

    // --- Widget support ---

    @Override
    public String display(ToolPageContext page, Object object) throws Exception {
        return displayWidget(this, getDisplayApi(), page, object);
    }

    @Override
    public void update(ToolPageContext page, Object object) throws Exception {
        updateWithWidget(this, getUpdateApi(), page, object);
    }
}
