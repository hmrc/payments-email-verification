
# payments-email-verification

This service acts as a layer in front of the [email-verification](https://github.com/hmrc/email-verification) service to
keep track of certain counts to prevent too many email verification attempts in certain scenarios. This service
supports the management of email-verification passcode journeys.

## Usage
Ensure you have the `payments-email-verification` and configured in your application.conf:
```
microservices {
   services {
     payments-email-verification {
       protocol = http
       host = localhost
       port = 10800
      } 
   }
} 
```
and bring in `payments-email-verification-cor` as a library dependency to your service **before v2.0.0**
```
"uk.gov.hmrc" %% "payments-email-verification-cor" % <version>
```
or for **v2.0.0 or later** 
```
"uk.gov.hmrc" %% "payments-email-verification-cor-play-30" % <version>
```



All endpoints are authenticated - a GG session must be present where the GG credentials can be retrieved.

### Start an email verification passcode journey
Given you have just captured an email address from the user, to start an email verification journey call the 
start email verification journey endpoint by making use of the `PaymentsEmailVerificationConnector`:
```scala
import paymentsEmailVerification.connectors.PaymentsEmailVerificationConnector
import paymentsEmailVerification.models.api.{StartEmailVerificationJourneyRequest, StartEmailVerificationJourneyResponse}

class MyClass @Inject()(paymentsEmailVerificationConnector: PaymentsEmailVerificationConnector) {
  
  val request: StartEmailVerificationJourneyRequest = StartEmailVerificationJourneyRequest(...)
  val result: Future[StartEmailVerificationJourneyResponse]  = paymentsEmailVerificationConnector.startEmailVerification(request)
}
```
This will make a call to the `email-verification` service to start an email verification passcode journey.  The 
parameters in `StartEmailVerificationJourneyRequest` 
correspond to the same parameters used in the underlying `email-verification` call (see 
[here](https://github.com/hmrc/email-verification#post-verify-email) for more details).

You can map on the result to handle the different scenarios:
```scala
import paymentsEmailVerification.models.api.StartEmailVerificationJourneyResponse.Success
import paymentsEmailVerification.models.api.StartEmailVerificationJourneyResponse.Error
import paymentsEmailVerification.models.EmailVerificationState

val result: Future[StartEmailVerificationJourneyResponse]  = paymentsEmailVerificationConnector.startEmailVerification(request)

result.map{
    case Success(redirectUrl) =>
      // email verification journey successfully started. Redirect user to `redirectUrl` - this should take the user 
      // to the email-verification frontend journey to enter a passcode
    
    case Error(EmailVerificationState.AlreadyVerified) =>
      // the email address has already been verified within the last 24 hours

    case Error(EmailVerificationState.TooManyDifferentEmailAddresses) =>
      // the user has requested to start an email verification journey for too many different email addresses 
      // (default max = 10). The user will be locked out of starting another passcode journey until 24 hours 
      // has elapsed after the earliest attempt was made
    
    case Error(EmailVerificationState.TooManyPasscodeAttempts) =>
      // the user has attempted too many incorrect passcode for the email address. They are not allowed any 
      // more attempts for this email address for 24 hours

    case Error(EmailVerificationState.TooManyPasscodeJourneysStarted) =>
      // the user has requested to start an email verification journey for the same email address too many 
      // times. They are locked out of trying this email address again for 24 hours
}
```
The different error scenarios are checked for in the order given above.

## Getting the email verification result
Once the `email-verification` service makes a callback to your service, to get the email-verification
result use the `PaymentsEmailVerificationConnector`:
```scala
import paymentsEmailVerification.connectors.PaymentsEmailVerificationConnector
import paymentsEmailVerification.models.api.GetEmailVerificationResultRequest
import paymentsEmailVerification.models.{Email, EmailVerificationResult}

class MyClass @Inject()(paymentsEmailVerificationConnector: PaymentsEmailVerificationConnector) {
  
  val request: GetEmailVerificationResultRequest = GetEmailVerificationResultRequest(Email("email@test.com"))
  val result: Future[EmailVerificationResult]  = paymentsEmailVerificationConnector.getEmailVerificationResult(request)
  
  result.map{
    case EmailVerificationResult.Verified =>
      // email address was successfully verified

    case EmailVerificationResult.Locked =>
      // the user has attempted too many incorrect passcode for the email address. They are not allowed any more attempts
      // for this email address for 24 hours
  }
}
```

## Getting the lockout expiry time
When the user has request to start an email verification journey for too many different email addresses, the time 
when the user can make another attempt can get obtained using
`PaymentsEmailVerificationConnector`:
```scala
import paymentsEmailVerification.connectors.PaymentsEmailVerificationConnector
import java.time.LocalDateTime

class MyClass @Inject()(paymentsEmailVerificationConnector: PaymentsEmailVerificationConnector) {
  
 // LocalDateTime to be understood to be time quoted in UTC 
 val result: Future[LocalDateTime] = paymentsEmailVerificationConnector.getEarliestCreatedAtTime()

}
```

## License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").