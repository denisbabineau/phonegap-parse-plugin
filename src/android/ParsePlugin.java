package org.apache.cordova.core;

import android.app.Application;
import android.util.Log;

import java.util.Dictionary;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseCrashReporting;

public class ParsePlugin extends CordovaPlugin {

    private static final String TAG = "ParsePlugin";
    private static final String ACTION_INITIALIZE = "initialize";
    private static final String ACTION_GET_INSTALLATION_ID = "getInstallationId";
    private static final String ACTION_GET_INSTALLATION_OBJECT_ID = "getInstallationObjectId";
    private static final String ACTION_GET_SUBSCRIPTIONS = "getSubscriptions";
    private static final String ACTION_SUBSCRIBE = "subscribe";
    private static final String ACTION_UNSUBSCRIBE = "unsubscribe";
    private static final String ACTION_REGISTER_CALLBACK = "registerCallback";
    private static final String ACTION_TRACK_EVENT = "trackEvent";

    private static final String PARSE_APP_ID = "parse_app_id";
    private static final String PARSE_CLIENT_KEY = "parse_client_key";
    private static final String PARSE_JS_KEY = "parse_js_key";

    private static CordovaWebView sWebView;
    private static String sEventCallback = null;
    private static boolean sForeground = false;
    private static JSONObject sLaunchNotification = null;
    private static JSONObject sKeys = null;

    public static void initializeParseWithApplication(Application app) {
        String appId = getStringByKey(app, PARSE_APP_ID);
        String clientKey = getStringByKey(app, PARSE_CLIENT_KEY);
        String jsKey = getStringByKeyOpt(app, PARSE_JS_KEY, "");

        // Save in static var for later use by plugin
        sKeys = new JSONObject();
        try {
            sKeys.put(PARSE_APP_ID, appId);
            sKeys.put(PARSE_CLIENT_KEY, clientKey);
            sKeys.put(PARSE_JS_KEY, jsKey);
        } catch (JSONException e) {
            Log.d(TAG, "initializeParseWithApplication: Failed to store parse keys in JSON object");
        }

        // Initialize Crash Reporting.
        ParseCrashReporting.enable(app);

        // Enable Local Datastore.
        Parse.enableLocalDatastore(app);

        Log.d(TAG, "Initializing with parse_app_id: " + appId + " and parse_client_key:" + clientKey);
        Parse.initialize(app, appId, clientKey);
    }

    private static String getStringByKey(Application app, String key) {
        int resourceId = app.getResources().getIdentifier(key, "string", app.getPackageName());
        return app.getString(resourceId);
    }

    private static String getStringByKeyOpt(Application app, String key, String opt) {
        int resourceId = app.getResources().getIdentifier(key, "string", app.getPackageName());
        return (resourceId == 0) ? opt : app.getString(resourceId);
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals(ACTION_REGISTER_CALLBACK)) {
            this.registerCallback(callbackContext, args);
            return true;
        } else if (action.equals(ACTION_INITIALIZE)) {
            this.initialize(callbackContext, args);
            return true;
        } else if (action.equals(ACTION_GET_INSTALLATION_ID)) {
            this.getInstallationId(callbackContext);
            return true;
        } else if (action.equals(ACTION_GET_INSTALLATION_OBJECT_ID)) {
            this.getInstallationObjectId(callbackContext);
            return true;
        } else if (action.equals(ACTION_GET_SUBSCRIPTIONS)) {
            this.getSubscriptions(callbackContext);
            return true;
        } else if (action.equals(ACTION_SUBSCRIBE)) {
            this.subscribe(args.getString(0), callbackContext);
            return true;
        } else if (action.equals(ACTION_UNSUBSCRIBE)) {
            this.unsubscribe(args.getString(0), callbackContext);
            return true;
        } else if (action.equals(ACTION_TRACK_EVENT)) {
            Map<String, String> dimensions = new HashMap<String, String>();
            JSONObject object = args.getJSONObject(1);
            Iterator<String> iter = object.keys();
            while (iter.hasNext()) {
                String key = iter.next();
                Object value = object.get(key);
                dimensions.put(key, value.toString());
            }
            this.trackEvent(args.getString(0), dimensions, callbackContext);
            return true;
        }
        return false;
    }

    private void registerCallback(final CallbackContext callbackContext, final JSONArray args) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                try {
                    sEventCallback = args.getString(0);
                    callbackContext.success();
                    // if the app was opened from a notification, handle it now that the device is ready
                    handleLaunchNotification();
                } catch (JSONException e) {
                    callbackContext.error("JSONException");
                }
            }
        });
    }

    private void initialize(final CallbackContext callbackContext, final JSONArray args) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                if ((args != null) && (args.length() > 0)) {
                    try {
                        JSONObject options = args.getJSONObject(0);
                        if (options.has("appJsVersion")) {
                            String jsVersion = options.getString("appJsVersion");
                            ParseInstallation.getCurrentInstallation().put("appJsVersion", jsVersion);
                        }
                    } catch (JSONException e) {
                        // Ignored
                    }
                }
                ParseInstallation.getCurrentInstallation().saveInBackground();
                ParseAnalytics.trackAppOpenedInBackground(cordova.getActivity().getIntent());
                callbackContext.success(sKeys);
            }
        });
    }

    private void getInstallationId(final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                String installationId = ParseInstallation.getCurrentInstallation().getInstallationId();
                callbackContext.success(installationId);
            }
        });
    }

    private void getInstallationObjectId(final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                String objectId = ParseInstallation.getCurrentInstallation().getObjectId();
                callbackContext.success(objectId);
            }
        });
    }

    private void getSubscriptions(final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                List<String> channels = ParseInstallation.getCurrentInstallation().getList("channels");
                callbackContext.success(channels.toString());
            }
        });
    }

    private void subscribe(final String channel, final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                ParsePush.subscribeInBackground(channel);
                callbackContext.success();
            }
        });
    }

    private void unsubscribe(final String channel, final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                ParsePush.unsubscribeInBackground(channel);
                callbackContext.success();
            }
        });
    }

    private void trackEvent(final String name, final Map<String, String> dimensions, final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                ParseAnalytics.trackEventInBackground(name, dimensions);
                callbackContext.success();
            }
        });
    }

    /*
    * Use the cordova bridge to call the jsCB and pass it jsonPayload as param
    */
    public static void javascriptEventCallback(JSONObject jsonPayload) {
        if (sEventCallback != null && !sEventCallback.isEmpty() && sWebView != null) {
            String snippet = "javascript:" + sEventCallback + "(" + jsonPayload.toString() + ")";
            Log.v(TAG, "javascriptCB: " + snippet);
            sWebView.sendJavascript(snippet);
        }
    }

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        sEventCallback = null;
        sWebView = this.webView;
        sForeground = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sEventCallback = null;
        sWebView = null;
        sForeground = false;
    }

    @Override
    public void onPause(boolean multitasking) {
        super.onPause(multitasking);
        sForeground = false;
    }

    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);
        sForeground = true;
    }

    public static boolean isInForeground() {
        return sForeground;
    }

    public static void setLaunchNotification(JSONObject jsonPayload) {
        sLaunchNotification = jsonPayload;
    }

    private void handleLaunchNotification() {
        if (isInForeground() && sLaunchNotification != null) {
            javascriptEventCallback(sLaunchNotification);
            sLaunchNotification = null;
        }
    }
}
