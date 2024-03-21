/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/**
 * Set the error message.
 * @param {type} message Message to display.
 */
function setErrorMessage(message) {
    $("#ajaxErrorMessage").html(message);
  }
  
  /**
   * Set the warning message.
   * @param {type} message Message to display.
   */
  function setWarningMessage(message) {
    $("#ajaxWarningMessage").html(message);
  }
  
  /**
   * Set the success message.
   * @param {type} message Message to display.
   */
  function setSuccessMessage(message) {
    $("#ajaxSuccessMessage").html(message);
  }
  
  /**
   * Add an error message.
   * @param {type} message Message to display.
   */
  function addErrorMessage(message) {
    var li = document.createElement("li");
    li.appendChild(document.createTextNode(message));
    $("#ajaxErrorMessages").append(li);
  }
  
  /**
   * Add a warning message.
   * @param {type} message Message to display.
   */
  function addWarningMessage(message) {
    var li = document.createElement("li");
    li.appendChild(document.createTextNode(message));
    $("#ajaxWarningMessages").append(li);
  }
  
  /**
   * Clean error messages.
   */
  function clearErrorMessages() {
    $("#ajaxErrorMessages").empty();
  }
  
  /**
   * Clean warning messages.
   */
  function clearWarningMessages() {
    $("#ajaxWarningMessages").empty();
  }
  
  /**
   * Show error messages block.
   */
  function showErrorMessages() {
    $("#ajaxErrorMessageBlock").show(500);
  }
  
  /**
   * Hide error messages block.
   */
  function hideErrorMessages() {
    $("#ajaxErrorMessageBlock").hide(500);
  }
  
  /**
   * Show error messages block.
   */
  function showWarningMessages() {
    $("#ajaxWarningMessageBlock").show(500);
  }
  
  /**
   * Hide warning messages block.
   */
  function hideWarningMessages() {
    $("#ajaxWarningMessageBlock").hide(500);
  }
  
  /**
   * Show success messages block.
   */
  function showSuccessMessages() {
    $("#ajaxSuccessMessageBlock").show(500);
  }
  
  /**
   * Hide success messages block.
   */
  function hideSuccessMessages() {
    $("#ajaxSuccessMessageBlock").hide(500);
  }
  
  // Set the data-hide attribute to only hide message and not delete container.
  $(document).ready(
    function () {
      $("[data-hide]").on("click", function () {
        $("." + $(this).attr("data-hide")).hide();
      });
    }
  );
  