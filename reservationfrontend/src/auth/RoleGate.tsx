import React from 'react';
import {useAuth} from './AuthContext';
import {Role, hasAnyRole} from './roles';

type Props = {
    anyOf: Role[];
    children: React.ReactNode;
    fallback?: React.ReactNode; // optional UI when blocked
};

const RoleGate: React.FC<Props> = ({anyOf, children, fallback = null}) => {
    const {user} = useAuth();
    const allowed = hasAnyRole(user?.roles, anyOf);
    return <>{allowed ? children : fallback}</>;
};

export default RoleGate;
