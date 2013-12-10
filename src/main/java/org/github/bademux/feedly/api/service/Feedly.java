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

package org.github.bademux.feedly.api.service;

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.GenericData;
import com.google.api.client.util.Key;

import org.github.bademux.feedly.api.model.Category;
import org.github.bademux.feedly.api.model.EntriesResponse;
import org.github.bademux.feedly.api.model.FeedInfo;
import org.github.bademux.feedly.api.model.FeedsResponse;
import org.github.bademux.feedly.api.model.MarkReadsResponse;
import org.github.bademux.feedly.api.model.MarkTagsResponse;
import org.github.bademux.feedly.api.model.Stream;
import org.github.bademux.feedly.api.model.StreamsResponse;
import org.github.bademux.feedly.api.model.Subscription;
import org.github.bademux.feedly.api.model.Tag;
import org.github.bademux.feedly.api.model.Topic;
import org.github.bademux.feedly.api.model.UnreadResponse;
import org.github.bademux.feedly.api.oauth2.FeedlyCredential;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

import static com.google.api.client.repackaged.com.google.common.base.Preconditions.checkNotNull;

//TODO: implement /v3/evernote/ Evernote API
public class Feedly extends AbstractClient {

  public static final String SCOPE = "https://cloud.feedly.com/subscriptions";

  public static final String DEFAULT_ROOT_URL = "http://cloud.feedly.com/";

  public static final String DEFAULT_SERVICE_PATH = "v3/";

  /** Feedly UserId */
  private final String userId;

  public Feedly(HttpTransport transport, JsonFactory jsonFactory,
                FeedlyCredential httpRequestInitializer) {
    this(new Builder(transport, jsonFactory, httpRequestInitializer));
  }

  protected Feedly(Builder builder) {
    super(builder);
    userId = ((FeedlyCredential) builder.getHttpRequestInitializer()).getUserId();
  }


  /** /v3/profile/ Profile */
  public Profile profile() { return new Profile(); }

  public class Profile {

    public Get get() throws IOException { return new Get(); }

    public class Get extends Request<org.github.bademux.feedly.api.model.Profile> {

      private static final String REST_PATH = "profile";

      public Get() {
        super(Feedly.this, "GET", REST_PATH, null,
              org.github.bademux.feedly.api.model.Profile.class);
      }
    }

    public Update update(org.github.bademux.feedly.api.model.Profile profile) throws IOException {
      //email givenName familyName picture gender locale
      return new Update(profile);
    }

    public class Update extends Request<org.github.bademux.feedly.api.model.Profile> {

      private static final String REST_PATH = "profile";

      public Update(org.github.bademux.feedly.api.model.Profile profile) {
        super(Feedly.this, "POST", REST_PATH, profile,
              org.github.bademux.feedly.api.model.Profile.class);
        setDisableGZipContent(true);
      }

      @Override
      public Update setDisableGZipContent(boolean disableGZipContent) {
        return (Update) super.setDisableGZipContent(disableGZipContent);
      }

      @Override
      public Update setRequestHeaders(HttpHeaders headers) {
        return (Update) super.setRequestHeaders(headers);
      }

      @Override
      public Update set(String parameterName, Object value) {
        return (Update) super.set(parameterName, value);
      }
    }
  }

  /** /v3/preferences/ Preferences */
  public Preferences preferences() { return new Preferences(); }

  public class Preferences {

    public Get get() throws IOException { return new Get(); }

    public class Get extends Request<org.github.bademux.feedly.api.model.Preferences> {

      private static final String REST_PATH = "preferences";

      public Get() {
        super(Feedly.this, "GET", REST_PATH, null,
              org.github.bademux.feedly.api.model.Preferences.class);
      }
    }

    public Update update(org.github.bademux.feedly.api.model.Preferences preferences)
        throws IOException {
      return new Update(preferences);
    }

    public class Update extends Request<Void> {

      private static final String REST_PATH = "preferences";

      public Update(org.github.bademux.feedly.api.model.Preferences preferences) {
        super(Feedly.this, "POST", REST_PATH, preferences, Void.class);
        setDisableGZipContent(true);
      }

      @Override
      public Update setDisableGZipContent(boolean disableGZipContent) {
        return (Update) super.setDisableGZipContent(disableGZipContent);
      }

      @Override
      public Update setRequestHeaders(HttpHeaders headers) {
        return (Update) super.setRequestHeaders(headers);
      }

      @Override
      public Update set(String parameterName, Object value) {
        return (Update) super.set(parameterName, value);
      }
    }
  }

  /** /v3/categories/ Categories */
  public Categories categories() { return new Categories(); }

  public class Categories {

    public List list() throws IOException { return new List(); }

    public class List extends Request<Category.Categories> {

      private static final String REST_PATH = "categories";

      public List() {
        super(Feedly.this, "GET", REST_PATH, null, Category.Categories.class);
      }

      @Override
      public List setDisableGZipContent(boolean disableGZipContent) {
        return (List) super.setDisableGZipContent(disableGZipContent);
      }

      @Override
      public List setRequestHeaders(HttpHeaders headers) {
        return (List) super.setRequestHeaders(headers);
      }

      @Override
      public List set(String parameterName, Object value) {
        return (List) super.set(parameterName, value);
      }
    }

    public Update update(Category category) { return new Update(category); }

    public class Update extends Request<Void> {

      private static final
      String REST_PATH = "categories/user%2F{userId}%2Fcategory%2F{categoryId}";

      @Key
      private final String categoryId;
      @Key
      private final String userId;

