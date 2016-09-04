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

package com.symphony.jirabot.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.json.JSONObject;

/**
 * Created by ryan.dsouza on 5/27/16.
 */
@JsonIgnoreProperties(
        ignoreUnknown = true
)

public class JiraUser {

    private String self;
    private String emailAddress;
    private String displayName;

    public JiraUser(JSONObject object) {
        this.self = object.getString("self");
        this.displayName = object.getString("displayName");
        this.emailAddress = object.getString("emailAddress");
    }

    public JiraUser() {

    }
    public void setSelf(String self) {
        this.self = self;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getSelf() {
        return self;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getDisplayName() {
        return displayName;
    }
}
