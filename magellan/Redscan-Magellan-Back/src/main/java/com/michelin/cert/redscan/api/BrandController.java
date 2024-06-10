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
import com.michelin.cert.redscan.service.BrandService;
import com.michelin.cert.redscan.utils.models.Brand;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Brand controller.
 *
 * @author Maxime ESCOURBIAC
 * @author Axel REMACK
 */
@RestController
@RequestMapping("/api/brands")
public class BrandController extends DatalakeStorageItemController<Brand> {

  @Autowired
  public void setService(BrandService service) {
    this.service = service;
  }

  /**
   * Default constructor.
   */
  @Autowired
  public BrandController() {
  }

  /**
   * Ventilate all brands.
   *
   * @return True if the ventilation succeed.
   */
  @Operation(summary = "Ventilate brands reinjection.", security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses(
      value = {
        @ApiResponse(
          responseCode = "200",
          description = "The ventilate operation state",
          content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Boolean.class))
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
  @PostMapping("/ventilate")
  @PreAuthorize("hasAuthority('maintainer')")
  public boolean ventilate() {
    boolean success;
    try {
      success = ((BrandService) service).ventilate();
    } catch (Exception ex) {
      throw new GenericException(ex.getMessage());
    }
    return success;
  }

}
