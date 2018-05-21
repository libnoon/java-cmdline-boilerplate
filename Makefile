default: build

JAVAC_ARGS = -g --release 10 -Xlint:all -Xdoclint:all
MODULE = com.example.mainmod
JAR_NAME = mainmod.jar

lib:
	mkdir $@

lib/jopt-simple-6.0-alpha-2.jar: lib
	wget http://central.maven.org/maven2/net/sf/jopt-simple/jopt-simple/6.0-alpha-2/jopt-simple-6.0-alpha-2.jar --output-document $@

build:
	javac $(JAVAC_ARGS) -d src --module-path lib --module-source-path src --module $(MODULE)
	jar --create --file lib/$(JAR_NAME) --main-class com.example.cli.Main --manifest src/$(MODULE).mf -C src/$(MODULE) .

clean:
	rm -f lib/$(JAR_NAME)
	find -name *.class -delete
