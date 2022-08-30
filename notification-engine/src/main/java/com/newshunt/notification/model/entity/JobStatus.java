/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */

package com.newshunt.notification.model.entity;

/**
 * @author anshul.jain on 11/3/2016.
 *         <p>
 *         An enum for the pull and job schedule.
 */

public enum JobStatus {
  //Allow pull and schedule next job
  ALLOW,
  //Reject pull but schedule next job
  REJECT_PULL,
  //Reject pull but schedule next job with battery as mandatory requirement.
  REJECT_PULL_JOB_REQUIRES_CHARGING;
}
