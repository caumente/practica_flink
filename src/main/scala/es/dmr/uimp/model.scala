package es.dmr.uimp

import org.apache.flink.streaming.api.datastream.DataStream
import org.pmml4s.model.Model

object model extends App {

  def modelPredictTest(model: Model, row: Array[Int]) : String = {
    val prediction = model.predict(row.drop(1))
    val prob: Double = prediction(0).toString.toDouble

    return "The class for " + row(0) + " is: " + evaluateProb(prob)._1 + " with probability " + evaluateProb(prob)._2
  }

  def modelPredict(model: Model, row: Array[Int], token: String) : String = {
    val prediction = model.predict(row.drop(1))
    println("prediction: ", prediction)
    val prob: Double = prediction(0).toString.toDouble

    return """ "uuid": """ + row(0).toInt + """, "value": """ + evaluateProb(prob)._1 + """, "token":"""" + token + """""""
  }

  def evaluateProb(prob_0:Double) : (Int, Double) = {

    var label: Int = 2
    var prob = prob_0

    if (prob_0 >= 0.5){
      label = 0
    }else{
      label = 1
      prob = 1.0-prob_0
    }

    return (label, prob)
  }


  val model = Model.fromFile("./src/main/scala/es/dmr/uimp/naive_bayes_model.pmml")
  val row4 = Array[Int](11111111, 48,1,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,1,0,1,0,0,0,0,0,0,0,1,1,0,1,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0)
  val row3 = Array[Int](22222222, 50,0,1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0)
  val row2 = Array[Int](33333333, 48,0,1,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,1,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,1,0,1,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,1,0,0,1,0,1,0,0,0,0,0,0,0,0,1,0,0,1,0,0,0,0,0,1,0,0,0,0,0,0,0,1,0,0,1,0,1,0,1,0,0)



  println(modelPredict(model, row2, "my_token"))
  println(modelPredict(model, row3, "my_token"))
  println(modelPredict(model, row4, "my_token"))

  val miarray = "2,3,4,5".split(",").map(_.toInt)

  println(miarray(0))
  println(row2(0))


  def modelPredictProd(model: Model, row: Array[Int], token: String) : String = {
    val prediction = model.predict(row.drop(1))
    val prob: Double = prediction(0).toString.toDouble

    return """ "uuid": """ + row(0).toInt + """, "value": """ + evaluateProb(prob)._1 + """, "token":"""" + token + """""""
  }

  def evaluateProbProd(prob_0:Double) : (Int, Double) = {

    var label: Int = 2
    var prob = prob_0

    if (prob_0 >= 0.5){
      label = 0
    }else{
      label = 1
      prob = 1.0-prob_0
    }

    return (label, prob)
  }

}






