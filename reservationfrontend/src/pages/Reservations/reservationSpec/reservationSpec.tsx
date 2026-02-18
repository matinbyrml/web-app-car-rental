import React, { useCallback, useEffect, useState } from 'react';
import {useLocation, useNavigate, useParams} from 'react-router-dom';
import './reservationSpec.css';
import keycloak from "../../../keycloak"
import {useAuth} from "../../../auth/AuthContext.tsx";

interface Reservation {
    id: number;
    userId: number;
    vehicleId: number;
    startDate: string;
    endDate: string;
    status: 'PENDING' | 'PICKED_UP' | 'RETURNED' | string;
    approvalUrl?: string;
}

const ReservationSpec: React.FC = () => {
    const { id } = useParams<{ id?: string }>();
    const navigate = useNavigate();
    const location = useLocation();
    const { user } = useAuth();
    const roles = user?.roles ?? [];
    const isCustomer = roles.includes('ROLE_CUSTOMER');
    const isManagerial = roles.some(r => ['ROLE_STAFF','ROLE_MANAGER','ROLE_FLEET_MANAGER','STAFF','MANAGER','FLEET_MANAGER'].includes(r));

    const [resv, setResv] = useState<Reservation | null>(null);
    const [error, setError] = useState<string | null>(null);
    const defaultBack = (!isManagerial && isCustomer) ? '/my-reservations' : '/reservations';
    const backTo = (location.state as any)?.from || defaultBack;

    /** fetch the reservation from the API */
    const fetchRes = useCallback(async () => {
        if (!id) return;
        try {
            const r = await fetch(`/api/v1/reservations/${id}`, {
                credentials: 'include'
            });
            if (!r.ok) throw new Error();
            setResv(await r.json());
        } catch {
            setError('Not found');
        }
    }, [id]);

    /* initial load */
    useEffect(() => {
        fetchRes();
    }, [fetchRes]);

    /* poll while status is PENDING */
    useEffect(() => {
        if (!resv || resv.status !== 'PENDING') return;
        const t = setInterval(fetchRes, 3000);
        return () => clearInterval(t);
    }, [resv, fetchRes]);

    /** call an action endpoint (pickup / return) */
    const callAction = async (path: string) => {
        if (!id) return;
         try {
          await keycloak.updateToken(30);
        } catch (error) {
            console.error('Failed to refresh token:', error);
        }
        try {
            const r = await fetch(`/api/v1/reservations/${id}/${path}`, {
                credentials: 'include',
                method: 'PUT',
                headers: {
                    'Authorization': 'Bearer' + "TODO", //TODO aggiungi tipo cookie
                }
            });
            if (!r.ok) throw new Error();
            setResv(await r.json());
        } catch {
            setError('Action failed');
        }
    };

    /** cancel the reservation */
    const handleCancel = async () => {
        if (!id) return;
         try {
          await keycloak.updateToken(30);
        } catch (error) {
            console.error('Failed to refresh token:', error);
        }
        await fetch(`/api/v1/reservations/${id}`,
        {
            credentials: 'include',
            method: 'DELETE',
            headers: {
            'Authorization': 'Bearer' + "TODO", //TODO aggiungi tipo cookie
          }
        });
        navigate('/reservations');
    };

    /* render states */
    if (!id) return <p>Missing reservation id</p>;
    if (error) return <p>Error: {error}</p>;
    if (!resv) return <p>Loading…</p>;

    /* main UI */
    return (
        <div className="detail-container">
            <h2>Reservation #{resv.id}</h2>
            <ul>
                <li>User ID: {resv.userId}</li>
                <li>Vehicle ID: {resv.vehicleId}</li>
                <li>From: {new Date(resv.startDate).toLocaleDateString()}</li>
                <li>To: {new Date(resv.endDate).toLocaleDateString()}</li>
                <li>Status: {resv.status}</li>
            </ul>

            <div className="actions">
                {resv.status === 'PENDING' && (
                    <>
                        {resv.approvalUrl && (
                            <a href={resv.approvalUrl} target="_blank" rel="noopener noreferrer">
                                <button>Pay now 💳</button>
                            </a>
                        )}
                        <button onClick={() => callAction('pickup')}>Pick&nbsp;Up</button>
                        <button onClick={() => callAction('return')}>Return</button>
                        <button onClick={handleCancel}>Cancel</button>
                    </>
                )}

                {resv.status === 'PICKED_UP' && (
                    <button onClick={() => callAction('return')}>Return</button>
                )}
            </div>

            <div className="actions">
                <button onClick={fetchRes}>Refresh</button>
                <button onClick={() => navigate(backTo)}>Back to list</button>
            </div>
        </div>
    );
};

export default ReservationSpec;
