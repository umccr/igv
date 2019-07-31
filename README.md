# igv
[![Build Status](https://travis-ci.org/igvteam/igv.svg?branch=master)](https://travis-ci.org/igvteam/igv)

UMCCR's AWS Integrative Genomics Viewer - desktop genome visualization tool for Amazon

This is a temporary fork of [Broad's IGV](https://github.com/igvteam/igv), while the [AWS PR gets merged](https://github.com/igvteam/igv/pull/620). 

### Prerequisites

Make sure you have a Java11 SDK installed on your machine, i.e [AWS Corretto Java SDK](https://docs.aws.amazon.com/corretto/latest/corretto-11-ug/downloads-list.html).

If you are a developer, [IntelliJ IDEA](https://www.jetbrains.com/idea/download/download-thanks.html) works best, while VSCode gets better Gradle/Java support.

### Auth

Make sure you drop this file under `~/igv/oauth-config.json`, otherwise the federated authentication will not work:

```json
{
	"apiKey": "",
	"client_id": "2i5nr4gphrcti8o402g5rj97gk",
	"project_id": "igv",
	"scope": "email%20openid%20profile",
	"authorization_endpoint": "https://igv.auth.ap-southeast-2.amazoncognito.com/login",
	"token_endpoint": "https://igv.auth.ap-southeast-2.amazoncognito.com/oauth2/token",
	"auth_provider": "Amazon",
	"auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
	"client_secret": "",
	"aws_cognito_fed_pool_id": "ap-southeast-2:73641aab-24c8-44ac-8bd6-63d140cdc08f",
	"aws_cognito_pool_id": "ap-southeast-2_WzghYXuNl",
	"aws_region": "ap-southeast-2",
	"redirect_uris": [
	  "http://localhost:60151/oauthCallback"
	]
}
```

NOTE: At the time of writing this, the JSON file above is incompatible with upstream IGV (it will not start at all).
NOTE: This requirement will be removed, hopefully soon, by re-using [PROPERTIES_URL](https://github.com/igvteam/igv/blob/0f595577bf2ff7101f8b7c2df1487a5765263f79/src/main/java/org/broad/igv/google/OAuthUtils.java#L110) endpoint fetching mechanism.

### Download

Head out to: https://github.com/umccr/igv/releases and download the latest UMCCR version.
