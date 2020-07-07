package com.schuwalow.regex

sealed abstract class Regex { self =>
  def nullable: Boolean
  def derivative(c: Char): Regex

  def matches(str: String): Boolean =
    str.foldLeft(self)(_ derivative _).nullable

  def * : Regex =
    self match {
      case pattern @ Regex.Repetition(_) => pattern
      case Regex.emptyString => Regex.emptyString
      case Regex.emptySet => Regex.emptyString
      case pattern => Regex.Repetition(pattern)
    }

  def + : Regex =
    self >> self.*

  def >>(that: Regex): Regex =
    (self, that) match {
      case (Regex.emptySet, _) => Regex.emptySet
      case (_, Regex.emptySet) => Regex.emptySet
      case (Regex.emptyString, pattern) => pattern
      case (pattern, Regex.emptyString) => pattern
      case (p1, p2) => Regex.Sequence(p1, p2)
    }

  def &(that: Regex): Regex =
    (self, that) match {
      case (Regex.emptySet, _) => Regex.emptySet
      case (_, Regex.emptySet) => Regex.emptySet
      case (Regex.emptySet.neg, pattern) => pattern
      case (pattern, Regex.emptySet.neg) => pattern
      case (p1, p2) if p1 == p2 => p1
      case (p1, p2) => Regex.And(p1, p2)
    }

  def |(that: Regex): Regex =
    (self, that) match {
      case (Regex.emptySet, pattern) => pattern
      case (pattern, Regex.emptySet) => pattern
      case (Regex.emptySet.neg, _) => Regex.emptySet.neg
      case (_, Regex.emptySet.neg) => Regex.emptySet.neg
      case (p1, p2) if (p1 == p2) => p1
      case (p1, p2) => Regex.Or(p1, p2)
    }

  def delta: Regex =
    if (nullable) Regex.emptyString else Regex.emptySet

  lazy val neg: Regex =
    self match {
      case Regex.Complement(pattern) =>
        pattern
      case pattern =>
        Regex.Complement(pattern)
    }

  def rep(n: Int): Regex =
    List.fill(n)(self).foldLeft(Regex.emptyString)(_ >> _)
}

object Regex {
  // the empty pattern
  private[Regex] case object EmptyString extends Regex {
    override val nullable: Boolean =
      true
    override def derivative(c: Char): Regex =
      emptySet
  }

  private[Regex] final case class CharacterSet(values: Set[Char]) extends Regex {
    override def nullable: Boolean =
      false
    override def derivative(c: Char): Regex =
      if (values.contains(c)) emptyString else emptySet
  }

  private[Regex] final case class And(first: Regex, second: Regex) extends Regex {
    override def nullable: Boolean =
      first.nullable & second.nullable
    override def derivative(c: Char): Regex =
      first.derivative(c) & second.derivative(c)
  }

  private[Regex] final case class Or(first: Regex, second: Regex) extends Regex {
    override def nullable: Boolean =
      first.nullable | second.nullable
    override def derivative(c: Char): Regex =
      first.derivative(c) | second.derivative(c)
  }

  private[Regex] final case class Sequence(first: Regex, second: Regex) extends Regex {
    override def nullable: Boolean =
      first.nullable & second.nullable
    override def derivative(c: Char): Regex =
      (first.derivative(c) >> second) | (first.delta >> second.derivative(c))
  }

  private[Regex] final case class Repetition(value: Regex) extends Regex { self =>
    override def nullable: Boolean =
      true
    override def derivative(c: Char): Regex =
      derivative(c) >> self
  }

  private[Regex] final case class Complement(pattern: Regex) extends Regex {
    override def nullable: Boolean =
      !pattern.nullable
    override def derivative(c: Char): Regex =
      pattern.derivative(c).neg
  }

  def character(value: Char): Regex =
    CharacterSet(Set(value))

  def characterSet(values: Set[Char]): Regex =
    CharacterSet(values)

  val emptySet: Regex =
    CharacterSet(Set.empty)

  val emptyString: Regex =
    EmptyString

  def neg(pattern: Regex) =
    pattern.neg
}
