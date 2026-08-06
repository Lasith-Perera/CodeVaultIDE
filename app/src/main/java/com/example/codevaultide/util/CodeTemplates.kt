package com.example.codevaultide.util

object CodeTemplates {
    fun getInitialCode(fileName: String): String {
        return when {
            fileName.endsWith(".c") -> """
                #include <stdio.h>

                int main() {
                    printf("Hello, World!\n");
                    return 0;
                }
            """.trimIndent()

            fileName.endsWith(".cpp") -> """
                #include <iostream>

                int main() {
                    std::cout << "Hello, World!" << std::endl;
                    return 0;
                }
            """.trimIndent()

            fileName.endsWith(".java") -> """
                public class Main {
                    public static void main(String[] args) {
                        System.out.println("Hello, World!");
                    }
                }
            """.trimIndent()

            fileName.endsWith(".py") -> """
                # Python 3
                def main():
                    print("Hello, World!")

                if __name__ == "__main__":
                    main()
            """.trimIndent()

            fileName.endsWith(".js") -> """
                // JavaScript
                function main() {
                    console.log("Hello, World!");
                }

                main();
            """.trimIndent()

            fileName.endsWith(".ts") -> """
                // TypeScript
                function main(): void {
                    console.log("Hello, World!");
                }

                main();
            """.trimIndent()

            fileName.endsWith(".rs") -> """
                fn main() {
                    println!("Hello, World!");
                }
            """.trimIndent()

            fileName.endsWith(".kt") -> """
                fun main() {
                    println("Hello, World!")
                }
            """.trimIndent()

            fileName.endsWith(".cs") -> """
                using System;

                class Program {
                    static void Main() {
                        Console.WriteLine("Hello, World!");
                    }
                }
            """.trimIndent()

            fileName.endsWith(".html") -> """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <title>Document</title>
                </head>
                <body>
                    <h1>Hello, World!</h1>
                </body>
                </html>
            """.trimIndent()

            fileName.endsWith(".txt") -> """
                Welcome to CodeVault IDE!
            """.trimIndent()

            else -> "// New File: $fileName\n"
        }
    }
}