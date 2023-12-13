import AppDependencies.PlayVersion
import uk.gov.hmrc.DefaultBuildSettings.scalaSettings
import uk.gov.hmrc.ShellPrompt
import wartremover.WartRemover.autoImport.wartremoverExcluded


val appName: String = "payments-email-verification"

ThisBuild / scalaVersion  := "2.13.12"
ThisBuild / majorVersion  := 2

lazy val scalaCompilerOptions = Seq(
  "-Xfatal-warnings",
  "-Xlint:-missing-interpolator,_",
  "-Xlint:adapted-args",
  "-Xlint:-byname-implicit",
  "-Ywarn-unused:implicits",
  "-Ywarn-unused:imports",
  "-Ywarn-unused:locals",
  "-Ywarn-unused:params",
  "-Ywarn-unused:patvars",
  "-Ywarn-unused:privates",
  "-Ywarn-value-discard",
  "-Ywarn-dead-code",
  "-deprecation",
  "-feature",
  "-unchecked",
  "-language:implicitConversions",
  "-Wconf:cat=unused-imports&src=html/.*:s",
  "-Wconf:src=routes/.*:s"
)

lazy val commonSettings = Seq[SettingsDefinition](
  scalacOptions ++= scalaCompilerOptions,
  (update / evictionWarningOptions) := EvictionWarningOptions.default.withWarnScalaVersionEviction(false),
  shellPrompt := ShellPrompt(version.value),
  buildInfoPackage := name.value.toLowerCase().replaceAllLiterally("-", ""),
  (Compile / doc / scalacOptions) := Seq(), //this will allow to have warnings in `doc` task and not fail the build
  scalaSettings,
  uk.gov.hmrc.DefaultBuildSettings.defaultSettings(),
  WartRemoverSettings.wartRemoverSettings,
  ScoverageSettings.scoverageSettings,
  SbtUpdatesSettings.sbtUpdatesSettings
) ++ ScalariformSettings.scalariformSettings

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin, SbtAutoBuildPlugin, SbtGitVersioning)
  .settings(commonSettings *)
  .settings(
    libraryDependencies ++= AppDependencies.microserviceDependencies,
    wartremoverExcluded ++= (Compile / routes).value
  )
  .settings(PlayKeys.playDefaultPort := 10800)
  .settings(resolvers += Resolver.jcenterRepo)
  .disablePlugins(sbt.plugins.JUnitXmlReportPlugin)
  .dependsOn(cor30)
  .aggregate(cor28, cor30)


/**
 * Collection Of Routines
 */

lazy val cor28 = Project(appName + "-cor-play-28", file("cor/play-28"))
  .disablePlugins(play.sbt.PlayScala)
  .settings(commonSettings *)
  .settings(
    libraryDependencies ++= AppDependencies.corDependencies(PlayVersion.Play28),
    corSharedSources
  )


lazy val cor30 = Project(appName + "-cor-play-30", file("cor/play-30"))
  .disablePlugins(play.sbt.PlayScala)
  .settings(commonSettings *)
  .settings(
    libraryDependencies ++= AppDependencies.corDependencies(PlayVersion.Play30),
    corSharedSources
  )

def corSharedSources = Seq(
  Compile / unmanagedSourceDirectories += baseDirectory.value / "../shared/src/main/scala",
)
