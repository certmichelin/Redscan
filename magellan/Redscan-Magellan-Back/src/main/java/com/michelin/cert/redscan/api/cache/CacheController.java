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

package com.michelin.cert.redscan.api.cache;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



/**
 * Cache controller.
 *
 * @author Axel REMACK
 */
@RestController
@RequestMapping("/api/cache")
public class CacheController {
  @Value("${cache.url}")
  String cacheUrl;

  /**
   * Default constructor.
   */
  public CacheController() {  }

  /**
   * Get number of cached entries.
   *
   * @return Number of cached entries.
   */
  @Operation(summary = "Get cache count.", security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Number of cache entries",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = Integer.class))
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
  @GetMapping("/count")
  public int getNbCache() {
    String result = Unirest.get(String.format("%s/count", cacheUrl)).asString().getBody();
    return Integer.valueOf(result);
  }

  /**
   * Get cached entry.
   *
   * @param response Server response to send.
   * @param key Key of cached entry to retrieve.
   * @param validity Validity in hours
   *
   * @return Cached entry.
   */
  @Operation(summary = "Get cache entry.", security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Cache entry.",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = String.class))
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
  @GetMapping("/{key}/{validity}")
  public String getCache(HttpServletResponse response, @Parameter(description = "Cache key.") @PathVariable("key") String key, @Parameter(description = "Cache validity.") @PathVariable("validity") int validity) throws IOException {
    HttpResponse<String> cacheRes = Unirest.get(String.format("%s/cache/%s/%d", cacheUrl, key, validity)).asString();
    response.setStatus(cacheRes.getStatus());
    return cacheRes.getBody();
  }

  /**
   * Post cached entry.
   *
   * @param body Cache value.
   * @param response Cache status.
   * @return True if creation succeeded.
   */
  @Operation(summary = "Cache an item.", security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Cache status.",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = String.class))
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
  @PostMapping()
  public String postCache(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Cache value.") @RequestBody String body, HttpServletResponse response) {
    HttpResponse<String> cacheRes = Unirest.post(String.format("%s/cache", cacheUrl)).header("content-type", "application/json").body(body).asString();
    response.setStatus(cacheRes.getStatus());

    return cacheRes.getBody();
  }

  /**
   * Flush old entries in cache.
   *
   * @return True if flush succeeded.
   */
  @Operation(summary = "Flush old entries in cache.", security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Operation status.",
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
  @PreAuthorize("hasAuthority('maintainer')")
  @DeleteMapping()
  public boolean flushCache(HttpServletResponse response) {
    boolean result = false;
    HttpResponse<String> cacheRes = Unirest.delete(String.format("%s/cache", cacheUrl)).asString();
    response.setStatus(cacheRes.getStatus());

    if (cacheRes.isSuccess()) {
      result = true;
    }

    return result;
  }

}
