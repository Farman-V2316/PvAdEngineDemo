/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.news.model.entity.server.navigation;

import java.io.Serializable;
import java.util.List;

/**
 * Represents a topic, source, group, category nodes and a node may have
 * sub-nodes <br/>
 * which will be grouped as one or more of the “featured”, “favourite” and
 * “normal”. <br/>
 * A group of sub-nodes will have a common heading and the nodes of a group will
 * have the same property of a node.
 * <p/>
 * “node” will hold the detail description of the node which could be any of the
 * topic, source, group, category, etc. <br/>
 * The node detail object can have its own class inheritance hierarchy of
 * representing each nodes. <br/>
 * The following JSON will act as a tree template with the ability to hold
 * hierarchical representation of nodes.
 * <p/>
 * <code >
 * {
 * “node”: {
 * // this will be the node detail data type for topic/source defined in sections below
 * },
 * “sections”: [    // represents groupings and having an array allows to dynamically add groups
 * {
 * “type”: “featured|normal|favourite”,
 * "heading": {
 * "<language>": "<name represented in the specific language>"
 * },
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
public class NavigationTree<T> implements Serializable {

  private static final long serialVersionUID = -6857950701844000303L;

  /**
   * This will be the node detail data type for topic/source
   */
  private T node;

  private String version;

  /**
   * Represents groupings and having an array allows to dynamically add groups
   */
  private List<Section<T>> sections;

  public T getNode() {
    return node;
  }

  public void setNode(T node) {
    this.node = node;
  }

  public List<Section<T>> getSections() {
    return sections;
  }

  public void setSections(List<Section<T>> sections) {
    this.sections = sections;
  }

  @Override
  public String toString() {
    return new StringBuilder().append(getClass()).append(" [").append("node=").append(node)
        .append(", sections=").append(sections).append("]").toString();
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }
}