      public Update(Category category) {
        super(Feedly.this, "POST", REST_PATH, new GenericJson().set("label", category.getLabel()),
              Void.class);
        this.userId = checkNotNull(Feedly.this.userId);
        this.categoryId = category.getName();
        setDisableGZipContent(true);
      }

      public String getUserId() { return userId; }

      public String getCategoryId() { return categoryId; }

      @Override
      public Update setDisableGZipContent(boolean disableGZipContent) {
        return (Update) super.setDisableGZipContent(disableGZipContent);
      }

      @Override
      public Update setRequestHeaders(HttpHeaders headers) {
        return (Update) super.setRequestHeaders(headers);
      }

      @Override
      public Update set(String parameterName, Object value) {
        return (Update) super.set(parameterName, value);
      }
    }

    public Delete detete(String categoryId) { return new Delete(categoryId); }

    public Delete detete(Category category) { return detete(category.getId()); }

    public class Delete extends Request<Void> {

      private static final
      String REST_PATH = "categories/{categoryId}";

      @Key
      private final String categoryId;

      public Delete(String categoryId) {
        super(Feedly.this, "DELETE", REST_PATH, null, Void.class);
        this.categoryId = categoryId;
      }

      public String getCategoryId() {
        return categoryId;
      }

      @Override
      public Delete setDisableGZipContent(boolean disableGZipContent) {
        return (Delete) super.setDisableGZipContent(disableGZipContent);
      }

      @Override
      public Delete setRequestHeaders(HttpHeaders headers) {
        return (Delete) super.setRequestHeaders(headers);
      }

      @Override
      public Delete set(String parameterName, Object value) {
        return (Delete) super.set(parameterName, value);
      }
    }
  }

  /** /v3/subscriptions/ Subscriptions */
  public Subscriptions subscriptions() {
    return new Subscriptions();
  }

  public class Subscriptions {

    public List list() throws IOException { return new List(); }

    public class List extends Request<Subscription.Subscriptions> {

      private static final String REST_PATH = "subscriptions";

      public List() {
        super(Feedly.this, "GET", REST_PATH, null, Subscription.Subscriptions.class);
      }

      @Override
      public List setDisableGZipContent(boolean disableGZipContent) {
        return (List) super.setDisableGZipContent(disableGZipContent);
      }

      @Override
      public List setRequestHeaders(HttpHeaders headers) {
        return (List) super.setRequestHeaders(headers);
      }

      @Override
      public List set(String parameterName, Object value) {
        return (List) super.set(parameterName, value);
      }
    }

    public Update update(Subscription subscription) {
      return new Update(subscription);
    }

    public class Update extends Request<Void> {

      private static final String REST_PATH = "subscriptions";

      public Update(Subscription subscription) {
        super(Feedly.this, "POST", REST_PATH, subscription, Void.class);
        setDisableGZipContent(true);
      }

      @Override
      public Update setDisableGZipContent(boolean disableGZipContent) {
        return (Update) super.setDisableGZipContent(disableGZipContent);
      }

      @Override
      public Update setRequestHeaders(HttpHeaders headers) {
        return (Update) super.setRequestHeaders(headers);
      }

      @Override
      public Update set(String parameterName, Object value) {
        return (Update) super.set(parameterName, value);
      }
    }

    public Delete detete(Subscription subscription) { return new Delete(subscription); }

    public class Delete extends Request<Void> {

      private static final String REST_PATH = "subscriptions/{subscriptionId}";

      @Key
      private final String subscriptionId;

      public Delete(Subscription subscription) {
        super(Feedly.this, "DELETE", REST_PATH, null, Void.class);
        this.subscriptionId = subscription.getId();
      }

      public String getSubscription() {
        return subscriptionId;
      }

      @Override
      public Delete setDisableGZipContent(boolean disableGZipContent) {
        return (Delete) super.setDisableGZipContent(disableGZipContent);
      }

      @Override
      public Delete setRequestHeaders(HttpHeaders headers) {
        return (Delete) super.setRequestHeaders(headers);
      }

      @Override
      public Delete set(String parameterName, Object value) {
        return (Delete) super.set(parameterName, value);
      }
    }
  }

  /** /v3/topics/ Topics */
  public Topics topics() { return new Topics(); }

  public class Topics {

    public List list() throws IOException { return new List(); }

    public class List extends Request<org.github.bademux.feedly.api.model.Topic.Topics> {

      private static final String REST_PATH = "topics";

      public List() {
        super(Feedly.this, "GET", REST_PATH, null,
              org.github.bademux.feedly.api.model.Topic.Topics.class);
      }

      @Override
      public List setDisableGZipContent(boolean disableGZipContent) {
        return (List) super.setDisableGZipContent(disableGZipContent);
      }

      @Override
      public List setRequestHeaders(HttpHeaders headers) {
        return (List) super.setRequestHeaders(headers);
      }

      @Override
      public List set(String parameterName, Object value) {
        return (List) super.set(parameterName, value);
      }
    }

    public Update update(Topic topic) {
      return new Update(topic);
    }

    public class Update extends Request<Void> {

      private static final String REST_PATH = "topics";

      public Update(Topic topic) {
        super(Feedly.this, "POST", REST_PATH, topic, Void.class);
        setDisableGZipContent(true);
      }

      @Override
      public Update setDisableGZipContent(boolean disableGZipContent) {
        return (Update) super.setDisableGZipContent(disableGZipContent);
      }

      @Override
      public Update setRequestHeaders(HttpHeaders headers) {
        return (Update) super.setRequestHeaders(headers);
      }

