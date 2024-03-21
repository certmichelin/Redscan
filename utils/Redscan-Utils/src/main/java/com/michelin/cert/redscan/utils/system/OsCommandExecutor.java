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

import java.io.File;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;

/**
 * Os Command Executor.
 *
 * @author Maxime ESCOURBIAC
 */
public class OsCommandExecutor {

  /**
   * Execute securely a process.
   *
   * @param command Command to execute.
   * @return The StreamGobbler of the run.
   */
  public StreamGobbler execute(String command) {
    return execute(command, null, false);
  }

  /**
   * Execute securely a process.
   *
   * @param command Command to execute.
   * @param flushInput True if the stream read by the application need to be closed for the execution.
   * @return The StreamGobbler of the run.
   */
  public StreamGobbler execute(String command, boolean flushInput) {
    return execute(command, null, flushInput);
  }

  /**
   * Execute securely a process.
   *
   * @param command Command to execute.
   * @param executionDir Execution directory.
   * @return The StreamGobbler of the run.
   */
  public StreamGobbler execute(String command, File executionDir) {
    return execute(command, executionDir, false);
  }

  /**
   * Execute securely a process.
   *
   * @param command Command to execute.
   * @param executionDir Execution directory.
   * @param flushInput True if the stream read by the application need to be closed for the execution.
   * @return The StreamGobbler of the run.
   */
  public StreamGobbler execute(String command, File executionDir, boolean flushInput) {
    StreamGobbler streamGobbler = null;
    try {
      Process process = Runtime.getRuntime().exec(command, null, executionDir);

      //Check if the steam read by the application need to be closed. 
      //This case was necessary for some application like gospider where echo 'blah' | gospider was possible.
      if (flushInput) {
        process.getOutputStream().flush();
        process.getOutputStream().close();
      }

      streamGobbler = new StreamGobbler(process.getInputStream(), process.getErrorStream());
      Executors.newSingleThreadExecutor().submit(streamGobbler);
      streamGobbler.setExitStatus(process.waitFor());
    } catch (Exception ex) {
      LogManager.getLogger(OsCommandExecutor.class).error(String.format("Exception : %s", ex.getMessage()));
    }
    return streamGobbler;
  }
}
