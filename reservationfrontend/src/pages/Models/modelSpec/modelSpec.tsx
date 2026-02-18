// ModelSpec.tsx
import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import {
  FaCalendarAlt,
  FaRoad,
  FaDoorOpen,
  FaUsers,
  FaSuitcaseRolling,
  FaCogs,
  FaExchangeAlt,
  FaTachometerAlt,
  FaMusic,
  FaShieldAlt,
  FaTags,
} from 'react-icons/fa';
import './modelSpec.css';
import keycloak from "../../../keycloak"
import {useAuth} from "../../../auth/AuthContext.tsx";

interface CarModelDetails {
  id: number;
  brand: string;
  model: string;
  year: number;
  segment: string;
  doors: number;
  seats: number;
  luggage: number;
  category: string;
  engineType: string;
  transmissionType: string;
  drivetrain: string;
  motorDisplacement: number;
  airConditioning: boolean;
  infotainmentSystem: string;
  safetyFeatures: string;
  price: number;
}

const ModelSpec: React.FC = () => {
  const [modelDetails, setModelDetails] = useState<CarModelDetails | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<Error | null>(null);
  const [isEditing, setIsEditing] = useState<boolean>(false);
  const location = useLocation();
  const navigate = useNavigate();
  const { user, isAuthenticated } = useAuth();
  const roles = user?.roles ?? [];
  const isStaffish = roles.some(r =>
      ['ROLE_MANAGER', 'ROLE_STAFF', 'ROLE_FLEET_MANAGER'].includes(r)
  );

  useEffect(() => {
    const fetchModelDetails = async () => {
      const params = new URLSearchParams(location.search);
      const carModelId = params.get('id');

      if (carModelId) {
        try {
          const response = await fetch(`api/v1/models/${carModelId}`,
          {
             credentials: 'include'
          });
          if (!response.ok) {
            throw new Error('Network response was not ok');
          }
          const result: CarModelDetails = await response.json();
          setModelDetails(result);
        } catch (err) {
          setError(err as Error);
        } finally {
          setLoading(false);
        }
      }
    };

    fetchModelDetails();
  }, [location]);

  const handleInputChange = (
      e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>
  ) => {
    const { name, value, type } = e.target;
    const checked = (e.target as HTMLInputElement).checked;
    setModelDetails(prev => ({
      ...prev!,
      [name]:
          type === 'checkbox'
              ? checked
              : type === 'number'
                  ? Number(value)
                  : value,
    }));
  };

  const updateModelDetails = async () => {
    const params = new URLSearchParams(location.search);
    const carModelId = params.get('id');

    if (carModelId && modelDetails) {
      try {
         try {
          await keycloak.updateToken(30);
        } catch (error) {
            console.error('Failed to refresh token:', error);
        }

        const response = await fetch(
            `/api/v1/models/${carModelId}`,
            {
              credentials: 'include',
              method: 'PUT',
              headers: { 
                'Authorization': 'Bearer' + "TODO", //TODO aggiungi tipo cookie
                'Content-Type': 'application/json' 
              },
              body: JSON.stringify(modelDetails),
            }
        );
        if (!response.ok) throw new Error('Failed to update model details');
        const updated: CarModelDetails = await response.json();
        setModelDetails(updated);
        setIsEditing(false);
        alert('Model details updated successfully!');
      } catch (err) {
        setError(err as Error);
        alert('Error updating model details');
      }
    }
  };

  const deleteModel = async () => {
    const params = new URLSearchParams(location.search);
    const carModelId = params.get('id');
    if (!carModelId) return;

    if (!window.confirm('Are you sure you want to delete this model?')) return;
    try {
       try {
          await keycloak.updateToken(30);
      } catch (error) {
          console.error('Failed to refresh token:', error);
      }

      const response = await fetch(
          `/api/v1/models/${carModelId}`,
          {
            credentials: 'include',
            method: 'DELETE',
            headers: {
              'Authorization': 'Bearer' + "TODO", //TODO aggiungi tipo cookie
            }
          }
      );
      if (!response.ok) throw new Error('Failed to delete model');
      alert('Model deleted successfully!');
      navigate('/models');
    } catch (err) {
      setError(err as Error);
      alert('Error deleting model');
    }
  };

  const toggleEdit = () => setIsEditing(edit => !edit);

  if (loading)
    return (
        <div className="modelSpec">
          <p>Loading model details...</p>
        </div>
    );

  if (error)
    return (
        <div className="modelSpec">
          <p>An error occurred: {error.message}</p>
          <button onClick={() => navigate('/models')}>Back to Models</button>
        </div>
    );

  if (!modelDetails)
    return (
        <div className="modelSpec">
          <p>No model details found.</p>
          <button onClick={() => navigate('/models')}>Back to Models</button>
        </div>
    );

  return (
      <div className="modelSpec">
        <header>
          <h1>Car Model Details</h1>
          <button onClick={() => navigate('/models')}>Back to Models</button>
        </header>

        {isEditing ? (
            <div className="edit-form">
              <h2>
                Edit Model: {modelDetails.brand} {modelDetails.model}
              </h2>
              <div className="form-group">
                <label>Brand:</label>
                <input
                    type="text"
                    name="brand"
                    value={modelDetails.brand}
                    onChange={handleInputChange}
                />
              </div>
              <div className="form-group">
                <label>Model:</label>
                <input
                    type="text"
                    name="model"
                    value={modelDetails.model}
                    onChange={handleInputChange}
                />
              </div>
              <div className="form-group">
                <label>Year:</label>
                <input
                    type="number"
                    name="year"
                    value={modelDetails.year}
                    onChange={handleInputChange}
                />
              </div>
              <div className="form-group">
                <label>Segment:</label>
                <select
                    name="segment"
                    value={modelDetails.segment}
                    onChange={handleInputChange}
                >
                  <option value="COMPACT">COMPACT</option>
                  <option value="SUV">SUV</option>
                  <option value="SEDAN">SEDAN</option>
                  <option value="LUXURY">LUXURY</option>
                </select>
              </div>
              <div className="form-group">
                <label>Price:</label>
                <input
                    type="number"
                    step="0.01"
                    min="0"
                    name="price"
                    value={modelDetails.price}
                    onChange={handleInputChange}
                />
              </div>
              <div className="form-group">
                <label>
                  <input
                      type="checkbox"
                      name="airConditioning"
                      checked={modelDetails.airConditioning}
                      onChange={handleInputChange}
                  />
                  Air Conditioning
                </label>
              </div>
              <div className="button-group">
                <button onClick={updateModelDetails}>Save Changes</button>
                <button onClick={toggleEdit}>Cancel</button>
              </div>
            </div>
        ) : (
            <div className="details-view">
              <h2>
                {modelDetails.brand} {modelDetails.model}
              </h2>

              <div className="detail-item">
                <FaCalendarAlt className="detail-icon" />
                <span className="detail-label">Year:</span>
                <span className="detail-value">{modelDetails.year}</span>
              </div>
              <div className="detail-item">
                <FaRoad className="detail-icon" />
                <span className="detail-label">Segment:</span>
                <span className="detail-value">{modelDetails.segment}</span>
              </div>
              <div className="detail-item">
                <FaDoorOpen className="detail-icon" />
                <span className="detail-label">Doors:</span>
                <span className="detail-value">{modelDetails.doors}</span>
              </div>
              <div className="detail-item">
                <FaUsers className="detail-icon" />
                <span className="detail-label">Seats:</span>
                <span className="detail-value">{modelDetails.seats}</span>
              </div>
              <div className="detail-item">
                <FaSuitcaseRolling className="detail-icon" />
                <span className="detail-label">Luggage:</span>
                <span className="detail-value">{modelDetails.luggage}</span>
              </div>
              <div className="detail-item">
                <FaCogs className="detail-icon" />
                <span className="detail-label">Engine:</span>
                <span className="detail-value">{modelDetails.engineType}</span>
              </div>
              <div className="detail-item">
                <FaExchangeAlt className="detail-icon" />
                <span className="detail-label">Transmission:</span>
                <span className="detail-value">{modelDetails.transmissionType}</span>
              </div>
              <div className="detail-item">
                <FaTachometerAlt className="detail-icon" />
                <span className="detail-label">Drivetrain:</span>
                <span className="detail-value">{modelDetails.drivetrain}</span>
              </div>
              <div className="detail-item">
                <FaMusic className="detail-icon" />
                <span className="detail-label">Infotainment:</span>
                <span className="detail-value">{modelDetails.infotainmentSystem}</span>
              </div>
              <div className="detail-item">
                <FaShieldAlt className="detail-icon" />
                <span className="detail-label">Safety:</span>
                <span className="detail-value">{modelDetails.safetyFeatures}</span>
              </div>
              <div className="detail-item">
                <FaTags className="detail-icon" />
                <span className="detail-label">Category:</span>
                <span className="detail-value">{modelDetails.category}</span>
              </div>
              <div className="detail-item">
                <FaTags className="detail-icon" />
                <span className="detail-label">Price:</span>
                <span className="detail-value">
              €{modelDetails.price.toFixed(2)}/day
            </span>
              </div>

                {isAuthenticated && isStaffish && (
                    <div className="button-group">
                        <button onClick={toggleEdit}>Edit Model</button>
                        <button onClick={deleteModel} className="delete-button">
                            Delete Model
                        </button>
                    </div>
                )
                }
            </div>
        )}
      </div>
  );
};

export default ModelSpec;
