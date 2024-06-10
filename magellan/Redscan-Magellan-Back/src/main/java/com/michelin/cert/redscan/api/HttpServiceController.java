/*
 * Copyright 2023 Michelin CERT (https://cert.michelin.com/)
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

package com.michelin.cert.redscan.api;

import com.michelin.cert.redscan.api.exception.GenericException;
import com.michelin.cert.redscan.api.exception.NotFoundException;
import com.michelin.cert.redscan.service.HttpServiceService;
import com.michelin.cert.redscan.utils.models.services.HttpService;
import io.swagger.v3.oas.annotations.Operation;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * HTTP service controller.
 *
 * @author Axel REMACK
 */
@RestController
@RequestMapping("/api/http-services")
public class HttpServiceController extends DatalakeStorageItemController<HttpService> {

  @Autowired
  public void setService(HttpServiceService service) {
    this.service = service;
  }

  /**
   * Default constructor.
   */
  @Autowired
  public HttpServiceController() {
  }

  /**
   * Get screenshot taken by puppeteer for a http service.
   *
   * @param id HTTP Service id.
   * @return The png file in byte array.
   */
  @Operation(summary = "Get the screenshot associated to the http service", security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "The screenshot associated to the http service",
            content = {
              @Content(
                  mediaType = "image/png",
                  schema = @Schema(implementation = byte[].class))
            }
        ),
        @ApiResponse(
            responseCode = "401",
            description = "The token was not provided",
            content = @Content(schema = @Schema(implementation = Void.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "The provided token does not have enough privileges",
            content = @Content(schema = @Schema(implementation = Void.class))
        )
      })
  @PreAuthorize("hasAuthority('maintainer')")
  @GetMapping("/{id}/screenshot")
  @ResponseBody
  public ResponseEntity<byte[]> getScreenshot(@PathVariable String id) {
    try {
      ResponseEntity<byte[]> result = new ResponseEntity<>(HttpStatus.NOT_FOUND);
      HttpService httpService = (HttpService) service.find(id);
      if (httpService != null) {
        if (httpService.getData().containsKey("puppeteer")) {
          String base64Screenshot = httpService.getData().get("puppeteer").toString();
          result = ResponseEntity.ok()
              .contentType(MediaType.IMAGE_PNG)
              .body(Base64.getDecoder().decode(base64Screenshot));
        } else {
          throw new NotFoundException(id);
        }
      } else {
        throw new NotFoundException(id);
      }
      return result;
    } catch (NotFoundException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new GenericException(ex.getMessage());
    }
  }
}
