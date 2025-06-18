package biz.digitalindustry.groovyforth

class OldForth {
    protected List<Integer> stack = new Stack()
    Map<String, WordEntry> dictionary = [:]
    Map<Integer, Integer> memory = [:]
    int here = 0

    boolean defining = false
    String currentWord = null
    List<String> currentDefinition = []
    List<String> tokenStream = []
    int tokenIndex = 0

    class WordEntry {
        int address = 0
        Closure behavior = null
    }

    OldForth() {
        addWordEntry('.', { println(stack.pop()) })
        addWordEntry('DUP', { def a = stack.peek(); stack << a })
        addWordEntry('DROP', { stack.pop() })
        addWordEntry('SWAP', { def b = stack.pop(); def a = stack.pop(); stack << b; stack << a })
        addWordEntry('OVER', { def b = stack[-2]; stack << b })
        addWordEntry('+', { def b = stack.pop(); def a = stack.pop(); stack << (a + b) })
        addWordEntry('-', { def b = stack.pop(); def a = stack.pop(); stack << (a - b) })
        addWordEntry('*', { def b = stack.pop(); def a = stack.pop(); stack << (a * b) })
        addWordEntry('/', { def b = stack.pop(); def a = stack.pop(); stack << (a.intdiv(b)) })
        addWordEntry('MOD', { def b = stack.pop(); def a = stack.pop(); stack << (a % b) })
        addWordEntry('=', { def b = stack.pop(); def a = stack.pop(); stack << (a == b ? -1 : 0) })
        addWordEntry('<', { def b = stack.pop(); def a = stack.pop(); stack << (a < b ? -1 : 0) })
        addWordEntry('>', { def b = stack.pop(); def a = stack.pop(); stack << (a > b ? -1 : 0) })
        addWordEntry('EMIT', { def code = stack.pop(); print((char) code) })
        addWordEntry('CR', { println "" })
        addWordEntry('/MOD', {
            def rhs = stack.pop()
            def lhs = stack.pop()
            stack << (lhs % rhs)
            stack << (lhs.intdiv(rhs))
        })
        addWordEntry('1+', { def a = stack.pop(); stack << (a + 1) })
        addWordEntry('1-', { def a = stack.pop(); stack << (a - 1) })
        addWordEntry('2+', { def a = stack.pop(); stack << (a + 2) })
        addWordEntry('2-', { def a = stack.pop(); stack << (a - 2) })
        addWordEntry('0=', { def a = stack.pop(); stack << (a == 0 ? -1 : 0) })
        addWordEntry('0<', { def a = stack.pop(); stack << (a < 0 ? -1 : 0) })
        addWordEntry('0>', { def a = stack.pop(); stack << (a > 0 ? -1 : 0) })
        addWordEntry('AND', { def b = stack.pop(); def a = stack.pop(); stack << (a & b) })
        addWordEntry('OR', { def b = stack.pop(); def a = stack.pop(); stack << (a | b) })
        addWordEntry('INVERT', { def a = stack.pop(); stack << (~a) })
        addWordEntry('ROT', {
            def c = stack.pop()
            def b = stack.pop()
            def a = stack.pop()
            stack << b
            stack << c
            stack << a
        })
        addWordEntry('-ROT', {
            def c = stack.pop()
            def b = stack.pop()
            def a = stack.pop()
            stack << c
            stack << a
            stack << b
        })
        addWordEntry('NIP', {
            def b = stack.pop()
            stack.pop()
            stack << b
        })
        addWordEntry('TUCK', {
            def b = stack.pop()
            def a = stack.pop()
            stack << b
            stack << a
            stack << b
        })
        addWordEntry('2DUP', {
            def b = stack[-1]
            def a = stack[-2]
            stack << a
            stack << b
        })
        addWordEntry('2DROP', {
            stack.pop()
            stack.pop()
        })
        addWordEntry('2SWAP', {
            def d = stack.pop()
            def c = stack.pop()
            def b = stack.pop()
            def a = stack.pop()
            stack << c
            stack << d
            stack << a
            stack << b
        })
        addWordEntry('@', {
            def addr = stack.pop()
            stack << (memory[addr] ?: 0)
        })
        addWordEntry('!', {
            def addr = stack.pop()
            def value = stack.pop()
            memory[addr] = value
        })
        addWordEntry('HERE', { stack << here })
        addWordEntry('ALLOT', {
            def n = stack.pop()
            here += n
        })
        addWordEntry(',', {
            def value = stack.pop()
            memory[here] = value
            here += 1
        })
        addWordEntry('CELL+', {
            def addr = stack.pop()
            stack << (addr + 1)
        })
        addWordEntry('CELLS', {
            def n = stack.pop()
            stack << n
        })
        addWordEntry('DUMP', {
            println "Memory (up to HERE = $here):"
            (0..<here).each { addr ->
                if (memory.containsKey(addr)) {
                    println "$addr: ${memory[addr]}"
                }
            }
        })
        addWordEntry('CREATE', {
            def name = nextToken()
            def addr = here
            dictionary[name.toUpperCase()] = new WordEntry(
                    address: addr,
                    behavior: { stack << addr }
            )
        })
        addWordEntry('DOES>', {
            def behaviorTokens = remainingTokens()
            def lastWord = dictionary.keySet().last()
            def wordEntry = dictionary[lastWord]
            wordEntry.behavior = {
                behaviorTokens.each { token -> eval(token) }
            }
        })
        addWordEntry(':', {
            throw new RuntimeException("':' should be handled in eval directly")
        })
        addWordEntry(';', {
            throw new RuntimeException("';' should be handled in eval directly")
        })
        addWordEntry("\'", {
            def name = nextToken().toUpperCase()
            if (!dictionary.containsKey(name)) {
                throw new RuntimeException("Undefined word: $name")
            }
            def entry = dictionary[name]
            stack << entry.behavior
        })
        addWordEntry('EXECUTE', {
            def behavior = stack.pop()
            if (behavior instanceof Closure) {
                behavior.call()
            } else {
                throw new RuntimeException("EXECUTE expected a Closure on the stack")
            }
        })
        addWordEntry('?BRANCH', {
            def jumpTo = currentExecutionTokens[++executionIndex]
            def flag = stack.pop()
            if (flag == 0) {
                executionIndex = jumpTo.toInteger() - 1 // -1 to counter ++ later
            }
        })
        addWordEntry('BRANCH', {
            def jumpTo = currentExecutionTokens[++executionIndex]
            executionIndex = jumpTo.toInteger() - 1
        })

    }

