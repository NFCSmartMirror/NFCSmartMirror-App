(function() {
	'use strict';
	
	var exampleApp = angular.module('exampleApp');
	
	/**
     * This controller uses the JavaScript ModelAPI.
     */
	exampleApp.controller('ExampleController2', [ '$scope', '$timeout', function($scope, $timeout) {
		
		$scope.profileKeys = [];
		// iterate over all ModelAPI profiles
		for ( var key in ModelAPIProfiles) {
			if (ModelAPIProfiles.hasOwnProperty(key)) {
				var val = ModelAPIProfiles[key];
				if (typeof val === "string") {
					$scope.profileKeys.push(key);
				}
			}
		}
		
		// first request StorageAPI and then execute the StorageAPI.loadInt action
		$scope.testValueLoadedFromStorageAPI = null;
		ModelAPIProfiles.get(ModelAPIProfiles.storageId, {
			// got the App API
			success : function(storageAPI) {
				// now execute StorageAPI.loadInt action
				storageAPI.action({
					// Formulate an ActionRequest:
					// * requestIdentifier: optionally set an identifier to know what the action was for, if you set it null it will be an automatically
					// generated one
					// * modelIdentifier: setting an own modelIdentifier has no effect (anymore), it will always be overwritten with the one that the ModelAPI
					// object was created for
					// * objectQuery: some XPath expression that points to a single object in the model that the ActionRequest should use as target
					// * actionName: the name of the action to execute at the target
					// * parameters: the ordered values of the parameters of the call of the action
					request : new ActionRequest(/* requestIdentifier */null, /* modelIdentifier */null, /* objectQuery */ ".", /* actionName */ "loadInt", /* parameters */ [ new ValueParameter("test") ]),

					success : function(value, storageAPI, request) {
						$scope.testValueLoadedFromStorageAPI = value;
					},
					
					error : function(storageAPI, responseRequestID, responseErrorCode, responseError) {
						console.error("Action " + responseRequestID + " '" + objectQuery + "' failed due to " + responseErrorCode + ": " + responseError);
					}
				});
			}
		});
		
		// request EnvironmentAPI
		$scope.environmentAPI = null;
		$scope.roomsQueredWithEnvironmentAPI = [];
		ModelAPIProfiles.get(ModelAPIProfiles.ContextModelId, {
			// redo success on reconnect
			redoOnReconnect : true,
			// got the App API
			success : function(environmentAPI) {
				$scope.environmentAPI = environmentAPI;
				
				environmentAPI.query({
					// what should be the this in the success and error callbacks, can be anything but null
					context : { contextCanBe : "any JS object you want to be set as this when one of the callbacks are invoked" },

					// Formulate a QueryRequest:
					// * requestIdentifier: optionally set an identifier to know what the query was for, if you set it null it will be an automatically
					// generated one
					// * modelIdentifier: setting an own modelIdentifier has no effect (anymore), it will always be overwritten with the one that the ModelAPI
					// object was created for
					// * query: some XPath expression that points to the data in the model that the QueryRequest should retrieve
					request : new QueryRequest(/* requestIdentifier */null, /* modelIdentifier */null, /* query */ "./locations"),
					
					// Define a success callback to retrieve the values of the QueryRequest:
					// * values: the retrieved values
					// * environmentAPI: the ModelAPI object used for querying, in this example the parameter is just named conveniently to what it would be,
					// but you can rename it to anything you want to have
					// * request: the final QueryRequest send to the remote AppAPI
					success : function(values, environmentAPI, request) {
						$scope.roomsQueredWithEnvironmentAPI = values;
					},
					
					// Define an error callback to handle failed request, same for any request type:
					// * environmentAPI: the ModelAPI object that sent the request, in this example the parameter is just named conveniently to what it would
					// be, but you can rename it to anything you want to have
					// * responseRequestID: identifier of the failed request
					// * responseErrorCode: error code determined by the IOLITE bus-service
					// * responseError: explicit error message provided by the IOLITE bus-service
					error : function(environmentAPI, responseRequestID, responseErrorCode, responseError) {
						console.error("Query " + responseRequestID + " '" + query + "' failed, because: " + responseError);
					}
				});
				
				// now subscribe to devices
				$scope.subscribeDevices();
			}
		});
		
		// subscribe to devices
		$scope.subscriptionRequestId = null;
		$scope.subscriptionLogs = "";
		$scope.devices = null;
		$scope.subscribeDevices = function() {
			if($scope.environmentAPI) {
				$scope.environmentAPI.subscribe({
					// Formulate a SubscribeRequest:
					// * requestIdentifier: optionally set an identifier to know what the subscribe was for, if you set it null it will be an automatically
					// generated one
					// * modelIdentifier: setting an own modelIdentifier has no effect (anymore), it will always be overwritten with the one that the ModelAPI
					// object was created for
					// * query: some XPath expression that points to the data in the model that is to be subscribed
					// * callback: deprecated, set to null
					// * minimumUpdateInterval: receive updates in no less than the given interval in milliseconds
					request : new SubscribeRequest(/* requestIdentifier */null, /* modelIdentifier */null, /* query */ "./devices", /* callback */null,
							/* minimumUpdateInterval */ 100),

					// should the received updates be analyzed to delegate change callbacks (the four following ones)
					// if deepInspect is set to false (default), the updates are directly delegated to the success callback on each update
					deepInspect : true,

					// callback for added elements due to updates
					elementAdded : function(environmentAPI, objectStack, hashStack, element, index) {
						// root in own model: if you have an own application model reflecting the structure of the remote model then put it here
						var pointer = ModelAPIClient.getTopOfObjectStack(/* root in own model */$scope.devices, objectStack, hashStack);
						// pointer.parent is the container of element
						// pointer.tag is the containment
						// pointer.top is element in model of "root" or if "root=null" in model of objectStack
						var message = "Added " + JSON.stringify(pointer) + " at " + index;
						console.debug(message);
						$scope.$apply(function() {
							$scope.subscriptionLogs += message + "\n";
						});
					},

					// callback for removed elements
					elementRemoved : function(environmentAPI, objectStack, hashStack, element) {
						var pointer = ModelAPIClient.getTopOfObjectStack($scope.devices, objectStack, hashStack);
						var message = "Removed " + JSON.stringify(pointer);
						console.debug(message);
						$scope.$apply(function() {
							$scope.subscriptionLogs += message + "\n";
						});
					},

					// callback for moved elements
					elementMoved : function(environmentAPI, objectStack, hashStack, element, newIndex, oldIndex) {
						var pointer = ModelAPIClient.getTopOfObjectStack($scope.devices, objectStack, hashStack);
						var message = "Moved " + JSON.stringify(pointer) + " from " + oldIndex + " to " + newIndex;
						console.debug(message);
						$scope.$apply(function() {
							$scope.subscriptionLogs += message + "\n";
						});
					},

					// callback for changed attributes
					attributeChanged : function(environmentAPI, objectStack, hashStack, newAttributeValue, oldAttributeValue) {
						var pointer = ModelAPIClient.getTopOfObjectStack($scope.devices, objectStack, hashStack);
						var message = "Changed " + JSON.stringify(pointer) + " from " + oldAttributeValue + " to " + newAttributeValue;
						console.debug(message);
						$scope.$apply(function() {
							$scope.subscriptionLogs += message + "\n";
						});
					},

					// success is called only once if deepInspect==true
					success : function(values, environmentAPI, request) {
						$scope.subscriptionRequestId = request.requestID;
						$scope.devices = values;
						var message = "Subscribe " + JSON.stringify(request) + " returned initial values " + JSON.stringify(values);
						console.debug(message);
						$scope.$apply(function() {
							$scope.subscriptionLogs += message + "\n";
						});
						
						// unsubscribe after 1 minute
						$timeout($scope.unsubscribeDevices, 60000);
					},

					error : function(environmentAPI, responseRequestID, responseErrorCode, responseError) {
						console.error("Subscribe " + responseRequestID + " '" + query + "' failed");
					}
				});
			}
		};
		// unsubscribe
		$scope.unsubscribeDevices = function() {
			if($scope.environmentAPI && $scope.subscriptionRequestId) {
				$scope.environmentAPI.unsubscribe({
					// Formulate some UnsubscribeRequest:
					// * requestIdentifier: optionally set an identifier to know what the unsubscribe was for, if you set it null it will be an
					// automatically
					// generated one
					// * modelIdentifier: setting an own modelIdentifier has no effect (anymore), it will always be overwritten with the one that
					// the ModelAPI
					// object was created for
					// * requestIdOfSubscribe: request identifier of the subscribe that is to be unsubscribed
					request : new UnsubscribeRequest(/* requestIdentifier */null, /* modelIdentifier */null, $scope.subscriptionRequestId),
					
					success : function() {
						var message = "Unsubscribed after 1 minute " + $scope.subscriptionRequestId;
						console.debug(message);
						$scope.$apply(function() {
							$scope.subscriptionLogs += message + "\n";
						});
					},
					
					error : function(environmentAPI, responseRequestID, responseErrorCode, responseError) {
						console.error("Unsubscribe " + responseRequestID + " failed for subscribe " + $scope.subscriptionRequestId);
					}
				});
			}
		};
		
     } ]);
})();