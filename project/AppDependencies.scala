import sbt._

object AppDependencies {

  private val playVersion = s"-play-30"

  private val bootstrapVersion = "8.2.0"
  private val hmrcMongoVersion = "1.6.0"
  private val enumeratumPlayVersion = "1.8.0"
  private val catsVersion = "2.10.0"
  private val cryptoVersion = "7.6.0"
  private val playJsonDerivedCodesVersion = "10.1.0"

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

  lazy val corDependencies: Seq[ModuleID] = Seq(
    // format:: OFF
    "uk.gov.hmrc" %% s"bootstrap-common$playVersion" % AppDependencies.bootstrapVersion % Provided,
    "org.typelevel" %% "cats-core" % catsVersion,
    "com.beachape" %% "enumeratum-play" % AppDependencies.enumeratumPlayVersion,
    "org.julienrf" %% "play-json-derived-codecs" % AppDependencies.playJsonDerivedCodesVersion
  // format: ON
  )
}
