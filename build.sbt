import uk.gov.hmrc.DefaultBuildSettings.scalaSettings
import uk.gov.hmrc.ShellPrompt
import wartremover.WartRemover.autoImport.wartremoverExcluded


val appName: String = "payments-email-verification"

ThisBuild / scalaVersion  := "3.3.4"
ThisBuild / majorVersion  := 4

lazy val scalaCompilerOptions = Seq(
  "-Xfatal-warnings",
  "-Wvalue-discard",
  "-deprecation",
  "-feature",
  "-unchecked",
  "-language:implicitConversions",
  "-language:strictEquality",
  // required in place of silencer plugin
  "-Wconf:msg=unused-imports&src=html/.*:s",
  "-Wconf:src=routes/.*:s"
)

lazy val commonSettings = Seq[SettingsDefinition](
  scalacOptions ++= scalaCompilerOptions,
  (update / evictionWarningOptions) := EvictionWarningOptions.default.withWarnScalaVersionEviction(false),
  shellPrompt := ShellPrompt(version.value),
  buildInfoPackage := name.value.toLowerCase().replaceAllLiterally("-", ""),
  (Compile / doc / scalacOptions) := Seq(), //this will allow to have warnings in `doc` task and not fail the build
  scalafmtOnCompile := true,
  scalaSettings,
  uk.gov.hmrc.DefaultBuildSettings.defaultSettings(),
  WartRemoverSettings.wartRemoverSettings,
  ScoverageSettings.scoverageSettings,
  SbtUpdatesSettings.sbtUpdatesSettings
)

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(commonSettings *)
  .settings(
    libraryDependencies ++= AppDependencies.microserviceDependencies,
    wartremoverExcluded ++= (Compile / routes).value
  )
  .settings(PlayKeys.playDefaultPort := 10800)
  .settings(resolvers += Resolver.jcenterRepo)
  .disablePlugins(sbt.plugins.JUnitXmlReportPlugin)
  .dependsOn(cor30)
  .aggregate(cor30)


/**
 * Collection Of Routines
 */

lazy val cor30 = Project(appName + "-cor-play-30", file("cor/play-30"))
  .disablePlugins(play.sbt.PlayScala)
  .settings(commonSettings *)
  .settings(
    libraryDependencies ++= AppDependencies.corJourneyDependencies,
    corSharedSources
  )

def corSharedSources = Seq(
  Compile / unmanagedSourceDirectories += baseDirectory.value / "../shared/src/main/scala",
)
