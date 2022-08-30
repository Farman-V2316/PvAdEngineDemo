/*
* Copyright (c) 2016 Newshunt. All rights reserved.
*/
package com.newshunt.dataentity.notification;

import java.io.Serializable;

/**
 * An enum for telling the mechanism of the notification at the client side.
 *
 * @author raunak.yadav
 */
public enum NotificationDeliveryMechanism implements Serializable {
  PUSH(0),
  PULL(1),
  TEST(2);

  private final int value;

  NotificationDeliveryMechanism(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }

  public static NotificationDeliveryMechanism fromDeliveryType(int mechanism) {
    for (NotificationDeliveryMechanism deliveryMechanism : NotificationDeliveryMechanism.values()) {
      if (deliveryMechanism.value == mechanism) {
        return deliveryMechanism;
      }
    }
    return null;
  }
}