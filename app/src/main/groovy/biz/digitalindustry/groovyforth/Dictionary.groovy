package biz.digitalindustry.groovyforth

class Dictionary extends HashMap<String, WordEntry> {
    //Map<String, WordEntry> dictionary = [:]

    Dictionary() {
        addWordEntry('.',    { ctx -> println(ctx.stack.pop()) })
        addWordEntry('DUP',  { ctx -> def a = ctx.stack.peek(); ctx.stack << a })
        addWordEntry('DROP', { ctx -> ctx.stack.pop() })
        addWordEntry('SWAP', { ctx -> def b = ctx.stack.pop(); def a = ctx.stack.pop(); ctx.stack << b; ctx.stack << a })
        addWordEntry('OVER', { ctx -> def b = ctx.stack[-2]; ctx.stack << b })
        addWordEntry('+',    { ctx ->
            def b = ctx.stack.pop();
            def a = ctx.stack.pop();
            ctx.stack << (a + b)
        })
        addWordEntry('-',    { ctx ->
            def b = ctx.stack.pop();
            def a = ctx.stack.pop();
            ctx.stack << (a - b)
        })
        addWordEntry('*',    { ctx -> def b = ctx.stack.pop(); def a = ctx.stack.pop(); ctx.stack << (a * b) })
        addWordEntry('/',    { ctx -> def b = ctx.stack.pop(); def a = ctx.stack.pop(); ctx.stack << (a.intdiv(b)) })
        addWordEntry('MOD',  { ctx -> def b = ctx.stack.pop(); def a = ctx.stack.pop(); ctx.stack << (a % b) })
        addWordEntry('=',    { ctx -> def b = ctx.stack.pop(); def a = ctx.stack.pop(); ctx.stack << (a == b ? -1 : 0) })
        addWordEntry('<',    { ctx -> def b = ctx.stack.pop(); def a = ctx.stack.pop(); ctx.stack << (a < b ? -1 : 0) })
        addWordEntry('>',    { ctx -> def b = ctx.stack.pop(); def a = ctx.stack.pop(); ctx.stack << (a > b ? -1 : 0) })
        addWordEntry('EMIT', { ctx -> def code = ctx.stack.pop(); print((char) code) })
        addWordEntry('CR', { println "" })
        addWordEntry('/MOD',   { ctx -> def rhs = ctx.stack.pop(); def lhs = ctx.stack.pop(); ctx.stack << (lhs % rhs); ctx.stack << (lhs.intdiv(rhs)) })
        addWordEntry('1+',     { ctx -> def a = ctx.stack.pop(); ctx.stack << (a + 1) })
        addWordEntry('1-',     { ctx -> def a = ctx.stack.pop(); ctx.stack << (a - 1) })
        addWordEntry('2+',     { ctx -> def a = ctx.stack.pop(); ctx.stack << (a + 2) })
        addWordEntry('2-',     { ctx -> def a = ctx.stack.pop(); ctx.stack << (a - 2) })
        addWordEntry('0=',     { ctx -> def a = ctx.stack.pop(); ctx.stack << (a == 0 ? -1 : 0) })
        addWordEntry('0<',     { ctx -> def a = ctx.stack.pop(); ctx.stack << (a < 0 ? -1 : 0) })
        addWordEntry('0>',     { ctx -> def a = ctx.stack.pop(); ctx.stack << (a > 0 ? -1 : 0) })
        addWordEntry('AND',    { ctx -> def b = ctx.stack.pop(); def a = ctx.stack.pop(); ctx.stack << (a & b) })
        addWordEntry('OR',     { ctx -> def b = ctx.stack.pop(); def a = ctx.stack.pop(); ctx.stack << (a | b) })
        addWordEntry('INVERT', { ctx -> def a = ctx.stack.pop(); ctx.stack << (~a) })
        addWordEntry('ROT',    { ctx -> def c = ctx.stack.pop(); def b = ctx.stack.pop(); def a = ctx.stack.pop(); ctx.stack << b; ctx.stack << c; ctx.stack << a })
        addWordEntry('-ROT',   { ctx -> def c = ctx.stack.pop(); def b = ctx.stack.pop(); def a = ctx.stack.pop(); ctx.stack << c; ctx.stack << a; ctx.stack << b })
        addWordEntry('NIP',    { ctx -> def b = ctx.stack.pop(); ctx.stack.pop(); ctx.stack << b })
        addWordEntry('TUCK',   { ctx -> def b = ctx.stack.pop(); def a = ctx.stack.pop(); ctx.stack << b; ctx.stack << a; ctx.stack << b })
        addWordEntry('2DUP',   { ctx -> def b = ctx.stack[-1]; def a = ctx.stack[-2]; ctx.stack << a; ctx.stack << b })
        addWordEntry('2DROP',  { ctx -> ctx.stack.pop(); ctx.stack.pop() })
        addWordEntry('2SWAP',  { ctx -> def d = ctx.stack.pop(); def c = ctx.stack.pop(); def b = ctx.stack.pop(); def a = ctx.stack.pop(); ctx.stack << c; ctx.stack << d; ctx.stack << a; ctx.stack << b })
        addWordEntry('@',      { ctx -> def addr = ctx.stack.pop(); ctx.stack << (ctx.memory[addr] ?: 0) })
        addWordEntry('!',      { ctx -> def addr = ctx.stack.pop(); def value = ctx.stack.pop(); ctx.memory[addr] = value })
        addWordEntry('HERE',   { ctx -> ctx.stack << ctx.here })
        addWordEntry('ALLOT',  { ctx -> def n = ctx.stack.pop(); ctx.here += n })
        addWordEntry(',',      { ctx -> def value = ctx.stack.pop(); ctx.memory[ctx.here] = value; ctx.here += 1 })
        addWordEntry('CELL+',  { ctx -> def addr = ctx.stack.pop(); ctx.stack << (addr + 1) })
        addWordEntry('CELLS',  { ctx -> def n = ctx.stack.pop(); ctx.stack << n })

        addWordEntry('DUMP',   {
            ctx ->
                println "Memory (up to HERE = ${ctx.here}):"
                (0..<ctx.here).each { addr ->
                    if (ctx.memory.containsKey(addr)) {
                        println "$addr: ${ctx.memory[addr]}"
                    }
                }
        })

        addWordEntry('CREATE', {
            ctx ->
                def name = ctx.inputTokens[++ctx.tokenIndex]
                def addr = ctx.here
                ctx.dictionary[name.toUpperCase()] = new WordEntry(
                        address: addr,
                        behavior: { innerCtx -> innerCtx.stack << addr }
                )
        })

        /*addWordEntry('DOES>', {
            def behaviorTokens = remainingTokens()
            def lastWord = dictionary.keySet().last()
            def wordEntry = dictionary[lastWord]
            wordEntry.behavior = {
                behaviorTokens.each { token -> eval(token) }
            }
        })*/
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
        addWordEntry('IF', { ctx ->
            def flag = ctx.stack.pop()
            if (flag == 0) {
                // Skip to ELSE or THEN
                int depth = 1
                while (ctx.tokenIndex + 1 < ctx.inputTokens.size() && depth > 0) {
                    ctx.tokenIndex++
                    def t = ctx.inputTokens[ctx.tokenIndex].toUpperCase()
                    if (t == 'IF') depth++
                    else if (t == 'THEN') depth--
                    else if (t == 'ELSE' && depth == 1) break
                }
            }
        })

        addWordEntry('ELSE', { ctx ->
            // Skip to THEN
            int depth = 1
            while (ctx.tokenIndex + 1 < ctx.inputTokens.size() && depth > 0) {
                ctx.tokenIndex++
                def t = ctx.inputTokens[ctx.tokenIndex].toUpperCase()
                if (t == 'IF') depth++
                else if (t == 'THEN') depth--
            }
        })

        addWordEntry('THEN', { ctx ->
            // No-op — handled by control logic
        })

        addWordEntry('DOES>', { ctx ->
            def bodyTokens = []
            ctx.tokenIndex++
            while (ctx.tokenIndex < ctx.inputTokens.size()) {
                def token = ctx.inputTokens[ctx.tokenIndex]
                if (token == ';') break
                bodyTokens << token
                ctx.tokenIndex++
            }

            def wordName = ctx.lastCreatedWord
            if (wordName == null || !ctx.dictionary.containsKey(wordName)) {
                throw new RuntimeException("DOES> used without a preceding CREATE")
            }

            def word = ctx.dictionary[wordName]
            def addr = word.address

            word.behavior = { innerCtx ->
                innerCtx.stack << addr
                def scopedCtx = new RuntimeContext(innerCtx, bodyTokens)
                Forth.interpret(scopedCtx)
            }
        })

    }
    void addWordEntry(String name, Closure behavior) {
        this[name.toUpperCase()] = new WordEntry(behavior: behavior)
    }
}
