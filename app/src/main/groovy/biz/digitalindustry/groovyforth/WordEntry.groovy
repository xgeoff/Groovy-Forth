package biz.digitalindustry.groovyforth

class WordEntry {
    int address = -1                           // Optional memory address for compiled words
    Closure<RuntimeContext> behavior = null  // Native word behavior
    List<String> tokenStream = null          // Token stream for user-defined word

    void execute(RuntimeContext context) {
        if (behavior != null) {
            behavior.call(context)
        } else if (tokenStream != null) {
            def ctx = new RuntimeContext(context.stack, tokenStream, context.dictionary, context.memory, context.here)
            Forth.interpret(ctx)
        } else if (address > -1) {
            context.stack << address
        } else {
            throw new IllegalStateException("Word has no behavior or tokenStream")
        }
    }
}
