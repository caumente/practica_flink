package es.dmr.uimp

import org.pmml4s.model.Model

object model extends App {

  def evaluateProb(prob_0:Double) : Tuple2[Int, Double] = {

    var label: Int = 2
    var prob = prob_0

    if (prob_0 >= 0.5){
      label = 0
    }else{
      label = 1
      prob = 1.0-prob_0
    }

    return Tuple2(label, prob)
  }
  //println("Probabily class 0: ",prediction(0))
  //println("Probabily class 1: ",prediction(1))


  val model = Model.fromFile("./src/main/scala/es/dmr/uimp/naive_bayes_model.pmml")
  val row4 = Array[Int](48,1,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,1,0,1,0,0,0,0,0,0,0,1,1,0,1,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0)
  val row3 = Array[Int](50,0,1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0)
  val row2 = Array[Int](48,0,1,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,1,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,1,0,1,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,1,0,0,1,0,1,0,0,0,0,0,0,0,0,1,0,0,1,0,0,0,0,0,1,0,0,0,0,0,0,0,1,0,0,1,0,1,0,1,0,0)

  val prediction4 = model.predict(row4)
  val prediction3 = model.predict(row3)
  val prediction2 = model.predict(row2)

  val prob4: Double = prediction4(0).toString.toDouble
  val prob3: Double = prediction3(0).toString.toDouble
  val prob2: Double = prediction2(0).toString.toDouble

  println("This class is: " + evaluateProb(prob4)._1 + " with probability " + evaluateProb(prob4)._2 )
  println("This class is: " + evaluateProb(prob3)._1  + " with probability " + evaluateProb(prob3)._2 )
  println("This class is: " + evaluateProb(prob2)._1  + " with probability " + evaluateProb(prob2)._2 )

}





