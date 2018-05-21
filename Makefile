default: build doc

CENTRAL = http://central.maven.org/maven2
JAVAC_ARGS = -Xlint:all -Xlint:-requires-automatic -Xdoclint:all
MODULE = com.example.mainmod
JAR_NAME = mainmod.jar

lib:
	mkdir $@

lib/jopt-simple-6.0-alpha-2.jar: lib
	wget $(CENTRAL)/net/sf/jopt-simple/jopt-simple/6.0-alpha-2/jopt-simple-6.0-alpha-2.jar --output-document $@

build:
	javac $(JAVAC_ARGS) -d src --module-path lib --module-source-path src --module $(MODULE)
	jar --create --file lib/$(JAR_NAME) --main-class com.example.cli.Main --manifest src/$(MODULE).mf -C src/$(MODULE) .

clean:
	rm -f lib/$(JAR_NAME)
	find -name *.class -delete

.PHONY: doc
doc:
	mkdir -p doc
	javadoc -html5 --module $(MODULE) --module-path lib --module-source-path src -d doc `find src/$(MODULE) -name \*.java`
