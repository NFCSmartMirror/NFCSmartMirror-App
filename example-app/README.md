# Introduction

The _example-app_ is an example IOLITE App. It shows some basic examples of IOLITE
API usage, as well as basics of the App UI development.

Build with `mvn package`. Then put into `<user-home>/.iolite/apps` and install it in the HCC's _Apps/Store_ area.

You can use `-Deploy` option so maven puts the resulting file into `<user-home>/.iolite/apps` automatically, e.g. `mvn -Deploy package`.

To use the project in Eclipse, call `mvn eclipse:eclipse -DdownloadJavadocs=true` to generate Eclipse project files.

# User Interface Resources

This is a simple example app using following tools/libraries/frameworks:
* [Grunt](http://gruntjs.com/) as a build system
* [Bootstrap](http://getbootstrap.com/) together with [Less](http://lesscss.org/) as a style sheet framework
* [Bower](https://bower.io/) as a front end package manager
* [AngularJS](https://angularjs.org/) as a web application framework
* [Karma](https://karma-runner.github.io/1.0/index.html) with [Jasmin](https://jasmine.github.io/) as a JS unit test framework

You don't need to install the above dependencies yourself, the maven will automatically download and install them.

## How to add a new front end development dependency?
All development dependencies like `Grunt` or `Karma` are maintained by the node package manager `npm`. To install a new development dependency, execute `/path/to/angular-example-app/node/node /path/to/angular-example-app/node/node_modules/npm/bin/npm-cli.js install --save-dev YOUR_DEPENDENCY`.

If you have already installed the `Node.js` globally, call only `npm install --save-dev YOUR_DEPENDENCY`

For already installed dependencies, see the file [package.json](package.json).

## How to add a new front end dependency?
All front end dependencies like `Angular` or `JQuery` are maintained by bower. You can check the file [bower.js](bower.js) to see what dependencies are already installed.

To add a new dependency, execute the following steps:
* Install the dependency with bower:  `/path/to/angular-example-app/node/node /path/to/angular-example-app/node_modules/bower/bin/bower install --save YOUR_DEPENDENCY`
* Merge all dependencies into a single JS file `third-party.js`:  `/path/to/angular-example-app/node/node /path/to/angular-example-app/node_modules/grunt-cli/bin/grunt update-thirdparty`

If you have already installed the `Node.js` globally, it is easier: `bower install --save YOUR_DEPENDENCY` and `grunt update-thirdparty`.

Sometimes, you want not only JS but some other assets like LESS files. In this case, you have to update the task `copy:thirdparty` in the file [Gruntfile.js](Gruntfile.js)

## Validating front end resources

During the maven build, all JS/LESS/HTML assets are checked for syntax errors.

* JS files are checked with [JSHint](http://jshint.com/docs/):
 * configuration file: [.jshintrc](.jshintrc)
 * grunt task: [grunt-contrib-jshint](https://github.com/gruntjs/grunt-contrib-jshint)
* LESS files with [CSSLint](https://github.com/CSSLint/csslint/wiki):
 * configuration file: [.lesshintrc](.lesshintrc)
 * grunt task: [grunt-lesshint](https://github.com/jgable/grunt-lesslint)
* HTML files with [HTMLHint](https://github.com/yaniswang/HTMLHint/):
 * configuration file: [.htmlhintrc](.htmlhintrc)
 * grunt task: [grunt-htmlhint](https://github.com/yaniswang/grunt-htmlhint)

## Angular unit tests

We recommend you to write unit tests for your Angular components using [Karma](https://karma-runner.github.io/1.0/index.html) with [Jasmin](https://jasmine.github.io/).

For each of your component, write a test and put in the folder `src/main/test/resources/assets/app/`. The test file name should end with the suffix `*.spec.js`. During the maven build, maven searches the folder and executes these tests.

## Style sheets

The example app uses [Bootstrap](http://getbootstrap.com/) together with [Less](http://lesscss.org/) as a style sheet framework. For the Bootstrap Less classes, see the folder `src/main/thirdparty-resources/assets/lib/bootstrap/less/`.
You should extend those Bootstrap Less classes using either the [extend](http://lesscss.org/features/#extend-feature) or [mixins](http://lesscss.org/features/#mixins-feature) Less feature. For example, see the folder `src/main/resources/assets/less/`.

During the maven build, a CSS file `src/main/generated-resources/assets/css/example-bootstrap.css` is generated. You shouldn't touch this file. Instead of that, please edit the Less files in `src/main/resources/assets/less/`.
