.PHONY: build run

LIBRARY = gopher.so
ifeq ($(OS),Windows_NT)
    LIBRARY = gopher.dll
endif

build: $(LIBRARY)

run: build
	java -Djava.library.path=$(CURDIR) --add-modules=jdk.incubator.foreign --enable-native-access=ALL-UNNAMED Main.java

run19: build
	C:\Program Files\Eclipse Adoptium\jdk-19.0.0.36-hotspot\bin\java -Djava.library.path=$(CURDIR) --enable-native-access=ALL-UNNAMED --enable-preview --source 19 Main19.java

$(LIBRARY): main.go
	go build -o $(LIBRARY) -buildmode=c-shared main.go
