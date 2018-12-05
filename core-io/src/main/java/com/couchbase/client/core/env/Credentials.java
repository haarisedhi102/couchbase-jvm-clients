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

package com.couchbase.client.core.env;

/**
 * The {@link Credentials} encapsulate different ways of carrying credentials throughout
 * the client.
 *
 * <p>Usually this will be username and password for RBAC, but other means are possible (like
 * X.509 client certificate authentication or legacy per-bucket credentials).</p>
 *
 * @since 2.0.0
 */
public interface Credentials {

  /**
   * Returns the corresponding username for a given bucket.
   *
   * @param bucket the name of the bucket.
   * @return the username for the given bucket.
   */
  String usernameForBucket(String bucket);

  /**
   * Returns the corresponding password for a given bucket.
   *
   * @param bucket the name of the bucket.
   * @return the password for the given bucket.
   */
  String passwordForBucket(String bucket);

}