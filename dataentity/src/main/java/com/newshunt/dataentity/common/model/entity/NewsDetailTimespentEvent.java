/*
 * Created by Rahul Ravindran at 26/9/19 12:07 AM
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.common.model.entity;

import com.newshunt.dataentity.analytics.entity.NhAnalyticsUserAction;

import java.util.Map;

/**
 * Created by karthik on 21/11/17.
 */

public abstract class NewsDetailTimespentEvent {

    public boolean isCreateEvent() {
        return false;
    }

    public boolean isUpdateParamEvent() {
        return false;
    }

    public boolean isSendEvent() {
        return false;
    }

    public boolean isClearStaleEvent() {
        return false;
    }

    public boolean isDeleteEvent() {
        return false;
    }

    public static class NewsDetailCreateTimespentEvent extends NewsDetailTimespentEvent {

        private final Long fragmentId;
        private final Map<String, Object> params;

        public NewsDetailCreateTimespentEvent(Long fragmentId, Map<String, Object> params) {
            this.fragmentId = fragmentId;
            this.params = params;
        }

        public Long getFragmentId() {
            return fragmentId;
        }

        public Map<String, Object> getParams() {
            return params;
        }

        @Override
        public boolean isCreateEvent() {
            return true;
        }
    }

    public static class NewsDetailUpdateTimespentEvent extends NewsDetailTimespentEvent {

        private final Long fragmentId;
        private final String paramName;
        private final String paramValue;

        public NewsDetailUpdateTimespentEvent(Long fragmentId, String paramName, String paramValue) {
            this.fragmentId = fragmentId;
            this.paramName = paramName;
            this.paramValue = paramValue;
        }

        public Long getFragmentId() {
            return fragmentId;
        }

        public String getParamName() {
            return paramName;
        }

        public String getParamValue() {
            return paramValue;
        }

        @Override
        public boolean isUpdateParamEvent() {
            return true;
        }
    }

    public static class NewsDetailClearTimespentEvent extends NewsDetailTimespentEvent {

        public NewsDetailClearTimespentEvent() {
        }

        @Override
        public boolean isClearStaleEvent() {
            return true;
        }
    }

    public static class NewsDetailDeleteTimespentEvent extends NewsDetailTimespentEvent {
        private final Long fragmentId;

        public NewsDetailDeleteTimespentEvent(Long fragmentId) {
            this.fragmentId = fragmentId;
        }

        public Long getFragmentId() {
            return fragmentId;
        }

        @Override
        public boolean isDeleteEvent() {
            return true;
        }
    }

    public static class NewsDetailSendTimespentEvent extends NewsDetailTimespentEvent {
        private final Long fragmentId;
        private final Map<Integer, Long> timespent;
        private boolean paused;
        private NhAnalyticsUserAction exitAction;

        public NewsDetailSendTimespentEvent(Long fragmentId, Map<Integer, Long> timespent,
                                            boolean paused, NhAnalyticsUserAction exitAction) {
            this.fragmentId = fragmentId;
            this.timespent = timespent;
            this.paused = paused;
            this.exitAction = exitAction;
        }

        public Long getFragmentId() {
            return fragmentId;
        }

        public NhAnalyticsUserAction getExitAction() {
            return exitAction;
        }

        public Map<Integer, Long> getParams() {
            return timespent;
        }


        @Override
        public boolean isSendEvent() {
            return true;
        }

        public boolean isPaused() {
            return paused;
        }
    }
}
