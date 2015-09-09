var configHelper = cordova.require('cordova/confighelper');

var appId = undefined;
var jsKey = undefined;
var initialized = false;
var initialize_callback = undefined;

/**
 * Read keys from config.xml
 */
(function init() {
    function ready() {
        initialized = true;
        if (initialize_callback) {
            initialize_callback();
            initialize_callback = undefined;
        }
    }

    configHelper.readConfig(function(config) {
        // Gets called twice under some circumstances ?
        if (!initialized) {
            appId = config.getPreferenceValue("APP_ID");
            jsKey = config.getPreferenceValue("JS_KEY");
            ready();
        }
    }, function(err) {
        if (!initialized) {
            console.error(err);
            ready();
        }
    });
})();

module.exports = {
    initialize: function (success, error) {
        var cb = function(success, error) {
            if (appId && jsKey) {
                if (success) {
                    success({ parse_app_id: appId, parse_js_key: jsKey });
                }
            } else if (error) {
                error("APP_ID and/or JS_KEY is not defined in browser preferences");
            }
        };

        if (!initialized) {
            initialize_callback = cb.bind(this, success, error);
        } else {
            cb(success, error);
        }
    }
};

require("cordova/exec/proxy").add("ParsePlugin", module.exports);
