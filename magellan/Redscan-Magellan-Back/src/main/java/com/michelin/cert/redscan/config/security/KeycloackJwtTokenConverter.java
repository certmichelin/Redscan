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

package com.michelin.cert.redscan.config.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Keycloack JWT token converter.
 *
 * @author Maxime ESCOURBIAC
 */
public class KeycloackJwtTokenConverter implements Converter<Jwt, JwtAuthenticationToken> {

  public KeycloackJwtTokenConverter() {
  }

  @Override
  public JwtAuthenticationToken convert(Jwt jwt) {
    List<GrantedAuthority> authorities = new ArrayList<>();
    if (jwt.getClaim("resource_access") != null) {
      Map<String, Object> ressourceAccess = jwt.getClaim("resource_access");
      if (ressourceAccess.containsKey("magellan")) {
        Map<String, Object> magellanAccess = (Map<String, Object>) ressourceAccess.get("magellan");
        ObjectMapper mapper = new ObjectMapper();
        List<String> roles = mapper.convertValue(magellanAccess.get("roles"), new TypeReference<List<String>>() {
        });
        for (String role : roles) {
          System.out.println("AUTHORITIES:" + role);
          authorities.add(new SimpleGrantedAuthority(role));
        }
      }
    }
    return new JwtAuthenticationToken(jwt, authorities);
  }
}
