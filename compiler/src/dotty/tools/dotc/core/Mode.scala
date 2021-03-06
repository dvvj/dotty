package dotty.tools.dotc.core

/** A collection of mode bits that are part of a context */
case class Mode(val bits: Int) extends AnyVal {
  import Mode._
  def | (that: Mode): Mode = Mode(bits | that.bits)
  def & (that: Mode): Mode = Mode(bits & that.bits)
  def &~ (that: Mode): Mode = Mode(bits & ~that.bits)
  def is (that: Mode): Boolean = (bits & that.bits) == that.bits

  def isExpr: Boolean = (this & PatternOrTypeBits) == None

  override def toString: String =
    (0 until 31).filter(i => (bits & (1 << i)) != 0).map(modeName).mkString("Mode(", ",", ")")
}

object Mode {
  val None: Mode = Mode(0)

  private val modeName = new Array[String](32)

  def newMode(bit: Int, name: String): Mode = {
    modeName(bit) = name
    Mode(1 << bit)
  }

  val Pattern: Mode = newMode(0, "Pattern")
  val Type: Mode = newMode(1, "Type")

  val ImplicitsEnabled: Mode = newMode(2, "ImplicitsEnabled")
  val InferringReturnType: Mode = newMode(3, "InferringReturnType")

  /** This mode bit is set if we collect information without reference to a valid
   *  context with typerstate and constraint. This is typically done when we
   *  cache the eligibility of implicits. Caching needs to be done across different constraints.
   *  Therefore, if TypevarsMissContext is set, subtyping becomes looser, and assumes
   *  that TypeParamRefs can be sub- and supertypes of anything. See TypeComparer.
   */
  val TypevarsMissContext: Mode = newMode(4, "TypevarsMissContext")

  val CheckCyclic: Mode = newMode(5, "CheckCyclic")

  /** We are looking at the arguments of a supercall */
  val InSuperCall: Mode = newMode(6, "InSuperCall")

  /** We are in a pattern alternative */
  val InPatternAlternative: Mode = newMode(7, "InPatternAlternative")

  /** Allow GADTFlexType labelled types to have their bounds adjusted */
  val GADTflexible: Mode = newMode(8, "GADTflexible")

  /** We are currently printing something: avoid to produce more logs about
   *  the printing
   */
  val Printing: Mode = newMode(10, "Printing")

  /** We are currently typechecking an ident to determine whether some implicit
   *  is shadowed - don't do any other shadowing tests.
   */
  val ImplicitShadowing: Mode = newMode(11, "ImplicitShadowing")

  /** We are currently in a `viewExists` check. In that case, ambiguous
   *  implicits checks are disabled and we succeed with the first implicit
   *  found.
   */
  val ImplicitExploration: Mode = newMode(12, "ImplicitExploration")

  /** We are currently unpickling Scala2 info */
  val Scala2Unpickling: Mode = newMode(13, "Scala2Unpickling")

  /** We are currently unpickling from Java 8 or higher */
  val Java8Unpickling: Mode = newMode(14, "Java8Unpickling")

  /** Use Scala2 scheme for overloading and implicit resolution */
  val OldOverloadingResolution: Mode = newMode(15, "OldOverloadingResolution")

  /** Allow hk applications of type lambdas to wildcard arguments;
   *  used for checking that such applications do not normally arise
   */
  val AllowLambdaWildcardApply: Mode = newMode(16, "AllowHKApplyToWildcards")

  /** Read original positions when unpickling from TASTY */
  val ReadPositions: Mode = newMode(17, "ReadPositions")

  /** Don't suppress exceptions thrown during show */
  val PrintShowExceptions: Mode = newMode(18, "PrintShowExceptions")

  val PatternOrTypeBits: Mode = Pattern | Type | InPatternAlternative

  /** We are elaborating the fully qualified name of a package clause.
   *  In this case, identifiers should never be imported.
   */
  val InPackageClauseName: Mode = newMode(19, "InPackageClauseName")

  /** We are in the IDE */
  val Interactive: Mode = newMode(20, "Interactive")

  /** We are typing the body of an inline method */
  val InlineableBody: Mode = newMode(21, "InlineableBody")

  /** Read comments from definitions when unpickling from TASTY */
  val ReadComments: Mode = newMode(22, "ReadComments")
}