      @Override
      public Update set(String parameterName, Object value) {
        return (Update) super.set(parameterName, value);
      }
    }

    public Delete detete(Topic topic) { return new Delete(topic); }

    public class Delete extends Request<Void> {

      private static final String REST_PATH = "topics/user%2F{userId}%2Ftopic%2F{topicId}";

      @Key
      private final String topicId;
      @Key
      private final String userId;

      public Delete(Topic topic) {
        super(Feedly.this, "DELETE", REST_PATH, null, Void.class);
        this.userId = checkNotNull(Feedly.this.userId);
        this.topicId = topic.getName();
      }

      public String getUserId() { return userId; }

      public String getTopicId() { return topicId; }

      @Override
      public Delete setDisableGZipContent(boolean disableGZipContent) {
        return (Delete) super.setDisableGZipContent(disableGZipContent);
      }

      @Override
      public Delete setRequestHeaders(HttpHeaders headers) {
        return (Delete) super.setRequestHeaders(headers);
      }

      @Override
      public Delete set(String parameterName, Object value) {
        return (Delete) super.set(parameterName, value);
      }
    }
  }

  /** /v3/tags/ Tags */
  public Tags tags() { return new Tags(); }

  public class Tags {

    public List list() throws IOException { return new List(); }

    public class List extends Request<org.github.bademux.feedly.api.model.Tag.Tags> {

      private static final String REST_PATH = "tags";

      public List() {
        super(Feedly.this, "GET", REST_PATH, null,
              org.github.bademux.feedly.api.model.Tag.Tags.class);
      }

      @Override
      public List setDisableGZipContent(boolean disableGZipContent) {
        return (List) super.setDisableGZipContent(disableGZipContent);
      }

      @Override
      public List setRequestHeaders(HttpHeaders headers) {
        return (List) super.setRequestHeaders(headers);
      }

      @Override
      public List set(String parameterName, Object value) {
        return (List) super.set(parameterName, value);
      }
    }

    public Update update(org.github.bademux.feedly.api.model.Tag tag) { return new Update(tag); }

    public class Update extends Request<Void> {

      private static final String REST_PATH = "tags/{tagId}";

      @Key
      private final String tagId;

      public Update(org.github.bademux.feedly.api.model.Tag tag) {
        super(Feedly.this, "POST", REST_PATH, new GenericJson().set("label", tag.getLabel()),
              Void.class);
        tagId = tag.getName();
        setDisableGZipContent(true);
      }


      @Override
      public Update setDisableGZipContent(boolean disableGZipContent) {
        return (Update) super.setDisableGZipContent(disableGZipContent);
      }

      @Override
      public Update setRequestHeaders(HttpHeaders headers) {
        return (Update) super.setRequestHeaders(headers);
      }

      @Override
      public Update set(String parameterName, Object value) {
        return (Update) super.set(parameterName, value);
      }
    }

    public Delete detete(Collection<String> tagIds) {
      return new Delete(tagIds);
    }

    public Delete deteteByTag(Collection<org.github.bademux.feedly.api.model.Tag> tags) {
      Collection<String> tagIds = new ArrayList<String>(tags.size());
      for (org.github.bademux.feedly.api.model.Tag tag : tags) { tagIds.add(tag.getId()); }
      return detete(tagIds);
    }

    public class Delete extends Request<Void> {

      private static final String REST_PATH = "tags/{tagIds}";

      @Key
      private final Collection<String> tagIds;

      public Delete(Collection<String> tagIds) {
        super(Feedly.this, "DELETE", REST_PATH, null, Void.class);
        this.tagIds = tagIds;
      }

      @Override
      public Delete setDisableGZipContent(boolean disableGZipContent) {
        return (Delete) super.setDisableGZipContent(disableGZipContent);
      }

      @Override
      public Delete setRequestHeaders(HttpHeaders headers) {
        return (Delete) super.setRequestHeaders(headers);
      }

      @Override
      public Delete set(String parameterName, Object value) {
        return (Delete) super.set(parameterName, value);
      }
    }

    public Tag tagByEntity(Collection<org.github.bademux.feedly.api.model.Entry> entries,
                           Collection<org.github.bademux.feedly.api.model.Tag> tags) {
      Collection<String> entryIds = new ArrayList<String>(entries.size());
      for (org.github.bademux.feedly.api.model.Entry entry : entries) {entryIds.add(entry.getId());}

      Collection<String> tagIds = new ArrayList<String>(tags.size());
      for (org.github.bademux.feedly.api.model.Tag tag : tags) { tagIds.add(tag.getId()); }
      return tag(entryIds, tagIds);
    }


    public Tag tag(Collection<String> entryIds, Collection<String> tagIds) {
      return new Tag(entryIds, tagIds);
    }


    public class Tag extends Request<Void> {

      private static final String REST_PATH = "tags/{tagIds}";

      @Key
      private final Collection<String> tagIds;

      public Tag(final Collection<String> entryIds, final Collection<String> tagIds) {
        super(Feedly.this, "PUT", REST_PATH,
              new GenericData().set("entryIds", checkNotNull(entryIds)), Void.class);
        this.tagIds = tagIds;
        setDisableGZipContent(true);
      }

      @Override
      public Tag setDisableGZipContent(boolean disableGZipContent) {
        return (Tag) super.setDisableGZipContent(disableGZipContent);
      }

      @Override
      public Tag setRequestHeaders(HttpHeaders headers) {
        return (Tag) super.setRequestHeaders(headers);
      }

