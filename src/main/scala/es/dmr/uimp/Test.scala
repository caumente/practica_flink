package es.dmr.uimp


////////////////////////
import java.util.Properties

import org.apache.flink.api.common.functions.MapFunction
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode

import scala.collection.mutable.ArrayBuffer
import scala.util.parsing.json.JSON
import spray.json._
import DefaultJsonProtocol._
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.datastream.DataStream
import org.apache.flink.streaming.api.scala.OutputTag
//import org.apache.flink.streaming.api.functions.co.ProcessJoinFunction
//import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.flink.streaming.util.serialization.SimpleStringSchema
import org.apache.flink.streaming.util.serialization.JSONKeyValueDeserializationSchema




object Test extends App {

  // creamos el entorno y le asignamos las propiedades
  val env = StreamExecutionEnvironment.getExecutionEnvironment()
  env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
  println("Environment")


  val properties = new Properties()
  properties.setProperty("bootstrap.servers", "bigdatamaster2019.dataspartan.com:29093")
  properties.setProperty("group.id", "test")
  println("Properties")

  // Deserializador de datos de Kafka (UTF-8)
  val simpleDeserializer = new SimpleStringSchema()
  val JSONDeserializer = new JSONKeyValueDeserializationSchema(false)
  println("Serializers")

  // Consumer de cada topic
  val consumerDemo = new FlinkKafkaConsumer("topic_demographic", simpleDeserializer, properties)
  val consumerHist = new FlinkKafkaConsumer("topic_historic", JSONDeserializer, properties)
  println("Consumers")

  // configuramos que empiece a leer el topic desde el ultimo mensaje generado
  consumerDemo.setStartFromLatest()
  consumerHist.setStartFromLatest()

  // Generamos los streaming de datos
  val streamDemo = env.addSource(consumerDemo).rebalance()
  val streamHist = env.addSource(consumerHist)



    val demoMapFunction: MapFunction[ObjectNode, (Long, List[Int])] = {
      new MapFunction[ObjectNode, (Long, List[Int])](){
        override def map(row: ObjectNode): (Long, List[Int]) = {
          val iterator = row.get("value").fields()
          iterator.next() // saltamos el primer elemento uuid
          val buffer = new ArrayBuffer[Int]()
          while (iterator.hasNext){
            buffer.append(iterator.next().getValue.asInt())
          }
          (row.get("value").get("uuid").asLong(), buffer.toList)
        }
      }
    }

  val demoMapFunction2: MapFunction[ObjectNode, List[Int]] = {
    new MapFunction[ObjectNode, List[Int]](){
      override def map(row: ObjectNode): List[Int] = {
        val iterator = row.get("value").fields()
        val buffer = new ArrayBuffer[Int]()
        while (iterator.hasNext){
          buffer.append(iterator.next().getValue.asInt())
        }
        (buffer.toList)
      }
    }
  }






  streamDemo.print()







//    val histMapFunction: MapFunction[ObjectNode, (Long, List[Int])] = {
//      new MapFunction[ObjectNode, (Long, List[Int])](){
//        override def map(row: ObjectNode): (Long, List[Int]) = {
//          val iterator = row.get("value").get("products").fields()
//          val buffer = new ArrayBuffer[Int]()
//          while (iterator.hasNext){
//            buffer.append(iterator.next().getValue.asInt())
//          }
//          Tuple2(row.get("value").get("uuid").asLong(), buffer.toList)
//        }
//      }
//    }


  //val keyedStreamDemo = streamDemo.map(demoMapFunction)

  //val keyedStreamHist = streamHist.map(demoMapFunction2).keyBy(0)
  //keyedStreamDemo
  //val keyedStreamHist = streamHist.map(histMapFunction)






  //  keyedStreamDemo.join(keyedStreamDemo)
  //    .where()
  //    .equalTo(0)
  //    .window(EventTimeSessionWindows.withGap(Time.milliseconds(1)))
  //    .apply { (e1, e2) => e1 + "," + e2 }





  env.execute()


}


