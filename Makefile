.PHONY: build run

LIBRARY = gopher.so
ifeq ($(OS),Windows_NT)
    LIBRARY = gopher.dll
endif

build: $(LIBRARY) Main.class

run: build
	java -Djava.library.path=D:\Program\duke-gopher --add-modules=jdk.incubator.foreign --enable-native-access=ALL-UNNAMED Main

$(LIBRARY): main.go
	go build -o $(LIBRARY) -buildmode=c-shared main.go

Main.class: Main.java
	javac --add-modules=jdk.incubator.foreign Main.java
