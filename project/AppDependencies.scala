import sbt.*

object AppDependencies {

  private val playVersion = s"-play-30"

  private val bootstrapVersion = "9.11.0"
  private val hmrcMongoVersion = "2.6.0"
  private val enumeratumPlayVersion = "1.8.2"
  private val catsVersion = "2.13.0"
  private val cryptoVersion = "8.2.0"
  private val borerVersion = "1.16.0"

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
  val circeVersion = "0.14.10"

  lazy val corJourneyDependencies: Seq[ModuleID] = Seq(
    // format:: OFF
    "uk.gov.hmrc"   %% s"bootstrap-common$playVersion"   % AppDependencies.bootstrapVersion % Provided,
    "org.typelevel" %% "cats-core"                       % catsVersion,
    "com.beachape"  %% "enumeratum-play"                 % enumeratumPlayVersion,
    "io.bullet"     %% "borer-core"                      % borerVersion,
    "io.bullet"     %% "borer-derivation"                % borerVersion
    // format: ON
  )
}

