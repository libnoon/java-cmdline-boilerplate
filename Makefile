default: build

CENTRAL = http://central.maven.org/maven2
JAVAC_ARGS = -Xlint:all -Xlint:-requires-automatic -Xdoclint:all -deprecation
MODULE = com.example.mainmod
JAR_NAME = mainmod.jar

lib/jopt-simple-6.0-alpha-2.jar:
	-mkdir -p lib
	wget $(CENTRAL)/net/sf/jopt-simple/jopt-simple/6.0-alpha-2/jopt-simple-6.0-alpha-2.jar --output-document $@

deps: lib/jopt-simple-6.0-alpha-2.jar

build: deps
	javac $(JAVAC_ARGS) -d src --module-path lib --module-source-path src --module $(MODULE)
	jar --create --file lib/$(JAR_NAME) --main-class com.example.cli.Main --manifest src/$(MODULE).mf -C src/$(MODULE) .

clean:
	rm -rf lib/$(JAR_NAME) doc/
	find -name *.class -delete

.PHONY: doc
doc:
	mkdir -p doc
	cd src/$(MODULE) && javadoc --class-path ../../lib/\* -html5 -d ../../doc `find -name \*.java`
