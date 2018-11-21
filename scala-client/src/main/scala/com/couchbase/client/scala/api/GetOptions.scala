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

package com.couchbase.client.scala.api

import scala.concurrent.duration.FiniteDuration

case class GetOptions(timeout: FiniteDuration = null) {
  def timeout(timeout: FiniteDuration) = copy(timeout = timeout)
}
//
//case class GetOptions() {
//  private var timeout: FiniteDuration = null
//
//  def timeout(timeout: FiniteDuration): GetOptions = {
//    this.timeout = timeout
//    this
//  }
//
//  def build(): GetOptionsBuilt = GetOptionsBuilt(timeout)
//}

object GetOptions {
  def apply() = new GetOptions()
}