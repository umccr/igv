package org.umccr.awscdk;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.core.StackProps;

import software.amazon.awscdk.services.cognito.UserPool;
import software.amazon.awscdk.services.cognito.UserPoolClient;

import software.amazon.awscdk.services.cognito.CfnIdentityPool;

import software.amazon.awscdk.services.s3.Bucket;

public class IGVAmazonCognitoStack extends Stack {
    String awsRegion = this.getRegion();
    String providedS3Bucket;

    String userPoolClientID;
    String userPoolClientSecret;
    String userPoolID;
    String userPoolARN;

    String cognitoRoleARN;
    String identityPoolName;
    String cognitoEndpointName;

    public IGVAmazonCognitoStack(final Construct parent, final String id) {
        this(parent, id, null);
    }

    public IGVAmazonCognitoStack(final Construct parent, final String id, final StackProps props) {
        super(parent, id, props);

        providedS3Bucket = this.getNode().tryGetContext("s3bucket").toString();
        System.out.println(Bucket.fromBucketName(this, "IgvS3BucketBYO", providedS3Bucket).getBucketName());

        // 4. Set Allowed OAuth scopes to email and profile?
        // N. Show the admin which URL or oauth-config.json.gz to deploy

        final UserPool userPool = UserPool.Builder.create(this, "IGV User Pool").build();
        final UserPoolClient userPoolClient = UserPoolClient.Builder.create(this, "IGV User Pool Client").userPool(userPool).build();

        userPoolClientID = userPoolClient.getUserPoolClientId();
        userPoolClientSecret = userPoolClient.getUserPoolClientClientSecret();
        userPoolID = userPool.getUserPoolId();
        userPoolARN = userPool.getUserPoolArn();

        // XXX: UserPool created with ID: ${Token[TOKEN.25]} and ARN: ${Token[TOKEN.22]}
        System.out.println("UserPool created with ID: "+userPoolID+" and ARN: "+userPoolARN);

        final CfnIdentityPool identityPool = CfnIdentityPool.Builder.create(this, "IGV Identity Pool").allowUnauthenticatedIdentities(false).build();

        identityPoolName = identityPool.getIdentityPoolName();
        //identityPool.setCognitoIdentityProviders();


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
