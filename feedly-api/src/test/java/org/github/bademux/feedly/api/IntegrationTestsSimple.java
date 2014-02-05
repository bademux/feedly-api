/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *  Contributors:
 *               Bademus
 */

package org.github.bademux.feedly.api;

import com.google.api.client.util.IOUtils;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.ElementNameAndAttributeQualifier;
import org.github.bademux.feedly.api.model.Preferences;
import org.github.bademux.feedly.api.model.Profile;
import org.github.bademux.feedly.api.model.Topic;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class IntegrationTestsSimple extends AbstractIntegrationTest {

  @Test
  public void testOpml() throws IOException, SAXException {
    //prepare
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    IOUtils.copy(AbstractIntegrationTest.class.getResourceAsStream("/feedly.opml"), baos);
    String testOmplStr = new String(baos.toByteArray(), "UTF8");

    //upload
    service.opml().importSubscription(new ByteArrayInputStream(baos.toByteArray())).execute();

    //download
    String exportedOpmlStr = service.opml().exportSubscription().executeAndDownloadAsString();

    //compare
    Diff diff = new Diff(testOmplStr, exportedOpmlStr);
    diff.overrideElementQualifier(new ElementNameAndAttributeQualifier());
    assertXMLEqual("Imported and exported ompls shoud be equal", diff, true);
  }

  @Test
  public void testProfile() throws IOException {
    String email = UUID.randomUUID().toString() + "@example.com";
    Profile profileChangeSet = new Profile();
    profileChangeSet.setEmail(email);
    Profile testProfile = service.profile().update(profileChangeSet).execute();
    Profile profile = service.profile().get().execute();

    // https://mail.google.com/mail/u/0/#inbox/142674a34716d4a1
    // assertEquals("Wrong size", testProfile.size(), profile.size());
    assertEquals("Can't change user email", testProfile.getEmail(), profile.getEmail());
  }

  @Test
  public void testPreferences() throws IOException, InterruptedException {
    Preferences preferences = new Preferences();
    // TODO: negative value can be used
    preferences.setAutoMarkAsReadOnSelect(new Random().nextInt(10));
    service.preferences().update(preferences).execute();
    Preferences testPreferences = service.preferences().get().execute();
    assertEquals("Can't change AutoMarkAsReadOnSelect", testPreferences.getAutoMarkAsReadOnSelect(),
                 preferences.getAutoMarkAsReadOnSelect());
  }

  @Test
  public void testTopics() throws IOException, InterruptedException {
    final String testTopicName = "arduino";
    //cleanUp
    List<Topic> topics = service.topics().list().execute();
    for (Topic topic : topics) {
      service.topics().delete(topic).execute();
    }

    //add category
    Topic testTopic = service.newTopic(testTopicName, Topic.Interest.HIGH);
    service.topics().update(testTopic).execute();
    // validate
    topics = service.topics().list().execute();
    Topic topic = findIn(topics, testTopicName);
    assertNotNull("Category was not created", topic);
    assertEquals("Wrong topic name", testTopic.getName(), topic.getName());
    assertEquals("Wrong topic InterestLevel", testTopic.getInterest(), topic.getInterest());

    //update
    testTopic.setInterest(Topic.Interest.MEDIUM);
    service.topics().update(testTopic).execute();
    //validate
    topics = service.topics().list().execute();
    topic = findIn(topics, testTopicName);
    assertNotNull("Category was not created", topic);
    assertEquals("Wrong topic name", testTopic.getName(), topic.getName());
    assertEquals("Wrong topic InterestLevel", testTopic.getInterest(), topic.getInterest());

    //remove category
    service.topics().delete(testTopic).execute();
    topics = service.topics().list().execute();
    assertTrue("Topics shouldn't exist", topics.isEmpty());
  }


  @Before
  public void setUp() throws IOException {
    super.setUp(); //create service
  }
}