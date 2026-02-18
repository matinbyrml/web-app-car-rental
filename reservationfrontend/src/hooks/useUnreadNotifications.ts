// src/hooks/useUnreadNotifications.ts
import { useEffect, useState } from 'react';
import { useAuth } from '../auth/AuthContext';
import {NotificationsApi} from "../pages/Messages/notificationsApis.ts";

export function useUnreadNotifications(pollMs = 30000) {
    const { isAuthenticated } = useAuth();
    const [count, setCount] = useState<number>(0);

    const refresh = async () => {
        if (!isAuthenticated) {
            setCount(0);
            return;
        }
        try {
            const c = await NotificationsApi.unreadCount();
            setCount(c);
        } catch {
            /* ignore */
        }
    };

    useEffect(() => {
        refresh();
        if (!isAuthenticated) return;
        const id = setInterval(refresh, pollMs);
        return () => clearInterval(id);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [isAuthenticated]);

    return { count, refresh };
}
