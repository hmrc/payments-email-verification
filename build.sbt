import uk.gov.hmrc.DefaultBuildSettings.scalaSettings
import uk.gov.hmrc.ShellPrompt
import wartremover.WartRemover.autoImport.wartremoverExcluded


val appName: String = "payments-email-verification"

val appScalaVersion = "2.13.10"

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
  majorVersion := 0,
  scalacOptions ++= scalaCompilerOptions,
  update / evictionWarningOptions := EvictionWarningOptions.default.withWarnScalaVersionEviction(false),
  shellPrompt := ShellPrompt(version.value),
  buildInfoPackage := name.value.toLowerCase().replaceAllLiterally("-", ""),
  Compile / doc / scalacOptions := Seq(), //this will allow to have warnings in `doc` task and not fail the build
  scalaSettings,
  uk.gov.hmrc.DefaultBuildSettings.defaultSettings(),
  WartRemoverSettings.wartRemoverSettings,
  ScoverageSettings.scoverageSettings,
  SbtUpdatesSettings.sbtUpdatesSettings,
  libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
) ++ ScalariformSettings.scalariformSettings

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin, SbtAutoBuildPlugin, SbtGitVersioning)
  .settings(commonSettings: _*)
  .settings(
    scalaVersion := appScalaVersion,
    libraryDependencies ++= AppDependencies.microserviceDependencies,
    wartremoverExcluded ++= (Compile / routes).value
  )
  .settings(PlayKeys.playDefaultPort := 10800)
  .settings(resolvers += Resolver.jcenterRepo)
  .disablePlugins(sbt.plugins.JUnitXmlReportPlugin)
  .dependsOn(cor)
  .aggregate(cor)

/**
 * Collection Of Routines
 */
lazy val cor = Project(appName + "-cor", file("cor"))
  .enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning)
  .settings(commonSettings: _*)
  .settings(
    scalaVersion := appScalaVersion,
    libraryDependencies ++= AppDependencies.corJourneyDependencies
  )
