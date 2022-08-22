package main

import "C"
import "fmt"

//export recv
func recv(message string) {
	fmt.Println(message)
}

// CでGoの関数をコールする方法 - TIPS | Code Macchiato - よりいいコードを、よりDRYで
// https://code-macchiato.com/tips/call-go-function-in-c
func main() {
}
