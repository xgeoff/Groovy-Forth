package biz.digitalindustry.groovyforth

class Forth {
    List<Integer> stack = []
    Map<String, WordEntry> dictionary = [:]

    class WordEntry {
        int address
        Closure behavior = null
    }

    boolean defining = false
    String currentWord = null
    List<String> currentDefinition = []
    Map<Integer, Integer> memory = [:]  // sparse memory
    int here = 0                        // dictionary pointer, like DP in Jones FORTH


    Forth() {
        // Core primitives
        addWordEntry('.' , { println(stack.pop()) })
        dictionary['DUP'] = { def a = stack.peek(); stack << a }
        dictionary['DROP'] = { stack.pop() }
        dictionary['SWAP'] = { def b = stack.pop(); def a = stack.pop(); stack << b; stack << a }
        dictionary['OVER'] = { def b = stack[-2]; stack << b }
        dictionary['+'] = {
            def b = stack.pop();
            def a = stack.pop();
            stack << (a + b)
        }
        dictionary['-'] = { def b = stack.pop(); def a = stack.pop(); stack << (a - b) }
        dictionary['*'] = { def b = stack.pop(); def a = stack.pop(); stack << (a * b) }
        dictionary['/'] = {
            def b = stack.pop()
            def a = stack.pop()
            stack << (a.intdiv(b))  // safe integer division
        }
        dictionary['MOD'] = {
            def b = stack.pop()
            def a = stack.pop()
            stack << (a % b)
        }
        dictionary['='] = {
            def b = stack.pop()
            def a = stack.pop()
            stack << (a == b ? -1 : 0)  // FORTH truth: -1 is true, 0 is false
        }
        dictionary['<'] = {
            def b = stack.pop()
            def a = stack.pop()
            stack << (a < b ? -1 : 0)
        }
        dictionary['>'] = {
            def b = stack.pop()
            def a = stack.pop()
            stack << (a > b ? -1 : 0)
        }
        dictionary['EMIT'] = {
            def code = stack.pop()
            print((char) code)
        }
        dictionary['CR'] = {
            println ""
        }
        dictionary['/MOD'] = {
            def b = stack.pop()
            def a = stack.pop()
            stack << (a % b)
            stack << (a.intdiv(b))
        }
        dictionary['1+'] = {
            def a = stack.pop()
            stack << (a + 1)
        }
        dictionary['1-'] = {
            def a = stack.pop()
            stack << (a - 1)
        }
        dictionary['2+'] = {
            def a = stack.pop()
            stack << (a + 2)
        }
        dictionary['2-'] = {
            def a = stack.pop()
            stack << (a - 2)
        }
        dictionary['='] = {
            def b = stack.pop()
            def a = stack.pop()
            stack << (a == b ? -1 : 0)
        }
        dictionary['<'] = {
            def b = stack.pop()
            def a = stack.pop()
            stack << (a < b ? -1 : 0)
        }
        dictionary['>'] = {
            def b = stack.pop()
            def a = stack.pop()
            stack << (a > b ? -1 : 0)
        }
        dictionary['0='] = {
            def a = stack.pop()
            stack << (a == 0 ? -1 : 0)
        }
        dictionary['0<'] = {
            def a = stack.pop()
            stack << (a < 0 ? -1 : 0)
        }
        dictionary['0>'] = {
            def a = stack.pop()
            stack << (a > 0 ? -1 : 0)
        }
        dictionary['AND'] = {
            def b = stack.pop()
            def a = stack.pop()
            stack << (a & b)
        }
        dictionary['OR'] = {
            def b = stack.pop()
            def a = stack.pop()
            stack << (a | b)
        }
        dictionary['INVERT'] = {
            def a = stack.pop()
            stack << (~a)
        }
        dictionary['ROT'] = {
            def c = stack.pop()
            def b = stack.pop()
            def a = stack.pop()
            stack << b
            stack << c
            stack << a
        }
        dictionary['-ROT'] = {
            def c = stack.pop()
            def b = stack.pop()
            def a = stack.pop()
            stack << c
            stack << a
            stack << b
        }
        dictionary['NIP'] = {
            def b = stack.pop()
            stack.pop()
            stack << b
        }
        dictionary['TUCK'] = {
            def b = stack.pop()
            def a = stack.pop()
            stack << b
            stack << a
            stack << b
        }
        dictionary['2DUP'] = {
            def b = stack[-1]
            def a = stack[-2]
            stack << a
            stack << b
        }
        dictionary['2DROP'] = {
            stack.pop()
            stack.pop()
        }
        dictionary['2SWAP'] = {
            def d = stack.pop()
            def c = stack.pop()
            def b = stack.pop()
            def a = stack.pop()
            stack << c
            stack << d
            stack << a
            stack << b
        }
        dictionary['@'] = {
            def addr = stack.pop()
            stack << (memory[addr] ?: 0)
        }
        dictionary['!'] = {
            def value = stack.pop()
            def addr = stack.pop()
            memory[addr] = value
        }
        dictionary['HERE'] = {
            stack << here
        }
        dictionary['ALLOT'] = {
            def n = stack.pop()
            here += n
        }
        dictionary[','] = {
            def value = stack.pop()
            memory[here] = value
            here += 1
        }
        dictionary['CELL+'] = {
            def addr = stack.pop()
            stack << (addr + 1)
        }
        dictionary['CELLS'] = {
            def n = stack.pop()
            stack << n  // No-op for us; each cell is already size 1
        }
        dictionary['DUMP'] = {
            println "Memory (up to HERE = $here):"
            (0..<here).each { addr ->
                if (memory.containsKey(addr)) {
                    println "$addr: ${memory[addr]}"
                }
            }
        }
        dictionary['CREATE'] = {
            def name = nextToken() // you’ll need a simple token stream (see below)
            dictionary[name.toUpperCase()] = new WordEntry(address: here)
            here += 1  // reserve one cell
        }
        dictionary['DOES>'] = {
            // Capture tokens after DOES> and build a closure
            def behaviorTokens = remainingTokens()
            def lastWord = dictionary.keySet().last()
            def wordEntry = dictionary[lastWord]
            wordEntry.behavior = {
                behaviorTokens.each { token -> eval(token) }
            }
        }


        // Reserved words for defining new words
        dictionary[':'] = { throw new RuntimeException("':' should be handled in eval directly") }
        dictionary[';'] = { throw new RuntimeException("';' should be handled in eval directly") }
    }

    private void addWordEntry(String name, Closure behavior) {
        dictionary[name.toUpperCase()] = new WordEntry(behavior: behavior)
    }

    List<String> tokenStream = []
    int tokenIndex = 0

    String nextToken() {
        if (tokenIndex < tokenStream.size()) return tokenStream[tokenIndex++]
        else throw new RuntimeException("Unexpected end of input")
    }

    List<String> remainingTokens() {
        def remaining = tokenStream.subList(tokenIndex, tokenStream.size())
        tokenIndex = tokenStream.size()
        return remaining
    }

    public List<Integer> evalAndPop(String input, int n) {
        eval(input)
        def results = []
        n.times {
            results << stack.pop()
        }
        return results.reverse() // maintain left-to-right output
    }

    public void eval(String input) {
        tokenStream = input.split(/\s+/).findAll()
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
                    entry.behavior()
                } else {
                    stack << entry.address
                }
            } else {
                throw new RuntimeException("Unknown word: $token")
            }
        }
    }
}