package biz.digitalindustry.groovyforth

import spock.lang.Specification

class ForthDivModSpec extends Specification {

    def "slash MOD pushes remainder then quotient"() {
        given:
        def forth = new Forth()

        when:
        def result = forth.evalAndPop("13 5 /MOD", 2)

        then:
        result == [3, 2]  // 13 % 5 = 3, 13 / 5 = 2
    }

    def "slash MOD handles exact division"() {
        given:
        def forth = new Forth()

        when:
        def result = forth.evalAndPop("12 4 /MOD", 2)

        then:
        result == [0, 3]  // 12 % 4 = 0, 12 / 4 = 3
    }

    def "slash MOD handles remainder larger than zero"() {
        given:
        def forth = new Forth()

        when:
        def result = forth.evalAndPop("20 6 /MOD", 2)

        then:
        result == [2, 3]  // 20 % 6 = 2, 20 / 6 = 3
    }
}
