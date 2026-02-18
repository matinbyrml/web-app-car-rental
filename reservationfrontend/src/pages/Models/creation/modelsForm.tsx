import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './modelsForm.css';
import keycloak from "../../../keycloak"

interface CarModelFormData {
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
  price: number | string; // Allow both number and empty string
}

const ModelForm: React.FC = () => {
  const navigate = useNavigate();

  const [formData, setFormData] = useState<CarModelFormData>({
    brand: '',
    model: '',
    year: 2022,
    segment: 'COMPACT',
    doors: 4,
    seats: 5,
    luggage: 450,
    category: '',
    engineType: 'PETROL',
    transmissionType: 'AUTOMATIC',
    drivetrain: 'FWD',
    motorDisplacement: 1600,
    airConditioning: true,
    infotainmentSystem: 'RADIO',
    safetyFeatures: '',
    price: '',
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const { name, value, type } = e.target;
    const checked = (e.target as HTMLInputElement).checked;

    setFormData(prevData => ({
      ...prevData,
      [name]: type === 'checkbox'
          ? checked
          : type === 'number'
              ? (value === '' ? '' : Number(value)) // allow empty string when typing
              : value
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const finalData = {
        ...formData,
        price: typeof formData.price === 'string' ? parseFloat(formData.price) : formData.price, // ensure price is number
      };

      try {
          await keycloak.updateToken(30);
      } catch (error) {
          console.error('Failed to refresh token:', error);
      }

      const response = await fetch('/api/v1/models/', {
        credentials: 'include',
        method: 'POST',
        headers: {
          'Authorization': 'Bearer' + "TODO", //TODO aggiungi tipo cookie
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(finalData),
      });

      if (!response.ok) {
        throw new Error('Network response was not ok');
      }

      const result = await response.json();
      console.log('Success:', result);
      navigate('/models'); // ✅ Redirect after success
    } catch (error) {
      console.error('Error:', error);
    }
  };

  return (
      <div>
        <h1>Create Car Model</h1>
        <form onSubmit={handleSubmit}>
          <div>
            <label htmlFor="brand">Brand:</label>
            <input
                type="text"
                id="brand"
                name="brand"
                value={formData.brand}
                onChange={handleChange}
            />
          </div>

          <div>
            <label htmlFor="model">Model:</label>
            <input
                type="text"
                id="model"
                name="model"
                value={formData.model}
                onChange={handleChange}
            />
          </div>

          <div>
            <label htmlFor="year">Year:</label>
            <input
                type="number"
                id="year"
                name="year"
                value={formData.year}
                onChange={handleChange}
            />
          </div>

          <div>
            <label htmlFor="segment">Segment:</label>
            <select id="segment" name="segment" value={formData.segment} onChange={handleChange}>
              <option value="COMPACT">COMPACT</option>
              <option value="SUV">SUV</option>
              <option value="MIDSIZE">MIDSIZE</option>
              <option value="FULLSIZE">FULLSIZE</option>
              <option value="ECONOMY">ECONOMY</option>
              <option value="LUXURY">LUXURY</option>
            </select>
          </div>

          <div>
            <label htmlFor="doors">Doors:</label>
            <input
                type="number"
                id="doors"
                name="doors"
                value={formData.doors}
                onChange={handleChange}
            />
          </div>

          <div>
            <label htmlFor="seats">Seats:</label>
            <input
                type="number"
                id="seats"
                name="seats"
                value={formData.seats}
                onChange={handleChange}
            />
          </div>

          <div>
            <label htmlFor="luggage">Luggage Capacity (L):</label>
            <input
                type="number"
                id="luggage"
                name="luggage"
                value={formData.luggage}
                onChange={handleChange}
            />
          </div>

          <div>
            <label htmlFor="category">Category:</label>
            <input
                type="text"
                id="category"
                name="category"
                value={formData.category}
                onChange={handleChange}
            />
          </div>

          <div>
            <label htmlFor="engineType">Engine Type:</label>
            <select id="engineType" name="engineType" value={formData.engineType} onChange={handleChange}>
              <option value="PETROL">PETROL</option>
              <option value="DIESEL">DIESEL</option>
              <option value="ELECTRIC">ELECTRIC</option>
              <option value="HYBRID">HYBRID</option>
            </select>
          </div>

          <div>
            <label htmlFor="transmissionType">Transmission Type:</label>
            <select id="transmissionType" name="transmissionType" value={formData.transmissionType} onChange={handleChange}>
              <option value="MANUAL">MANUAL</option>
              <option value="AUTOMATIC">AUTOMATIC</option>
            </select>
          </div>

          <div>
            <label htmlFor="drivetrain">Drivetrain:</label>
            <select id="drivetrain" name="drivetrain" value={formData.drivetrain} onChange={handleChange}>
              <option value="FWD">FWD</option>
              <option value="RWD">RWD</option>
              <option value="AWD">AWD</option>
            </select>
          </div>

          <div>
            <label htmlFor="motorDisplacement">Motor Displacement (cc):</label>
            <input
                type="number"
                id="motorDisplacement"
                name="motorDisplacement"
                value={formData.motorDisplacement}
                onChange={handleChange}
            />
          </div>

          <div>
            <label htmlFor="airConditioning">Air Conditioning:</label>
            <input
                type="checkbox"
                id="airConditioning"
                name="airConditioning"
                checked={formData.airConditioning}
                onChange={handleChange}
            />
          </div>

          <div>
            <label htmlFor="infotainmentSystem">Infotainment System:</label>
            <select id="infotainmentSystem" name="infotainmentSystem" value={formData.infotainmentSystem} onChange={handleChange}>
              <option value="RADIO">RADIO</option>
              <option value="USB">USB</option>
              <option value="BLUETOOTH">BLUETOOTH</option>
            </select>
          </div>

          <div>
            <label htmlFor="safetyFeatures">Safety Features (comma separated):</label>
            <input
                type="text"
                id="safetyFeatures"
                name="safetyFeatures"
                value={formData.safetyFeatures}
                onChange={handleChange}
            />
          </div>

          <div>
            <label htmlFor="price">Daily Price (€):</label>
            <input
                type="number"
                step="0.01"
                min="0"
                id="price"
                name="price"
                value={formData.price}
                onChange={handleChange}
            />
          </div>

          <button type="submit">Create Model</button>
        </form>
      </div>
  );
};

export default ModelForm;
