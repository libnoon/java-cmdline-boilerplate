#!/usr/bin/env python

import sys
import getopt
import subprocess
import re
import os
import errno
import shutil

MODULES = ["mainmod"]

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

    if len(args) != 1:
        sys.exit("invalid arguments")
    target = args[0]

    if target == "build":
        build()
    elif target == "clean":
        clean()
    else:
        sys.exit("unknown target {%s}" % target)

def build():
    mkdir_if_necessary("lib")
    download("http://central.maven.org/maven2/net/sf/jopt-simple/jopt-simple/6.0-alpha-2/jopt-simple-6.0-alpha-2.jar")

    mkdir_if_necessary("mods")
    for module_name in MODULES:
        run(["javac",
             "-d", "src",
             "--module-path", "lib",
             "--module-source-path", "src",
             "--module", module_name])
        run(["jar", "--create",
             "--file", "lib/%s.jar" % module_name,
             "--main-class", "com.example.cli.Main",
             "-C", "src/%s" % module_name,
             "."])

def clean():
    try:
        shutil.rmtree("mods")
    except OSError as exc:
        if exc.errno == errno.ENOENT:
            pass
        else:
            raise

    for module_name in MODULES:
        try:
            os.unlink("lib/%s.jar" % module_name)
        except OSError as exc:
            if exc.errno == errno.ENOENT:
                pass
            else:
                raise

if __name__ == "__main__":
    main()
