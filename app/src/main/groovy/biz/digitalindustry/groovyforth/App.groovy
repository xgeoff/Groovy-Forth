package biz.digitalindustry.groovyforth

// App.groovy

class App {
    static void main(String[] args) {
        if (args.length == 0) {
            println "Usage: groovy Main.groovy <file.fth | -e \"forth code\"]"
            return
        }

        def forth = new Forth()

        if (args[0] == "-e" && args.length >= 2) {
            // Direct code execution
            def code = args[1..-1].join(" ")
            forth.eval(code)
        } else {
            // Treat first arg as filename
            def file = new File(args[0])
            if (!file.exists()) {
                println "File not found: ${args[0]}"
                return
            }
            def code = file.text
            forth.eval(code)
        }
    }
}

