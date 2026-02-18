export type Role =
    | 'ROLE_MANAGER'
    | 'ROLE_STAFF'
    | 'ROLE_FLEET_MANAGER'
    | 'ROLE_CUSTOMER';

export const hasAnyRole = (userRoles: string[] | undefined, required: Role[]) =>
    !!userRoles?.some(r => required.includes(r as Role));