      @Override
      public Tag set(String parameterName, Object value) {
        return (Tag) super.set(parameterName, value);
      }
    }

    public Untag untagByEntry(Collection<org.github.bademux.feedly.api.model.Entry> entries,
                              Collection<org.github.bademux.feedly.api.model.Tag> tags) {
      Collection<String> entryIds = new ArrayList<String>(entries.size());
      for (org.github.bademux.feedly.api.model.Entry entry : entries) {entryIds.add(entry.getId());}

      Collection<String> tagIds = new ArrayList<String>(tags.size());
      for (org.github.bademux.feedly.api.model.Tag tag : tags) { tagIds.add(tag.getId()); }
      return untag(entryIds, tagIds);
    }


    public Untag untag(Collection<String> entryIds, Collection<String> tagIds) {
      return new Untag(entryIds, tagIds);
    }

    public class Untag extends Request<Void> {

      private static final String REST_PATH = "tags/{tagIds}/{entryIds}";

      @Key
      private final Collection<String> entryIds;

      @Key
      private final Collection<String> tagIds;

      public Untag(Collection<String> entryIds, Collection<String> tagIds) {
        super(Feedly.this, "DELETE", REST_PATH, null, Void.class);
        this.entryIds = entryIds;
        this.tagIds = tagIds;
      }

      @Override
      public Untag setDisableGZipContent(boolean disableGZipContent) {
        return (Untag) super.setDisableGZipContent(disableGZipContent);
      }

      @Override
      public Untag setRequestHeaders(HttpHeaders headers) {
        return (Untag) super.setRequestHeaders(headers);
      }

      @Override
      public Untag set(String parameterName, Object value) {
        return (Untag) super.set(parameterName, value);
      }
    }
  }

  /** /v3/entries/ Entries */
  public Entry entries() { return new Entry(); }

  public class Entry {

    public List listEntries(Collection<org.github.bademux.feedly.api.model.Entry> entries)
        throws IOException {
      Collection<String> entryIds = new ArrayList<String>();
      for (org.github.bademux.feedly.api.model.Entry entry : checkNotNull(entries)) {
        entryIds.add(entry.getId());
      }
      return list(entryIds);
    }

    /** The number of entry ids you can pass as an input is limited to 1,000. */
    public List list(Collection<String> entryIds) throws IOException {
      return new List(entryIds);
    }

    public class List extends Request<org.github.bademux.feedly.api.model.Entry.Entries> {

      private static final String REST_PATH = "entries/.mget";

      public List(Collection<String> entryIds) {
        super(Feedly.this, "POST", REST_PATH, new GenericData().set("ids", entryIds),
              org.github.bademux.feedly.api.model.Entry.Entries.class);
        setDisableGZipContent(true);
      }

      @Override
      public List setDisableGZipContent(boolean disableGZipContent) {
        return (List) super.setDisableGZipContent(disableGZipContent);
      }

      @Override
      public List setRequestHeaders(HttpHeaders headers) {
        return (List) super.setRequestHeaders(headers);
      }

      @Override
      public List set(String parameterName, Object value) {
        return (List) super.set(parameterName, value);
      }
    }

    /**
     * BUG: https://groups.google.com/forum/?fromgroups=#!topic/feedly-cloud/vpWT17a_Sec
     *
     * @return Array with single item if entry exists
     */
    public Get get(String entryId) throws IOException {
      return new Get(entryId);
    }

    public class Get extends Request<org.github.bademux.feedly.api.model.Entry.Entries> {

      private static final String REST_PATH = "entries/{entryId}";

      @Key
      private final String entryId;

      public Get(String entryId) {
        super(Feedly.this, "GET", REST_PATH, null,
              org.github.bademux.feedly.api.model.Entry.Entries.class);
        this.entryId = entryId;
      }

      @Override
      public Get setDisableGZipContent(boolean disableGZipContent) {
        return (Get) super.setDisableGZipContent(disableGZipContent);
      }

      @Override
      public Get setRequestHeaders(HttpHeaders headers) {
        return (Get) super.setRequestHeaders(headers);
      }

      @Override
      public Get set(String parameterName, Object value) {
        return (Get) super.set(parameterName, value);
      }
    }

    public Update update(org.github.bademux.feedly.api.model.Entry entry) {
      return new Update(entry);
    }

    public class Update extends Request<Void> {

      private static final String REST_PATH = "entries";

      public Update(org.github.bademux.feedly.api.model.Entry entry) {
        super(Feedly.this, "POST", REST_PATH, entry, Void.class);
        setDisableGZipContent(true);
      }

      @Override
      public Update setDisableGZipContent(boolean disableGZipContent) {
        return (Update) super.setDisableGZipContent(disableGZipContent);
      }

      @Override
      public Update setRequestHeaders(HttpHeaders headers) {
        return (Update) super.setRequestHeaders(headers);
      }

      @Override
      public Update set(String parameterName, Object value) {
        return (Update) super.set(parameterName, value);
      }
    }
  }

  /** /v3/search/ Search */
  public Search search() { return new Search(); }

  public class Search {

    public Feeds feeds(String q) throws IOException { return new Feeds(q); }

    public class Feeds extends Request<FeedsResponse> {

      private static final String REST_PATH = "search/feeds";

      @Key
      private final String q;
      @Key
      private Integer n;

      public Feeds(String q) {
        super(Feedly.this, "GET", REST_PATH, null, FeedsResponse.class);
        this.q = checkNotNull(q);
        setDisableGZipContent(true);
      }

      public String getQ() { return q; }

      public Integer getCount() { return n; }

