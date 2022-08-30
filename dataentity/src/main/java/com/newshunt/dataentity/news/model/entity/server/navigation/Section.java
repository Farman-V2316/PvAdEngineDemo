/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.news.model.entity.server.navigation;

import com.newshunt.dataentity.common.model.entity.model.MultiValueResponse;

import java.io.Serializable;
import java.util.Map;

/**
 * Represents groupings and having an array allows to dynamically add groups
 * <p/>
 * These are sub-nodes which will be grouped as one or more of the “featured”,
 * “favourite” and “normal”.
 * <p/>
 * A group of sub-nodes will have a common heading and the nodes of a group will
 * have the same property of a node.
 * <p/>
 * In the below JSON, “node” will hold the detail description of the node which
 * could be any of the topic, source, group, category, etc.<br/>
 * The node detail object can have its own class inheritance hierarchy of
 * representing each nodes.
 * <p/>
 * The following JSON will act as a tree template with the ability to hold
 * hierarchical representation of nodes. <code>
 * {
 * “node”: {
 * // this will be the node detail data type for topic/source defined in sections below
 * },
 * “sections”: [    // represents groupings and having an array allows to dynamically add groups
 * {
 * "heading": {
 * "<language>": "<name represented in the specific language>"
 * },
 * “type”: “featured|normal|favourite”,
 * "kids": [
 * {} // represents a topic/source/newspaper tree node itself
 * ]
 * }
 * ]
 * }
 * </code>
 *
 * @author amarjit
 */
public class Section<T> implements Serializable {

  private static final long serialVersionUID = -2681705428814885300L;

  /**
   * section type such as featured|normal|favourite which is understood by
   * client for different rendering behaviour
   */
  private SectionType type;
  /**
   * Language specific headings: "<language>":
   * "<name represented in the specific language>"
   */
  private Map<String, String> heading;

  private String headingUni;
  /**
   * represents a topic/source/newspaper tree node itself
   */
  private MultiValueResponse<T> kids;

  public String getHeadingUni() {
    return headingUni;
  }

  public SectionType getType() {
    return type;
  }

  public void setType(SectionType type) {
    this.type = type;
  }

  public Map<String, String> getHeading() {
    return heading;
  }

  public void setHeading(Map<String, String> heading) {
    this.heading = heading;
  }

  public MultiValueResponse<T> getKids() {
    return kids;
  }

  public void setKids(MultiValueResponse<T> kids) {
    this.kids = kids;
  }

  @Override
  public String toString() {
    return new StringBuilder().append(getClass()).append(" [").append("type=").append(type)
        .append("heading=").append(heading).append("kids=").append(kids).append("]").toString();
  }
}
