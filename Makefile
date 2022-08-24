.PHONY: build run

LIBRARY = gopher.so
ifeq ($(OS),Windows_NT)
    LIBRARY = gopher.dll
endif

build: $(LIBRARY)

run: build
	java -Djava.library.path=$(CURDIR) --add-modules=jdk.incubator.foreign --enable-native-access=ALL-UNNAMED Main.java


$(LIBRARY): main.go
	go build -o $(LIBRARY) -buildmode=c-shared main.go
