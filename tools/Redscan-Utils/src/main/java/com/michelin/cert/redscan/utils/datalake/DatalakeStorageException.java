/*
 * Copyright 2021 Michelin CERT (https://cert.michelin.com/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.michelin.cert.redscan.utils.datalake;

/**
 * Datalake Storage custom exception class .
 *
 * @author Maxime ESCOURBIAC
 */
public class DatalakeStorageException extends Exception {

  /**
   * Default constructor.
   *
   * @param errorMessage Error message.
   */
  public DatalakeStorageException(String errorMessage) {
    super(errorMessage);
  }

  /**
   * DatalakeStorageException constructor.
   *
   * @param errorMessage Error message.
   * @param err Associated exception.
   */
  public DatalakeStorageException(String errorMessage, Throwable err) {
    super(errorMessage, err);
  }

}
