package biz.digitalindustry.groovyforth

import spock.lang.Specification

class ForthSpec extends Specification {

    def "basic arithmetic and stack behavior using evalAndPop"() {
        given:
        def forth = new Forth()

        expect:
        forth.evalAndPop("2 3 +", 1) == [5]
        forth.evalAndPop("7 1 -", 1) == [6]
        forth.evalAndPop("4 2 /", 1) == [2]
        forth.evalAndPop("10 3 MOD", 1) == [1]
    }

    def "create and access memory with CREATE , @ and !"() {
        given:
        def forth = new Forth()

        when:
        forth.eval("CREATE FOO 123 ,")
        def result1 = forth.evalAndPop("FOO @", 1)

        and:
        forth.eval("456 FOO !")
        def result2 = forth.evalAndPop("FOO @", 1)

        then:
        result1 == [123]
        result2 == [456]
    }

    def "define and invoke a user-defined word"() {
        given:
        def forth = new Forth()

        when:
        forth.eval(": SQUARE DUP * ;")
        def result = forth.evalAndPop("4 SQUARE", 1)

        then:
        result == [16]
    }

    def "2DUP duplicates the top two stack values"() {
        given:
        def forth = new Forth()

        when:
        def result = forth.evalAndPop("10 20 2DUP", 4)

        then:
        result == [10, 20, 10, 20]
    }
}
