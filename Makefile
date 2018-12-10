default: build

CENTRAL = http://central.maven.org/maven2
JAVAC_ARGS = -Xlint:all -Xlint:-requires-automatic -Xdoclint:all
MODULE = com.example.mainmod

lib/jopt-simple-6.0-alpha-2.jar:
	-mkdir -p lib
	wget $(CENTRAL)/net/sf/jopt-simple/jopt-simple/6.0-alpha-2/jopt-simple-6.0-alpha-2.jar --output-document $@

deps: lib/jopt-simple-6.0-alpha-2.jar

build: deps
	cd src/$(MODULE) && javac $(JAVAC_ARGS) --class-path ../../lib/\* `find -name \*.java`

clean:
	rm -rf doc/
	find -name *.class -delete

.PHONY: doc
doc:
	mkdir -p doc
	javadoc -html5 --module $(MODULE) --module-path lib --module-source-path src -d doc `find src/$(MODULE) -name \*.java`
