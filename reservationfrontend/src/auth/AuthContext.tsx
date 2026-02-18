import React, {createContext, useContext, useEffect, useMemo, useState} from 'react';

export interface UserInfo {
    name?: string;
    email?: string;
    roles?: string[];
    csrf?: string;
}

type AuthCtx = {
    user: UserInfo | null;
    isAuthenticated: boolean;
    apiFetch: (url: string, init?: RequestInit, includeCsrf?: boolean) => Promise<Response>;
    refreshMe: () => Promise<void>;
};

const AuthContext = createContext<AuthCtx | null>(null);
const AUTH_BASE = ''; // your gateway root

export const AuthProvider: React.FC<{children: React.ReactNode}> = ({children}) => {
    const [user, setUser] = useState<UserInfo | null>(null);

    const apiFetch = (url: string, init: RequestInit = {}, includeCsrf = false) =>
        fetch(url, {
            credentials: 'include',
            headers: {
                ...(includeCsrf && user?.csrf ? {'X-XSRF-TOKEN': user.csrf} : {}),
                ...init.headers,
            },
            ...init,
        });

    const refreshMe = async () => {
        try {
            const res = await apiFetch(`${AUTH_BASE}/me`);
            if (!res.ok) return setUser(null);
            const json = await res.json();
            // if logged out, BE returns { error, csrf }; keep CSRF anyway
            if (json?.name) setUser(json);
            else setUser({csrf: json?.csrf});
        } catch {
            setUser(null);
        }
    };

    useEffect(() => {
        refreshMe();
        // no deps on apiFetch: it uses user but only for CSRF on other calls
        // /me does not require CSRF
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    const value = useMemo<AuthCtx>(() => ({
        user,
        isAuthenticated: !!user?.name,
        apiFetch,
        refreshMe,
    }), [user]);

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
    const ctx = useContext(AuthContext);
    if (!ctx) throw new Error('useAuth must be used within <AuthProvider>');
    return ctx;
};
