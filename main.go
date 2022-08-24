package main

import "C"
import "fmt"

//export recv
func recv(message string) {
	fmt.Println(message)
}

//export send
func send() string {
	return "Hello Duke! (from Gopher)"
}

// CでGoの関数をコールする方法 - TIPS | Code Macchiato - よりいいコードを、よりDRYで
// https://code-macchiato.com/tips/call-go-function-in-c
func main() {
}
