
object uniont {
  trait Greeting(name: String) {
    def msg = s"How are you, $name!"
  }
  class C1 extends Greeting("Martin") { }
  def main(args:Array[String]):Unit = {
    println("hl")

    val u1: String | Int = 5
    val u2: String | Int = "str5"

    println("testing union types ...")
    println(s"u1: $u1")
    println(s"u2: $u2")

    val c1 = new C1
    println("testing trait parameters ...")
    println(c1.msg)
  }

}