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

import com.michelin.cert.redscan.api.exception.ConflictException;
import com.michelin.cert.redscan.api.exception.GenericException;
import com.michelin.cert.redscan.api.exception.NotFoundException;
import com.michelin.cert.redscan.service.DatalakeStorageItemService;
import com.michelin.cert.redscan.utils.datalake.DatalakeStorageItem;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;


/**
 * API abstract controller.
 *
 * @author Axel REMACK
 * @param <T> DatalakeStorageItem related to controller.
 */
public abstract class DatalakeStorageItemController<T extends DatalakeStorageItem> {

  public DatalakeStorageItemService service;

  /**
   * Default constructor.
   */
  @Autowired
  public DatalakeStorageItemController() {  }

  /**
   * DatalakeStorageItemService.
   *
   * @param service DatalakeStorageItemService instance.
   */
  public void setService(DatalakeStorageItemService service) {
    this.service = service;
  }

  /**
   * Get all datalake items of a defined type.
   *
   * @return All datalake items of a defined type.
   */
  @Operation(summary = "Get all datalake items of a defined type.", security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses(
      value = {
        @ApiResponse(
          responseCode = "200",
          description = "All elements",
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
  @GetMapping()
  public List<T> findAll() {
    try {
      return service.findAll();
    } catch (Exception ex) {
      throw new GenericException(ex.getMessage());
    }
  }

  /**
   * Get all datalake items of a defined type with particular sorting.
   *
   * @param field Sort field.
   * @param order Sort order.
   * @return All datalake items of a defined type.
   */
  @Operation(summary = "Get all datalake items of a defined type with particular sorting.", security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses(
          value = {
                  @ApiResponse(
                          responseCode = "200",
                          description = "All elements sorted",
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
  @GetMapping("/sort")
  public List<T> findAll(@Parameter(description = "Sort field") @RequestParam String field, @Parameter(description = "Sort order") @RequestParam String order) {
    try {
      String sortQuery = service.getSortQuery(field, order);
      return service.findAll(sortQuery);
    } catch (Exception ex) {
      throw new GenericException(ex.getMessage());
    }
  }

  /**
   * Get all datalake items of a defined type with pagination.
   *
   * @param page Page number.
   * @param size Number of brands in each page.
   * @return All brands in a specific page.
   */
  @Operation(summary = "Get all datalake items of a defined type with pagination", security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses(
      value = {
        @ApiResponse(
          responseCode = "200",
          description = "All elements according to pagination parameters",
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
  @GetMapping("/{page}/{size}")
  public List<T> findAll(@Parameter(description = "Zero-based page index") @PathVariable int page, @Parameter(description = "Page size") @PathVariable int size) {
    try {
      return service.findAll(String.valueOf(page), String.valueOf(size));
    } catch (Exception ex) {
      throw new GenericException(ex.getMessage());
    }
  }

  /**
   * Get all datalake items of a defined type with pagination and particular sorting.
   *
   * @param field Sort field.
   * @param order Sort order.
   * @param page Page number.
   * @param size Number of brands in each page.
   * @return All brands in a specific page.
   */
  @Operation(summary = "Get all datalake items of a defined type with pagination and particular sorting.", security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses(
          value = {
                  @ApiResponse(
                          responseCode = "200",
                          description = "All elements according to pagination and sorting parameters.",
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
  @GetMapping("/{page}/{size}/sort")
  public List<T> findAll(@Parameter(description = "Sort field") @RequestParam String field,
                         @Parameter(description = "Sort order") @RequestParam String order,
                         @Parameter(description = "Zero-based page index") @PathVariable int page,
                         @Parameter(description = "Page size") @PathVariable int size) {
    try {
      String sortQuery = service.getSortQuery(field, order);
      return service.findAll(sortQuery, String.valueOf(page), String.valueOf(size));
    } catch (Exception ex) {
      throw new GenericException(ex.getMessage());
    }
  }

  /**
   * Get all datalake items with a name including the searched string.
   *
   * @param name String to search.
   * @return All datalake items of a defined type with a name including the searched string.
   */
  @Operation(summary = "Get all datalake items with a name including the searched string.", security = @SecurityRequirement(name = "bearerAuth"))
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
  @GetMapping("/search/{name}")
  public List<T> search(@Parameter(description = "String to search.") @PathVariable String name) {
    try {
      return service.search(name);
    } catch (Exception ex) {
      throw new GenericException(ex.getMessage());
    }
  }

  /**
   * Get all datalake items with a name including the searched string with particular sorting.
   *
   * @param name String to search.
   * @param field Sort field.
   * @param order Sort order.
   * @return All datalake items of a defined type with a name including the searched string.
   */
  @Operation(summary = "Get all datalake items with a name including the searched string with particular sorting.", security = @SecurityRequirement(name = "bearerAuth"))
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
  @GetMapping("/search/{name}/sort")
  public List<T> search(@Parameter(description = "String to search.") @PathVariable String name, @Parameter(description = "Sort field") @RequestParam String field, @Parameter(description = "Sort order") @RequestParam String order) {
    try {
      String sortQuery = service.getSortQuery(field, order);
      return service.search(name, sortQuery);
    } catch (Exception ex) {
      throw new GenericException(ex.getMessage());
    }
  }

  /**
   * Get all datalake items with a name including the searched string with pagination.
   *
   * @param name String to search.
   * @param page Page number.
   * @param size Number of datalake items in each page.
   * @return All datalake items of a defined type with a name including the searched string in a specific page.
   */
  @Operation(summary = "Get all datalake items with a name including the searched string with pagination.", security = @SecurityRequirement(name = "bearerAuth"))
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
  @GetMapping("/search/{name}/{page}/{size}")
  public List<T> search(@Parameter(description = "String to search.") @PathVariable String name, @Parameter(description = "Zero-based page index") @PathVariable int page, @Parameter(description = "Page size") @PathVariable int size) {
    try {
      return service.search(name, String.valueOf(page), String.valueOf(size));
    } catch (Exception ex) {
      throw new GenericException(ex.getMessage());
    }
  }

  /**
   * Get all datalake items with a name including the searched string with pagination and particular sorting.
   *
   * @param name String to search.
   * @param page Page number.
   * @param size Number of datalake items in each page.
   * @param field Sort field.
   * @param order Sort order.
   * @return All datalake items of a defined type with a name including the searched string in a specific page.
   */
  @Operation(summary = "Get all datalake items with a name including the searched string with pagination and particular sorting.", security = @SecurityRequirement(name = "bearerAuth"))
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
  @GetMapping("/search/{name}/{page}/{size}/sort")
  public List<T> search(@Parameter(description = "String to search.") @PathVariable String name,
                        @Parameter(description = "Zero-based page index") @PathVariable int page,
                        @Parameter(description = "Page size") @PathVariable int size,
                        @Parameter(description = "Sort field") @RequestParam String field,
                        @Parameter(description = "Sort order") @RequestParam String order) {
    try {
      String sortQuery = service.getSortQuery(field, order);
      return service.search(name, sortQuery, String.valueOf(page), String.valueOf(size));
    } catch (Exception ex) {
      throw new GenericException(ex.getMessage());
    }
  }

  /**
   * Get a datalake item.
   *
   * @param id DatalakeStorageItem identifier.
   * @return A specific datalake item.
   */
  @Operation(summary = "Get a datalake item", security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses(
      value = {
        @ApiResponse(
          responseCode = "200",
          description = "A specific datalake item",
          content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DatalakeStorageItem.class))
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
  @GetMapping("/{id}")
  public T find(@Parameter(description = "DatalakeStorageItem identifier.") @PathVariable String id) throws NotFoundException {
    T item = null;
    try {
      item = (T) service.find(id);
    } catch (Exception ex) {
      throw new GenericException(ex.getMessage());
    }
    if (item == null) {
      throw new NotFoundException(id);
    }
    return item;
  }

  /**
   * Create a datalake item.
   *
   * @param item DatalakeStorageItem to create.
   * @return True if the creation succeed.
   */
  @Operation(summary = "Create a datalake item", security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses(
      value = {
        @ApiResponse(
          responseCode = "200",
          description = "Creation state",
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
  @PostMapping()
  public boolean create(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "DatalakeStorageItem to create.") @RequestBody T item) {
    boolean success;
    try {
      success =  service.create(item);
    } catch (Exception ex) {
      throw new GenericException(ex.getMessage());
    }
    if (!success) {
      throw new ConflictException();
    }
    return success;
  }

  /**
   * Delete a datalake item.
   *
   * @param id DatalakeStorageItem identifier.
   * @return True if the entity is deleted.
   */
  @Operation(summary = "Delete a datalake item", security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses(
      value = {
        @ApiResponse(
          responseCode = "200",
          description = "Deletion state",
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
  @DeleteMapping("/{id}")
  public boolean delete(@Parameter(description = "DatalakeStorageItem identifier.") @PathVariable String id) {
    boolean success;
    try {
      success = service.delete(id);
    } catch (Exception ex) {
      throw new GenericException(ex.getMessage());
    }
    if (!success) {
      throw new NotFoundException(id);
    }
    return success;
  }

  /**
   * Update a datalake item.
   *
   * @param item DatalakeStorageItem to update.
   * @return True if the update succeeded.
   */
  @Operation(summary = "Update a datalake item", security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses(
      value = {
        @ApiResponse(
          responseCode = "200",
          description = "Updation state",
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
  @PutMapping()
  public boolean update(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "DatalakeStorageItem to update.") @RequestBody T item) {
    boolean success;
    try {
      success = service.update(item);
    } catch (Exception ex) {
      throw new GenericException(ex.getMessage());
    }
    if (!success) {
      throw new ConflictException();
    }
    return success;
  }

  /**
   * Get all blocked datalake items of a defined type.
   *
   * @return All blocked datalake items of a defined type.
   */
  @Operation(summary = "Get all blocked datalake items of a defined type.", security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses(
      value = {
        @ApiResponse(
          responseCode = "200",
          description = "All blocked elements",
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
  @GetMapping("/blocked")
  public List<T> findAllBlocked() {
    try {
      return service.findAllBlocked();
    } catch (Exception ex) {
      throw new GenericException(ex.getMessage());
    }
  }

  /**
   * Get all blocked datalake items of a defined type with sorting.
   *
   * @param field Sort field.
   * @param order Sort order.
   * @return All blocked datalake items of a defined type.
   */
  @Operation(summary = "Get all blocked datalake items of a defined type with sorting.", security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses(
          value = {
                  @ApiResponse(
                          responseCode = "200",
                          description = "All blocked elements sorted",
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
  @GetMapping("/blocked/sort")
  public List<T> findAllBlocked(@Parameter(description = "Sort field") @RequestParam String field, @Parameter(description = "Sort order") @RequestParam String order) {
    try {
      String sortQuery = service.getSortQuery(field, order);
      return service.findAllBlocked(sortQuery);
    } catch (Exception ex) {
      throw new GenericException(ex.getMessage());
    }
  }

  /**
   * Get all blocked datalake items of a defined type with pagination.
   *
   * @param page Page number.
   * @param size Number of brands in each page.
   * @return All blocked datalake items of a defined type in a specific page.
   */
  @Operation(summary = "Get all blocked datalake items of a defined type with pagination.", security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses(
          value = {
                  @ApiResponse(
                          responseCode = "200",
                          description = "All blocked elements according to pagination parameters",
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
  @GetMapping("/blocked/{page}/{size}")
  public List<T> findAllBlocked(@Parameter(description = "Zero-based page index") @PathVariable int page, @Parameter(description = "Page size") @PathVariable int size) {
    try {
      return service.findAllBlocked(String.valueOf(page), String.valueOf(size));
    } catch (Exception ex) {
      throw new GenericException(ex.getMessage());
    }
  }

  /**
   * Get all blocked datalake items of a defined type with pagination and particular sorting.
   *
   * @param field Sort field.
   * @param order Sort order.
   * @param page Page number.
   * @param size Number of brands in each page.
   * @return All blocked datalake items of a defined type in a specific page.
   */
  @Operation(summary = "Get all blocked datalake items of a defined type with pagination and particular sorting.", security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses(
          value = {
                  @ApiResponse(
                          responseCode = "200",
                          description = "All blocked elements according to pagination and sorting parameters.",
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
  @GetMapping("/blocked/{page}/{size}/sort")
  public List<T> findAllBlocked(@Parameter(description = "Sort field") @RequestParam String field,
                                @Parameter(description = "Sort order") @RequestParam String order,
                                @Parameter(description = "Zero-based page index") @PathVariable int page,
                                @Parameter(description = "Page size") @PathVariable int size) {
    try {
      String sortQuery = service.getSortQuery(field, order);
      return service.findAllBlocked(sortQuery, String.valueOf(page), String.valueOf(size));
    } catch (Exception ex) {
      throw new GenericException(ex.getMessage());
    }
  }

  /**
   * Get all non-blocked datalake items of a defined type.
   *
   * @return All non-blocked datalake items of a defined type.
   */
  @Operation(summary = "Get all non-blocked datalake items of a defined type.", security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses(
          value = {
                  @ApiResponse(
                          responseCode = "200",
                          description = "All non-blocked elements",
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
  @GetMapping("/nonBlocked")
  public List<T> findAllNonBlocked() {
    try {
      return service.findAllNonBlocked();
    } catch (Exception ex) {
      throw new GenericException(ex.getMessage());
    }
  }

  /**
   * Get all non-blocked datalake items of a defined type with sorting.
   *
   * @param field Sort field.
   * @param order Sort order.
   * @return All non-blocked datalake items of a defined type.
   */
  @Operation(summary = "Get all non-blocked datalake items of a defined type with sorting.", security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses(
          value = {
                  @ApiResponse(
                          responseCode = "200",
                          description = "All non-blocked elements sorted",
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
  @GetMapping("/nonBlocked/sort")
  public List<T> findAllNonBlocked(@Parameter(description = "Sort field") @RequestParam String field, @Parameter(description = "Sort order") @RequestParam String order) {
    try {
      String sortQuery = service.getSortQuery(field, order);
      return service.findAllNonBlocked(sortQuery);
    } catch (Exception ex) {
      throw new GenericException(ex.getMessage());
    }
  }

  /**
   * Get all non-blocked datalake items of a defined type with pagination.
   *
   * @param page Page number.
   * @param size Number of brands in each page.
   * @return All non-blocked datalake items of a defined type in a specific page.
   */
  @Operation(summary = "Get all non-blocked datalake items of a defined type with pagination.", security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses(
          value = {
                  @ApiResponse(
                          responseCode = "200",
                          description = "All non-blocked elements according to pagination parameters",
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
  @GetMapping("/nonBlocked/{page}/{size}")
  public List<T> findAllNonBlocked(@Parameter(description = "Zero-based page index") @PathVariable int page, @Parameter(description = "Page size") @PathVariable int size) {
    try {
      return service.findAllNonBlocked(String.valueOf(page), String.valueOf(size));
    } catch (Exception ex) {
      throw new GenericException(ex.getMessage());
    }
  }

  /**
   * Get all non-blocked datalake items of a defined type with pagination and particular sorting.
   *
   * @param field Sort field.
   * @param order Sort order.
   * @param page Page number.
   * @param size Number of brands in each page.
   * @return All non-blocked datalake items of a defined type in a specific page.
   */
  @Operation(summary = "Get all non-blocked datalake items of a defined type with pagination and particular sorting.", security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses(
          value = {
                  @ApiResponse(
                          responseCode = "200",
                          description = "All non-blocked elements according to pagination and sorting parameters.",
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
  @GetMapping("/nonBlocked/{page}/{size}/sort")
  public List<T> findAllNonBlocked(@Parameter(description = "Sort field") @RequestParam String field,
                                @Parameter(description = "Sort order") @RequestParam String order,
                                @Parameter(description = "Zero-based page index") @PathVariable int page,
                                @Parameter(description = "Page size") @PathVariable int size) {
    try {
      String sortQuery = service.getSortQuery(field, order);
      return service.findAllNonBlocked(sortQuery, String.valueOf(page), String.valueOf(size));
    } catch (Exception ex) {
      throw new GenericException(ex.getMessage());
    }
  }

  /**
   * Block a datalake item from being scanned.
   *
   * @param id DatalakeStorageItem identifier.
   * @return True if the entity is blocked.
   */
  @Operation(summary = "Block a datalake item", security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses(
      value = {
        @ApiResponse(
          responseCode = "200",
          description = "Block state",
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
  @PutMapping("/block/{id}")
  public boolean block(@Parameter(description = "DatalakeStorageItem identifier.") @PathVariable String id) {
    boolean success;
    try {
      success = service.block(id);
    } catch (Exception ex) {
      throw new GenericException(ex.getMessage());
    }
    if (!success) {
      throw new ConflictException();
    }
    return success;
  }

  /**
   * Unblock a datalake item.
   *
   * @param id DatalakeStorageItem identifier.
   * @return True if the entity is unblocked.
   */
  @Operation(summary = "Unblock a datalake item", security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses(
      value = {
        @ApiResponse(
          responseCode = "200",
          description = "Unblock state",
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
  @PutMapping("/unblock/{id}")
  public boolean unblock(@Parameter(description = "DatalakeStorageItem identifier.") @PathVariable String id) throws NotFoundException {
    boolean success;
    try {
      success = service.unblock(id);
    } catch (Exception ex) {
      throw new GenericException(ex.getMessage());
    }
    if (!success) {
      throw new ConflictException();
    }
    return success;
  }

  /**
   * Export all datalake items of a defined type.
   *
   * @return Export of all datalake items of a defined type.
   */
  @Operation(summary = "Export all datalake items", security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses(
      value = {
        @ApiResponse(
          responseCode = "200",
          description = "Unblock state",
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
  @GetMapping("/export")
  public HttpEntity<List<T>> export() {
    HttpHeaders header = new HttpHeaders();
    header.setContentType(MediaType.APPLICATION_JSON);
    header.set(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=%ss.json", service.getDatalakeStorageItemClass().getSimpleName().toLowerCase()));
    return new HttpEntity<>(service.findAll(), header);
  }

  /**
   * Import datalake items of a defined type.
   *
   * @param items Datalake items to import.
   * @return Number of datalake items imported successfully.
   */
  @Operation(summary = "Import datalake items", security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses(
      value = {
        @ApiResponse(
          responseCode = "200",
          description = "Unblock state",
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
  @PostMapping("/import")
  public int upload(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "DatalakeStorageItems to import.") @RequestBody ArrayList<T> items) {
    int upsertedCount = 0;
    if (items != null) {
      for (T item : items) {
        if (service.find(item.getId()) == null) {
          if (service.create(item)) {
            ++upsertedCount;
          }
        } else {
          if (service.update(item)) {
            ++upsertedCount;
          }
        }
      }
    }
    return upsertedCount;
  }

  /**
   * Reinject datalake item in scan.
   *
   * @param id Identifier of DatalakeStorageItem to reinject.
   * @return True if reinjection succeeded.
   */
  @Operation(summary = "Reinject a datalake item", security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses(
      value = {
        @ApiResponse(
          responseCode = "200",
          description = "Reinjection state",
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
  @PutMapping("/reinject/{id}")
  public boolean reinject(@Parameter(description = "DatalakeStorageItem identifier.") @PathVariable String id) {
    boolean success;
    try {
      success = service.reinject(id);
    } catch (Exception ex) {
      throw new GenericException(ex.getMessage());
    }
    if (!success) {
      throw new NotFoundException(id);
    }
    return success;
  }

}