    private void addWordEntry(String name, Closure behavior) {
        dictionary[name.toUpperCase()] = new WordEntry(behavior: behavior)
    }

    String nextToken() {
        if (tokenIndex < tokenStream.size()) return tokenStream[tokenIndex++]
        else throw new RuntimeException("Unexpected end of input")
    }

    List<String> remainingTokens() {
        def remaining = tokenStream.subList(tokenIndex, tokenStream.size())
        tokenIndex = tokenStream.size()
        return remaining
    }

    List<Integer> evalAndPop(String input, int n) {
        eval(input)
        def results = []
        n.times { results << stack.pop() }
        return results.reverse()
    }

    void eval(String input) {
        List<String>tokenStream = input.split(/\s+/).findAll()
        eval(tokenStream)
    }

    void eval(List<String> tokenStream) {
        tokenIndex = 0

        while (tokenIndex < tokenStream.size()) {
            def token = nextToken()
            def upper = token.toUpperCase()

            if (defining) {
                if (token == ';') {
                    def body = currentDefinition.collect()
                    dictionary[currentWord.toUpperCase()] = new WordEntry(
                            address: here,
                            behavior: {
                                body.each { word -> eval(word) }
                            }
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
                if (entry.behavior != null) {
                    entry.behavior.call()
                } else {
                    stack << entry.address
                }
            } else {
                throw new RuntimeException("Unknown word: $token")
            }
        }
    }
    class Macro {
        List<String> tokenStream = []
    }
}
