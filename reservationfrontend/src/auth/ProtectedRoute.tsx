import React from 'react';
import {Navigate} from 'react-router-dom';
import {useAuth} from './AuthContext';
import {Role, hasAnyRole} from './roles';

type Props = {
    anyOf?: Role[];             // if omitted → just requires authentication
    children: React.ReactElement;
    redirectTo?: string;        // where to send blocked users
};

const ProtectedRoute: React.FC<Props> = ({anyOf, children, redirectTo = '/'}) => {
    const {isAuthenticated, user} = useAuth();

    if (!isAuthenticated) {
        // not logged in → could also redirect to /login page if you have one
        return <Navigate to={redirectTo} replace />;
    }
    if (anyOf && !hasAnyRole(user?.roles, anyOf)) {
        // logged in but missing role
        return <Navigate to={redirectTo} replace />;
    }
    return children;
};

export default ProtectedRoute;
