# igv
[![Build Status](https://travis-ci.org/igvteam/igv.svg?branch=master)](https://travis-ci.org/igvteam/igv)

UMCCR's AWS Integrative Genomics Viewer - desktop genome visualization tool for Amazon

This is a temporary fork of [Broad's IGV](https://github.com/igvteam/igv), while the [AWS PR gets merged](https://github.com/igvteam/igv/pull/620). 

### Prerequisites

Make sure you have a Java11 SDK installed on your machine, i.e [AWS Corretto Java SDK](https://docs.aws.amazon.com/corretto/latest/corretto-11-ug/downloads-list.html).

If you are a developer, [IntelliJ IDEA](https://www.jetbrains.com/idea/download/download-thanks.html) works best, while VSCode gets better Gradle/Java support.

### Auth

Make sure you drop this file under `~/igv/oauth-config.json`, otherwise the federated authentication will not work:


### Download

Head out to: https://github.com/umccr/igv/releases and download the latest UMCCR version.
