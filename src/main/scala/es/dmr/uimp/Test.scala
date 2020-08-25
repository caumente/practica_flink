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
import org.pmml4s.model.Model

import scala.collection.immutable.ListMap
//import org.apache.flink.streaming.api.functions.co.ProcessJoinFunction
//import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.flink.streaming.util.serialization.SimpleStringSchema
import org.apache.flink.streaming.util.serialization.JSONKeyValueDeserializationSchema

import io.circe._, io.circe.parser._



object Test extends App {

  /*
  En la primera parte del pipeline creamos un entorno de ejecución junto a sus propiedades. Posteriormente configuramos
  el deserializador que usaremos para leer el topic de kafka mediante el FlinkConsumer.
   */

  // Creamos el entorno y le asignamos las propiedades
  val env = StreamExecutionEnvironment.getExecutionEnvironment()
  env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)


  val properties = new Properties()
  properties.setProperty("bootstrap.servers", "bigdatamaster2019.dataspartan.com:29093")
  properties.setProperty("group.id", "test")


  // Deserializador de datos de Kafka (UTF-8)
  val simpleDeserializer = new SimpleStringSchema()
  val JSONDeserializer = new JSONKeyValueDeserializationSchema(false)


  // Consumer de cada topic
  val consumerDemo = new FlinkKafkaConsumer("topic_demographic", simpleDeserializer, properties)
  val consumerHist = new FlinkKafkaConsumer("topic_historic", simpleDeserializer, properties)


  // Configuramos que empiece a leer el topic desde el ultimo mensaje generado
  consumerDemo.setStartFromLatest()
  consumerHist.setStartFromLatest()



  /*
  En la segunda parte, comenzamos a leer el flujo de datos proveniente de Kafka. Al ser dos topics diferentes, usaremos
  un case class para cada uno de ellos, y haremos un join de tal forma que nos quedemos con un unico stream de datos
  en formato Array
   */


  // Generamos los streaming de datos
  val streamDemo = env.addSource(consumerDemo)
  val streamHist = env.addSource(consumerHist)


  // Definimos dos case class personalizados para cada topic
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

  // Decodificamos los topics
  val demoParsed: DataStream[strDemo] = streamDemo.map{event => parser.decode[strDemo](event).right.get}
  val histParsed: DataStream[strHist] = streamHist.map{event => parser.decode[strHist](event).right.get}

  // Asignamos la key de los topics y hacemos la unión entre ambos
  val demoKeyed = demoParsed.keyBy(_.uuid)
  val histKeyed = histParsed.keyBy(_.uuid)


  val joined: DataStream[Array[Int]] = demoKeyed
    .intervalJoin(histKeyed)
    .between(Time.milliseconds(0), Time.milliseconds(3001))
    .process(new ProcessJoinFunction[strDemo, strHist, Array[Int]] {
      override def processElement(left: strDemo,
                                  right: strHist,
                                  ctx: ProcessJoinFunction[strDemo, strHist, Array[Int]]#Context, out: Collector[Array[Int]]): Unit = {
        out.collect((left.uuid + "," +
                    left.age + "," +
                    left.man + "," +
                    left.woman + "," +
                    right.products.values.toList.mkString(",")).split(",").map(_.toInt))
      }
    })

  //joined.map(row => row(0).toInt).print()
  //val splitted : DataStream[Int] = joined.map(row => row.toInt)
  //splitted.print()
  //joined.map(row => row.split(",")).writeAsCsv("aaaa")

  /*
  En la tercera parte, debemos cargar el modelo generado previamente para evaluar cada uno de los nuevos registros
  que nos llegan. Una vez evaluados, devolvemos un string con la estructura necesaria para poder enviarlo al topic
  que leerá la plataforma.
   */


  def modelPredict(model: Model, ds: DataStream[Array[Int]], token: String) : String = {
    val uuid: DataStream[Int] = ds.map(row => row(0)) // obtenemos el uuid
    val features: DataStream[Array[Int]] = ds.map(row => row.drop(1)) // eliminamos el uuid de las features


    val prediction: DataStream[Array[Any]] = features.map(row => model.predict(row)) // evaluo el stream con el modelo
    val prob: DataStream[Double] = prediction.map(x => x(0).toString.toDouble) // calculo la prob de la etiqueta
    val label = prob.map(x => x.round) // genero la etiqueta de clase


    return """ "uuid": """ + uuid + """, "value": """ + label + """, "token":"""" + token + """""""
    }

  val model = Model.fromFile("./src/main/scala/es/dmr/uimp/naive_bayes_model.pmml")
  val result = modelPredict(model, joined, "my_token")

  resu




  env.execute()

}


