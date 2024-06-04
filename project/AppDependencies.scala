import sbt._

object AppDependencies {

  sealed trait PlayVersion

  object PlayVersion {

    case object Play28 extends PlayVersion
    case object Play30 extends PlayVersion

  }

  private val playVersion = s"-play-30"

  private val bootstrapVersion = "8.6.0"
  private val hmrcMongoVersion = "1.9.0"
  private val enumeratumPlayVersion = "1.8.0"
  private val catsVersion = "2.12.0"
  private val cryptoVersion = "8.0.0"

  lazy val microserviceDependencies: Seq[ModuleID] = {

    val compile = Seq(
      // format: OFF
      "uk.gov.hmrc"          %% s"bootstrap-backend$playVersion"    % bootstrapVersion,
      "uk.gov.hmrc"          %% s"crypto-json$playVersion"          % AppDependencies.cryptoVersion,
      "uk.gov.hmrc.mongo"    %% s"hmrc-mongo$playVersion"           % AppDependencies.hmrcMongoVersion
    // format: ON
    )

    val test = Seq(
      // format:: OFF
      "uk.gov.hmrc" %% s"bootstrap-test$playVersion" % bootstrapVersion,
      "uk.gov.hmrc.mongo" %% s"hmrc-mongo-test$playVersion" % hmrcMongoVersion
    // format: ON
    ).map(_ % Test)

    compile ++ test
  }

  def corDependencies(playVersion: PlayVersion): Seq[ModuleID] = {
    val playVersionSuffix = playVersion match {
      case PlayVersion.Play28 => "-play-28"
      case PlayVersion.Play30 => "-play-30"
    }

    val playJsonDerivedCodesVersion = playVersion match {
      case PlayVersion.Play28 => "7.0.0"
      case PlayVersion.Play30 => "10.1.0"
    }

    val enumeratumPlayVersion = playVersion match {
      case PlayVersion.Play28 => "1.7.0"
      case PlayVersion.Play30 => "1.8.0"
    }
    val playDependency = playVersion match {
      case PlayVersion.Play28 => "com.typesafe.play" %% "play" % "2.8.21"
      case PlayVersion.Play30 => "org.playframework" %% "play" % "3.0.1"
    }

    Seq(
      // format:: OFF
      playDependency % Provided,
      "uk.gov.hmrc" %% s"bootstrap-common$playVersionSuffix" % AppDependencies.bootstrapVersion % Provided,
      "org.typelevel" %% "cats-core" % catsVersion,
      "com.beachape" %% "enumeratum-play" % enumeratumPlayVersion,
      "org.julienrf" %% "play-json-derived-codecs" % playJsonDerivedCodesVersion
    // format: ON
    )
  }
}
