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

import org.github.bademux.feedly.api.model.Category;
import org.github.bademux.feedly.api.model.EntriesResponse;
import org.github.bademux.feedly.api.model.Entry;
import org.github.bademux.feedly.api.model.Feed;
import org.github.bademux.feedly.api.model.FeedInfo;
import org.github.bademux.feedly.api.model.MarkReadsResponse;
import org.github.bademux.feedly.api.model.MarkTagsResponse;
import org.github.bademux.feedly.api.model.StreamsResponse;
import org.github.bademux.feedly.api.model.Subscription;
import org.github.bademux.feedly.api.model.Tag;
import org.github.bademux.feedly.api.model.Topic;
import org.github.bademux.feedly.api.model.UnreadResponse;
import org.github.bademux.feedly.api.service.Feedly;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static com.google.api.client.repackaged.com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class IntegrationTestsWithInitialData extends AbstractIntegrationTest {

  @Test
  public void testSubscription() throws IOException {
    // subscribe
    Category testCategory = testSubscription.getCategories().get(0);

    // validate
    List<Subscription> subscriptions = service.subscriptions().list().execute();

    Subscription subscription = findIn(subscriptions, Subscription.PREFIX + '/' + TEST_FEED_1);
    assertNotNull("Subscription was not created", subscription);
    // 'global.uncategorized' category is not added to the list
    assertEquals("Wrong  number of categories", subscription.getCategories().size(),
                 subscription.getCategories().size());
    assertThat(testSubscription.getCategories(), hasItems(
        (Matcher) hasProperty("label", equalTo(subscription.getCategories().get(0).getLabel())),
        (Matcher) hasProperty("label", equalTo(subscription.getCategories().get(1).getLabel()))
    ));

    //remove subscription
    service.subscriptions().detete(subscription).execute();
    subscriptions = service.subscriptions().list().execute();
    assertEquals("Wrong  number of subscriptions", 0, subscriptions.size());
  }

  @Test
  public void testCategories() throws IOException {
    Category testCategory = testSubscription.getCategories().get(0);
    final String testCategoryName = testCategory.getName();

    // validate
    List<Category> categories = service.categories().list().execute();
    Category category = findIn(categories, testCategoryName);
    assertNotNull("Category was not created", category);
    assertEquals("Wrong category name", testCategory.getName(), category.getName());
    assertEquals("Wrong category label", testCategory.getLabel(), category.getLabel());

    //change label
    testCategory.setLabel(testCategoryName + "New");
    service.categories().update(testCategory).execute();

    // validate
    categories = service.categories().list().execute();
    category = findIn(categories, testCategoryName);
    assertNotNull("Category was not created", category);
    assertEquals("Wrong category name", testCategory.getName(), category.getName());
    assertEquals("Wrong category label", testCategory.getLabel(), category.getLabel());

    // remove category
    service.categories().detete(category).execute();
    categories = service.categories().list().execute();
    assertThat(categories, hasItem(
        not((Matcher) hasProperty("label", equalTo(testCategory.getName())))
    ));
  }

  @Test
  public void testStreams() throws IOException {
    //test subscriptions
    Feedly.Streams.Ids requestIds = service.streams().ids(testSubscription)
        .setCount(1).setUnreadOnly(false);
    int i = 0;
    do {
      try {
        StreamsResponse ids = requestIds.execute();
        // ...
        requestIds.setContinuation(ids.getContinuation());
      } catch (IOException e) {
        requestIds.setContinuation(null);
      }
    } while (isNullOrEmpty(requestIds.getContinuation()) == false && ++i < 3);
    assertThat(i, greaterThan(0));

    //test contents api
    Feedly.Streams.Contents requestContents = service.streams().contents(testSubscription)
        .setCount(1).setUnreadOnly(false);
    i = 0;
    do {
      try {
        EntriesResponse contents = requestContents.execute();
        // ...
        requestContents.setContinuation(contents.getContinuation());
      } catch (IOException e) {
        requestContents.setContinuation(null);
      }
    } while (isNullOrEmpty(requestContents.getContinuation()) == false && ++i < 3);
    assertThat(i, greaterThan(0));

    //test categories
    Category category = testSubscription.getCategories().get(0);

    requestIds = service.streams().ids(category)
        .setCount(1).setUnreadOnly(false);
    StreamsResponse ids = requestIds.execute();
    assertEquals(ids.ids().size(), (int) requestIds.getCount());

    requestContents = service.streams().contents(category)
        .setCount(1).setUnreadOnly(false);
    EntriesResponse entries = requestContents.execute();
    assertEquals(entries.items().size(), (int) requestContents.getCount());
  }

  @Test
  public void testMixes() throws IOException {
    //test subscriptions
    Feedly.Mixes.Get requestContents = service.mixes().contents(testSubscription)
        .setCount(1).setUnreadOnly(false);
    EntriesResponse entries = requestContents.execute();
    assertEquals(entries.items().size(), (int) requestContents.getCount());

    //test categories
    Category category = testSubscription.getCategories().get(0);

    requestContents = service.mixes().contents(category).setCount(1).setUnreadOnly(false);
    entries = requestContents.execute();
    assertEquals(entries.items().size(), (int) requestContents.getCount());

    //test topics
    Topic topic = new Topic("tech", Topic.Interest.MEDIUM);

    requestContents = service.mixes().contents(topic).setCount(1).setUnreadOnly(false);
    entries = requestContents.execute();
    assertEquals(entries.items().size(), (int) requestContents.getCount());
  }

  @Test
  public void testTags() throws IOException, InterruptedException {
    Category category = testSubscription.getCategories().get(0);

    //get Entry Ids
    Feedly.Streams.Ids request = service.streams().ids(category).setCount(2).setUnreadOnly(false);
    StreamsResponse ids = request.execute();
    assertEquals(ids.ids().size(), (int) request.getCount());
    String entryId1 = ids.ids().get(0);
    String entryId2 = ids.ids().get(1);

    //tag entry
    Tag testTag1 = service.newTag("test1");
    Tag testTag2 = service.newTag("test2");
    service.tags()
        .tag(asList(entryId1, entryId2), asList(testTag1.getId(), testTag2.getId())).execute();

    // validate
    List<Tag> tags = service.tags().list().execute();
    assertEquals(2, tags.size());
    assertEquals(tags.get(0), testTag1);
    assertEquals(tags.get(1), testTag2);

    Entry entry1 = service.entries().get(entryId1).execute().get(0);
    assertTrue(entry1.getTags().contains(testTag1));
    assertTrue(entry1.getTags().contains(testTag2));

    Entry entry2 = service.entries().get(entryId2).execute().get(0);
    assertTrue(entry2.getTags().contains(testTag1));
    assertTrue(entry2.getTags().contains(testTag2));

    //untag entry
    service.tags().untag(asList(entryId1, entryId2), asList(testTag1.getId())).execute();
    service.tags().untagByEntry(asList(entry2), asList(testTag2)).execute();
    // validate
    entry1 = service.entries().get(entryId1).execute().get(0);
    assertEquals(1, entry1.getTags().size());
    entry2 = service.entries().get(entryId2).execute().get(0);
    assertNull(entry2.getTags());

    //test tag operation
    MarkTagsResponse tagsOp = service.markers().tags().execute();
    assertNotNull(tagsOp.taggedEntries());
    assertNotNull(tagsOp.getTagIdBy(entryId1));
  }

  @Test
  public void testMarkers() throws IOException {
    Category category = testSubscription.getCategories().get(0);

    //check feed
    UnreadResponse counts = service.markers().counts()
        .setStreamId(testSubscription.getId()).execute();
    assertEquals(1, counts.unreadCounts().size());
    assertEquals(Feed.class, counts.unreadCounts().get(0).getType());
    assertEquals(testSubscription.getId(), counts.unreadCounts().get(0).getId());
    //check category
    counts = service.markers().counts().setStreamId(category.getId()).execute();
    assertEquals(2, counts.unreadCounts().size());
    assertThat(counts.unreadCounts(), hasItems(
        (Matcher) hasProperty("id", equalTo(counts.unreadCounts().get(0).getId())),
        (Matcher) hasProperty("id", equalTo(counts.unreadCounts().get(1).getId()))
    ));
    //check  all
    counts = service.markers().counts().execute();
    assertEquals(5, counts.unreadCounts().size());
    assertThat(counts.unreadCounts(), hasItems(
        (Matcher) hasProperty("id", equalTo(counts.unreadCounts().get(0).getId())),
        (Matcher) hasProperty("id", equalTo(counts.unreadCounts().get(1).getId())),
        (Matcher) hasProperty("id", containsString(Category.UNCATEGORIZED)),
        (Matcher) hasProperty("id", containsString(Category.ALL))
    ));
    //check entry
    Feedly.Streams.Ids request = service.streams().ids(category).setCount(1).setUnreadOnly(false);
    StreamsResponse ids = request.execute();
    assertEquals(ids.ids().size(), (int) request.getCount());
    String entryId1 = ids.ids().get(0);
    counts = service.markers().counts().setStreamId(entryId1).execute();
    assertEquals(1, counts.unreadCounts().size());
    assertThat(counts.unreadCounts(), hasItem((Matcher) hasProperty("id", equalTo(entryId1))));
  }

  @Test
  public void testMarkersMarkFeedAsRead() throws IOException {
    //check feed
    UnreadResponse counts = service.markers().counts()
        .setStreamId(testSubscription.getId()).execute();
    assertEquals(1, counts.unreadCounts().size());
    assertEquals(Feed.class, counts.unreadCounts().get(0).getType());
    assertEquals(testSubscription.getId(), counts.unreadCounts().get(0).getId());
    assertThat(counts.unreadCounts().get(0), allOf(
        hasProperty("id", equalTo(testSubscription.getId())), hasProperty("count", greaterThan(0))
    ));

    //get latest entry
    Feedly.Streams.Ids request = service.streams().ids(testSubscription)
        .setCount(1).setRanked(Feedly.Ranked.NEWEST);
    StreamsResponse ids = request.execute();
    assertEquals(ids.ids().size(), (int) request.getCount());
    String entryId1 = ids.ids().get(0);
    //mark feed as read
    service.markers().feedsAsRead(asList(testSubscription.getId()), entryId1).execute();
    counts = service.markers().counts().setStreamId(testSubscription.getId()).execute();
    assertEquals(1, counts.unreadCounts().size());
    assertThat(counts.unreadCounts().get(0), allOf(
        hasProperty("id", equalTo(testSubscription.getId())), hasProperty("count", equalTo(0))
    ));

    //test read operation
    MarkReadsResponse readsOp = service.markers().reads().execute();
    assertNotNull(readsOp.feeds());
    assertNotNull(readsOp.feedAsOf(testSubscription.getId()));
  }

  @Test
  public void testFeeds() throws IOException {
    FeedInfo feed = service.feeds().get(testSubscription.getId()).execute();
    assertEquals(feed.getId(), testSubscription.getId());

    List<FeedInfo> feeds = service.feeds().list(asList(testSubscription.getId())).execute();
    assertEquals(1, feeds.size());
    assertEquals(feeds.get(0).getId(), testSubscription.getId());
  }

  @Test
  public void testEntries() throws IOException {
    //get Entry Ids
    Category category = testSubscription.getCategories().get(0);
    Feedly.Streams.Ids request = service.streams().ids(category).setCount(2).setUnreadOnly(false);
    StreamsResponse ids = request.execute();
    assertEquals(ids.ids().size(), (int) request.getCount());
    String entryId1 = ids.ids().get(0);

    //check
    Entry entry1 = service.entries().get(entryId1).execute().get(0);
    assertEquals(entry1.getId(), entryId1);

    List<Entry> entries = service.entries().list(ids.ids()).execute();
    assertEquals(entries.size(), ids.ids().size());

    //Create and tag an entry
    Tag tag = service.newTag("TestTag");
    Entry testEntry = new Entry();
    testEntry.setTitle("TestEntry " + UUID.randomUUID().toString());
    testEntry.setContent("<p>Test html content</p>");
    Tag testTag = service.newTag("TestTag");
    testEntry.addTag(testTag);
    service.entries().update(testEntry).execute();
    EntriesResponse result = service.streams().contents(tag).execute();
    assertEquals(1, result.items().size());
    assertEquals(testEntry.getTitle(), result.items().get(0).getTitle());
    assertTrue(result.items().get(0).getTags().contains(testTag));
  }

  public final static String TEST_FEED_1 = "http://feeds.feedburner.com/design-milk";

  protected Subscription testSubscription;

  @Before
  public void setUp() throws IOException {
    super.setUp(); //create service
    testSubscription = newSubscriptionWithCategory(service, TEST_FEED_1);
  }

  @After
  public void tearDown() throws IOException {
    super.cleanUp();
  }
}