# duke-gopher
JEP419 sample (Call to Golang from Java)

## How to run

Install [Java 18 (OpenJDK)](https://adoptium.net/temurin/releases?version=18), [Go](https://go.dev/dl/) + [gcc](https://gcc.gnu.org/install/binaries.html).

```sh
go build -o libgopher.so -buildmode=c-shared main.go

java -Djava.library.path=$(pwd) --add-modules=jdk.incubator.foreign --enable-native-access=ALL-UNNAMED Main.java
```

or `make run`

## Related Documents

[JEP 419: Foreign Function &amp; Memory API](https://openjdk.org/jeps/419)
