/*
 *
 *
 * Copyright 2016 Symphony Communication Services, LLC
 *
 * Licensed to Symphony Communication Services, LLC under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package com.symphony.jirabot.clients;

import com.symphony.jirabot.configurations.SimpleConfigurationProvider;
import com.google.common.collect.ImmutableList;
import net.oauth.*;
import net.oauth.client.OAuthClient;
import net.oauth.client.httpclient4.HttpClient4;
import net.oauth.signature.RSA_SHA1;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import static net.oauth.OAuth.OAUTH_VERIFIER;

/**
 * Responsible for setting up authentication with Atlassian
 * Run main() for instructions TODO replace this with concise instructions
 */
public final class AtlassianOAuthClient {
    
    private static final Logger LOG = LoggerFactory.getLogger(AtlassianOAuthClient.class);

    protected static final String SERVLET_BASE_URL = "/plugins/servlet";

    private final String consumerKey;
    private final String privateKey;
    private final String baseUrl;
    private final String callback;
    private OAuthAccessor accessor;

    public enum Command {

        // TODO replace these names with something more descriptive
        FIRST_RUN("firstRun"),
        SECOND_RUN("secondRun");

        private final String name;

        Command(final String name) {
            this.name = name;
        }
    }

    public AtlassianOAuthClient(String consumerKey, String privateKey, String baseUrl, String callback)  {
        this.consumerKey = consumerKey;
        this.privateKey = privateKey;
        this.baseUrl = baseUrl;
        this.callback = callback;
    }

    public TokenSecretVerifierHolder getRequestToken() {
        try {
            OAuthAccessor accessor = getAccessor();
            OAuthClient oAuthClient = new OAuthClient(new HttpClient4());
            List<OAuth.Parameter> callBack;
            if (callback == null || "".equals(callback)) {
                callBack = Collections.<OAuth.Parameter>emptyList();
            }
            else {
                callBack = ImmutableList.of(new OAuth.Parameter(OAuth.OAUTH_CALLBACK, callback));
            }

            OAuthMessage message = oAuthClient.getRequestTokenResponse(accessor, "POST", callBack);
            TokenSecretVerifierHolder tokenSecretVerifier = new TokenSecretVerifierHolder();
            tokenSecretVerifier.token = accessor.requestToken;
            tokenSecretVerifier.secret = accessor.tokenSecret;
            tokenSecretVerifier.verifier = message.getParameter(OAUTH_VERIFIER);
            return tokenSecretVerifier;
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to obtain request token", e);
        }
        catch (OAuthException e) {
            throw new RuntimeException("Failed to obtain request token", e);
        }
        catch (URISyntaxException e) {
            throw new RuntimeException("Failed to obtain request token", e);
        }
    }

    public String swapRequestTokenForAccessToken(String requestToken, String tokenSecret, String oauthVerifier) {
        try {
            OAuthAccessor accessor = getAccessor();
            OAuthClient client = new OAuthClient(new HttpClient4());
            accessor.requestToken = requestToken;
            accessor.tokenSecret = tokenSecret;
            OAuthMessage message = client.getAccessToken(accessor, "POST",
                    ImmutableList.of(new OAuth.Parameter(OAuth.OAUTH_VERIFIER, oauthVerifier)));
            return message.getToken();
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to get Token from Access Token", e);
        }
        catch (OAuthException e) {
            throw new RuntimeException("Failed to get Token from Access Token", e);
        }
        catch (URISyntaxException e) {
            throw new RuntimeException("Failed to getToken from Access Token", e);
        }
    }

    public String makeAuthenticatedRequest(String url, String accessToken) {
        try {
            OAuthAccessor accessor = getAccessor();
            OAuthClient client = new OAuthClient(new HttpClient4());
            accessor.accessToken = accessToken;
            OAuthMessage response = client.invoke(accessor, url, Collections.<Map.Entry<?, ?>>emptySet());
            return response.readBodyAsString();
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to make an authenticated request.", e);
        }
        catch (OAuthException e) {
            throw new RuntimeException("Failed to make an authenticated request.", e);
        }
        catch (URISyntaxException e) {
            throw new RuntimeException("Failed to make an authenticated request.", e);
        }
    }

