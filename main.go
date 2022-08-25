package main

import "C"
import "fmt"

//export talk
func talk(message string) string {
	fmt.Println(message)

	return "Hello Duke! (from Gopher)"
}

// CでGoの関数をコールする方法 - TIPS | Code Macchiato - よりいいコードを、よりDRYで
// https://code-macchiato.com/tips/call-go-function-in-c
func main() {
}
