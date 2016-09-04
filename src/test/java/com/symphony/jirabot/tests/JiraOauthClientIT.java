package com.symphony.jirabot.tests;

import static org.junit.Assert.assertNotNull;

import com.symphony.jirabot.clients.JiraOauthClient;
import com.symphony.jirabot.configurations.SimpleConfigurationProvider;
import com.symphony.jirabot.models.JiraIssue;
import com.symphony.jirabot.models.JiraProject;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

/**
 * This is an integration test as it depends on an external system (i.e. JIRA).
 *
 * Created by ryan.dsouza on 6/7/16.
 */
public class JiraOauthClientIT {

    JiraOauthClient jiraClient = new JiraOauthClient(new SimpleConfigurationProvider());

    @Before
    public void authenticate() throws Exception {
        jiraClient.authenticate();
    }

    @Test
    public void getJiraUserForEmailAddress() throws Exception {
        assertNotNull(jiraClient.getJiraUserForEmailAddress("paul@symphony.com"));
    }

    @Test
    public void getAllProjects() throws Exception {
        JiraProject[] projects = jiraClient.getAllProjects();
        assertNotNull(projects);

        for (JiraProject project : projects) {
            assertNotNull(project);
        }
    }

    @Test
    public void getIssuesForProject() throws Exception {
        JiraProject[] projects = jiraClient.getAllProjects();
        assertNotNull(projects);

        for(JiraProject project : projects) {
            assertNotNull(project);

            ArrayList<JiraIssue> issues = jiraClient.getIssuesForProject(project);
            assertNotNull(issues);

            for (JiraIssue issue : issues) {
                assertNotNull(issue);
            }
        }
    }

    @Test
    public void makeAuthenticatedRequest() throws Exception {
        String myselfURL = "https://perzoinc.atlassian.net/rest/api/2/myself";
        String result = jiraClient.makeAuthenticatedRequest(myselfURL, null, "GET");
        assertNotNull(result);
    }
}