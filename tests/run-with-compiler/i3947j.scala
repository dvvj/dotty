
import scala.quoted._
import scala.quoted.Toolbox.Default._

object Test {

  def main(args: Array[String]): Unit = {
    implicit val toolbox: scala.quoted.Toolbox = scala.quoted.Toolbox.make

    def test[T](clazz: java.lang.Class[T]): Unit = {
      val lclazz = clazz.toExpr
      val name = '{ (~lclazz).getCanonicalName }
      println()
      println(name.show)
      println(name.run)
    }

    test(classOf[Array[Array[Int]]])
    test(classOf[Array[Array[Array[Int]]]])
  }

}
