package com.schuwalow.regex

import zio.test._
import zio.test.Assertion._
import zio.test.Gen

object RegexSpec extends DefaultRunnableSpec {

  override val spec = suite("Regex")(
    suite("simple patterns")(
      test("empty string") {
        val test = Regex.emptyString.matches("")
        assert(test)(isTrue)
      },
      test("character") {
        val test = Regex.character('a').matches("a")
        assert(test)(isTrue)
      },
      test("sequencing") {
        val test = (Regex.character('a') >> Regex.character('a')).matches("aa")
        assert(test)(isTrue)
      },
      test("and") {
        val test =
          ((Regex.character('a') | Regex.character('b')) & Regex.character('a'))
            .matches("a")
        assert(test)(isTrue)
      },
      test("or") {
        val test = (Regex.character('a') | Regex.character('b')).matches("b")
        assert(test)(isTrue)
      },
      test("neg") {
        val test = Regex.character('a').neg.matches("a")
        assert(test)(isFalse)
      },
      test("repetition") {
        val test = (Regex.character('a') >> Regex.character('b'))
          .rep(3)
          .matches("ababab")
        assert(test)(isTrue)
      }
    ),
    testM("character sequences") {
      check(Gen.anyString) { str =>
        val pattern = str.foldLeft(Regex.emptyString) {
          case (acc, c) => acc >> Regex.character(c)
        }
        assert(pattern.matches(str))(isTrue)
      }
    }
  )
}
