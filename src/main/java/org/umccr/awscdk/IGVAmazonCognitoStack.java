package org.umccr.awscdk;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.core.StackProps;

import software.amazon.awscdk.services.cognito.UserPool;
import software.amazon.awscdk.services.cognito.CfnUserPoolIdentityProvider;
import software.amazon.awscdk.services.cognito.UserPoolClient;
import software.amazon.awscdk.services.cognito.CfnIdentityPool;
import software.amazon.awscdk.services.s3.Bucket;

import java.util.List;

public class IGVAmazonCognitoStack extends Stack {
    String awsRegion = this.getRegion();
    String providedS3Bucket;

    // General Cognito attributes
    String cognitoRoleARN;
    String cognitoEndpointName;
    // UserPool attributes
    String userPoolClientID;
    String userPoolClientSecret;
    String userPoolID;
    String userPoolARN;
    // IdentityPool attributes
    String identityPoolName;


    public IGVAmazonCognitoStack(final Construct parent, final String id) {
        this(parent, id, null);
    }

    public IGVAmazonCognitoStack(final Construct parent, final String id, final StackProps props) {
        super(parent, id, props);

        providedS3Bucket = this.getNode().tryGetContext("s3bucket").toString();
        System.out.println(Bucket.fromBucketName(this, "IgvS3BucketBYO", providedS3Bucket).getBucketName());

        // TODO:
        // Set Allowed OAuth scopes to: "email and profile". CFN==NotFound
        // Set MFA and verification option to: "None – users will have to contact an administrator to reset their passwords". CFN==NotFound
        // Set advanced security to "Audit-only", at least. CFN=="UserPoolAddOns": https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-properties-cognito-userpool-userpooladdons.html. CDK==NotFound
        // "Do you want to remember your user's devices?": Always. CFN==NotFound.
        // "Prevent User Existence Errors": Enabled. CFN==NotFound.

        final UserPool userPool = UserPool.Builder.create(this, "IGV User Pool")
                                                  .selfSignUpEnabled(false)

                                                  .build();
        final UserPoolClient userPoolClient = UserPoolClient.Builder.create(this, "IGV User Pool Client")
                                                                    .userPool(userPool)
                                                                    .userPoolClientName("IGV User Pool Client")
                                                                    .generateSecret(true)
                                                                    .build();


        userPoolClientID = userPoolClient.getUserPoolClientId();
        userPoolClientSecret = userPoolClient.getUserPoolClientClientSecret();
        userPoolID = userPool.getUserPoolId();
        userPoolARN = userPool.getUserPoolArn();

        // TODO: Choose Google for our IDP of choice at UMCCR... but we might want to parametrize/generalize this
        final CfnUserPoolIdentityProvider userPoolIDP = CfnUserPoolIdentityProvider.Builder.create(this, "IGV User Pool IDP")
                                                                                           .userPoolId(userPoolID)
                                                                                            .idpIdentifiers(List.of("Google"))
                                                                                        .providerName("Google")
                                                                                        .providerType("Google")
                                                                                        .build();


        // XXX: UserPool created with ID: ${Token[TOKEN.25]} and ARN: ${Token[TOKEN.22]}
        System.out.println("UserPool created with ID: "+userPoolID+" and ARN: "+userPoolARN);

        final CfnIdentityPool identityPool = CfnIdentityPool.Builder.create(this, "IGV Identity Pool")
                                                                    // Sadly, this is actually required to deploy the stack :-!!!
                                                                    .allowUnauthenticatedIdentities(true)
                                                                    .build();

        identityPoolName = identityPool.getIdentityPoolName();

        generateClientOauthConfig(userPoolClientID, userPoolClientSecret,
                                  identityPoolName, userPoolID, cognitoRoleARN,
                                  awsRegion, cognitoEndpointName);
    }

    private void generateClientOauthConfig(String clientID, String clientSecret,
                                           String cognitoFedPoolID, String cognitoUserPoolID,
                                           String cognitoRoleARN, String awsRegion,
                                           String cognitoEndpointName) {

        JsonObject oauthConfig = new JsonObject();
        oauthConfig.addProperty("client_id", clientID);
        oauthConfig.addProperty("client_secret", clientSecret);
        oauthConfig.addProperty("aws_region", awsRegion);
        oauthConfig.addProperty("aws_cognito_fed_pool_id", cognitoFedPoolID);
        oauthConfig.addProperty("aws_cognito_pool_id", cognitoUserPoolID);
        oauthConfig.addProperty("aws_cognito_role_arn", cognitoRoleARN);
        oauthConfig.addProperty("authorization_endpoint", "https://"+cognitoEndpointName+".auth."+awsRegion+".amazoncognito.com/login");
        oauthConfig.addProperty("token_endpoint", "https://"+cognitoEndpointName+".auth."+awsRegion+".amazoncognito.com/token");


        // Static but necessary properties
        oauthConfig.addProperty("apiKey", "");
        oauthConfig.addProperty("project_id", "igv");
        oauthConfig.addProperty("auth_provider", "Amazon");
        oauthConfig.addProperty("auth_provider_x509_cert_url", "https://www.googleapis.com/oauth2/v1/certs");

        JsonArray redirectURIJsonArray = new JsonArray(1);
        redirectURIJsonArray.add("http://localhost:60151/oauthCallback");
        oauthConfig.addProperty("redirect_uris", redirectURIJsonArray.toString());

        System.out.println(oauthConfig.toString());
        //XXX: JSON serialize oauth-config.json to disk (preferably in .gz format)
    }
}
