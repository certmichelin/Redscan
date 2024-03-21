package com.michelin.cert.redscan.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.c4_soft.springaddons.security.oauth2.test.annotations.WithMockAuthentication;
import com.michelin.cert.redscan.service.BrandService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
//import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
//import org.keycloak.adapters.springsecurity.KeycloakSecurityComponents;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;


/**
 * API endpoint tests.
 *
 * @author Axel REMACK
 */
/*
@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = BrandController.class)
@ComponentScan(basePackageClasses = { KeycloakSecurityComponents.class, KeycloakSpringBootConfigResolver.class })
public class HTTPStatusTests {
  @Autowired
  MockMvc mockMvc;

  @MockBean
  private BrandService brandService;

  @DisplayName("Test for API endpoints : Find All brands with wrong user")
  @Test
  @WithMockAuthentication(name = "user_viewer", authorities = {"ROLE_viewer"})
  public void testFindAllWithWrongPermissions() throws Exception {
    mockMvc.perform(get("/api/brands/"))
            .andExpect(status().isForbidden());
  }

  @DisplayName("Test for API endpoints : Find All brands without authentication")
  @Test
  public void testFindAllWithoutAuth() throws Exception {
    mockMvc.perform(get("/api/brands/"))
            .andExpect(status().isUnauthorized());
  }

  @DisplayName("Test for API endpoints : Find All brands with proper authentication")
  @Test
  @WithMockAuthentication(name = "user_maintainer", authorities = {"ROLE_maintainer"})
  public void testFindAll() throws Exception {
    mockMvc.perform(get("/api/brands"))
            .andExpect(status().isOk());
  }

  @DisplayName("Test for API endpoints : NotFoundException raises 404 HTTP code")
  @Test
  @WithMockAuthentication(name = "user_maintainer", authorities = {"ROLE_maintainer"})
  public void testNotFoundExceptionCode() throws Exception {
    mockMvc.perform(get("/api/brands/999999"))
            .andExpect(status().isNotFound());
  }

  @DisplayName("Test for API endpoints : ConflictException raises 409 HTTP code")
  @Test
  @WithMockAuthentication(name = "user_maintainer", authorities = {"ROLE_maintainer"})
  public void testConflictExceptionCode() throws Exception {
    mockMvc.perform(put("/api/brands/unblock/1"))
            .andExpect(status().isConflict());
  }



}
*/