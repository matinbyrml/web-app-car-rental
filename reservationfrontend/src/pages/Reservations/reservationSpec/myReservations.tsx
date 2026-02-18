import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import '../reservations.css'; // reuse your styles if you have them
import keycloak from '../../../keycloak';
import { useAuth } from '../../../auth/AuthContext.tsx';

type ReservationStatus = 'PENDING' | 'PAID' | 'PICKED_UP' | 'RETURNED' | 'CANCELLED' | 'CANCELLED-PENDING-REFUND';

interface Reservation {
    id: number;
    userId: number;
    vehicleId: number;
    startDate: string; // ISO
    endDate: string;   // ISO
    totalPrice: number;
    status: ReservationStatus;
    vehicleLicensePlate?: string; // if your DTO includes it
    createdDate?: string;
}

interface Page<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    number: number; // current page index
    size: number;
}

const MyReservations: React.FC = () => {
    const [data, setData] = useState<Page<Reservation> | null>(null);
    const [loading, setLoading] = useState(true);
    const [page, setPage] = useState(0);
    const size = 10;
    const { user } = useAuth();
    const csrf: string = user?.csrf ?? '';
    const [error, setError] = useState<string | null>(null);

    const load = async (p = 0) => {
        setLoading(true);
        try {
            const res = await fetch(`/api/v1/reservations/me?page=${p}&size=${size}`, {
                credentials: 'include'
            });
            if (!res.ok) throw new Error(`HTTP ${res.status}`);
            const json = await res.json();
            setData(json);
        } catch (e) {
            console.error(e);
            setData({
                content: [],
                totalElements: 0,
                totalPages: 0,
                number: 0,
                size
            });
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => { load(page); }, [page]);

    const fmt = (iso?: string) =>
        iso ? new Date(iso).toLocaleString() : '';

    const handleCancel = async (id: number) => {
        setError(null);
        try {
            try {
                await keycloak.updateToken?.(30);
            } catch (err) {
                console.warn('Token refresh failed, proceeding with current token if any:', err);
            }
            const headers = new Headers({ 'Content-Type': 'application/json', 'X-XSRF-TOKEN': csrf });
            if (keycloak?.token) headers.set('Authorization', `Bearer ${keycloak.token}`);
            const res = await fetch(`/api/v1/reservations/${id}`, {
                credentials: 'include',
                method: 'DELETE',
                headers,
            });
            if (!res.ok) {
                const err = await res.json().catch(() => ({}));
                throw new Error(err.detail ?? res.statusText);
            }
            // reload reservations
            load(page);
        } catch (e: unknown) {
            setError(e instanceof Error ? e.message : 'An unexpected error occurred');
        }
    };

    if (loading) return (
        <div className="reservations-container">
            <header><h1>My Reservations</h1></header>
            <p>Loading…</p>
        </div>
    );

    return (
        <div className="reservations-container">
            <header><h1>My Reservations</h1></header>
            {error && <p className="error">{error}</p>}
            {data && data.content.length === 0 ? (
                <p>No reservations yet.</p>
            ) : (
                <>
                    <table>
                        <thead>
                        <tr>
                            <th>#</th>
                            <th>Vehicle</th>
                            <th>From</th>
                            <th>To</th>
                            <th>Total (€)</th>
                            <th>Status</th>
                            <th>Details</th>
                            <th>Cancel</th>
                        </tr>
                        </thead>
                        <tbody>
                        {data?.content.map(r => (
                            <tr key={r.id}>
                                <td>{r.id}</td>
                                <td>
                                    <Link to={`/vehicles-spec?id=${r.vehicleId}`} title="Open vehicle details">
                                        {r.vehicleLicensePlate}
                                    </Link>
                                </td>
                                <td>{fmt(r.startDate)}</td>
                                <td>{fmt(r.endDate)}</td>
                                <td>{r.totalPrice?.toFixed?.(2) ?? r.totalPrice}</td>
                                <td>
                                    <span className={`badge badge-${r.status.toLowerCase()}`}>{r.status}</span>
                                </td>
                                <td><Link to={`/reservation-spec/${r.id}`} state={{ from: '/my-reservations' }}>
                                    View
                                </Link></td>
                                <td>
                                    {(r.status === 'PENDING' || r.status === 'PAID') && (
                                        <button
                                            title="Cancella prenotazione"
                                            style={{ background: 'none', border: 'none', color: 'red', fontWeight: 'bold', fontSize: '1.2em', cursor: 'pointer' }}
                                            onClick={() => handleCancel(r.id)}
                                        >
                                            &#10006;
                                        </button>
                                    )}
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>

                    <div className="pagination">
                        <button disabled={page <= 0} onClick={() => setPage(p => p - 1)}>Prev</button>
                        <span>Page {page + 1} of {data?.totalPages ?? 1}</span>
                        <button
                            disabled={(data?.totalPages ?? 1) === 0 || page >= (data!.totalPages - 1)}
                            onClick={() => setPage(p => p + 1)}
                        >
                            Next
                        </button>
                    </div>
                </>
            )}
        </div>
    );
};

export default MyReservations;
