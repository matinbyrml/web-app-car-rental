import './vehicles.css';
import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {Role} from "../../auth/roles.ts";
import RoleGate from "../../auth/RoleGate.tsx";

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

interface MaintenanceRecord {
  id: number;
  date: string;
  description: string;
  cost: number;
}

interface Vehicle {
  id: number;
  licensePlate: string;
  vin: string;
  availability: 'AVAILABLE' | 'RENTED' | 'MAINTENANCE';
  km: number;
  pendingCleaning: boolean;
  pendingRepair: boolean;
  maintenanceRecordHistory: MaintenanceRecord[];
  vehicleModelId: number;     // <-- use vehicleModelId from API
  vehicleModel?: CarModel;    // <-- load separately
}

const Vehicles: React.FC = () => {
  const [data, setData] = useState<Vehicle[] | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<Error | null>(null);
  const [currentPage, setCurrentPage] = useState<number>(1);
  const itemsPerPage = 20;
  const navigate = useNavigate();

  useEffect(() => {
    const fetchData = async () => {
      try {
        const response = await fetch('/api/v1/vehicles/', {
          credentials: 'include'
        });
        if (!response.ok) {
          throw new Error('Network response was not ok');
        }
        const result = await response.json();
        const vehicles: Vehicle[] = result.content;

        // Now for each vehicle, fetch its CarModel
        const vehiclesWithModels = await Promise.all(vehicles.map(async (vehicle) => {
          try {
            const modelResponse = await fetch(`/api/v1/models/${vehicle.vehicleModelId}/`, {
              credentials: 'include'
            });
            if (modelResponse.ok) {
              const modelData: CarModel = await modelResponse.json();
              return { ...vehicle, vehicleModel: modelData };
            } else {
              // If model fetch fails, return vehicle without model
              return { ...vehicle };
            }
            // eslint-disable-next-line @typescript-eslint/no-unused-vars
          } catch (error) {
            console.error('Failed to fetch car model for vehicle', vehicle.id);
            return { ...vehicle };
          }
        }));

        setData(vehiclesWithModels);
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

  const handleViewDetails = (vehicleId: number) => {
    navigate(`/vehicles-spec?id=${vehicleId}`);
  };

  const getVehicleModelInfo = (vehicle: Vehicle) => {
    if (!vehicle.vehicleModel) {
      return 'No model information';
    }
    return `${vehicle.vehicleModel.brand} ${vehicle.vehicleModel.model} (${vehicle.vehicleModel.year})`;
  };

  const getVehiclePrice = (vehicle: Vehicle) => {
    return vehicle.vehicleModel?.price?.toFixed(2) || 'N/A';
  };

  return (
      <div className="vehicles-container">
        <header>
          <h1 className="vehicles-title">Vehicle Fleet Management</h1>
        </header>

        {loading && (
            <div className="loading-message">
              <p>Loading vehicle data...</p>
            </div>
        )}

        {error && (
            <div className="error-message">
              <p>Error fetching vehicle data: {error.message}</p>
              <button onClick={() => window.location.reload()}>Retry</button>
            </div>
        )}

        {data && (
            <div className="vehicles-content">
              <div className="vehicles-list">
                {currentItems.map((vehicle) => (
                    <div key={vehicle.id} className="vehicle-card">
                      <div className="vehicle-header">
                        <h3>{vehicle.licensePlate}</h3>
                      </div>

                      <div className="vehicle-details">
                        <p><strong>Model:</strong> {getVehicleModelInfo(vehicle)}</p>
                        <p><strong>VIN:</strong> {vehicle.vin}</p>
                        <p><strong>Mileage:</strong> {vehicle.km.toLocaleString()} km</p>
                        <p><strong>Daily Price:</strong> ${getVehiclePrice(vehicle)}</p>
                      </div>

                      <div className="vehicle-actions">
                        <button
                            onClick={() => handleViewDetails(vehicle.id)}
                            className="view-details-btn"
                        >
                          View Details
                        </button>
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

        <RoleGate anyOf={['ROLE_MANAGER', 'ROLE_STAFF', 'ROLE_FLEET_MANAGER' as Role]}>
        <div className="creation-button">
          <Link to="/create-vehicle" className="create-vehicle-link">
            <button className="create-vehicle-btn">Add New Vehicle</button>
          </Link>
         </div>
        </RoleGate>

      </div>

);
};

export default Vehicles;
