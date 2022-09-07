# duke-gopher
Foreign Function & Memory API sample (Call to Golang from Java)

## How to run

Install [Java 18](https://adoptium.net/temurin/releases?version=18) or [Java 19](https://adoptium.net/temurin/releases?version=19), [Go](https://go.dev/dl/) + [gcc](https://gcc.gnu.org/install/binaries.html).

```sh
go build -o libgopher.so -buildmode=c-shared main.go

# Java 18
java -Djava.library.path=$(pwd) --add-modules=jdk.incubator.foreign --enable-native-access=ALL-UNNAMED JEP419.java

# Java 19
java -Djava.library.path=$(pwd) --enable-native-access=ALL-UNNAMED --enable-preview --source 19 JEP424.java
```

or `make run`

## Related Documents

* [JEP 419: Foreign Function & Memory API (Second Incubator)](https://openjdk.org/jeps/419)
* [JEP 424: Foreign Function & Memory API (Preview)](https://openjdk.org/jeps/424)
