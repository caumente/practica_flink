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
import org.apache.flink.streaming.api.functions.co.ProcessJoinFunction
import org.apache.flink.streaming.api.scala.{KeyedStream, OutputTag}
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.types.Nothing
import org.apache.flink.util.Collector

import scala.collection.immutable.ListMap
//import org.apache.flink.streaming.api.functions.co.ProcessJoinFunction
//import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.flink.streaming.util.serialization.SimpleStringSchema
import org.apache.flink.streaming.util.serialization.JSONKeyValueDeserializationSchema

import io.circe._, io.circe.parser._



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
  val consumerHist = new FlinkKafkaConsumer("topic_historic", simpleDeserializer, properties)
  println("Consumers")

  // configuramos que empiece a leer el topic desde el ultimo mensaje generado
  consumerDemo.setStartFromLatest()
  consumerHist.setStartFromLatest()

  // Generamos los streaming de datos
  val streamDemo = env.addSource(consumerDemo)
  val streamHist = env.addSource(consumerHist)



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

  case class strHist(uuid: Int, products: ListMap[String, Int])
  object strHist {

    implicit val decoder: Decoder[strHist] = Decoder.instance { row =>
      for {
        uuid <- row.get[Int]("uuid")
        products <- row.get[ListMap[String, Int]]("products")
      } yield strHist(uuid, products)

    }
  }



  val demoParsed: DataStream[strDemo] = streamDemo.map{event => parser.decode[strDemo](event).right.get}
  val histParsed: DataStream[strHist] = streamHist.map{event => parser.decode[strHist](event).right.get}



  val demoKeyed = demoParsed.keyBy(_.uuid)
  val histKeyed = histParsed.keyBy(_.uuid)

  val joined = demoKeyed
    .intervalJoin(histKeyed)
    .between(Time.milliseconds(0), Time.milliseconds(3001))
    .process(new ProcessJoinFunction[strDemo, strHist, String] {
      override def processElement(left: strDemo,
                                  right: strHist,
                                  ctx: ProcessJoinFunction[strDemo, strHist, String]#Context, out: Collector[String]): Unit = {
        out.collect((left.uuid + "," +
                    left.age + "," +
                    left.man + "," +
                    left.woman + "," +
                    right.products.values.toList.mkString(",")))
      }
    })

  joined






  env.execute()

}


