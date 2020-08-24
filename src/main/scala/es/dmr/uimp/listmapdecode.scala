package es.dmr.uimp
import scala.collection.immutable.ListMap



object listmapdecode extends App{


    // Creating ListMap with values
    var listMap = ListMap("C"->"Csharp", "S"->"Scala", "J"->"Java")
    var values = listMap.values.toList.mkString(",")

    // Printing ListMap
    println(listMap)
    println(values)


}
