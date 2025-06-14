package biz.digitalindustry.groovyforth

import spock.lang.Specification

class ForthTickExecuteSpec extends Specification {

    def "tick pushes the behavior of a word and execute runs it"() {
        given:
        def forth = new Forth()

        when: "We define a word and use ' to get its execution token"
        forth.eval(": SQUARE DUP * ;")
        def result = forth.evalAndPop("5 ' SQUARE EXECUTE", 1)

        then: "It executes the behavior of SQUARE"
        result == [25]
    }

    def "tick fails if the word is undefined"() {
        given:
        def forth = new Forth()

        when:
        forth.eval("' UNDEFINEDWORD")

        then:
        def e = thrown(RuntimeException)
        e.message.contains("Undefined word")
    }

    def "execute fails if top of stack is not a closure"() {
        given:
        def forth = new Forth()

        when:
        forth.eval("42 EXECUTE")

        then:
        def e = thrown(RuntimeException)
        e.message.contains("EXECUTE expected a Closure")
    }
}