      public Feeds setCount(final Integer count) { n = count; return this; }

      @Override
      public Feeds setDisableGZipContent(boolean disableGZipContent) {
        return (Feeds) super.setDisableGZipContent(disableGZipContent);
      }

      @Override
      public Feeds setRequestHeaders(HttpHeaders headers) {
        return (Feeds) super.setRequestHeaders(headers);
      }

      @Override
      public Feeds set(String parameterName, Object value) {
        return (Feeds) super.set(parameterName, value);
      }
    }

    public Entries entries(Stream stream, String q) throws IOException {
      return new Entries(stream, q);
    }

    public class Entries extends Request<EntriesResponse> {

      private static final String REST_PATH = "streams/{streamId}/contents";

      @Key
      private String streamId;
      @Key
      private final String q;
      @Key
      private Integer count;
      @Key
      private Long newerThan;
      @Key
      private String continuation;
      @Key
      private Boolean unreadOnly;
      @Key
      private Collection<String> fields;
      @Key
      private Integer minMatches;

      public Entries(Stream stream, String q) {
        super(Feedly.this, "GET", REST_PATH, null, EntriesResponse.class);
        streamId = stream.getId();
        this.q = checkNotNull(q);
        setDisableGZipContent(true);
      }

      public String getStreamId() { return streamId; }

      public String getQ() { return q; }

      public Integer getCount() { return count; }

      public Entries setCount(final Integer count) { this.count = count; return this; }

      public Long getNewerThan() { return newerThan; }

      public Entries setNewerThan(final Long newerThan) {
        this.newerThan = newerThan;
        return this;
      }

      public String getContinuation() { return continuation; }

      public Entries setContinuation(final String continuation) {
        this.continuation = continuation;
        return this;
      }

      public Boolean getUnreadOnly() { return unreadOnly; }

      public Entries setUnreadOnly(final Boolean unreadOnly) {
        this.unreadOnly = unreadOnly;
        return this;
      }

      public Collection<String> getFields() { return fields; }

      public Entries setFields(final Collection<String> fields) {this.fields = fields; return this;}

      public Entries addFields(final String field) {
        if (fields == null) {
          fields = new ArrayList<>();
        }
        fields.add(field);
        return this;
      }

      public Integer getMinMatches() { return minMatches; }

      public Entries setMinMatches(final Integer minMatches) {
        this.minMatches = minMatches;
        return this;
      }

      @Override
      public Entries setDisableGZipContent(boolean disableGZipContent) {
        return (Entries) super.setDisableGZipContent(disableGZipContent);
      }

      @Override
      public Entries setRequestHeaders(HttpHeaders headers) {
        return (Entries) super.setRequestHeaders(headers);
      }

      @Override
      public Entries set(String parameterName, Object value) {
        return (Entries) super.set(parameterName, value);
      }
    }
  }

  /** /v3/markers/ Markers */
  public Markers markers() {
    return new Markers();
  }

  public class Markers {

    public Counts counts() throws IOException { return new Counts(); }

    public class Counts extends Request<UnreadResponse> {

      private static final String REST_PATH = "markers/counts";

      @Key
      private String streamId;
      @Key
      private Long newerThan;
      @Key
      private Boolean autorefresh = Boolean.TRUE;

      public Counts() {
        super(Feedly.this, "GET", REST_PATH, null, UnreadResponse.class);
        setDisableGZipContent(true);
      }

      public Long getNewerThan() { return newerThan; }

      public Counts setNewerThan(final Long newerThan) { this.newerThan = newerThan; return this; }

      public Boolean getAutorefresh() { return autorefresh; }

      public Counts setAutorefresh(final Boolean autorefresh) {
        this.autorefresh = autorefresh;
        return this;
      }

      public String getStreamId() { return streamId; }

      public Counts setStreamId(final String streamId) { this.streamId = streamId; return this; }

      @Override
      public Counts setDisableGZipContent(boolean disableGZipContent) {
        return (Counts) super.setDisableGZipContent(disableGZipContent);
      }

      @Override
      public Counts setRequestHeaders(HttpHeaders headers) {
        return (Counts) super.setRequestHeaders(headers);
      }

      @Override
      public Counts set(String parameterName, Object value) {
        return (Counts) super.set(parameterName, value);
      }
    }

    public MarkEntryAs entriesAsRead(Collection<String> entryIds)
        throws IOException {
      return new MarkEntryAs(entryIds, "markAsRead");
    }

    public MarkEntryAs entriesUnread(Collection<String> entryIds)
        throws IOException {
      return new MarkEntryAs(entryIds, "keepUnread");
    }

    public class MarkEntryAs extends Request<Void> {

      private static final String REST_PATH = "markers";

      public MarkEntryAs(final Collection<String> entryIds, final String actionType) {
        super(Feedly.this, "POST", REST_PATH,
              new GenericData().set("type", "entries")
                  .set("action", actionType)
                  .set("entryIds", entryIds),
              Void.class);
        setDisableGZipContent(true);
      }

      @Override
      public MarkEntryAs setDisableGZipContent(boolean disableGZipContent) {
        return (MarkEntryAs) super.setDisableGZipContent(disableGZipContent);
      }

      @Override
      public MarkEntryAs setRequestHeaders(HttpHeaders headers) {
        return (MarkEntryAs) super.setRequestHeaders(headers);
      }

      @Override
      public MarkEntryAs set(String parameterName, Object value) {
        return (MarkEntryAs) super.set(parameterName, value);
      }
    }

