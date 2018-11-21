#!/usr/bin/env python

import sys
import getopt
import subprocess
import re
import os
import errno
import shutil

MODULES = ["com.example.mainmod"]

def usage():
    print("""build [OPTION...] TARGET
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
            build()
        elif target == "clean":
            clean()
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

def build():
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

def clean():
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
    run("find -name *.class -o -name *.html -delete".split())

if __name__ == "__main__":
    main()
