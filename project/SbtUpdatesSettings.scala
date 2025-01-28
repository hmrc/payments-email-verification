import com.timushev.sbt.updates.UpdatesKeys.dependencyUpdates
import com.timushev.sbt.updates.UpdatesPlugin.autoImport.{dependencyUpdatesFailBuild, dependencyUpdatesFilter, moduleFilterRemoveValue}
import sbt.Keys.*
import sbt.*

object SbtUpdatesSettings {

  lazy val sbtUpdatesSettings = Seq(
    dependencyUpdatesFailBuild := true,
    (Compile / compile) := ((Compile / compile) dependsOn dependencyUpdates).value,
    dependencyUpdatesFilter -= moduleFilter("org.scala-lang"),
    dependencyUpdatesFilter -= moduleFilter("org.playframework"),
    dependencyUpdatesFilter -= moduleFilter("com.typesafe.play"),
    dependencyUpdatesFilter -= moduleFilter("com.beachape", "enumeratum-play"),
    dependencyUpdatesFilter -= moduleFilter("org.julienrf", "play-json-derived-codecs")
  )

}
