import React, { useEffect, useState } from 'react';
import { useAuth } from '../../../auth/AuthContext';

interface Reservation {
  id?: number;
  vehicleId: number;
  userId: number;
  startDate: string;
  endDate: string;
  status?: ReservationStatus;
  vehicleLicensePlate?: string;
}

enum ReservationStatus {
  PENDING = 'PENDING',
  PAID = 'PAID',
  PICKED_UP = 'PICKED_UP',
  RETURNED = 'RETURNED',
  CANCELLED = 'CANCELLED',
  CANCELLED_PENDING_REFUND = 'CANCELLED_PENDING_REFUND'
}

const ReservationManagmentUpdate: React.FC = () => {
  const [reservationDetails, setReservationDetails] = useState<Reservation | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<Error | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const params = new URLSearchParams(location.search);
  const reservationId = params.get('id');
  const { user } = useAuth();
    const csrf: string = user?.csrf ?? '';

  useEffect(() => {
    const fetchReservationDetails = async () => {
      if (reservationId) {
        try {
          const response = await fetch(`/api/v1/reservations/${reservationId}/`, {
            credentials: 'include'
          });
          if (!response.ok) {
            throw new Error('Network response was not ok');
          }
          const result: Reservation = await response.json();
          setReservationDetails(result);
        } catch (error) {
          setError(error as Error);
        } finally {
          setLoading(false);
        }
      }
    };

    fetchReservationDetails();
  }, [reservationId]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (!reservationDetails) return;
    const { name, value } = e.target;
    setReservationDetails({ ...reservationDetails, [name]: value });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!reservationId || !reservationDetails) return;

    try {
      setLoading(true);
      setSuccess(null);
      setError(null);

      const response = await fetch(`/api/v1/reservations/${reservationId}`, {
        method: 'PUT',
        credentials: 'include',
        headers: {
            'Content-Type': 'application/json',
            ...(csrf ? { 'X-XSRF-TOKEN': csrf } : {}),
        },
        body: JSON.stringify(reservationDetails),
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || 'Failed to update reservation');
      }

      const updated: Reservation = await response.json();
      setReservationDetails(updated);
      setSuccess('Reservation updated successfully!');
    } catch (err) {
      setError(err as Error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <p>Loading reservation...</p>;
  if (error) return <p style={{ color: 'red' }}>Error: {error.message}</p>;

  return (
    <div className="reservation-update-container">
      <h2>Update Reservation #{reservationDetails?.id}</h2>

      {success && <p style={{ color: 'green' }}>{success}</p>}

      {reservationDetails && (
        <form onSubmit={handleSubmit} className="reservation-form">
          <div>
            <label>Vehicle ID:</label>
            <input
              type="number"
              name="vehicleId"
              value={reservationDetails.vehicleId}
              onChange={handleChange}
              required
            />
          </div>

          <div>
            <label>Start Date:</label>
            <input
              type="date"
              name="startDate"
              value={reservationDetails.startDate.split('T')[0]}
              onChange={handleChange}
              required
            />
          </div>

          <div>
            <label>End Date:</label>
            <input
              type="date"
              name="endDate"
              value={reservationDetails.endDate.split('T')[0]}
              onChange={handleChange}
              required
            />
          </div>

          <button type="submit" className="update-btn">Update Reservation</button>
        </form>
      )}
    </div>
  );
};

export default ReservationManagmentUpdate;
