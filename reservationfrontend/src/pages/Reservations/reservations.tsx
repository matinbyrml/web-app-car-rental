import React, { useEffect, useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { toast } from 'react-toastify';
import './reservations.css';

interface Reservation {
    id: number;
    userId: number;
    vehicleId: number;
    startDate: string;
    endDate: string;
    status: string;
    vehicleLicensePlate: string;
}

const PAGE_SIZE = 10;

const Reservations: React.FC = () => {
    const [reservations, setReservations] = useState<Reservation[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(1);// dev

    const navigate = useNavigate();

    /* show toast after returning from a payment redirect */
    useEffect(() => {
        const p = new URLSearchParams(window.location.search);
        if (p.get('paid') === 'ok') toast.success('Payment completed ✅');
        if (p.get('paid') === 'ko') toast.error('Payment failed');
    }, []);

    /* fetch the current page whenever it changes */
    useEffect(() => {
        let isMounted = true;

        (async () => {
            try {
                const res = await fetch(
                    `/api/v1/reservations?size=${PAGE_SIZE}&page=${page}`,
                    { credentials: 'include' }
                );
                if (!res.ok) throw new Error('Fetch error');
                const data = await res.json();
                if (isMounted) {
                    setReservations(data.content);
                    setTotalPages(data.totalPages);
                }
            } catch (e: any) {
                if (isMounted) setError(e.message);
            } finally {
                if (isMounted) setLoading(false);
            }
        })();

        return () => {
            isMounted = false;
        };
    }, [page]);

    if (loading) return <p>Loading reservations…</p>;
    if (error) return <p>Error: {error}</p>;

    return (
        <div className="reservations-container">
            <header>
                <h1>Reservations</h1>
                <button onClick={() => navigate('/create-reservation')}>New Reservation</button>
            </header>

            <table>
                <thead>
                <tr>
                    <th>ID</th>
                    <th>User</th>
                    <th>Vehicle</th>
                    <th>From</th>
                    <th>To</th>
                    <th>Status</th>
                    <th />
                </tr>
                </thead>
                <tbody>
                {reservations.map((r) => (
                    <tr key={r.id}>
                        <td>{r.id}</td>
                        <td>
                            <Link to={`/user-info?id=${r.userId}`} title="Open user details">
                                {r.userId}
                            </Link>
                        </td>
                        <td>
                            <Link to={`/vehicles-spec?id=${r.vehicleId}`} title="Open vehicle details">
                                {r.vehicleLicensePlate}
                            </Link>
                        </td>
                        <td>{new Date(r.startDate).toLocaleDateString()}</td>
                        <td>{new Date(r.endDate).toLocaleDateString()}</td>
                        <td>{r.status}</td>
                        <td>
                            <Link to={`/reservation-spec/${r.id}`} state={{ from: '/reservations' }}>
                                Details
                            </Link>

                        </td>
                    </tr>
                ))}
                </tbody>
            </table>

            <div className="pagination">
                <button onClick={() => setPage((p) => Math.max(p - 1, 0))} disabled={page === 0}>
                    Prev
                </button>
                <span>
          Page {page + 1} of {totalPages}
        </span>
                <button
                    onClick={() => setPage((p) => Math.min(p + 1, totalPages - 1))}
                    disabled={page + 1 >= totalPages}
                >
                    Next
                </button>
            </div>
        </div>
    );
};

export default Reservations;
