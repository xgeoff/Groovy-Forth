package biz.digitalindustry.groovyforth

class RuntimeContext {
    Stack<Object> stack
    List<String> inputTokens  // the active execution stream
    Map<String, WordEntry> dictionary
    Map<Integer, Integer> memory
    int here = 0
    int tokenIndex = 0

    RuntimeContext() {
        stack = new Stack()
        dictionary = new Dictionary()
        memory = [:]
    }

    RuntimeContext(RuntimeContext parent, List<String> tokenStream){
        this.stack = parent.stack
        this.inputTokens = tokenStream
        this.dictionary = parent.dictionary
        this.memory = parent.memory
        this.here = parent.here
    }

    RuntimeContext(Stack<Object> stack, List<String> inputTokens, Map<String, WordEntry> dictionary, Map<Integer, Integer> memory, int here) {
        this.stack = stack
        this.inputTokens = inputTokens
        this.dictionary = dictionary
        this.memory = memory
        this.here = here
    }
/*
    void interpret(List<String> tokens) {
        int idx = 0
        while (idx < tokens.size()) {
            String token = tokens[idx]
            def word = dictionary[token]
            if (word) {
                word.execute(this)
            } else if (token.isNumber()) {
                stack.push(token.toInteger())
            } else {
                throw new RuntimeException("Unknown token: $token")
            }
            idx += 1
        }
    }*/
}