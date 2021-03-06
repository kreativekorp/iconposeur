SRCFILES := $(shell find src -name \*.java)
PACKAGES := $(shell find src -name \*.java | sed -E s/^src\\/\|\\/[^\\/]+$$//g | sort | uniq | tr / .)

all: clean bin doc iconposeur.jar iconposeur.zip Icon\ Poseur.app

eclipseall: eclipseclean iconposeur.jar iconposeur.zip Icon\ Poseur.app

clean:
	rm -rf bin
	rm -rf doc
	rm -rf *.jar
	rm -rf *.zip
	rm -rf *.app
	rm -rf *.dmg

eclipseclean:
	rm -rf *.jar
	rm -rf *.zip
	rm -rf *.app
	rm -rf *.dmg

bin: src
	mkdir -p bin
	javac -classpath dep/ual.jar -sourcepath src $(SRCFILES) -d bin

doc: src
	mkdir -p doc
	javadoc -classpath dep/ual.jar -sourcepath src $(PACKAGES) -d doc

iconposeur.jar: bin
	export COPYFILE_DISABLE=true
	find bin -name .DS_Store -delete
	jar cmf dep/MANIFEST.MF iconposeur.jar -C bin com/kreative/iconposeur
	chmod +x iconposeur.jar

iconposeur.zip: dep iconposeur.jar
	rm -rf Icon\ Poseur
	mkdir -p Icon\ Poseur
	cp -f dep/IconPoseur.exe Icon\ Poseur
	cp -f dep/*.jar Icon\ Poseur
	cp -f iconposeur.jar Icon\ Poseur
	zip -r iconposeur.zip Icon\ Poseur
	rm -rf Icon\ Poseur

Icon\ Poseur.app: dep iconposeur.jar
	mkdir -p Icon\ Poseur.app/Contents/MacOS
	mkdir -p Icon\ Poseur.app/Contents/Resources/Java
	cp -f dep/PkgInfo Icon\ Poseur.app/Contents
	cp -f dep/Info.plist Icon\ Poseur.app/Contents
	cp -f dep/universalJavaApplicationStub Icon\ Poseur.app/Contents/MacOS/Icon\ Poseur
	cp -f dep/iconposeur.icns Icon\ Poseur.app/Contents/Resources
	cp -f dep/*.jar Icon\ Poseur.app/Contents/Resources/Java
	cp -f iconposeur.jar Icon\ Poseur.app/Contents/Resources/Java

iconposeur.dmg: Icon\ Poseur.app
	rm -rf dmgtmp
	mkdir -p dmgtmp
	cp -R Icon\ Poseur.app dmgtmp
	hdiutil create -fs HFS+ -volname "Icon Poseur" -srcfolder dmgtmp iconposeur.dmg
	rm -rf dmgtmp

install: iconposeur.jar
	sudo mkdir -p /usr/local/lib/
	sudo cp -f dep/jai-imageio-core.jar /usr/local/lib/jai-imageio-core.jar
	sudo cp -f dep/jai-imageio-jpeg2000.jar /usr/local/lib/jai-imageio-jpeg2000.jar
	sudo cp -f dep/ual.jar /usr/local/lib/ual.jar
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