    private final OAuthAccessor getAccessor() {
        if (accessor == null) {
            OAuthServiceProvider serviceProvider = new OAuthServiceProvider(getRequestTokenUrl(),
                getAuthorizeUrl(), getAccessTokenUrl());
            OAuthConsumer consumer = new OAuthConsumer(callback, consumerKey, null, serviceProvider);
            consumer.setProperty(RSA_SHA1.PRIVATE_KEY, privateKey);
            consumer.setProperty(OAuth.OAUTH_SIGNATURE_METHOD, OAuth.RSA_SHA1);
            accessor = new OAuthAccessor(consumer);
        }
        return accessor;
    }

    private String getAccessTokenUrl() {
        return baseUrl + SERVLET_BASE_URL + "/oauth/access-token";
    }

    private String getRequestTokenUrl() {
        return  baseUrl + SERVLET_BASE_URL + "/oauth/request-token";
    }

    public String getAuthorizeUrlForToken(String token) {
        return getAuthorizeUrl() + "?oauth_token=" + token;
    }

    private String getAuthorizeUrl() {
        return baseUrl + SERVLET_BASE_URL + "/oauth/authorize";
    }

    final class TokenSecretVerifierHolder {
        public String token;
        public String verifier;
        public String secret;
    }

    private static String getCommandNames() {
        String names = "";
        for(Command command : Command.values()) {
            names += command.name + " ";
        }

        return names;
    }


    public static void main(String[] args) {

        Scanner keyboard = new Scanner(System.in);
        final String command;

        if(args.length == 0) {
            LOG.info("Enter argument: \t" + getCommandNames() + "\n");
            command = keyboard.nextLine();
        }
        else {
            command = args[0];
        }

        if(command != null && command.length() != 0) {

            SimpleConfigurationProvider configurationProvider = new SimpleConfigurationProvider();

            if(Command.FIRST_RUN.name.equals(command)) {

                AtlassianOAuthClient jiraOauthClient = new AtlassianOAuthClient(
                    configurationProvider.getJiraConsumerKey(),
                    configurationProvider.getJiraConsumerPrivateKey(),
                    configurationProvider.getJiraBaseURL(),
                    configurationProvider.getJiraCallback());

                TokenSecretVerifierHolder requestToken = jiraOauthClient.getRequestToken();
                final String authorizeUrl = jiraOauthClient.getAuthorizeUrlForToken(requestToken.token);
                LOG.info("Retrieved request token. go to " + authorizeUrl);
                LOG.info("Then copy the value of \"oauth_verifier\" here. Ex, pass \"awNfrI\" from " +
                    "\"https://placeholder/?oauth_token=t70PAmYPJs9qU316ViY2Adjh6QVzFovg&oauth_verifier=awNfrI\"");

                String oauthVerifier = keyboard.nextLine();
                final String accessToken = jiraOauthClient.swapRequestTokenForAccessToken(
                    requestToken.token, configurationProvider.getJiraConsumerPrivateKey(),
                    oauthVerifier);

                LOG.info("Token is " + requestToken.token);
                LOG.info("Token secret is " + requestToken.secret);
                LOG.info("Access token is : " + accessToken);
            }
            else if(Command.SECOND_RUN.name.equals(command)) {

                final AtlassianOAuthClient jiraOauthClient = new AtlassianOAuthClient(
                    configurationProvider.getJiraConsumerKey(),
                    configurationProvider.getJiraConsumerPrivateKey(), null,
                    configurationProvider.getJiraCallbackURI());

                LOG.info("Enter access token: ");
                final String accessToken = keyboard.nextLine();

                LOG.info("Enter URL of issue to access ex. " +
                    "\"https://perzoinc.atlassian.net/rest/api/2/issue/DES-9574\"");
                final String issueURL = keyboard.nextLine();

                try {
                    final String responseAsString =
                        jiraOauthClient.makeAuthenticatedRequest(issueURL, accessToken);

                    JSONObject root = new JSONObject(responseAsString);
                    JSONObject fields = root.getJSONObject("fields");
                    String assigneeEmail = fields.getJSONObject("assignee").getString("emailAddress");
                    String status = fields.getJSONObject("status").getString("name");
                    LOG.info(assigneeEmail + ' ' + status);
                } catch (Exception e) {
                    LOG.info(e.getCause() == null ? e.getMessage() : e.getCause().getMessage());
                }
            }
            else {
                LOG.info("No arguments: \t" + getCommandNames());
            }
        }
        else {
            LOG.info("No arguments: \t" + getCommandNames());
        }
    }
}