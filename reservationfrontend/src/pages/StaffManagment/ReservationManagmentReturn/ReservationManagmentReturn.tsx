import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
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

interface ReturnInspectionRequestDTO {
  kmAtReturn: number;
  cleanliness: CleanlinessStatus;
  needsMaintenance: boolean;
  damages?: string[];
}

enum CleanlinessStatus {
  CLEAN = 'CLEAN',
  NEEDS_CLEANING = 'NEEDS_CLEANING',
  DIRTY = 'DIRTY'
}

const ReservationManagmentReturn: React.FC = () => {
  const [reservationDetails, setReservationDetails] = useState<Reservation | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<Error | null>(null);

  const [formData, setFormData] = useState<ReturnInspectionRequestDTO>({
  kmAtReturn: 0,
  cleanliness: CleanlinessStatus.CLEAN,
  needsMaintenance: false,
  damages: [],
  });

  const [damageInput, setDamageInput] = useState<string>('');


  const params = new URLSearchParams(location.search);
  const reservationId = params.get('id');
  const navigate = useNavigate();
  const { user } = useAuth();
  const csrf: string = user?.csrf ?? '';


  useEffect(() => {
    const fetchReservationDetails = async () => {
      if (reservationId) {
        try {
          const response = await fetch(`/api/v1/reservations/${reservationId}/return`, {
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json',
                ...(csrf ? { 'X-XSRF-TOKEN': csrf } : {}),
            },
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


  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!reservationId) return;

    try {
      setLoading(true);
      setError(null);

      const response = await fetch(`/api/v1/reservations/${reservationId}/return`, {
        method: 'PUT',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(formData),
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || 'Failed to update reservation');
      }

      const returned: Reservation = await response.json();
      if (returned) {
        navigate(`/reservation-spec/${reservationId}`);
      }
    } catch (err) {
      setError(err as Error);
    } finally {
      setLoading(false);
    }
  };


  if (loading) return <p>Loading reservation...</p>;
  if (error) return <p style={{ color: 'red' }}>Error: {error.message}</p>;


  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ) => {
    const { name, value, type } = e.target;
    const checked = type === 'checkbox' ? (e.target as HTMLInputElement).checked : false;

    setFormData((prev) => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value,
    }));
  };

  const handleAddDamage = () => {
    if (damageInput.trim() && !formData.damages?.includes(damageInput)) {
      setFormData((prev) => ({
        ...prev,
        damages: [...(prev.damages || []), damageInput],
      }));
      setDamageInput('');
    }
  };

  const handleRemoveDamage = (damage: string) => {
    setFormData((prev) => ({
      ...prev,
      damages: prev.damages?.filter((d) => d !== damage),
    }));
  };

    return (
    <div className="form-container">
        <div className="vehicle-reference">
        <p><strong>ID vehicle:</strong> {reservationDetails?.vehicleId}</p>
      </div>
    <form onSubmit={handleSubmit}>
      <div>
        <label>
          Km At Return:
          <input
            type="number"
            name="kmAtReturn"
            value={formData.kmAtReturn}
            onChange={handleChange}
            required
          />
        </label>
      </div>

      <div>
        <label>
          Cleanliness:
          <select
            name="cleanliness"
            value={formData.cleanliness}
            onChange={handleChange}
          >
            <option value={CleanlinessStatus.CLEAN}>Clean</option>
            <option value={CleanlinessStatus.NEEDS_CLEANING}>NEEDS CLEANING</option>
            <option value={CleanlinessStatus.DIRTY}>DIRTY</option>
          </select>
        </label>
      </div>

      <div>
        <label>
          <input
            type="checkbox"
            name="needsMaintenance"
            checked={formData.needsMaintenance}
            onChange={handleChange}
          />
          Needs maintenance
        </label>
      </div>

      <div>
        <label>
          Damanges (optional):
          <div>
            <input
              type="text"
              value={damageInput}
              onChange={(e) => setDamageInput(e.target.value)}
            />
            <button type="button" onClick={handleAddDamage}>
              Add
            </button>
          </div>
          <ul>
            {formData.damages?.map((damage, index) => (
              <li key={index}>
                {damage}
                <button type="button" onClick={() => handleRemoveDamage(damage)}>
                  Remove
                </button>
              </li>
            ))}
          </ul>
        </label>
      </div>

      <button type="submit">Submit</button>
    </form>
    </div>
  );

};

export default ReservationManagmentReturn;