    public MarkCategoryAsRead categoriesAsRead(Collection<String> categoryIds,
                                               String lastReadEntryId)
        throws IOException {
      return new MarkCategoryAsRead(categoryIds, lastReadEntryId);
    }

    public MarkCategoryAsRead categoriesAsRead(Collection<String> categoryIds, Long asOf)
        throws IOException {
      return new MarkCategoryAsRead(categoryIds, asOf);
    }

    public class MarkCategoryAsRead extends Request<Void> {

      private static final String REST_PATH = "markers";

      public MarkCategoryAsRead(final Collection<String> categoryIds,
                                final String lastReadEntryId) {
        this(categoryIds, new GenericData().set("lastReadEntryId", lastReadEntryId));
      }


      public MarkCategoryAsRead(final Collection<String> categoryIds, final Long asOf) {
        this(categoryIds, new GenericData().set("asOf", asOf.toString()));
      }

      protected MarkCategoryAsRead(final Collection<String> categoryIds, GenericData entity) {
        super(Feedly.this, "POST", REST_PATH, entity.set("categoryIds", categoryIds)
            .set("type", "category").set("action", "markAsRead"),
              Void.class);
        setDisableGZipContent(true);
      }

      @Override
      public MarkCategoryAsRead setDisableGZipContent(boolean disableGZipContent) {
        return (MarkCategoryAsRead) super.setDisableGZipContent(disableGZipContent);
      }

      @Override
      public MarkCategoryAsRead setRequestHeaders(HttpHeaders headers) {
        return (MarkCategoryAsRead) super.setRequestHeaders(headers);
      }

      @Override
      public MarkCategoryAsRead set(String parameterName, Object value) {
        return (MarkCategoryAsRead) super.set(parameterName, value);
      }
    }

    public MarkFeedAsRead feedsAsRead(Collection<String> feedIds, String lastReadEntryId)
        throws IOException {
      return new MarkFeedAsRead(feedIds, lastReadEntryId);
    }

    public MarkFeedAsRead feedsAsRead(Collection<String> feedIds, Long asOf) throws IOException {
      return new MarkFeedAsRead(feedIds, asOf);
    }

    public class MarkFeedAsRead extends Request<Void> {

      private static final String REST_PATH = "markers";

      public MarkFeedAsRead(final Collection<String> feedIds, final String lastReadEntryId) {
        this(feedIds, new GenericData().set("lastReadEntryId", lastReadEntryId));
      }


      public MarkFeedAsRead(final Collection<String> feedIds, final Long asOf) {
        this(feedIds, new GenericData().set("asOf", asOf.toString()));
      }

      protected MarkFeedAsRead(final Collection<String> feedIds, GenericData entity) {
        super(Feedly.this, "POST", REST_PATH,
              entity.set("type", "feeds").set("action", "markAsRead").set("feedIds", feedIds),
              Void.class);
        setDisableGZipContent(true);
      }

      @Override
      public MarkFeedAsRead setDisableGZipContent(boolean disableGZipContent) {
        return (MarkFeedAsRead) super.setDisableGZipContent(disableGZipContent);
      }

      @Override
      public MarkFeedAsRead setRequestHeaders(HttpHeaders headers) {
        return (MarkFeedAsRead) super.setRequestHeaders(headers);
      }

      @Override
      public MarkFeedAsRead set(String parameterName, Object value) {
        return (MarkFeedAsRead) super.set(parameterName, value);
      }
    }

    public Reads reads() throws IOException { return new Reads(); }

    public class Reads extends Request<MarkReadsResponse> {

      private static final String REST_PATH = "markers/reads";

      @Key
      private Long newerThan;

      public Reads() {
        super(Feedly.this, "GET", REST_PATH, null, MarkReadsResponse.class);
        setDisableGZipContent(true);
      }

      public Long getNewerThan() { return newerThan; }

      public Reads setNewerThan(final Long newerThan) { this.newerThan = newerThan; return this; }

      @Override
      public Reads setDisableGZipContent(boolean disableGZipContent) {
        return (Reads) super.setDisableGZipContent(disableGZipContent);
      }

      @Override
      public Reads setRequestHeaders(HttpHeaders headers) {
        return (Reads) super.setRequestHeaders(headers);
      }

      @Override
      public Reads set(String parameterName, Object value) {
        return (Reads) super.set(parameterName, value);
      }
    }

    public Tags tags() throws IOException { return new Tags(); }

    public class Tags extends Request<MarkTagsResponse> {

      private static final String REST_PATH = "markers/tags";

      @Key
      private Long newerThan;

      public Tags() {
        super(Feedly.this, "GET", REST_PATH, null, MarkTagsResponse.class);
        setDisableGZipContent(true);
      }

      public Long getNewerThan() { return newerThan; }

      public Tags setNewerThan(final Long newerThan) { this.newerThan = newerThan; return this; }

      @Override
      public Tags setDisableGZipContent(boolean disableGZipContent) {
        return (Tags) super.setDisableGZipContent(disableGZipContent);
      }

      @Override
      public Tags setRequestHeaders(HttpHeaders headers) {
        return (Tags) super.setRequestHeaders(headers);
      }

      @Override
      public Tags set(String parameterName, Object value) {
        return (Tags) super.set(parameterName, value);
      }
    }
  }

  /** /v3/feeds/ Feeds */
  public Feeds feeds() { return new Feeds(); }

  public class Feeds {

    public List list(Collection<String> feedIds) throws IOException {
      return new List(feedIds);
    }

    public class List extends Request<FeedInfo.FeedsInfo> {

