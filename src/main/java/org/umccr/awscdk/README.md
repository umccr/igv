# IGV Cloud Developer Kit Amazon backend deployment

This package leverages [CDK for Java][cdk-java] to ease the lengthy and [error-prone manual setup for the IGV Amazon
backend][umccr-igv-backend], which can lead to [potentially difficult to debug auth configuration errors][first-igv-aws-issue].

It also serves the purpose of rapidly iterating on implementing new Amazon [integrations and features in an IaC fashion][iac].  

The `cdk.json` file tells the CDK Toolkit how to execute your app.

## Useful commands

 * `cdk ls`          list all stacks in the app
 * `cdk synth`       emits the synthesized CloudFormation template
 * `cdk deploy`      deploy this stack to your default AWS account/region
 * `cdk diff`        compare deployed stack with current state
 * `cdk docs`        open CDK documentation

Enjoy!

[cdk-java]: https://aws.amazon.com/blogs/aws/aws-cloud-development-kit-cdk-java-and-net-are-now-generally-available/
[umccr-igv-backend]: https://umccr.org/blog/igv-amazon-backend-setup/
[first-igv-aws-issue]: https://github.com/igvteam/igv/issues/764#issuecomment-592600537
[iac]: https://en.wikipedia.org/wiki/Infrastructure_as_code