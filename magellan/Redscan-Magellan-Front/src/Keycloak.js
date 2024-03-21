import Keycloak from 'keycloak-js'

const keycloakConfig = {
    url: process.env.REACT_APP_PUBLIC_AUTH,
    realm: 'Redscan',
    clientId: 'magellan'
}

const keycloak = new Keycloak(keycloakConfig);
export default keycloak;