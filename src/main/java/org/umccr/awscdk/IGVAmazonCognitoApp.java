package org.umccr.awscdk;

import software.amazon.awscdk.core.App;

public final class IGVAmazonCognitoApp {
    public static void main(final String[] args) {
        App app = new App();
        new IGVAmazonCognitoStack(app, "IGV Cognito Stack");
        app.synth();
    }
}
