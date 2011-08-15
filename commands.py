# Here you can create play commands that are specific to the module, and extend existing commands
import os, os.path
import sys
import subprocess

MODULE = 'scalagen'

# Commands that are specific to your module

COMMANDS = ['scalagen:generate', 'scalagen:g', 'scalagen:jquery', 'scalagen:j']

def execute(**kargs):
    command = kargs.get("command")
    app = kargs.get("app")
    args = kargs.get("args")
    env = kargs.get("env")

    if command in ("scalagen:generate", "scalagen:g"):
        print "~ Generating code"
        print "~ "
        java_cmd = app.java_cmd([], None, "play.modules.scalagen.Generator", args)
        try:
            subprocess.call(java_cmd, env=os.environ)
        except OSError:
            print "Could not execute the java executable, please make sure the JAVA_HOME environment variable is set properly (the java executable should reside at JAVA_HOME/bin/java). "
            sys.exit(-1)
        print
	
    if command in ("scalagen:jquery", "scalagen:j"):
        app.override('app/views/main.scala.html', 'app/views/main.scala.html')
        app.override('public/stylesheets/jqgrid.css', 'public/stylesheets/jqgrid.css')
        app.override('public/stylesheets/main.css', 'public/stylesheets/main.css')
        app.override('public/javascripts/jquery-1.5.2.min.js', 'public/javascripts/jquery-1.5.2.min.js')
        app.override('public/javascripts/jquery-ui-1.8.1.custom.min.js', 'public/javascripts/jquery-ui-1.8.1.custom.min.js')
        app.override('public/javascripts/jquery.custom.format.js', 'public/javascripts/jquery.custom.format.js')
        app.override('public/javascripts/jquery.jqGrid.min.js', 'public/javascripts/jquery.jqGrid.min.js')
        app.override('public/javascripts/jquery.layout.js', 'public/javascripts/jquery.layout.js')
        app.override('public/javascripts/i18n/grid.locale-en.js', 'public/javascripts/i18n/grid.locale-en.js')
        app.override('public/images/ui-icons_217bc0_256x240.png', 'public/images/ui-icons_217bc0_256x240.png')
        app.override('public/images/ui-icons_469bdd_256x240.png', 'public/images/ui-icons_469bdd_256x240.png')
        print "~~~~~ Copied JQuery files"

# This will be executed before any command (new, run...)
def before(**kargs):
    command = kargs.get("command")
    app = kargs.get("app")
    args = kargs.get("args")
    env = kargs.get("env")


# This will be executed after any command (new, run...)
def after(**kargs):
    command = kargs.get("command")
    app = kargs.get("app")
    args = kargs.get("args")
    env = kargs.get("env")

    if command == "new":
        pass
