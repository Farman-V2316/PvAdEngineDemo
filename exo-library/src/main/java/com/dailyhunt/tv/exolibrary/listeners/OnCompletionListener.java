/*
 * Copyright (C) 2016 Brian Wernick
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dailyhunt.tv.exolibrary.listeners;


/**
 * Interface definition for a callback to be invoked when playback of
 * a media source has completed.
 */
public interface OnCompletionListener {
    /**
     * Called when the end of a media source is reached during playback.
     */
    void onCompletion();

  /**
   * To count the no of times video is played in case video is looped
   * Used in case of mp4 gifs
   */
  void onLoopComplete();
}