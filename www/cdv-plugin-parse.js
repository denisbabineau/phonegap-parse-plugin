var parsePlugin = {

    registerCallback: function(ecb, successCallback, errorCallback) {
        cordova.exec(
            successCallback,
            errorCallback,
            'ParsePlugin',
            'registerCallback',
            [ecb]
        );
    },

    initialize: function(options, successCallback, errorCallback) {
        cordova.exec(
            successCallback,
            errorCallback,
            'ParsePlugin',
            'initialize',
            [options]
        );
    },

    getInstallationId: function(successCallback, errorCallback) {
        cordova.exec(
            successCallback,
            errorCallback,
            'ParsePlugin',
            'getInstallationId',
            []
        );
    },

    getInstallationObjectId: function(successCallback, errorCallback) {
        cordova.exec(
            successCallback,
            errorCallback,
            'ParsePlugin',
            'getInstallationObjectId',
            []
        );
    },

    getSubscriptions: function(successCallback, errorCallback) {
        cordova.exec(
            successCallback,
            errorCallback,
            'ParsePlugin',
            'getSubscriptions',
            []
        );
    },

    subscribe: function(channel, successCallback, errorCallback) {
        cordova.exec(
            successCallback,
            errorCallback,
            'ParsePlugin',
            'subscribe',
            [ channel ]
        );
    },

    unsubscribe: function(channel, successCallback, errorCallback) {
        cordova.exec(
            successCallback,
            errorCallback,
            'ParsePlugin',
            'unsubscribe',
            [ channel ]
        );
    },

    // iOS only
    resetBadge: function(successCallback, errorCallback) {
        cordova.exec(
            successCallback,
            errorCallback,
            'ParsePlugin',
            'resetBadge',
            []
        );
    },

    trackEvent: function(name, dimensions, successCallback, errorCallback) {
        cordova.exec(
            successCallback,
            errorCallback,
            'ParsePlugin',
            'trackEvent',
            [ name, dimensions ]
        );
    }
};
module.exports = parsePlugin;
