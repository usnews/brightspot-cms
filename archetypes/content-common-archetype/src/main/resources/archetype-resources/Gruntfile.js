module.exports = function(grunt) {

    // Tasks
    grunt.registerTask('build', ['clean', 'build-js', 'less']);
    grunt.registerTask('build-js', ['concat:legacy', 'uglify:all']);
    grunt.registerTask('default', ['build']);
    grunt.registerTask('lint', ['jslint']);
    grunt.registerTask('require-dependencies', ['shell:dependo']);

    // Project configuration.
    grunt.initConfig({
        pkg: grunt.file.readJSON('package.json'),
        // cleans up the compiled JS file
        clean: {
            all: [
                './src/main/webapp/assets/script/build'
            ]
        },
        less: {
            dev: {
                options: {
                    paths: ["/src/main/webapp/assets/style/less"],
                    rootpath: "",
                    compress: true,
                    cleancss: true
                },
                files: {
                    "./src/main/webapp/assets/style/css/${artifactId}.css": "./src/main/webapp/assets/style/less/${artifactId}.less"
                }
            }
        },
        jslint: {
            dev: {
                failOnError: false,
                src: [
                    'Gruntfile.js',
                    './src/main/webapp/assets/script/**/*.js'
                ],
                exclude: [
                    './src/main/webapp/assets/script/vendor/**/*.js'
                ],
                directives: {
                    browser: true,
                    predef: [
                        'jQuery',
                        '$',
                        'define',
                        'require'
                    ]
                }
            }
        },
        concat: {
            legacy: {
                src: [
                    './src/main/webapp/assets/script/vendor/jquery/jquery-2.1.0.js',
                ],
                dest: './src/main/webapp/assets/script/build/${artifactId}.js',
                nonull: true
            }
        },
        uglify: {
            all: {
                options: {
                    preserveComments: 'some',
                    mangle: {
                        except: ['jQuery']
                    }
                },
                files: {
                }
            }
        },
        shell : {
            dependo : { /* note: this is done via shall rather than grunt-dependo as the module had issues as of 0.1.1 */
                options: {
                    stdout: true
                },
                command: './node_modules/.bin/dependo -f amd src/main/webapp/assets/script/ > dependencies.html && open dependencies.html'
            }
        }
    });


    // Includes
    grunt.loadNpmTasks('grunt-contrib-uglify');
    grunt.loadNpmTasks('grunt-contrib-less');
    grunt.loadNpmTasks('grunt-jslint');
    grunt.loadNpmTasks('grunt-contrib-concat');
    grunt.loadNpmTasks('grunt-contrib-clean');
    grunt.loadNpmTasks('grunt-shell');

};
