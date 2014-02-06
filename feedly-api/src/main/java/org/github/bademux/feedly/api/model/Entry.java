/*
 * Copyright 2013 Bademus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *    Contributors:
 *                 Bademus
 */

package org.github.bademux.feedly.api.model;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import com.google.api.client.util.NullValue;
import com.google.api.client.util.Value;

import java.util.ArrayList;
import java.util.List;

public final class Entry extends GenericJson implements Markable {

  @Key
  private String id;
  @Key
  private Boolean unread;
  @Key
  private String title;
  @Key
  private List<String> keywords;
  @Key
  private Long published;
  @Key
  private Long updated;
  @Key
  private Long crawled;
  @Key
  private String author;
  @Key
  private Long engagement;
  @Key
  private Double engagementRate;
  @Key
  private List<Category> categories;
  @Key
  private List<Tag> tags;
  @Key
  private Origin origin;
  @Key
  private List<Location> alternate;
  @Key
  private List<Location> canonical;
  @Key
  private Content content;
  @Key
  private String originId;
  @Key
  private String fingerprint;
  @Key
  private String sid;
  @Key
  private Content summary;

  public String getId() { return id; }

  public Boolean getUnread() { return unread; }

  public String getTitle() { return title; }

  public void setTitle(final String title) { this.title = title; }

  public List<String> getKeywords() { return keywords; }

  public void setKeywords(final List<String> keywords) { this.keywords = keywords; }

  public void addKeyword(final String keyword) {
    if (keywords == null) {
      keywords = new ArrayList<String>();
    }
    keywords.add(keyword);
  }

  public Long getPublished() { return published; }

  public Long getUpdated() { return updated; }

  public Long getCrawled() { return crawled; }

  public String getAuthor() { return author; }

  public Long getEngagement() { return engagement; }

  public Double getEngagementRate() { return engagementRate; }

  public List<Category> getCategories() { return categories; }

  public List<Tag> getTags() { return tags; }

  public void setTags(final List<Tag> tags) { this.tags = tags; }

  public void addTag(final Tag tag) {
    if (tags == null) {
      tags = new ArrayList<Tag>();
    }
    tags.add(tag);
  }

  public List<Location> getAlternate() { return alternate; }

  public void setAlternate(List<Location> alternate) {
    this.alternate = alternate;
  }

  public void addAlternate(Location alternate) {
    if (alternate == null) {
      this.alternate = new ArrayList<Location>();
    }
    this.alternate.add(alternate);
  }

  public List<Location> getCanonical() { return canonical; }

  public Origin getOrigin() { return origin; }

  public Content getContent() { return content; }

  public void setContent(final String content, final Content.Direction direction) {
    this.content = new Content(content, direction);
  }

  public void setContent(final String content) { setContent(content, Content.Direction.LTR); }

  public String getOriginId() { return originId; }

  public String getFingerprint() { return fingerprint; }

  public Content getSummary() { return summary; }

  @Override
  public org.github.bademux.feedly.api.model.Entry set(String fieldName, Object value) {
    return (org.github.bademux.feedly.api.model.Entry) super.set(fieldName, value);
  }

  @Override
  public org.github.bademux.feedly.api.model.Entry clone() {
    return (org.github.bademux.feedly.api.model.Entry) super.clone();
  }

  public static class Location {

    @Key
    private String href;
    @Key
    private String type;

    public Location(final String href, final String type) {
      this.href = href;
      this.type = type;
    }

    public Location() {}

    public String getHref() { return href; }

    public String getType() { return type; }
  }

  public static class Content {

    public enum Direction {@Value("ltr")LTR, @Value("rtl")RTL, @NullValue UNKNOWN}

    @Key
    private String content;
    @Key
    private Direction direction;

    public Content(final String content, final Direction direction) {
      this.content = content;
      this.direction = direction;
    }

    public Content() {}

    public String getContent() { return content; }

    public Direction getDirection() { return direction; }
  }

  public static class Origin {

    @Key
    private String streamId;
    @Key
    private String title;
    @Key
    private String htmlUrl;

    public Origin(final String streamId, final String title, final String htmlUrl) {
      this.streamId = streamId;
      this.title = title;
      this.htmlUrl = htmlUrl;
    }

    public Origin() {}

    public String getStreamId() { return streamId; }

    public String getTitle() { return title; }

    public String getHtmlUrl() { return htmlUrl; }

    public Feed toFeed() {
      return new Subscription(streamId.substring(streamId.indexOf('/') + 1), title);
    }
  }

  @SuppressWarnings("serial")
  public static class Entries extends ArrayList<org.github.bademux.feedly.api.model.Entry> {}
}
