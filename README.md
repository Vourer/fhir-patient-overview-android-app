# FHIR Patient Overview App

A simple Android client app that allows you to connect to a HAPI FHIR server and:
- browse through Patient resources
- display and edit some of Patients' details
- display a hronologically ordered list of Observations and Medication Requests for each Patient, as well as access details of each item on such list
- display simple time series of body weight and height change for each Patient
- display version history for each Patient (if a Patient resource has multiple saved versions) 

### Requirements

In order to run this app on your device, you may need to set up a local HAPI FHIR server.

Here you can find a [guide about Command Line Tool for HAPI FHIR](https://hapifhir.io/hapi-fhir/docs/tools/hapi_fhir_cli.html), which can be used to run a local server. 

You will also need to download HAPI FHIR software, which can be found [here](https://github.com/hapifhir/hapi-fhir/releases).
When building this app, I was working on version `4.2.0`.

After setting up your local server all that's left to do is to change `BASE_URL` in `RestClient` object to your own Server Base URL. If you're using R4 version of HAPI FHIR like me, then it should look like:
```kotlin
private const val BASE_URL = "http://${your_IP_Address}:8080/baseR4"
```

When you're done with that, you're ready to use the app.
