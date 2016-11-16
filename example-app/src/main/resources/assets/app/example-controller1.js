(function() {
	'use strict';
	
	var exampleApp = angular.module('exampleApp');
	
	/**
     * This controller calls 'rooms' and 'devices' request handlers which are registered in the ExampleApp.java.
     */
	exampleApp.controller('ExampleController1', [ '$scope', '$http', function($scope, $http) {
		$scope.rooms = [];
		$scope.devices = [];
		
		// the App had registered 'rooms' path, request it with the angular service $http.
		$http.get('rooms').then(function onSuccess(response) {
			console.debug("fetchig rooms was successfull");
			console.debug(response.data);
			$scope.rooms = response.data.rooms;
    	}, function onFailure(response) {
    		console.error("can't get rooms");
    	});
		
		// example request with POST with a parameter
		$http.post('devices', { 'propertyType' : "http://iolite.de#on" }).then(function onSuccess(response) {
			console.debug("fetchig devices was successfull");
	    	console.debug(response.data);
	    	$scope.devices = response.data.devices;
    	}, function onFailure(response) {
    		console.error("can't get devices");
    	});
     } ]);
})();