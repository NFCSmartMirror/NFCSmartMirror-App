(function() {
	'use strict';
	
	// depends on ngSanitize
	var exampleApp = angular.module('exampleApp', [ 'ngSanitize']);
	
	// configuration
	exampleApp.config([ '$provide', '$httpProvider', function($provide, $httpProvider) {

		// because of IFrame sand boxing, we can't read the cookie. So try catch here.
		$provide.decorator("$$cookieReader", [ '$delegate', function($delegate) {
			return function() {
				var cookies = {};
				try {
					cookies = $delegate();
				}
				catch (err) {
					// console.warn(err);
				}
				return cookies;
			};
		} ]);

		// intercept every HTTP request and append session id
		$httpProvider.interceptors.push(function($q) {
			return {
				'request' : function(config) {
					if (!angular.isUndefined(window.Client) && !angular.isUndefined(window.Client.SID)) {
						config.url = URI(config.url).setSearch('SID', window.Client.SID);
					}
					return config;
				}
			};
		});

		// to avoid Bad Request in Firefox, add manually X-Requested-With header
		$httpProvider.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest';
	} ]);

	exampleApp.run(function() {
		// empty
	});
})();