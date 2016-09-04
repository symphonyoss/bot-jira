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

package com.symphony.jirabot.configurations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collections;
import java.util.Set;

/**
 * Created by ryan.dsouza on 6/2/16.
 */
public class SymphonyClientConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(SymphonyClientConfiguration.class);

    private final IConfigurationProvider configurationProvider;

    public SymphonyClientConfiguration(IConfigurationProvider configurationProvider) {
        this.configurationProvider = configurationProvider;
    }

    /**
     * returns a File object for the certificate, if it exists, else throws a RuntimeException
     */
    public File getCertificateFile() {
        String classpathResource = '/' + configurationProvider.getSymphonyCertificateFileName();
        LOG.info("attempting to load certificate file as classpath resource at " + classpathResource);
        File certificate = new File(getClass().getResource(classpathResource).getFile());
        if(!certificate.exists()) {
            throw new RuntimeException("no certificate found at " + certificate.getAbsolutePath());
        }
        return certificate;
    }

    public String getKeystorePassword() {
        return configurationProvider.getSymphonyKeystorePassword();
    }

    public String getKeystoreType() {
        return configurationProvider.getSymphonyKeystoreType();
    }

    public String getBaseURL() {
        return configurationProvider.getSymphonyBaseURL();
    }

    public Set<String> getNamesOfSymphonyRoomsToPostIn() {
        return Collections.unmodifiableSet(configurationProvider.getNamesOfSymphonyRoomsToPostIn());
    }

    public String getPodBasePath() {
        return this.getBaseURL() + "/pod";
    }

    public String getAgentBasePath() {
        return this.getBaseURL() + "/agent";
    }

    public String getSBEBasePath() {
        return this.getBaseURL() + "/sessionauth";
    }

    public String getKeyManagerBasePath() {
        return this.getBaseURL() + "/keyauth";
    }
}
