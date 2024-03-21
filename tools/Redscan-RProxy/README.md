# Redscan-RPRoxy
Apache reverse proxy for redscan.
It's implement HTTPS and OpenId connect authentication with Keycloak.

## Prerequisite
Keycloak

## Deployment
Directory to mount

      - ./conf/redscan.conf:/usr/local/apache2/conf/redscan.conf
      - ./certs/:/usr/local/apache2/conf/certs/


Port to expose 443

When deploying please change certs, OIDCSSLValidateServer, OIDCCryptoPassphrase, OIDCProviderMetadataURL, OIDCClientSecret when deploying.