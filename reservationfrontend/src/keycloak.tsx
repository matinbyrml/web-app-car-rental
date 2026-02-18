import Keycloak from 'keycloak-js';
import { jwtDecode, JwtPayload } from 'jwt-decode';
import Cookies from 'js-cookie';

export interface KeycloakJwt extends JwtPayload{
  realm_access: {
    roles: [ "CUSTOMER", "STAFF", "MANAGER", "FLEET_MANAGER"]
  },
}

const keycloak = new Keycloak({
  url: 'http://localhost:9090/realms/master/protocol/openid-connect',
  realm: 'alberioauto',
  clientId: 'gateway-client',
});

function getCookie(name: string): string | undefined {
  return Cookies.get(name);
}

function decodeKeycloakTokenFromCookie(cookieName = 'KEYCLOAK_IDENTITY'): KeycloakJwt | null {
  const token = getCookie(cookieName);
  if (!token) return null;

  try {
    return jwtDecode<KeycloakJwt>(token);
  } catch (error) {
    console.error('Errore nella decodifica del token JWT dal cookie:', error);
    return null;
  }
}

function getRolesForCurrentUser(){
  const role = decodeKeycloakTokenFromCookie();
  if (role){
    return role.realm_access?.roles;
  }
  else{
    console.error("Cookie assente");
    return null;
  }
}

const rolePermissions: Record<KeycloakJwt["realm_access"]["roles"][number], string[]> = {
  CUSTOMER: ["boh", "boh"], //TODO da scrivere
  STAFF: [""],
  MANAGER: [""],
  FLEET_MANAGER: [""],
};

export function hasAccess(endpoint: string): boolean { //TODO vedere dove mettere sto metodo o se usare altro 
  const roles = getRolesForCurrentUser()
  if (roles == null) 
    return false;
  else{
    return roles.some((role) =>
      rolePermissions[role]?.some((ep) => endpoint.startsWith(ep))
    );
  }
}

export default keycloak;
