/*
 * Copyright (c) 2018 Couchbase, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.couchbase.client.core.error;

/**
 * This exception indicates that a consumer of a request stopped listening (for whatever
 * reason) and as a result the request has been cancelled.
 *
 * <p>This should be treated like any other {@link RequestCanceledException}, it just gives
 * some type-based meaning to why the request has been cancelled.</p>
 *
 * @since 2.0.0
 */
public class StoppedListeningException extends RequestCanceledException {

  public static final StoppedListeningException INSTANCE = new StoppedListeningException();

  static {
    INSTANCE.setStackTrace(new StackTraceElement[0]);
  }

  private StoppedListeningException() {
  }

}