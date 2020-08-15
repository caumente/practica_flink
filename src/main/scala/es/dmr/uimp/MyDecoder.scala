package es.dmr.uimp

import io.circe._
import io.circe.parser._

object MyDecoder extends App {

  val stringDemo =
    """{
         "uuid": 321,
         "age": 25,
         "man": 0,
         "woman": 1
       }"""

  val stringHist =
    """{
         "uuid": 321,
         "products": {
                      'cat17': 0,
                      'cat74': 0,
                      'cat42': 0,
                      'cat96': 0,
                      'cat13': 0,
                      'cat16': 1,
                      'cat11': 0
                     }
       }"""



  case class strDemo(uuid: Int, age: Int, man: Int, woman: Int)

  object strDemo {

    implicit val decoder: Decoder[strDemo] = Decoder.instance { row =>
      for {
        uuid <- row.get[Int]("uuid")
        age <- row.get[Int]("age")
        man <- row.get[Int]("man")
        woman <- row.get[Int]("woman")
      } yield strDemo(uuid, age, man, woman)

    }

  }

  val demoParsed = decode[strDemo](stringDemo)
  println(demoParsed)


}


