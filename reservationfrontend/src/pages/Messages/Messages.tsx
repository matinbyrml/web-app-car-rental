// src/pages/Messages/Messages.tsx
/* eslint-disable @typescript-eslint/no-unused-vars */
import React, { useEffect, useMemo, useState } from 'react';
import { useAuth } from '../../auth/AuthContext';

import './messages.css';
import {NotificationsApi, NotificationStatus, Notification, Page} from "./notificationsApis.ts";

const ITEMS_PER_PAGE = 20;

const Messages: React.FC = () => {
    const { user, isAuthenticated } = useAuth();
    const csrf: string = user?.csrf ?? '';

    const [status, setStatus] = useState<'ALL' | NotificationStatus>('ALL');
    const [page, setPage] = useState<number>(0);
    const [data, setData] = useState<Page<Notification> | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<Error | null>(null);
    const [unreadCount, setUnreadCount] = useState<number>(0);

    const canMarkAll: boolean = useMemo<boolean>(() => {
        if (!data) return false;
        return data.content.some((n: Notification) => n.status === 'UNREAD');
    }, [data]);

    const loadData = async (): Promise<void> => {
        setLoading(true);
        setError(null);
        try {
            const pageData: Page<Notification> = await NotificationsApi.list({
                status,
                page,
                size: ITEMS_PER_PAGE
            });
            setData(pageData);
        } catch (e) {
            setError(e as Error);
        } finally {
            setLoading(false);
        }
    };

    const refreshUnread = async (): Promise<void> => {
        try {
            const c: number = await NotificationsApi.unreadCount();
            setUnreadCount(c);
        } catch {
            /* ignore */
        }
    };

    useEffect(() => {
        if (!isAuthenticated) return;
        void loadData();
        void refreshUnread();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [isAuthenticated, page, status]);

    const markRead = async (id: number): Promise<void> => {
        if (!data) return;
        const prev: Page<Notification> = data;
        setData({
            ...data,
            content: data.content.map((n: Notification) =>
                n.id === id ? { ...n, status: 'READ' as NotificationStatus } : n
            )
        });
        try {
            await NotificationsApi.markRead(id, csrf);
            void refreshUnread();
        } catch (e) {
            setData(prev);
            setError(e as Error);
        }
    };

    const markAllRead = async (): Promise<void> => {
        if (!data) return;
        const prev: Page<Notification> = data;
        setData({
            ...data,
            content: data.content.map((n: Notification) =>
                n.status === 'UNREAD' ? { ...n, status: 'READ' as NotificationStatus } : n
            )
        });
        try {
            await NotificationsApi.markAllRead(csrf);
            void refreshUnread();
        } catch (e) {
            setData(prev);
            setError(e as Error);
        }
    };

    const archive = async (id: number): Promise<void> => {
        if (!data) return;
        const prev: Page<Notification> = data;
        setData({
            ...data,
            content: data.content.map((n: Notification) =>
                n.id === id ? { ...n, status: 'ARCHIVED' as NotificationStatus } : n
            )
        });
        try {
            await NotificationsApi.archive(id, csrf);
        } catch (e) {
            setData(prev);
            setError(e as Error);
        }
    };

    const remove = async (id: number): Promise<void> => {
        if (!data) return;
        const prev: Page<Notification> = data;
        setData({
            ...data,
            content: data.content.filter((n: Notification) => n.id !== id)
        });
        try {
            await NotificationsApi.deleteSoft(id, csrf);
            void refreshUnread();
        } catch (e) {
            setData(prev);
            setError(e as Error);
        }
    };

    const onChangeStatus = (value: 'ALL' | NotificationStatus): void => {
        setPage(0);
        setStatus(value);
    };

    if (!isAuthenticated) {
        return (
            <div className="msg-page">
                <p>Please log in to view your messages.</p>
            </div>
        );
    }

    return (
        <div className="msg-page">
            <div className="msg-header">
                <h2>
                    Messages {unreadCount > 0 && <span className="badge">{unreadCount} unread</span>}
                </h2>

                <div className="msg-controls">
                    <select
                        value={status}
                        onChange={(e: React.ChangeEvent<HTMLSelectElement>) =>
                            onChangeStatus(e.target.value as 'ALL' | NotificationStatus)
                        }
                        className="msg-select"
                        aria-label="Filter status"
                    >
                        <option value="ALL">All</option>
                        <option value="UNREAD">Unread</option>
                        <option value="READ">Read</option>
                        <option value="ARCHIVED">Archived</option>
                    </select>

                    <button className="msg-btn" onClick={() => void markAllRead()} disabled={!canMarkAll}>
                        Mark all read
                    </button>
                </div>
            </div>

            {loading && <p>Loading…</p>}
            {error && <p className="error">Error: {error.message}</p>}
            {!loading && data && data.content.length === 0 && <p>No messages.</p>}

            {!loading && data && data.content.length > 0 && (
                <ul className="msg-list">
                    {data.content
                        .slice() // copia per non mutare l'array originale
                        .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
                        .map((n: Notification) => (
                            <li key={n.id} className={`msg-item ${n.status.toLowerCase()}`}>
                                <div className="msg-top">
                                    <span className="msg-type">{n.type}</span>
                                    <span className="msg-date">{new Date(n.createdAt).toLocaleString()}</span>
                                </div>
                                <h4 className="msg-title">{n.title}</h4>
                                <p className="msg-body">{n.body}</p>
                                <div className="msg-actions">
                                    {n.status === 'UNREAD' && (
                                        <button className="msg-btn" onClick={() => void markRead(n.id)}>
                                            Mark read
                                        </button>
                                    )}
                                    {n.status !== 'ARCHIVED' && (
                                        <button className="msg-btn" onClick={() => void archive(n.id)}>
                                            Archive
                                        </button>
                                    )}
                                    <button className="msg-btn danger" onClick={() => void remove(n.id)}>
                                        Delete
                                    </button>
                                </div>
                            </li>
                        ))}
                </ul>
            )}

            {!loading && data && data.totalPages > 1 && (
                <div className="msg-pagination">
                    <button
                        className="msg-btn"
                        disabled={page === 0}
                        onClick={() => setPage((p: number) => p - 1)}
                    >
                        Prev
                    </button>
                    <span>
            Page {(data?.number ?? 0) + 1} / {data?.totalPages ?? 1}
          </span>
                    <button
                        className="msg-btn"
                        disabled={page + 1 >= (data?.totalPages ?? 1)}
                        onClick={() => setPage((p: number) => p + 1)}
                    >
                        Next
                    </button>
                </div>
            )}
        </div>
    );
};

export default Messages;
