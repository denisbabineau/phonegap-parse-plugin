var configHelper = cordova.require('cordova/confighelper');

var appId = undefined;
var jsKey = undefined;

/**
 * Read keys from config.xml
 */
(function init() {
    configHelper.readConfig(function(config) {
        appId = config.getPreferenceValue("APP_ID");
        jsKey = config.getPreferenceValue("JS_KEY");
    }, function(err) {
        console.error(err);
    });
})();

module.exports = {
    initialize: function (success, error) {
        if (appId && jsKey) {
            if (success) {
                success({ parse_app_id: appId, parse_js_key: jsKey });
            }
        } else if (error) {
            error("APP_ID and/or JS_KEY is not defined in browser preferences");
        }
    }
};

require("cordova/exec/proxy").add("ParsePlugin", module.exports);
