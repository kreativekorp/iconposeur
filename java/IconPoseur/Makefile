SRCFILES := $(shell find src -name \*.java)
PACKAGES := $(shell find src -name \*.java | sed -E s/^src\\/\|\\/[^\\/]+$$//g | sort | uniq | tr / .)

all: clean bin doc iconposeur.jar Icon\ Poseur.app

eclipseall: eclipseclean iconposeur.jar Icon\ Poseur.app

clean:
	rm -rf bin
	rm -rf doc
	rm -rf *.jar
	rm -rf *.app

eclipseclean:
	rm -rf *.jar
	rm -rf *.app

bin: src
	mkdir -p bin
	if ! javac -Xbootclasspath/p:dep/javaDesktopStub.jar -classpath dep/orange-extensions.jar -sourcepath src $(SRCFILES) -d bin; then javac -classpath dep/orange-extensions.jar -sourcepath src $(SRCFILES) -d bin; fi

doc: src
	mkdir -p doc
	javadoc -classpath dep/javaDesktopStub.jar:dep/orange-extensions.jar -sourcepath src $(PACKAGES) -d doc

iconposeur.jar: bin
	export COPYFILE_DISABLE=true
	find bin -name .DS_Store -delete
	jar cmf dep/MANIFEST.MF iconposeur.jar -C bin com/kreative/iconposeur
	chmod +x iconposeur.jar

Icon\ Poseur.app: dep iconposeur.jar
	mkdir -p Icon\ Poseur.app/Contents/MacOS
	mkdir -p Icon\ Poseur.app/Contents/Resources/Java
	cp -f dep/PkgInfo Icon\ Poseur.app/Contents
	cp -f dep/Info.plist Icon\ Poseur.app/Contents
	cp -f dep/universalJavaApplicationStub Icon\ Poseur.app/Contents/MacOS/Icon\ Poseur
	cp -f dep/iconposeur.icns Icon\ Poseur.app/Contents/Resources
	cp -f dep/jai-*.jar Icon\ Poseur.app/Contents/Resources/Java
	cp -f iconposeur.jar Icon\ Poseur.app/Contents/Resources/Java

install: iconposeur.jar
	sudo mkdir -p /usr/local/lib/
	sudo cp -f iconposeur.jar /usr/local/lib/iconposeur.jar
	sudo chmod -f 644 /usr/local/lib/iconposeur.jar
	sudo mkdir -p /usr/local/bin/
	sudo cp -f dep/iconposeur /usr/local/bin/iconposeur
	sudo chmod -f 755 /usr/local/bin/iconposeur
	sudo mkdir -p /usr/share/icons/hicolor/128x128/apps/
	sudo cp -f dep/iconposeur.png /usr/share/icons/hicolor/128x128/apps/iconposeur.png
	sudo chmod -f 644 /usr/share/icons/hicolor/128x128/apps/iconposeur.png
	sudo mkdir -p /usr/share/applications/
	sudo cp -f dep/iconposeur.desktop /usr/share/applications/iconposeur.desktop
	sudo chmod -f 644 /usr/share/applications/iconposeur.desktop

.PHONY: all eclipseall clean eclipseclean install
