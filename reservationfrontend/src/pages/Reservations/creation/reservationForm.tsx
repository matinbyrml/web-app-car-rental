/* src/pages/Reservations/creation/ReservationForm.tsx */
import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import './reservationForm.css';
import keycloak from '../../../keycloak';
import {useAuth} from "../../../auth/AuthContext.tsx";

interface Vehicle { 
    id: number; 
    licensePlate: string; 
    pricePerDay?: number;
    vin: string;
    availability: 'AVAILABLE' | 'RENTED' | 'MAINTENANCE';
    km: number;
    pendingCleaning: boolean;
    pendingRepair: boolean;
    maintenanceRecordHistory: any[];
    vehicleModel?: any;
}

const ReservationForm: React.FC = () => {
    const location = useLocation();
    const navigate = useNavigate();
    const { user } = useAuth();
    const csrf: string = user?.csrf ?? '';
    
    /* data lists */

    const [vehicleDetails] = useState<Vehicle | null>(
        location.state?.vehicle || null
    );
    const [startDay, setStartDay] = useState('');
    const [endDay, setEndDay] = useState('');

    const [error, setError] = useState<string | null>(null);
    const [isWaitingForPayment, setIsWaitingForPayment] = useState(false);

    const vehicleId = vehicleDetails?.id

    /* helpers */
    const yearOk = (d: string) => d && d.split('-')[0].length <= 4;
    const valid = () =>
        startDay &&
        endDay &&
        new Date(endDay) > new Date(startDay) &&
        yearOk(startDay) &&
        yearOk(endDay) &&
        !!vehicleId;

    const pricePreview = () => {
        if (!vehicleDetails?.pricePerDay || !valid()) return null;
        const days =
            (new Date(endDay).getTime() - new Date(startDay).getTime()) / (1000 * 60 * 60 * 24);
        return (((days || 1) as number) * vehicleDetails.pricePerDay).toFixed(2);
    };

    /* submit */
    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!valid()) return setError('Please fix the form');

        const body = {
            // userId omitted: backend takes it from JWT
            vehicleId,
            startDate: `${startDay}T00:00`,
            endDate: `${endDay}T00:00`,
        };

        try {
            setError(null);

            try {
                await keycloak.updateToken(30);
            } catch (err) {
                console.warn('Token refresh failed, proceeding with current token if any:', err);
            }

            // Build headers without any undefined values
            const headers = new Headers({ 'Content-Type': 'application/json', 'X-XSRF-TOKEN': csrf });
            if (keycloak?.token) headers.set('Authorization', `Bearer ${keycloak.token}`);

            // 1. Create reservation (identity from JWT)
            const res = await fetch(`/api/v1/reservations?vehicleId=${vehicleId}`, {
                credentials: 'include',
                method: 'POST',
                headers, // <-- HeadersInit with no undefined
                body: JSON.stringify(body),
            });

            if (!res.ok) {
                const err = await res.json().catch(() => ({}));
                throw new Error(err.detail ?? res.statusText);
            }
            const reservation = await res.json();

            // 2. Reservation created successfully, now wait for payment processing
            setIsWaitingForPayment(true);

            // 3. Wait for approval URL from PaymentService and redirect directly
            window.location.href = await waitForApprovalUrl(reservation.id);
        } catch (e: unknown) {
            const errorMessage = e instanceof Error ? e.message : 'An unexpected error occurred';
            setError(errorMessage);
            setIsWaitingForPayment(false);
        }
    };

    /* wait for approval URL with polling */
    const waitForApprovalUrl = async (reservationId: number): Promise<string> => {
        const maxAttempts = 30; // ~30 seconds

        for (let i = 0; i < maxAttempts; i++) {
            try {
                const response = await fetch(`/api/v1/orders/approval-url/${reservationId}`, {
                    credentials: 'include',
                });

                if (response.ok) {
                    const data = await response.json();
                    if (data.approvalUrl) {
                        return data.approvalUrl;
                    }
                }

                await new Promise(resolve => setTimeout(resolve, 1000));
            } catch (error) {
                console.warn(`Attempt ${i + 1} failed:`, error);
                await new Promise(resolve => setTimeout(resolve, 1000));
            }
        }

        throw new Error('Approval URL non disponibile. Riprova più tardi.');
    };


    if (isWaitingForPayment) {
        return (
            <div className="form-container">
                <div className="payment-waiting">
                    <h2>Elaborazione in corso...</h2>
                    <p>Attendi, a breve sarai reindirizzato alla pagina di PayPal per il pagamento.</p>
                    <div className="spinner"></div>
                </div>
            </div>
        );
    }

    const username =
        (keycloak?.tokenParsed as { preferred_username?: string; name?: string } | undefined)
            ?.preferred_username ??
        (keycloak?.tokenParsed as { name?: string } | undefined)?.name ??
        'Current user';

    return (
        <div className="form-container">
            <h2>New Reservation</h2>
            <p className="info">Booking as: <strong>{username}</strong></p>

            {error && <p className="error">{error}</p>}

            <form onSubmit={handleSubmit} onChange={() => setError(null)}>
                {/* Mostra i dettagli del veicolo invece del dropdown */}
                {vehicleDetails && (
                    <div className="vehicle-details">
                        <h3>Selected Vehicle</h3>
                        <p><strong>License Plate:</strong> {vehicleDetails.licensePlate}</p>
                        <p><strong>VIN:</strong> {vehicleDetails.vin}</p>
                        <p><strong>Status:</strong> {vehicleDetails.availability}</p>
                        {vehicleDetails.pricePerDay && (
                            <p><strong>Price per day:</strong> €{vehicleDetails.pricePerDay.toFixed(2)}</p>
                        )}
                    </div>                )}

                {/* dates */}
                <label htmlFor="start-date">From:
                    <input
                        id="start-date"
                        type="date"
                        value={startDay}
                        onChange={(e) => setStartDay(e.target.value)}
                        required
                    />
                </label>
                <label htmlFor="end-date">To:
                    <input
                        id="end-date"
                        type="date"
                        value={endDay}
                        onChange={(e) => setEndDay(e.target.value)}
                        required
                    />
                </label>

                {(!yearOk(startDay) || !yearOk(endDay)) && (
                    <p className="error">Year must be ≤ 4 digits</p>
                )}

                {pricePreview() && (
                    <p className="price">Total ≈ €{pricePreview()}</p>
                )}

                {/* actions */}
                <button type="submit" disabled={!valid()}>
                    Reserve
                </button>
                <button type="button" onClick={() => navigate('/reservations')}>
                    Cancel
                </button>
            </form>
        </div>
    );
};

export default ReservationForm;