      private static final String REST_PATH = "feeds/.mget";

      public List(Collection<String> feedIds) {
        super(Feedly.this, "POST", REST_PATH, feedIds,
              FeedInfo.FeedsInfo.class);
        setDisableGZipContent(true);
      }

      @Override
      public List setDisableGZipContent(boolean disableGZipContent) {
        return (List) super.setDisableGZipContent(disableGZipContent);
      }

      @Override
      public List setRequestHeaders(HttpHeaders headers) {
        return (List) super.setRequestHeaders(headers);
      }

      @Override
      public List set(String parameterName, Object value) {
        return (List) super.set(parameterName, value);
      }
    }

    public Get get(String feedId) throws IOException { return new Get(feedId); }

    public class Get extends Request<FeedInfo> {

      private static final String REST_PATH = "feeds/{feedId}";

      @Key
      private String feedId;

      public Get(String feedId) {
        super(Feedly.this, "GET", REST_PATH, null, FeedInfo.class);
        this.feedId = checkNotNull(feedId);
        setDisableGZipContent(true);
      }

      @Override
      public Get setDisableGZipContent(boolean disableGZipContent) {
        return (Get) super.setDisableGZipContent(disableGZipContent);
      }

      @Override
      public Get setRequestHeaders(HttpHeaders headers) {
        return (Get) super.setRequestHeaders(headers);
      }

      @Override
      public Get set(String parameterName, Object value) {
        return (Get) super.set(parameterName, value);
      }
    }
  }

  /** /v3/streams/ Streams */
  public Streams streams() { return new Streams(); }

  public class Streams {

    public Ids ids(Stream stream) throws IOException {
      return new Ids(stream);
    }

    public class Ids extends Request<StreamsResponse> {

      private static final String REST_PATH = "streams/{streamId}/ids";

      @Key
      private String streamId;
      @Key
      private Integer count;
      @Key
      private Long newerThan;
      @Key
      private String continuation;
      @Key
      private Boolean unreadOnly;
      @Key
      private Ranked ranked;

      public Ids(Stream stream) {
        super(Feedly.this, "GET", REST_PATH, null, StreamsResponse.class);
        this.streamId = stream.getId();
        setDisableGZipContent(true);
      }

      public String getStreamId() { return streamId; }

      public Integer getCount() { return count; }

      public Ids setCount(final Integer count) {this.count = count; return this; }

      public Long getNewerThan() { return newerThan; }

      public Ids setNewerThan(final Long newerThan) { this.newerThan = newerThan; return this;}

      public String getContinuation() { return continuation; }

      public Ids setContinuation(final String continuation) {
        this.continuation = continuation;
        return this;
      }

      public Boolean getUnreadOnly() { return unreadOnly; }

      public Ids setUnreadOnly(final Boolean unreadOnly) {
        this.unreadOnly = unreadOnly;
        return this;
      }

      public Ranked getRanked() { return ranked; }

      public Ids setRanked(final Ranked ranked) {this.ranked = ranked; return this; }

      @Override
      public Ids setDisableGZipContent(boolean disableGZipContent) {
        return (Ids) super.setDisableGZipContent(disableGZipContent);
      }

      @Override
      public Ids setRequestHeaders(HttpHeaders headers) {
        return (Ids) super.setRequestHeaders(headers);
      }

      @Override
      public Ids set(String parameterName, Object value) {
        return (Ids) super.set(parameterName, value);
      }
    }


    public Contents contents(Stream stream) throws IOException { return new Contents(stream); }

    public class Contents extends Request<EntriesResponse> {

      private static final String REST_PATH = "streams/{streamId}/contents";

      @Key
      private String streamId;
      @Key
      private Integer count;
      @Key
      private Long newerThan;
      @Key
      private String continuation;
      @Key
      private Boolean unreadOnly;
      @Key
      private String ranked;

      public Contents(Stream stream) {
        super(Feedly.this, "GET", REST_PATH, null, EntriesResponse.class);
        streamId = stream.getId();
        setDisableGZipContent(true);
      }

      public String getStreamId() { return streamId; }

      public Integer getCount() { return count; }

      public Contents setCount(final Integer count) {this.count = count; return this; }

      public Long getNewerThan() { return newerThan; }

      public Contents setNewerThan(final Long newerThan) { this.newerThan = newerThan; return this;}

      public String getContinuation() { return continuation; }

      public Contents setContinuation(final String continuation) {
        this.continuation = continuation;
        return this;
      }

      public Boolean getUnreadOnly() { return unreadOnly; }

      public Contents setUnreadOnly(final Boolean unreadOnly) {
        this.unreadOnly = unreadOnly;
        return this;
      }

      public String getRanked() { return ranked; }

      public Contents setRanked(final String ranked) {this.ranked = ranked; return this; }

      @Override
      public Contents setDisableGZipContent(boolean disableGZipContent) {
        return (Contents) super.setDisableGZipContent(disableGZipContent);
      }

      @Override
      public Contents setRequestHeaders(HttpHeaders headers) {
        return (Contents) super.setRequestHeaders(headers);
      }

      @Override
      public Contents set(String parameterName, Object value) {
        return (Contents) super.set(parameterName, value);
      }
    }
  }

  /** /v3/mixes/ Mixes */
  public Mixes mixes() { return new Mixes(); }

  public class Mixes {

    public Get contents(Stream stream) throws IOException { return new Get(stream); }

    public class Get extends Request<EntriesResponse> {

      private static final String REST_PATH = "mixes/{streamId}/contents";

