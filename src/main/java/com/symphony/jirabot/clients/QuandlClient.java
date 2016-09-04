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

import com.google.common.collect.Collections2;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by ryan.dsouza on 7/5/16.
 */
public class QuandlClient {

    private static final String BASE_URL = "https://www.quandl.com/api/v3/";
    private static final String DATABASE_URL = BASE_URL + "databases/";
    private static final String DATASET_URL = BASE_URL + "datasets/";

    private final String apiKey;

    public QuandlClient(String apiKey) {
        this.apiKey = apiKey;
    }

    private static String mapToQueryString(HashMap<String, Object> parameters) {
        if(parameters == null || parameters.size() == 0) {
            return "";
        }

        StringBuilder queryString = new StringBuilder("&");
        for(String key : parameters.keySet()) {
            queryString.append(urlEncodeUTF8(key));
            queryString.append("=");
            queryString.append(urlEncodeUTF8(parameters.get(key).toString()));
            queryString.append("&");
        }

        if(queryString.charAt(queryString.length() - 1) == '&') {
            queryString = queryString.deleteCharAt(queryString.length() - 1);
        }

        return queryString.toString();
    }

    private static String urlEncodeUTF8(String string) {
        try {
            return URLEncoder.encode(string, "UTF-8");
        }
        catch (UnsupportedEncodingException error) {
            throw new UnsupportedOperationException(error);
        }
    }

    private JSONObject makeGetRequest(String endpoint, HashMap<String, Object> parameters) {

        String url = BASE_URL + endpoint + "?api_key=" + this.apiKey + mapToQueryString(parameters);

        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            return new JSONObject(response.toString());
        }
        catch (MalformedURLException error) {
            throw new UnsupportedOperationException(error);
        }
        catch (IOException error) {
            throw new RuntimeException(error);
        }
        catch(JSONException error) {
            throw new RuntimeException(error);
        }
    }

    public String getListOfDatabases() {
        JSONObject databases = this.makeGetRequest("databases.json", null);

        return databases.toString();
    }

    public static void main(String[] ryan) throws Exception{

        System.out.println("HI");
        QuandlClient client = new QuandlClient("wo4FRHqkoEDcvfqQ7wtF");

        System.out.println(client.getListOfDatabases());

    }
}
