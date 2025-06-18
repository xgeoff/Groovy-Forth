package biz.digitalindustry.groovyforth

class TestForth {

    RuntimeContext context = new RuntimeContext()
    Forth forth = new Forth()

    public void eval(String input) {
        context.inputTokens = input.split(/\s+/).findAll()
        forth.interpret(context)
    }

    public List<Object> evalAndPop(String input, int n) {
        context.inputTokens = input.split(/\s+/).findAll()
        forth.interpret(context)
        def results = []
        def stack = context.getStack()
        n.times { results << stack.pop() }
        return results.reverse()
    }
}
