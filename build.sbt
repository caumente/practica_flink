name := "PracticaFlink"

//scalaVersion := "2.12.11"
scalaVersion := "2.11.8"


val flinkVersion = "1.9.0"
libraryDependencies += "org.apache.flink" %% "flink-scala" % flinkVersion
libraryDependencies += "org.apache.flink" %% "flink-streaming-scala" % flinkVersion
libraryDependencies += "org.apache.flink" %% "flink-connector-kafka" % flinkVersion
//libraryDependencies += "org.apache.flink" %% "flink-connector-kafka-0.10" % flinkVersion
libraryDependencies += "org.apache.kafka" %% "kafka-streams-scala" % "2.2.1-cdh6.3.3"
libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.2"
libraryDependencies += "org.json4s" %% "json4s-core" % "3.5.1"
libraryDependencies += "org.json4s" %% "json4s-native" % "3.5.1"
libraryDependencies += "io.spray" %%  "spray-json" % "1.3.5"

resolvers += "Cloudera" at "https://repository.cloudera.com/artifactory/cloudera-repos/"
resolvers += "Streams" at "https://mvnrepository.com/artifact/org.apache.kafka/kafka-streams-scala"
resolvers += "Connectors" at "https://mvnrepository.com/artifact/org.apache.flink/flink-connector-kafka"
resolvers += "Connectors2" at "https://mvnrepository.com/artifact/org.apache.flink/flink-connector-kafka-0.10"



assemblyMergeStrategy in assembly := {
  case m if m.toLowerCase.endsWith("manifest.mf")          => MergeStrategy.discard
  case m if m.toLowerCase.matches("meta-inf.*\\.sf$")      => MergeStrategy.discard
  case "log4j.properties"                                  => MergeStrategy.discard
  case m if m.toLowerCase.startsWith("meta-inf/services/") => MergeStrategy.filterDistinctLines
  case "reference.conf"                                    => MergeStrategy.concat
  case _                                                   => MergeStrategy.first
}

