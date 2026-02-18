// src/api/notificationsApi.ts
export type NotificationType = 'SYSTEM' | 'RESERVATION' | 'PAYMENT' | 'VEHICLE';
export type NotificationStatus = 'UNREAD' | 'READ' | 'ARCHIVED';

export interface Notification {
    id: number;
    userId: number;
    type: NotificationType;
    status: NotificationStatus;
    title: string;
    body: string;
    createdBy: string;
    createdAt: string; // ISO
    deleted: boolean;
}

export interface Page<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    number: number; // current page index
    size: number;
}

export const NotificationsApi = {
    async list(params: { status?: 'ALL' | NotificationStatus; page?: number; size?: number }) {
        const q = new URLSearchParams();
        if (params.status && params.status !== 'ALL') q.set('status', params.status);
        q.set('page', String(params.page ?? 0));
        q.set('size', String(params.size ?? 20));
        const res = await fetch(`/api/v1/notifications?${q.toString()}`, { credentials: 'include' });
        if (!res.ok) throw new Error(`Failed to load notifications (${res.status})`);
        return (await res.json()) as Page<Notification>;
    },

    async unreadCount() {
        const res = await fetch(`/api/v1/notifications/unread-count`, { credentials: 'include' });
        if (!res.ok) throw new Error(`Failed to load unread count (${res.status})`);
        const j = await res.json();
        return Number(j.count ?? 0);
    },

    async markRead(id: number, csrf?: string) {
        const res = await fetch(`/api/v1/notifications/${id}/read`, {
            method: 'PATCH',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json',
                ...(csrf ? { 'X-XSRF-TOKEN': csrf } : {}),
            },
        });
        if (!res.ok) throw new Error('Failed to mark as read');
    },

    async markAllRead(csrf?: string) {
        const res = await fetch(`/api/v1/notifications/mark-all-read`, {
            method: 'PATCH',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json',
                ...(csrf ? { 'X-XSRF-TOKEN': csrf } : {}),
            },
        });
        if (!res.ok) throw new Error('Failed to mark all read');
    },

    async archive(id: number, csrf?: string) {
        const res = await fetch(`/api/v1/notifications/${id}/archive`, {
            method: 'PATCH',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json',
                ...(csrf ? { 'X-XSRF-TOKEN': csrf } : {}),
            },
        });
        if (!res.ok) throw new Error('Failed to archive');
    },

    async deleteSoft(id: number, csrf?: string) {
        const res = await fetch(`/api/v1/notifications/${id}`, {
            method: 'DELETE',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json',
                ...(csrf ? { 'X-XSRF-TOKEN': csrf } : {}),
            },
        });
        if (!res.ok) throw new Error('Failed to delete');
    },
};
