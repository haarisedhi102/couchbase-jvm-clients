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

package com.couchbase.client.java;

import com.couchbase.client.core.Core;
import com.couchbase.client.core.annotation.Stability;
import com.couchbase.client.core.error.context.ReducedQueryErrorContext;
import com.couchbase.client.core.io.CollectionIdentifier;
import com.couchbase.client.java.codec.JsonSerializer;
import com.couchbase.client.java.env.ClusterEnvironment;
import com.couchbase.client.java.query.QueryOptions;
import com.couchbase.client.java.query.ReactiveQueryResult;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.couchbase.client.core.util.Validators.notNull;
import static com.couchbase.client.java.ReactiveCluster.DEFAULT_QUERY_OPTIONS;

/**
 * The scope identifies a group of collections and allows high application
 * density as a result.
 *
 * <p>If no scope is explicitly provided, the default scope is used.</p>
 *
 * @since 3.0.0
 */
public class ReactiveScope {

  /**
   * The underlying async scope which actually performs the actions.
   */
  private final AsyncScope asyncScope;

  /**
   * Stores already opened collections for reuse.
   */
  private final Map<String, ReactiveCollection> collectionCache = new ConcurrentHashMap<>();

  /**
   * Creates a new {@link ReactiveScope}.
   *
   * @param asyncScope the underlying async scope.
   */
  ReactiveScope(final AsyncScope asyncScope) {
    this.asyncScope = asyncScope;
  }

  /**
   * The name of the scope.
   *
   * @return the name of the scope.
   */
  public String name() {
    return asyncScope.name();
  }

  /**
   * The name of the bucket this scope is attached to.
   */
  public String bucketName() {
    return asyncScope.bucketName();
  }

  /**
   * Returns the underlying async scope.
   */
  public AsyncScope async() {
    return asyncScope;
  }

  /**
   * Provides access to the underlying {@link Core}.
   *
   * <p>This is advanced API, use with care!</p>
   */
  @Stability.Volatile
  public Core core() {
    return asyncScope.core();
  }

  /**
   * Provides access to the configured {@link ClusterEnvironment} for this scope.
   */
  public ClusterEnvironment environment() {
    return asyncScope.environment();
  }

  /**
   * Opens the default collection for this scope.
   *
   * @return the default collection once opened.
   */
  ReactiveCollection defaultCollection() {
    return collectionCache.computeIfAbsent(
      CollectionIdentifier.DEFAULT_COLLECTION,
      n -> new ReactiveCollection(asyncScope.defaultCollection())
    );
  }

  /**
   * Opens a collection for this scope with an explicit name.
   *
   * @param collectionName the collection name.
   * @return the requested collection if successful.
   */
  @Stability.Volatile
  public ReactiveCollection collection(final String collectionName) {
    return collectionCache.computeIfAbsent(collectionName, n -> new ReactiveCollection(asyncScope.collection(n)));
  }

  /**
   * Performs a N1QL query with default {@link QueryOptions} in a Scope
   *
   * @param statement the N1QL query statement as a raw string.
   * @return the {@link ReactiveQueryResult} once the response arrives successfully.
   */
  @Stability.Volatile
  public Mono<ReactiveQueryResult> query(final String statement) {
    return this.query(statement, DEFAULT_QUERY_OPTIONS);
  }

  /**
   * Performs a N1QL query with custom {@link QueryOptions} in a Scope
   *
   * @param statement the N1QL query statement as a raw string.
   * @param options the custom options for this query.
   * @return the {@link ReactiveQueryResult} once the response arrives successfully.
   */
  @Stability.Volatile
  public Mono<ReactiveQueryResult> query(final String statement, final QueryOptions options) {
    return Mono.defer(() -> {
      notNull(options, "QueryOptions", () -> new ReducedQueryErrorContext(statement));
      final QueryOptions.Built opts = options.build();
      JsonSerializer serializer = opts.serializer() == null ? environment().jsonSerializer() : opts.serializer();
      return async().queryAccessor().queryReactive(
          async().queryRequest(bucketName(), name(), statement, opts, core(), environment()), opts, serializer);
    });
  }

}
