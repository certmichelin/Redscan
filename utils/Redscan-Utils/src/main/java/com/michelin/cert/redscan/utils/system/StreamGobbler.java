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

package com.michelin.cert.redscan.utils.system;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * StreamGobbler for fetch command output.
 *
 * @author Maxime ESCOURBIAC
 */
public class StreamGobbler implements Runnable {

  private final InputStream inputStream;
  private final InputStream errorStream;
  private Object[] standardOutputs;
  private Object[] errorOutputs;
  private int exitStatus;

  /**
   * Public constructor.
   *
   * @param inputStream Process input stream.
   * @param errorStream Process error stream.
   */
  public StreamGobbler(InputStream inputStream, InputStream errorStream) {
    this.inputStream = inputStream;
    this.errorStream = errorStream;
  }

  /**
   * Get Object Array corresponding to the standard output.
   *
   * @return Object Array corresponding to the standard output.
   */
  public Object[] getStandardOutputs() {
    return standardOutputs;
  }

  /**
   * Get Object Array corresponding to the error output.
   *
   * @return Object Array corresponding to the error output.
   */
  public Object[] getErrorOutputs() {
    return errorOutputs;
  }

  /**
   * Exit status code of the process.
   *
   * @return Exit status code of the process.
   */
  public int getExitStatus() {
    return exitStatus;
  }

  /**
   * Exit status code of the process.
   *
   * @param exitStatus Exit status code of the process.
   */
  public void setExitStatus(int exitStatus) {
    this.exitStatus = exitStatus;
  }

  /**
   * Return true if the command return something in standard output.
   *
   * @return Boolean if the command is having standard output.
   */
  public Boolean isHavingStdOuput() {
    if (standardOutputs != null) {
      for (Object object : standardOutputs) {
        String result = object.toString();
        if (!result.isEmpty()) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public void run() {
    standardOutputs = new BufferedReader(new InputStreamReader(inputStream)).lines().toArray();
    errorOutputs = new BufferedReader(new InputStreamReader(errorStream)).lines().toArray();
  }
}
