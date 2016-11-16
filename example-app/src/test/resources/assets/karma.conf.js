module.exports = function(config) {
	config.set({
		// project directory as base path
		basePath : '../../../../',

		files : [
		// external dependencies
		'src/main/thirdparty-resources/assets/lib/third-party.js',

		// mock support
		'node_modules/angular-mocks/angular-mocks.js',

		// JS files
		'src/main/resources/assets/app/*.js',

		// include *.spec.js files
		'src/test/resources/assets/app/*.spec.js' ],

		autoWatch : true,
		frameworks : [ 'jasmine' ],
		browsers : [ 'PhantomJS' ],
		plugins : [ 'karma-phantomjs-launcher', 'karma-jasmine', 'karma-junit-reporter' ]
	});
};
