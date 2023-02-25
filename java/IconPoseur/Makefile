SRCFILES := $(shell find src -name \*.java)
RESFILES := $(shell find src -type f \! -name .\* \! -name \*.java | sed s/^src\\///)
PACKAGES := $(shell find src -name \*.java | sed -E s/^src\\/\|\\/[^\\/]+$$//g | grep -v \\.java$$ | sort | uniq | tr / .)

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
ifeq ($(origin JAVACFLAGS), undefined)
	$(eval JAVACFLAGS = $(shell dep/minJavaVerOpts))
endif
	javac $(JAVACFLAGS) -classpath dep/ual.jar -sourcepath src $(SRCFILES) -d bin
	$(foreach res,$(RESFILES),cp src/$(res) bin/$(res);)

doc: src
	mkdir -p doc
	javadoc $(JAVADOCFLAGS) -classpath dep/ual.jar -sourcepath src $(PACKAGES) -d doc

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

Icon\ Poseur.app: Icon\ Poseur-Pre10.15.app Icon\ Poseur-MacOS10.15.app Icon\ Poseur-MacOS11.0.app

Icon\ Poseur-Pre10.15.app: dep iconposeur.jar
	mkdir -p Icon\ Poseur-Pre10.15.app/Contents/MacOS
	mkdir -p Icon\ Poseur-Pre10.15.app/Contents/Resources/Java
	cp -f dep/PkgInfo Icon\ Poseur-Pre10.15.app/Contents
	cp -f dep/Info.plist Icon\ Poseur-Pre10.15.app/Contents
	cp -f dep/universalJavaApplicationStub-Pre10.15 Icon\ Poseur-Pre10.15.app/Contents/MacOS/Icon\ Poseur
	cp -f dep/iconposeur.icns Icon\ Poseur-Pre10.15.app/Contents/Resources
	cp -f dep/*.jar Icon\ Poseur-Pre10.15.app/Contents/Resources/Java
	cp -f iconposeur.jar Icon\ Poseur-Pre10.15.app/Contents/Resources/Java

Icon\ Poseur-MacOS10.15.app: dep iconposeur.jar
	mkdir -p Icon\ Poseur-MacOS10.15.app/Contents/MacOS
	mkdir -p Icon\ Poseur-MacOS10.15.app/Contents/Resources/Java
	cp -f dep/PkgInfo Icon\ Poseur-MacOS10.15.app/Contents
	cp -f dep/Info.plist Icon\ Poseur-MacOS10.15.app/Contents
	cp -f dep/universalJavaApplicationStub-MacOS10.15 Icon\ Poseur-MacOS10.15.app/Contents/MacOS/Icon\ Poseur
	cp -f dep/iconposeur.icns Icon\ Poseur-MacOS10.15.app/Contents/Resources
	cp -f dep/*.jar Icon\ Poseur-MacOS10.15.app/Contents/Resources/Java
	cp -f iconposeur.jar Icon\ Poseur-MacOS10.15.app/Contents/Resources/Java

Icon\ Poseur-MacOS11.0.app: dep iconposeur.jar
	mkdir -p Icon\ Poseur-MacOS11.0.app/Contents/MacOS
	mkdir -p Icon\ Poseur-MacOS11.0.app/Contents/Resources/Java
	cp -f dep/PkgInfo Icon\ Poseur-MacOS11.0.app/Contents
	cp -f dep/Info.plist Icon\ Poseur-MacOS11.0.app/Contents
	cp -f dep/universalJavaApplicationStub-MacOS11.0 Icon\ Poseur-MacOS11.0.app/Contents/MacOS/Icon\ Poseur
	cp -f dep/iconposeur.icns Icon\ Poseur-MacOS11.0.app/Contents/Resources
	cp -f dep/*.jar Icon\ Poseur-MacOS11.0.app/Contents/Resources/Java
	cp -f iconposeur.jar Icon\ Poseur-MacOS11.0.app/Contents/Resources/Java

iconposeur.dmg: Icon\ Poseur.app
	rm -rf dmgtmp
	mkdir -p dmgtmp
	cp -R Icon\ Poseur*.app dmgtmp
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

.PHONY: all eclipseall clean eclipseclean Icon\ Poseur.app install
