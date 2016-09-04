package com.symphony.jirabot.tests;

import static org.junit.Assert.assertNotNull;

import com.symphony.api.agent.model.V2Message;
import com.symphony.api.pod.model.V2RoomDetail;
import com.symphony.jirabot.clients.SymphonyClient;
import com.symphony.jirabot.configurations.SimpleConfigurationProvider;
import com.symphony.jirabot.formatters.MessageML;

import org.junit.Before;
import org.junit.Test;

/**
 * This is an integration test as it depends on an external system (i.e. Symphony platform).
 *
 * Created by ryan.dsouza on 6/7/16.
 */
public class SymphonyClientIT {

    SymphonyClient symphonyClient = new SymphonyClient(new SimpleConfigurationProvider());

    String symphonyRoomID = "MVxbiGfFR/AWs2+E2XQKfH///q00Jfq8dA==";

    @Before
    public void authenticate() throws Exception {
        symphonyClient.authenticate();
    }

    @Test
    public void getRoomForSearchQuery() throws Exception {
        V2RoomDetail roomDetail = symphonyClient.getRoomForSearchQuery("iv");
        assertNotNull(roomDetail);
    }

    @Test
    public void sendMessage() throws Exception {
        String plainText = "Creating a plain text message";
        V2Message response = symphonyClient.sendMessage(symphonyRoomID, plainText);
        assertNotNull(response);
    }

    @Test
    public void sendMessage1() throws Exception {
        MessageML messageML = new MessageML();
        assertNotNull(messageML);

        messageML.addBoldText("Test");
        messageML.addLineBreak();
        messageML.addItalicText("Creating a message using MessageML");
        messageML.addLineBreak();
        messageML.addLink("https://symphony.com");

        assertNotNull(messageML.toString());

        V2Message response = symphonyClient.sendMessage(symphonyRoomID, messageML);
        assertNotNull(response);
    }

    @Test
    public void joinRoom() throws Exception {

    }

}