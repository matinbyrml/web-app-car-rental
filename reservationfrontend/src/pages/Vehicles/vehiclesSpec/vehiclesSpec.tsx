import React, { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import './vehicleSpec.css';
import keycloak from "../../../keycloak"
import {Role} from "../../../auth/roles.ts";
import RoleGate from "../../../auth/RoleGate.tsx";

interface CarModel {
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

interface Vehicle {
  id: number;
  licensePlate: string;
  vin: string;
  availability: 'AVAILABLE' | 'RENTED' | 'MAINTENANCE';
  km: number;
  pendingCleaning: boolean;
  pendingRepair: boolean;
  maintenanceRecordHistory: any[];
  vehicleModel?: CarModel; // Ora usiamo direttamente CarModel
}


const VehicleSpec: React.FC = () => {
  const [vehicleDetails, setVehicleDetails] = useState<Vehicle | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<Error | null>(null);
  const [isEditing, setIsEditing] = useState<boolean>(false);
  const location = useLocation();
  const navigate = useNavigate();
  const params = new URLSearchParams(location.search);
  const vehicleId = params.get('id');

  useEffect(() => {
    const fetchVehicleDetails = async () => {
      if (vehicleId) {
        try {
          const response = await fetch(`/api/v1/vehicles/${vehicleId}/`, {
            credentials: 'include'
          });
          if (!response.ok) {
            throw new Error('Network response was not ok');
          }
          const result: Vehicle = await response.json();
          setVehicleDetails(result);
        } catch (error) {
          setError(error as Error);
        } finally {
          setLoading(false);
        }
      }
    };

    fetchVehicleDetails();
  }, [location, vehicleId]);

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const { name, value, type } = e.target;
    const checked = (e.target as HTMLInputElement).checked;
    
    setVehicleDetails((prev: any) => ({
      ...prev!,
      [name]: type === 'checkbox' ? checked : 
              (type === 'number' ? Number(value) : value)
    }));
  };

  const updateVehicleDetails = async () => {
    if (vehicleId && vehicleDetails) {

      try {
          await keycloak.updateToken(30);
      } catch (error) {
          console.error('Failed to refresh token:', error);
      }
      try {
        const response = await fetch(`/api/v1/vehicles/${vehicleId}/`, {
          credentials: 'include',
          method: 'PUT',
          headers: {
            'Authorization': 'Bearer' + "TODO", //TODO aggiungi tipo cookie
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(vehicleDetails),
        });

        if (!response.ok) {
          throw new Error('Failed to update vehicle details');
        }

        const updatedDetails: Vehicle = await response.json();
        setVehicleDetails(updatedDetails);
        setIsEditing(false);
        alert('Vehicle details updated successfully!');
      } catch (error) {
        setError(error as Error);
        alert('Error updating vehicle details');
      }
    }
  };

  const deleteVehicle = async () => {
    if (vehicleId) {
      if (!window.confirm('Are you sure you want to delete this vehicle?')) return;
      
      try {
          await keycloak.updateToken(30);
      } catch (error) {
          console.error('Failed to refresh token:', error);
      }

      try {
        const response = await fetch(`/api/v1/vehicles/${vehicleId}/`, {
          credentials: 'include',
          method: 'DELETE',
          headers: {
            'Authorization': 'Bearer' + "TODO", //TODO aggiungi tipo cookie
          }
        });

        if (!response.ok) {
          throw new Error('Failed to delete vehicle');
        }

        alert('Vehicle deleted successfully!');
        navigate('/vehicles');
      } catch (error) {
        setError(error as Error);
        alert('Error deleting vehicle');
      }
    }
  };

  const toggleEdit = () => {
    setIsEditing(!isEditing);
  };

  if (loading) {
    return (
      <div className='vehicle-spec'>
        <p>Loading vehicle details...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className='vehicle-spec'>
        <p>An error occurred: {error.message}</p>
        <button onClick={() => navigate('/vehicles')}>Back to Vehicles</button>
      </div>
    );
  }

  if (!vehicleDetails) {
    return (
      <div className='vehicle-spec'>
        <p>No vehicle details found</p>
        <button onClick={() => navigate('/vehicles')}>Back to Vehicles</button>
      </div>
    );
  }

  return (
    <div className="vehicle-spec">
      <header>
        <h1>Vehicle Details</h1>
        <button onClick={() => navigate('/vehicles')}>Back to Vehicles</button>
      </header>

      {isEditing ? (
        <div className="edit-form">
          <h2>Editing: {vehicleDetails.licensePlate}</h2>
          
          <div className="form-group">
            <label>License Plate:</label>
            <input
              type="text"
              name="licensePlate"
              value={vehicleDetails.licensePlate}
              onChange={handleInputChange}
            />
          </div>

          <div className="form-group">
            <label>VIN:</label>
            <input
              type="text"
              name="vin"
              value={vehicleDetails.vin}
              onChange={handleInputChange}
            />
          </div>

          <div className="form-group">
            <label>Mileage (km):</label>
            <input
              type="number"
              name="km"
              value={vehicleDetails.km}
              onChange={handleInputChange}
            />
          </div>

          <div className="form-group">
            <label>Availability:</label>
            <select
              name="availability"
              value={vehicleDetails.availability}
              onChange={handleInputChange}
            >
              <option value="AVAILABLE">Available</option>
              <option value="RENTED">Rented</option>
              <option value="MAINTENANCE">Maintenance</option>
            </select>
          </div>

          <div className="form-group">
            <label>
              <input
                type="checkbox"
                name="pendingCleaning"
                checked={vehicleDetails.pendingCleaning}
                onChange={handleInputChange}
              />
              Needs Cleaning
            </label>
          </div>

          <div className="form-group">
            <label>
              <input
                type="checkbox"
                name="pendingRepair"
                checked={vehicleDetails.pendingRepair}
                onChange={handleInputChange}
              />
              Needs Repair
            </label>
          </div>

          <div className="button-group">
            <button onClick={updateVehicleDetails}>Save Changes</button>
            <button onClick={toggleEdit}>Cancel</button>
          </div>
        </div>
      ) : (
        <div className="details-view">
          <h2>{vehicleDetails.licensePlate}</h2>
          
          <div className="detail-section">
            <h3>Basic Information</h3>
            <div className="detail-item">
              <span className="detail-label">VIN:</span>
              <span className="detail-value">{vehicleDetails.vin}</span>
            </div>
            <div className="detail-item">
              <span className="detail-label">Mileage:</span>
              <span className="detail-value">{vehicleDetails.km.toLocaleString()} km</span>
            </div>
            <div className="detail-item">
              <span className="detail-label">Status:</span>
              <span className={`status-badge ${vehicleDetails.availability.toLowerCase()}`}>
                {vehicleDetails.availability}
              </span>
            </div>
          </div>

          <div className="detail-section">
            <h3>Maintenance Status</h3>
            <div className="detail-item">
              <span className="detail-label">Cleaning:</span>
              <span className="detail-value">
                {vehicleDetails.pendingCleaning ? 'Needed' : 'Up to date'}
              </span>
            </div>
            <div className="detail-item">
              <span className="detail-label">Repairs:</span>
              <span className="detail-value">
                {vehicleDetails.pendingRepair ? 'Needed' : 'Up to date'}
              </span>
            </div>
          </div>

          {vehicleDetails.vehicleModel && (
            <div className="detail-section">
              <h3>Model Information</h3>
              <div className="detail-item">
                <span className="detail-label">Model:</span>
                <span className="detail-value">
                  {vehicleDetails.vehicleModel.brand} {vehicleDetails.vehicleModel.model}
                </span>
              </div>
              <div className="detail-item">
                <span className="detail-label">Year:</span>
                <span className="detail-value">{vehicleDetails.vehicleModel.year}</span>
              </div>
              <div className="detail-item">
                <span className="detail-label">Price:</span>
                <span className="detail-value">
                  ${vehicleDetails.vehicleModel.price.toFixed(2)} per day
                </span>
              </div>
            </div>
          )}

          <div className="button-group">
            <RoleGate anyOf={['ROLE_MANAGER', 'ROLE_STAFF', 'ROLE_FLEET_MANAGER' as Role]}>

            <button onClick={toggleEdit}>Edit Vehicle</button>
            <button onClick={deleteVehicle} className="delete-button">Delete Vehicle</button>
            {vehicleId && (
              <Link to={`/vehicle-maintenance?id=${vehicleId}`} className="notes-link">
                <button>View maintenance</button>
              </Link>
              
            )
            }
            </RoleGate>

            <RoleGate anyOf={['ROLE_CUSTOMER' as Role]}>
              <button onClick={() => navigate("/reservation-form", { state: { vehicle: vehicleDetails } })}>
                Check reservation
              </button>
            </RoleGate>

          </div>
        </div>
      )}
    </div>
  );
};

export default VehicleSpec;