// this is a Karma config for continuous integration test
var baseConfig = require('./karma.conf.js');

module.exports = function(config) {
	// load base config
	baseConfig(config);	

	// override base config
	config.set({
		autoWatch: false,
		singleRun: true,
		colors: false
	});
};
