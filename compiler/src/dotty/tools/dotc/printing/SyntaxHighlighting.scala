package dotty.tools.dotc.printing

import dotty.tools.dotc.ast.untpd
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.StdNames._
import dotty.tools.dotc.parsing.Parsers.Parser
import dotty.tools.dotc.parsing.Scanners.Scanner
import dotty.tools.dotc.parsing.Tokens._
import dotty.tools.dotc.reporting.Reporter
import dotty.tools.dotc.util.Positions.Position
import dotty.tools.dotc.util.SourceFile

import java.util.Arrays

/** This object provides functions for syntax highlighting in the REPL */
object SyntaxHighlighting {

  /** if true, log erroneous positions being highlighted */
  private final val debug = true

  // Keep in sync with SyntaxHighlightingTests
  val NoColor: String         = Console.RESET
  val CommentColor: String    = Console.BLUE
  val KeywordColor: String    = Console.YELLOW
  val ValDefColor: String     = Console.CYAN
  val LiteralColor: String    = Console.RED
  val StringColor: String     = Console.GREEN
  val TypeColor: String       = Console.MAGENTA
  val AnnotationColor: String = Console.MAGENTA

  def highlight(in: String)(implicit ctx: Context): String = {
    def freshCtx = ctx.fresh.setReporter(Reporter.NoReporter)
    if (in.isEmpty || ctx.settings.color.value == "never") in
    else {
      implicit val ctx = freshCtx
      val source = new SourceFile("<highlighting>", in)
      val colorAt = Array.fill(in.length)(NoColor)

      def highlightRange(from: Int, to: Int, color: String) =
        Arrays.fill(colorAt.asInstanceOf[Array[AnyRef]], from, to, color)

      def highlightPosition(pos: Position, color: String) = if (pos.exists) {
        if (pos.start < 0 || pos.end > in.length) {
          if (debug)
            println(s"Trying to highlight erroneous position $pos. Input size: ${in.length}")
        }
        else
          highlightRange(pos.start, pos.end, color)
      }

      val scanner = new Scanner(source)
      while (scanner.token != EOF) {
        val start = scanner.offset
        val token = scanner.token
        val name = scanner.name
        scanner.nextToken()
        val end = scanner.lastOffset

        // Branch order is important. For example,
        // `true` is at the same time a keyword and a literal
        token match {
          case _ if literalTokens.contains(token) =>
            highlightRange(start, end, LiteralColor)

          case STRINGPART =>
            // String interpolation parts include `$` but
            // we don't highlight it, hence the `-1`
            highlightRange(start, end - 1, LiteralColor)

          case _ if alphaKeywords.contains(token) =>
            highlightRange(start, end, KeywordColor)

          case IDENTIFIER if name == nme.INLINEkw =>
            // `inline` is a "soft" keyword
            highlightRange(start, end, KeywordColor)

          case IDENTIFIER if name == nme.??? =>
            highlightRange(start, end, Console.RED_B)

          case _ =>
        }
      }

      for (pos <- scanner.commentPositions)
        highlightPosition(pos, CommentColor)

      object TreeHighlighter extends untpd.UntypedTreeTraverser {
        import untpd._

        def ignored(tree: NameTree) = {
          val name = tree.name.toTermName
          // trees named <error> and <init> have weird positions
          name == nme.ERROR || name == nme.CONSTRUCTOR
        }

        def highlightAnnotations(tree: MemberDef): Unit =
          for (annotation <- tree.rawMods.annotations)
            highlightPosition(annotation.pos, AnnotationColor)

        def highlight(trees: List[Tree])(implicit ctx: Context): Unit =
          trees.foreach(traverse)

        def traverse(tree: Tree)(implicit ctx: Context): Unit = {
          tree match {
            case tree: NameTree if ignored(tree) =>
              ()
            case tree: ValOrDefDef =>
              highlightAnnotations(tree)
              highlightPosition(tree.namePos, ValDefColor)
            case tree: MemberDef /* ModuleDef | TypeDef */ =>
              highlightAnnotations(tree)
              highlightPosition(tree.namePos, TypeColor)
            case tree: Ident if tree.isType =>
              highlightPosition(tree.pos, TypeColor)
            case _: TypTree =>
              highlightPosition(tree.pos, TypeColor)
            case _ =>
          }
          traverseChildren(tree)
        }
      }

      val parser = new Parser(source)
      val trees = parser.blockStatSeq()
      TreeHighlighter.highlight(trees)

      val highlighted = new StringBuilder()

      for (idx <- colorAt.indices) {
        val prev = if (idx == 0) NoColor else colorAt(idx - 1)
        val curr = colorAt(idx)
        if (curr != prev)
          highlighted.append(curr)
        highlighted.append(in(idx))
      }

      if (colorAt.last != NoColor)
        highlighted.append(NoColor)

      highlighted.toString
    }
  }
}
