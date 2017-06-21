'use strict';

module.exports = function(grunt) {
	/* for validating front end resources */
	grunt.loadNpmTasks('grunt-contrib-jshint');
	grunt.loadNpmTasks('grunt-htmlhint');
	grunt.loadNpmTasks('grunt-lesshint');
	/** for third party dependencies */
	grunt.loadNpmTasks('grunt-bower-concat');
	grunt.loadNpmTasks('grunt-contrib-copy');

	grunt.loadNpmTasks('grunt-contrib-less');

	/**
	 * Call 'bower install --save DEPENDENCY && grunt update-thirdparty' to add a new dependency.
	 */
	grunt.registerTask('update-thirdparty', [ 'bower_concat:third_party', 'copy:third_party' ]);
	/**
	 * Validate JS/CSS/HTML assets. Called by maven.
	 */
	grunt.registerTask('validate-frontend-resources', [ 'lesshint', 'jshint', 'htmlhint' ]);
	/**
	 * Build front end assets like compiling LESS to CSS etc. Called by maven.
	 */
	grunt.registerTask('build-frontend-resources', [ 'less']);

	var LIB_DIRECTORY = 'src/main/thirdparty-resources/assets/lib/';
	var ASSETS_DIRECTORY = 'src/main/resources/assets/';
	var GENERATED_ASSETS_DIRECTORY = 'src/main/generated-resources/assets/';

	var configs = {};

	/*
	 * Checking JS files for syntax errors.
	 */

	configs.jshint = {
		files : [ 'Gruntfile.js', 'src/main/resources/**/*.js' ],
		options : {
			jshintrc : '.jshintrc'
		}
	};

	/*
	 * Checking HTML files for syntax errors.
	 */

	configs.htmlhint = {
		iolite: {
			options: {
				htmlhintrc: '.htmlhintrc'
			},
			src: [ASSETS_DIRECTORY + '**/*.html']
		}
	};

	/*
	 * Checking LESS files for syntax errors.
	 */

	configs.lesshint = {
		options: {
			lesshintrc: '.lesshintrc'
		},
		iolite: {
			src: [ASSETS_DIRECTORY + "**/*.less"]
		}
	};

	/*
	 * Maintain third party dependencies with bower.
	 */

	// create a file from all bower dependencies.
	configs.bower_concat = {
		third_party : {
			dest : LIB_DIRECTORY + 'third-party.js',
			dependencies : {
				'angular' : [ 'jquery' ]
			}
		}
	};

	/*
	 * Copy third party resources.
	 */
	configs.copy = {
		third_party : {
			files : [
			// copy bootstrap LESS files
			{
				expand : true,
				cwd : 'bower_components/bootstrap/',
				// only LESS files
				src : [ 'less/**/*.less' ],
				dest : LIB_DIRECTORY + 'bootstrap/'
			} ],
		},
	};

	/*
	 * Generating CSS file from LESS sources
	 */
	configs.less = {
		dist : {
			options : {
				strictMath : true,
				paths : [  ],
				plugins : [ new (require('less-plugin-autoprefix'))({
					browsers : [ "last 3 versions" ]
				}) ],

				modifyVars : {
				// do nothing
				}
			},
			src : ASSETS_DIRECTORY + 'less/example-bootstrap.less',
			dest : GENERATED_ASSETS_DIRECTORY + 'css/example-bootstrap.css',
		}
	};

	grunt.initConfig(configs);
};
