/**
 * A simple Karma test for the angular controller 'ExampleController1'.
 * 
 * @author Erdene-Ochir Tuguldur
 */
describe("exampleApp", function () {
	'use strict';

    beforeEach(function () {
        // import our app
        module('exampleApp');
    });

    describe("ExampleController1", function () {
    	var httpMock = null;
        var scope = null;
        var controller = null;

        beforeEach(inject(function ($rootScope, $controller, $httpBackend) {
            scope = $rootScope.$new();
            httpMock = $httpBackend;

            controller = $controller('ExampleController1', {
                $scope: scope
            });
        }));

        it("should be defined", function () {
            expect(controller).toBeDefined();
        });
        
        it("should be not null", function () {
            expect(controller).not.toBeNull();
        });

    });
});