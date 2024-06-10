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
import com.michelin.cert.redscan.service.MasterDomainService;
import com.michelin.cert.redscan.utils.models.MasterDomain;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * Master domain controller.
 *
 * @author Axel REMACK
 */
@RestController
@RequestMapping("/api/masterdomains")
public class MasterDomainController extends DatalakeStorageItemController<MasterDomain> {

  @Autowired
  public void setService(MasterDomainService service) {
    this.service = service;
  }

  /**
   * Default constructor.
   */
  @Autowired
  public MasterDomainController() {
  }

  /**
   * Ventilate all master domains.
   *
   * @return True if the ventilation succeed.
   */
  @Operation(summary = "Ventilate master domains reinjection.", security = @SecurityRequirement(name = "bearerAuth"))
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
  @PreAuthorize("hasAuthority('maintainer')")
  @PostMapping("/ventilate")
  public boolean ventilate() {
    boolean success;
    try {
      success = ((MasterDomainService) service).ventilate();
    } catch (Exception ex) {
      throw new GenericException(ex.getMessage());
    }
    return success;
  }

  /**
   * Get in scope master domains.
   *
   * @return In scope master domains.
   */
  @Operation(summary = "Get in scope master domains.", security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses(
          value = {
                  @ApiResponse(
                          responseCode = "200",
                          description = "Filtered elements",
                          content = {
                                  @Content(
                                          mediaType = "application/json",
                                          schema = @Schema(implementation = List.class))
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
  @GetMapping("/inScope")
  public List<MasterDomain> findInScope() {
    try {
      return ((MasterDomainService) service).findInScope();
    } catch (Exception ex) {
      throw new GenericException(ex.getMessage());
    }
  }

  /**
   * Get in scope master domains with particular sorting.
   *
   * @param field Sort field.
   * @param order Sort order.
   * @return In scope master domains.
   */
  @Operation(summary = "Get in scope master domains with particular sorting.", security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses(
          value = {
                  @ApiResponse(
                          responseCode = "200",
                          description = "Filtered elements sorted",
                          content = {
                                  @Content(
                                          mediaType = "application/json",
                                          schema = @Schema(implementation = List.class))
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
  @GetMapping("/inScope/sort")
  public List<MasterDomain> findInScope(@Parameter(description = "Sort field") @RequestParam String field, @Parameter(description = "Sort order") @RequestParam String order) {
    try {
      String sortQuery = service.getSortQuery(field, order);
      return ((MasterDomainService) service).findInScope(sortQuery);
    } catch (Exception ex) {
      throw new GenericException(ex.getMessage());
    }
  }

  /**
   * Get in scope master domains with pagination.
   *
   * @param page Page number.
   * @param size Number of datalake items in each page.
   * @return In scope master domains with pagination.
   */
  @Operation(summary = "Get in scope master domains with pagination.", security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses(
          value = {
                  @ApiResponse(
                          responseCode = "200",
                          description = "Filtered elements with pagination",
                          content = {
                                  @Content(
                                          mediaType = "application/json",
                                          schema = @Schema(implementation = List.class))
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
  @GetMapping("/inScope/{page}/{size}")
  public List<MasterDomain> findInScope(@Parameter(description = "Zero-based page index") @PathVariable int page, @Parameter(description = "Page size") @PathVariable int size) {
    try {
      return ((MasterDomainService) service).findInScope(String.valueOf(page), String.valueOf(size));
    } catch (Exception ex) {
      throw new GenericException(ex.getMessage());
    }
  }

  /**
   * Get in scope master domains with pagination and particular sorting.
   *
   * @param page Page number.
   * @param size Number of datalake items in each page.
   * @param field Sort field.
   * @param order Sort order
   * @return In scope master domains with pagination.
   */
  @Operation(summary = "Get in scope master domains with pagination and particular sorting.", security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses(
          value = {
                  @ApiResponse(
                          responseCode = "200",
                          description = "Filtered elements with pagination and particular sorting",
                          content = {
                                  @Content(
                                          mediaType = "application/json",
                                          schema = @Schema(implementation = List.class))
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
  @GetMapping("/inScope/{page}/{size}/sort")
  public List<MasterDomain> findInScope(@Parameter(description = "Zero-based page index") @PathVariable int page,
                                        @Parameter(description = "Page size") @PathVariable int size,
                                        @Parameter(description = "Sort field") @RequestParam String field,
                                        @Parameter(description = "Sort order") @RequestParam String order) {
    try {
      String sortQuery = service.getSortQuery(field, order);
      return ((MasterDomainService) service).findInScope(sortQuery, String.valueOf(page), String.valueOf(size));
    } catch (Exception ex) {
      throw new GenericException(ex.getMessage());
    }
  }

  /**
   * Get out of scope master domains.
   *
   * @return Out of scope master domains.
   */
  @Operation(summary = "Get out of scope master domains.", security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses(
          value = {
                  @ApiResponse(
                          responseCode = "200",
                          description = "Filtered elements",
                          content = {
                                  @Content(
                                          mediaType = "application/json",
                                          schema = @Schema(implementation = List.class))
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
  @GetMapping("/outOfScope")
  public List<MasterDomain> findOutOfScope() {
    try {
      return ((MasterDomainService) service).findOutOfScope();
    } catch (Exception ex) {
      throw new GenericException(ex.getMessage());
    }
  }

  /**
   * Get out of scope master domains with particular sorting.
   *
   * @param field Sort field.
   * @param order Sort order.
   * @return Out of scope master domains, sorted.
   */
  @Operation(summary = "Get out of scope master domains with particular sorting.", security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses(
          value = {
                  @ApiResponse(
                          responseCode = "200",
                          description = "Filtered elements, sorted",
                          content = {
                                  @Content(
                                          mediaType = "application/json",
                                          schema = @Schema(implementation = List.class))
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
  @GetMapping("/outOfScope/sort")
  public List<MasterDomain> findOutOfScope(@Parameter(description = "Sort field") @RequestParam String field, @Parameter(description = "Sort order") @RequestParam String order) {
    try {
      String sortQuery = service.getSortQuery(field, order);
      return ((MasterDomainService) service).findOutOfScope(sortQuery);
    } catch (Exception ex) {
      throw new GenericException(ex.getMessage());
    }
  }

  /**
   * Get out of scope master domains with pagination.
   *
   * @param page Page number.
   * @param size Number of datalake items in each page.
   * @return Out of scope master domains with pagination.
   */
  @Operation(summary = "Get out of scope master domains with pagination.", security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses(
          value = {
                  @ApiResponse(
                          responseCode = "200",
                          description = "Filtered elements with pagination",
                          content = {
                                  @Content(
                                          mediaType = "application/json",
                                          schema = @Schema(implementation = List.class))
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
  @GetMapping("/outOfScope/{page}/{size}")
  public List<MasterDomain> findOutOfScope(@Parameter(description = "Zero-based page index") @PathVariable int page, @Parameter(description = "Page size") @PathVariable int size) {
    try {
      return ((MasterDomainService) service).findOutOfScope(String.valueOf(page), String.valueOf(size));
    } catch (Exception ex) {
      throw new GenericException(ex.getMessage());
    }
  }

  /**
   * Get out of scope master domains with pagination and particular sorting.
   *
   * @param field Sort field.
   * @param order Sort order.
   * @param page Page number.
   * @param size Number of datalake items in each page.
   * @return Out of scope master domains with pagination and particular sorting.
   */
  @Operation(summary = "Get out of scope master domains with pagination and particular sorting.", security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses(
          value = {
                  @ApiResponse(
                          responseCode = "200",
                          description = "Filtered elements with pagination and particular sorting",
                          content = {
                                  @Content(
                                          mediaType = "application/json",
                                          schema = @Schema(implementation = List.class))
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
  @GetMapping("/outOfScope/{page}/{size}/sort")
  public List<MasterDomain> findOutOfScope(@Parameter(description = "Zero-based page index") @PathVariable int page,
                                           @Parameter(description = "Page size") @PathVariable int size,
                                           @Parameter(description = "Sort field") @RequestParam String field,
                                           @Parameter(description = "Sort order") @RequestParam String order) {
    try {
      String sortQuery = service.getSortQuery(field, order);
      return ((MasterDomainService) service).findOutOfScope(sortQuery, String.valueOf(page), String.valueOf(size));
    } catch (Exception ex) {
      throw new GenericException(ex.getMessage());
    }
  }

  /**
   * Get master domains to review.
   *
   * @return Master domains to review.
   */
  @Operation(summary = "Get master domains to review.", security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses(
          value = {
                  @ApiResponse(
                          responseCode = "200",
                          description = "Filtered elements",
                          content = {
                                  @Content(
                                          mediaType = "application/json",
                                          schema = @Schema(implementation = List.class))
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
  @GetMapping("/toReview")
  public List<MasterDomain> findToReview() {
    try {
      return ((MasterDomainService) service).findToReview();
    } catch (Exception ex) {
      throw new GenericException(ex.getMessage());
    }
  }

  /**
   * Get master domains to review with particular sorting.
   *
   * @param field Sort field.
   * @param order Sort order.
   * @return Master domains to review, sorted.
   */
  @Operation(summary = "Get master domains to review with particular sorting.", security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses(
          value = {
                  @ApiResponse(
                          responseCode = "200",
                          description = "Filtered elements, sorted",
                          content = {
                                  @Content(
                                          mediaType = "application/json",
                                          schema = @Schema(implementation = List.class))
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
  @GetMapping("/toReview/sort")
  public List<MasterDomain> findToReview(@Parameter(description = "Sort field") @RequestParam String field, @Parameter(description = "Sort order") @RequestParam String order) {
    try {
      String sortQuery = service.getSortQuery(field, order);
      return ((MasterDomainService) service).findToReview(sortQuery);
    } catch (Exception ex) {
      throw new GenericException(ex.getMessage());
    }
  }

  /**
   * Get master domains to review, with pagination.
   *
   * @param page Page number.
   * @param size Number of datalake items in each page.
   * @return Master domains to review, with pagination.
   */
  @Operation(summary = "Get master domains to review, with pagination.", security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses(
          value = {
                  @ApiResponse(
                          responseCode = "200",
                          description = "Filtered elements with pagination",
                          content = {
                                  @Content(
                                          mediaType = "application/json",
                                          schema = @Schema(implementation = List.class))
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
  @GetMapping("/toReview/{page}/{size}")
  public List<MasterDomain> findToReview(@Parameter(description = "Zero-based page index") @PathVariable int page, @Parameter(description = "Page size") @PathVariable int size) {
    try {
      return ((MasterDomainService) service).findToReview(String.valueOf(page), String.valueOf(size));
    } catch (Exception ex) {
      throw new GenericException(ex.getMessage());
    }
  }

  /**
   * Get master domains to review, with pagination with particular sorting.
   *
   * @param field Sort field.
   * @param order Sort order.
   * @param page Page number.
   * @param size Number of datalake items in each page.
   * @return Master domains to review, with pagination and particular sorting.
   */
  @Operation(summary = "Get master domains to review, with pagination and particular sorting.", security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses(
          value = {
                  @ApiResponse(
                          responseCode = "200",
                          description = "Filtered elements with pagination and particular sorting",
                          content = {
                                  @Content(
                                          mediaType = "application/json",
                                          schema = @Schema(implementation = List.class))
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
  @GetMapping("/toReview/{page}/{size}/sort")
  public List<MasterDomain> findToReview(@Parameter(description = "Sort field") @RequestParam String field,
                                         @Parameter(description = "Sort order") @RequestParam String order,
                                         @Parameter(description = "Zero-based page index") @PathVariable int page,
                                         @Parameter(description = "Page size") @PathVariable int size) {
    try {
      String sortQuery = service.getSortQuery(field, order);
      return ((MasterDomainService) service).findToReview(sortQuery, String.valueOf(page), String.valueOf(size));
    } catch (Exception ex) {
      throw new GenericException(ex.getMessage());
    }
  }


}
