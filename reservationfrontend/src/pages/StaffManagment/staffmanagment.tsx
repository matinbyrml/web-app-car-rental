import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import RoleGate from '../../auth/RoleGate';
import { Role } from '../../auth/roles';
import { useAuth } from '../../auth/AuthContext';

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

const StaffManagment: React.FC = () => {
  const [data, setData] = useState<Reservation[] | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<Error | null>(null);
  const [currentPage, setCurrentPage] = useState<number>(1);
  const itemsPerPage = 20;
    const navigate = useNavigate();
    const { user } = useAuth();
      const csrf: string = user?.csrf ?? '';

  useEffect(() => {
    const fetchData = async () => {
      try {
        const response = await fetch('/api/v1/reservations/', {
          credentials: 'include'
        });
        if (!response.ok) {
          throw new Error('Network response was not ok');
        }
        const result = await response.json();
        const reservations: Reservation[] = result.content;

        setData(reservations);

      } catch (error) {
        setError(error as Error);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

    const indexOfLastItem = currentPage * itemsPerPage;
    const indexOfFirstItem = indexOfLastItem - itemsPerPage;
    const currentItems = data ? data.slice(indexOfFirstItem, indexOfLastItem) : [];
    const totalPages = data ? Math.ceil(data.length / itemsPerPage) : 0;

    const handlePageChange = (pageNumber: number) => {
        setCurrentPage(pageNumber);
    };

    const getStatusBadgeClass = (status: ReservationStatus) => {
    switch (status) {
        case ReservationStatus.PENDING:
            return 'status-pending';
        case ReservationStatus.PAID:
            return 'status-paid';
        case ReservationStatus.PICKED_UP:
            return 'status-picked-up';
        case ReservationStatus.RETURNED:
            return 'status-returned';
        case ReservationStatus.CANCELLED:
            return 'status-cancelled';
        case ReservationStatus.CANCELLED_PENDING_REFUND:
            return 'status-cancelled-refund';
        default:
            return 'status-unknown';
        }
    };

    const formatDate = (dateString: string) => {
        return new Date(dateString).toLocaleDateString();
    };

    const onPickUp = async (id: number) => {
        try {
            const response = await fetch(`/api/v1/reservations/${id}/pickup`, {
            method: 'PUT',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json',
                ...(csrf ? { 'X-XSRF-TOKEN': csrf } : {}),
            },
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Failed to pick up reservation');
            }

            const updatedReservation: Reservation = await response.json();

            setData((prevData) =>
            prevData
                ? prevData.map((res) => (res.id === updatedReservation.id ? updatedReservation : res))
                : null
            );
        } catch (err) {
            console.error('Error picking up reservation:', err);
            alert('Error: ' + (err as Error).message);
        }
    };

    const onReturn = (reservationId: number) => {
        navigate(`/reservation-return?id=${reservationId}`);
    }

    const onUpdate = (reservationId: number) => {
        navigate(`/reservation-update?id=${reservationId}`);
    };


    const onCancel = async (id: number) => {
        try {
            const response = await fetch(`/api/v1/reservations/${id}`, {
            method: 'DELETE',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json',
                ...(csrf ? { 'X-XSRF-TOKEN': csrf } : {}),
            }
            });

            if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Failed to cancel reservation');
            }
            const cancelledReservation: Reservation = await response.json();
            setData((prevData) =>
            prevData
                ? prevData.map((res) => (res.id === cancelledReservation.id ? cancelledReservation : res))
                : null
            );

            alert(
            cancelledReservation.status === ReservationStatus.CANCELLED
                ? 'Reservation cancelled successfully.'
                : 'Reservation cancelled. Refund will be processed.'
            );
        } catch (err) {
            console.error('Error cancelling reservation:', err);
            alert('Error: ' + (err as Error).message);
        }
    };



    return (
    <div className="reservations-container">
      <header>
        <h1 className="reservations-title">Reservation Management</h1>
      </header>

      {loading && (
        <div className="loading-message">
          <p>Loading reservation data...</p>
        </div>
      )}

      {error && (
        <div className="error-message">
          <p>Error fetching reservation data: {error.message}</p>
          <button onClick={() => window.location.reload()}>Retry</button>
        </div>
      )}

      {data && data.length > 0 && (
        <div className="reservations-content">
          <div className="reservations-list">
            {currentItems.map((reservation) => (
              <div key={reservation.id} className="reservation-card">
                <div className="reservation-header">
                  <h3>Reservation #{reservation.id}</h3>
                  <span className={`status-badge ${getStatusBadgeClass(reservation.status!)}`}>
                    {reservation.status}
                  </span>
                </div>

                <div className="reservation-details">
                  <p><strong>Vehicle:</strong> {reservation.vehicleLicensePlate}</p>
                  <p><strong>User ID:</strong> {reservation.userId}</p>
                  <p><strong>Start Date:</strong> {formatDate(reservation.startDate)}</p>
                  <p><strong>End Date:</strong> {formatDate(reservation.endDate)}</p>
                </div>

                <div className="reservation-actions">
                  {reservation.status === ReservationStatus.PAID && (
                    <button
                      onClick={() => onPickUp(reservation.id!)}
                      className="action-btn pick-up-btn"
                    >
                      Pick Up
                    </button>
                  )}
                  
                  {reservation.status === ReservationStatus.PICKED_UP && (
                    <button
                    onClick={() => onReturn(reservation.id!)}
                      className="action-btn return-btn"
                    >
                      Return
                    </button>
                  )}

                  <button
                    onClick={() => onUpdate(reservation.id!)}
                    className="view-details-btn">
                          View Details
                    </button>

                  {(reservation.status === ReservationStatus.PENDING || 
                    reservation.status === ReservationStatus.PAID) && (
                    <button
                      onClick={() => onCancel(reservation.id!)}
                      className="action-btn cancel-btn"
                    >
                      Cancel
                    </button>
                  )}
                </div>
              </div>
            ))}
          </div>

          {totalPages > 1 && (
            <div className="pagination-controls">
              <button
                onClick={() => handlePageChange(currentPage - 1)}
                disabled={currentPage === 1}
                className="pagination-btn"
              >
                Previous
              </button>
              <span className="page-info">
                Page {currentPage} of {totalPages}
              </span>
              <button
                onClick={() => handlePageChange(currentPage + 1)}
                disabled={currentPage === totalPages}
                className="pagination-btn"
              >
                Next
              </button>
            </div>
          )}
        </div>
      )}

      {data && data.length === 0 && !loading && !error && (
        <div className="no-reservations">
          <p>No reservations found.</p>
        </div>
      )}

      <RoleGate anyOf={['ROLE_CUSTOMER' as Role]}>
        <div className="creation-button">
          <Link to="/create-reservation" className="create-reservation-link">
            <button className="create-reservation-btn">Create New Reservation</button>
          </Link>
        </div>
      </RoleGate>
    </div>
  );

}


export default StaffManagment;