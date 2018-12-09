#!/usr/bin/env python

import sys
import getopt
import subprocess
import re
import os
import errno
import shutil

MODULES = ["com.example.mainmod"]
MAIN_CLASS = "com.example.cli.Main"

def usage():
    print("""make [OPTION...] TARGET
Build this project.
Targets: build, clean
 Options:
  -h         Show this help.""")

def mkdir_if_necessary(path):
    try:
        os.mkdir(path)
    except OSError as exc:
        if exc.errno == errno.EEXIST:
            pass
        else:
            raise

def recursive_mkdir_if_necessary(path):
    try:
        os.makedirs(path)
    except OSError as exc:
        if exc.errno == errno.EEXIST:
            pass
        else:
            raise

def run(args):
    print("+ " + " ".join(args))
    subprocess.check_call(args)

def download(url):
    match =  re.compile("[^/]+$").search(url)
    if not match:
        sys.exit("cannot find filename in url={%s}" % url)
    filename = "lib/%s" % match.group(0)
    if os.path.exists(filename):
        pass
    else:
        run(["wget", url, "--output-document", filename])

def main():
    try:
        opts, args = getopt.gnu_getopt(sys.argv[1:], 'h', ['help'])
    except getopt.GetoptError, err:
        print(str(err))
        sys.exit(1)
    for o, a in opts:
        if o in ('-h', '--help'):
            usage()
            sys.exit(0)
        else:
            sys.exit('unhandled option %s' % o)

    if len(args) < 1:
        sys.exit("not enough parameters")

    for target in args:
        if target == "build":
            target_build(args[1:])
        elif target == "clean":
            target_clean(args[1:])
        elif target == "jar":
            target_jar(args[1:])
        elif target == "run":
            target_run(args[1:])
        elif target == "run_without_jar":
            target_run_without_jar(args[1:])
        else:
            sys.exit("unknown target {%s}" % target)

def close_stdin():
    os.close(0)

def find_java_files(directory):
    """Perform ``find DIRECTORY -name \*.java``.

    :returns:

        a :class:`list` of filenames.

    """
    command = ["find", directory, "-name", "*.java"]
    print("+ %s" % " ".join(command))
    popen = subprocess.Popen(command,
                             stdout=subprocess.PIPE,
                             preexec_fn=close_stdin,
                             close_fds=True)
    output, _ = popen.communicate()
    if popen.returncode != 0:
        raise Exception("%s failed with status %d" % (" ".join(command), popen.returncode))
    return output.splitlines()

def javadoc_dir(module_name):
    return "src/%s/javadoc" % module_name

def target_build(args):
    mkdir_if_necessary("lib")
    download("http://central.maven.org/maven2/net/sf/jopt-simple/jopt-simple/6.0-alpha-2/jopt-simple-6.0-alpha-2.jar")

    for module_name in MODULES:
        run(["javac",
             "-d", "src",
             "-Xlint:all",
             "-Xlint:-requires-automatic",
             "-Xdoclint:all",
             "--module-path", "lib",
             "--module-source-path", "src",
             "--module", module_name])
        recursive_mkdir_if_necessary(javadoc_dir(module_name))

def target_jar(args):
    target_build([])
    for module_name in MODULES:
        run(["javadoc",
             "-html5",
             "--module", module_name,
             "--module-path", "lib",
             "--module-source-path", "src",
             "-d", javadoc_dir(module_name)]
            + find_java_files("src/%s" % module_name))
        run(["jar", "--create",
             "--file", "lib/%s.jar" % module_name,
             "--main-class", "com.example.cli.Main",
             "-C", "src/%s" % module_name,
             "."])

def target_run(args):
    """This target is intended to run the program without generating a
    jar.

    However, I'm currently failing to find how to do that.  What's the
    correct commandline?  Is it documented somewhere?

    """
    target_jar([])
    run(["java",
         "--module-path", "lib",
         "--module", "%s/%s" % (MODULES[0], MAIN_CLASS),
    ] + args)


def target_run_without_jar(args):
    """This target is intended to run the program without generating a
    jar.

    However, I'm currently failing to find how to do that.  What's the
    correct commandline?  Is it documented somewhere?

    """
    target_build([])
    run(["java",
         "--module-path", "src:lib",
         "--module", "%s/%s" % (MODULES[0], MAIN_CLASS),
    ] + args)

def target_clean(args):
    for module_name in MODULES:
        try:
            os.unlink("lib/%s.jar" % module_name)
        except OSError as exc:
            if exc.errno == errno.ENOENT:
                pass
            else:
                raise
        if os.path.exists(javadoc_dir(module_name)):
            shutil.rmtree(javadoc_dir(module_name))
    run("find ( -name *.class -o -name *.html ) -delete".split())

if __name__ == "__main__":
    main()
