import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import './vehiclesForm.css';
import keycloak from "../../../keycloak"

interface CarModel {
  id: number;
  brand: string;
  model: string;
  year: number;
}

interface VehicleFormData {
  licensePlate: string;
  vin: string;
  km: number | '';
  availability: string;
  pendingCleaning: boolean;
  pendingRepair: boolean;
  vehicleModelId: number | '';
}

const VehicleForm: React.FC = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState<VehicleFormData>({
    licensePlate: '',
    vin: '',
    km: '',
    availability: 'AVAILABLE',
    pendingCleaning: false,
    pendingRepair: false,
    vehicleModelId: '',
  });

  const [models, setModels] = useState<CarModel[]>([]);
  const [loadingModels, setLoadingModels] = useState<boolean>(true);

  useEffect(() => {
    const fetchModels = async () => {
      try {
        const resp = await fetch('/api/v1/models?page=0&size=50',
            {
              credentials: 'include'
            });
        if (!resp.ok) throw new Error('Unable to load car models');
        const data = await resp.json();
        setModels(Array.isArray(data) ? data : (data.content ?? []));
      } catch (error) {
        alert('Error loading car models');
      } finally {
        setLoadingModels(false);
      }
    };
    fetchModels();
  }, []);

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ) => {
    // Generic handler for input changes
    // @ts-ignore
    const { name, value, type, checked } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: type === 'checkbox'
        ? checked
        : name === 'km'
        ? (value === '' ? '' : Number(value))
        : value,
    }));
  };

  const handleModelChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const value = e.target.value;
    setFormData(prev => ({
      ...prev,
      vehicleModelId: value === '' ? '' : Number(value)
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!formData.vehicleModelId) {
      alert('Please select a car model!');
      return;
    }
    if (formData.km === '' || Number.isNaN(formData.km)) {
      alert('Please enter the kilometers!');
      return;
    }

    try {
      const { ...vehicleData } = formData;
      vehicleData.km = Number(vehicleData.km);

       try {
          await keycloak.updateToken(30);
      } catch (error) {
          console.error('Failed to refresh token:', error);
      }

      const response = await fetch('/api/v1/vehicles/', {
        credentials: 'include',
        method: 'POST',
        headers: { 
          'Authorization': 'Bearer' + "TODO", //TODO aggiungi tipo cookie
          'Content-Type': 'application/json' },
        body: JSON.stringify(vehicleData),
      });

      if (!response.ok) {
        let errorMessage = 'Error creating vehicle.';
        try {
          const errorBody = await response.json();
          // Extract only status and detail if available
          const statusMsg = errorBody.status ? `Status: ${errorBody.status}` : '';
          const detailMsg = errorBody.detail ? `Detail: ${errorBody.detail}` : '';
          errorMessage += `\n${statusMsg}\n${detailMsg}`;

        } catch {
          errorMessage += `\n${response.statusText}`;
        }
        throw new Error(errorMessage);
      }


      alert('Vehicle created successfully!');
      navigate('/vehicles');
    } catch (error) {
      alert(error instanceof Error ? error.message : 'Error creating vehicle');
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <div className="form-group">
        <label htmlFor="licensePlate">License Plate:</label>
        <input
          type="text"
          id="licensePlate"
          name="licensePlate"
          maxLength={10}
          value={formData.licensePlate}
          onChange={handleChange}
          required
          placeholder="Enter license plate"
        />
      </div>
      <div className="form-group">
        <label htmlFor="vin">VIN:</label>
        <input
          type="text"
          id="vin"
          name="vin"
          maxLength={17}
          value={formData.vin}
          onChange={handleChange}
          required
          placeholder="Enter VIN"
        />
      </div>
      <div className="form-group">
        <label htmlFor="km">Kilometers:</label>
        <input
          type="number"
          id="km"
          name="km"
          value={formData.km}
          onChange={handleChange}
          min={0}
          required
          placeholder="Enter kilometers"
        />
      </div>
      <div className="form-group">
        <label htmlFor="availability">Availability:</label>
        <select
          id="availability"
          name="availability"
          value={formData.availability}
          onChange={handleChange}
          required
        >
          <option value="AVAILABLE">Available</option>
          <option value="RENTED">Rented</option>
          <option value="MAINTENANCE">Maintenance</option>
        </select>
      </div>
      <div className="form-group">
        <label>
          Pending Cleaning:
          <input
            type="checkbox"
            name="pendingCleaning"
            checked={formData.pendingCleaning}
            onChange={handleChange}
          />
        </label>
      </div>
      <div className="form-group">
        <label>
          Pending Repair:
          <input
            type="checkbox"
            name="pendingRepair"
            checked={formData.pendingRepair}
            onChange={handleChange}
          />
        </label>
      </div>
      <div className="form-group">
        <label htmlFor="vehicleModelId">Car Model:</label>
        {loadingModels ? (
          <span>Loading models...</span>
        ) : (
          <select
            id="vehicleModelId"
            name="vehicleModelId"
            value={formData.vehicleModelId}
            onChange={handleModelChange}
            required
          >
            <option value="">-- Select model --</option>
            {models.map((m) => (
              <option key={m.id} value={m.id}>
                {m.brand} {m.model} ({m.year})
              </option>
            ))}
          </select>
        )}
      </div>
      <button type="submit" className="submit-btn">
        Submit
      </button>
    </form>
  );
};

export default VehicleForm;