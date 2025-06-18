package biz.digitalindustry.groovyforth

class Forth {

    Forth() {}

    public void interpret(String input) {
        RuntimeContext context = new RuntimeContext()
        context.inputTokens = input.split(/\s+/).findAll()
        interpret(context)
    }

    public static void interpret(RuntimeContext context) {
        //List<String> input = context.getInputTokens()
        boolean defining = false
        String currentWord = null
        List<String> currentDefinition = []
        Stack stack = context.getStack()
        Map<String, WordEntry> dictionary = context.getDictionary()

        while (context.tokenIndex < context.inputTokens.size()) {
            def token = context.inputTokens[context.tokenIndex]
            def upper = token.toUpperCase()

            if (defining) {
                if (token == ';') {
                    dictionary[currentWord.toUpperCase()] = new WordEntry(
                            tokenStream: currentDefinition.collect()
                    )
                    defining = false
                    currentWord = null
                    currentDefinition.clear()
                } else if (currentWord == null) {
                    currentWord = token
                } else {
                    currentDefinition << token
                }
            } else if (token == ':') {
                defining = true
            } else if (token.isInteger()) {
                stack << token.toInteger()
            } else if (dictionary.containsKey(upper)) {
                def entry = dictionary[upper]
                entry.execute(context)
            } else {
                throw new RuntimeException("Unknown word: $token")
            }
            context.tokenIndex++
        }
        context.tokenIndex = 0
    }
}
