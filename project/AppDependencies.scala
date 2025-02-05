import sbt.*

object AppDependencies {

  private val playVersion = s"-play-30"

  private val bootstrapVersion = "9.8.0"
  private val hmrcMongoVersion = "2.4.0"
  private val enumeratumPlayVersion = "1.8.2"
  private val catsVersion = "2.13.0"
  private val cryptoVersion = "8.1.0"
  private val playJsonDerivedCodecsVersion = "11.0.0"

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
      "uk.gov.hmrc"       %% s"bootstrap-test$playVersion"  % bootstrapVersion,
      "uk.gov.hmrc.mongo" %% s"hmrc-mongo-test$playVersion" % hmrcMongoVersion
    // format: ON
    ).map(_ % Test)

    compile ++ test
  }

  lazy val corJourneyDependencies: Seq[ModuleID] = Seq(
    // format:: OFF
    "uk.gov.hmrc"   %% s"bootstrap-common$playVersion"   % AppDependencies.bootstrapVersion % Provided,
    "org.typelevel" %% "cats-core"                       % catsVersion,
    "com.beachape"  %% "enumeratum-play"                 % enumeratumPlayVersion,
    "dev.zio"       %% "zio-json"                        % "0.7.15"
    // format: ON
  )
}

