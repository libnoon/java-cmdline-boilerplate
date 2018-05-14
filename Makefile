default: build

JAVAC_ARGS = -g -release 10 -Xlint:all

lib:
	mkdir $@

lib/jopt-simple-6.0-alpha-2.jar: lib
	wget http://central.maven.org/maven2/net/sf/jopt-simple/jopt-simple/6.0-alpha-2/jopt-simple-6.0-alpha-2.jar --output-document $@

build:
	javac -d src --module-path lib --module-source-path src --module mainmod
	jar --create --file lib/mainmod.jar --main-class com.example.cli.Main -C src/mainmod .

clean:
	rm -f lib/mainmod.jar
	find -name *.class -delete