      @Key
      private String streamId;
      @Key
      private Integer count;
      @Key
      private Long newerThan;
      @Key
      private Boolean unreadOnly;
      @Key
      private String hours;

      public Get(Stream stream) {
        super(Feedly.this, "GET", REST_PATH, null, EntriesResponse.class);
        this.streamId = stream.getId();
        setDisableGZipContent(true);
      }

      public String getStreamId() { return streamId; }

      public Integer getCount() { return count; }

      public Get setCount(final Integer count) {this.count = count; return this; }

      public Long getNewerThan() { return newerThan; }

      public Get setNewerThan(final Long newerThan) { this.newerThan = newerThan; return this;}

      public Boolean getUnreadOnly() { return unreadOnly; }

      public Get setUnreadOnly(final Boolean unreadOnly) {
        this.unreadOnly = unreadOnly;
        return this;
      }

      public String getHours() { return hours; }

      public Get setHours(final String hours) { this.hours = hours; return this; }

      @Override
      public Get setDisableGZipContent(boolean disableGZipContent) {
        return (Get) super.setDisableGZipContent(disableGZipContent);
      }

      @Override
      public Get setRequestHeaders(HttpHeaders headers) {
        return (Get) super.setRequestHeaders(headers);
      }

      @Override
      public Get set(String parameterName, Object value) {
        return (Get) super.set(parameterName, value);
      }
    }
  }

  /** /v3/opml/ OPML */
  public Opml opml() { return new Opml(); }

  public class Opml {

    public Export exportSubscription() throws IOException { return new Export(); }

    public class Export extends Request<Void> {

      private static final String REST_PATH = "opml";

      public Export() {
        super(Feedly.this, "GET", REST_PATH, null, Void.class);
        setDisableGZipContent(true);
      }

      public String executeAndDownloadAsString() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        executeAndDownloadTo(baos);
        return new String(baos.toByteArray(), "UTF8");
      }

      @Override
      public Export setDisableGZipContent(boolean disableGZipContent) {
        return (Export) super.setDisableGZipContent(disableGZipContent);
      }

      @Override
      public Export setRequestHeaders(HttpHeaders headers) {
        return (Export) super.setRequestHeaders(headers);
      }

      @Override
      public Export set(String parameterName, Object value) {
        return (Export) super.set(parameterName, value);
      }
    }

    /** @param content InputStream - will be auto closed */
    public Import importSubscription(InputStream content) throws IOException {
      return new Import(content);
    }

    public Import importSubscription(File file) throws IOException {
      return new Import(new FileInputStream(file));
    }

    public class Import extends Request<Void> {

      private static final String REST_PATH = "opml";

      public Import(InputStream content) {
        super(Feedly.this, "POST", REST_PATH, new InputStreamContent("text/xml", content),
              Void.class);
        setDisableGZipContent(true);
      }

      @Override
      public Import setDisableGZipContent(boolean disableGZipContent) {
        return (Import) super.setDisableGZipContent(disableGZipContent);
      }

      @Override
      public Import setRequestHeaders(HttpHeaders headers) {
        return (Import) super.setRequestHeaders(headers);
      }

      @Override
      public Import set(String parameterName, Object value) {
        return (Import) super.set(parameterName, value);
      }
    }
  }

  public Category newCategory(String name) {
    return new Category(name, userId);
  }

  public Tag newTag(String name) {
    return new Tag(name, userId);
  }

  public Topic newTopic(String name, Topic.Interest interest) {
    return new Topic(name, interest, userId);
  }

  public String getUserId() {
    return userId;
  }

  public enum Ranked {NEWEST, OLDEST}

  /**
   * Builder for {@link Feedly}.
   *
   * <p> Implementation is not thread-safe. </p>
   */
  public static class Builder extends AbstractClient.Builder {

    public Builder(HttpTransport transport, JsonFactory jsonFactory,
                   FeedlyCredential httpRequestInitializer) {
      this(transport, jsonFactory, DEFAULT_ROOT_URL, DEFAULT_SERVICE_PATH, httpRequestInitializer);
    }

    /** Extend point for org.github.bademux.feedly.api.service.SandboxFeedly.Builder */
    protected Builder(HttpTransport transport, JsonFactory jsonFactory, String rootUrl,
                      String servicePath, FeedlyCredential httpRequestInitializer) {
      super(transport, jsonFactory, rootUrl, servicePath, httpRequestInitializer);
    }

    /** Builds a new instance of {@link Feedly}. */
    @Override
    public Feedly build() {
      return new Feedly(this);
    }

    @Override
    public Builder setRootUrl(String rootUrl) {
      return (Builder) super.setRootUrl(rootUrl);
    }

    @Override
    public Builder setServicePath(String servicePath) {
      return (Builder) super.setServicePath(servicePath);
    }

    @Override
    public Builder setHttpRequestInitializer(
        com.google.api.client.http.HttpRequestInitializer httpRequestInitializer) {
      return (Builder) super.setHttpRequestInitializer(httpRequestInitializer);
    }

    @Override
    public Builder setSuppressPatternChecks(boolean suppressPatternChecks) {
      return (Builder) super.setSuppressPatternChecks(suppressPatternChecks);
    }

    @Override
    public Builder setSuppressRequiredParameterChecks(boolean suppressRequiredParameterChecks) {
      return (Builder) super.setSuppressRequiredParameterChecks(suppressRequiredParameterChecks);
    }

    @Override
    public Builder setSuppressAllChecks(boolean suppressAllChecks) {
      return (Builder) super.setSuppressAllChecks(suppressAllChecks);
    }
  }
}
