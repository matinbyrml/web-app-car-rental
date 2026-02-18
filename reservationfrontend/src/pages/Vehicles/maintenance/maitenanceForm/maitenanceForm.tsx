import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast, ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import './maitenanceForm.css'
import keycloak from "../../../../keycloak"

interface MaintenanceRecord {
  id: number;
  pastDefects: string;
  completedMaintenance: string;
  upcomingServiceNeeds: string; // should be date string
  date: string; // should be date string
  vehicleId: number;
}

const MaintenanceForm: React.FC = () => {
  const params = new URLSearchParams(location.search);
  const vehicleId = params.get('id');
  const navigate = useNavigate();

  const [formData, setFormData] = useState<Omit<MaintenanceRecord, 'id' | 'vehicleId'>>({
    pastDefects: '',
    completedMaintenance: '',
    upcomingServiceNeeds: '',
    date: '',
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData((prevData) => ({
      ...prevData,
      [name]: value,
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!vehicleId) {
      toast.error('Vehicle ID is missing!');
      return;
    }

    try {
        await keycloak.updateToken(30);
    } catch (error) {
        console.error('Failed to refresh token:', error);
    }

    try {
      const response = await fetch(`/api/v1/vehicles/${vehicleId}/maintenances/`, {
        credentials: 'include',
        method: 'POST',
        headers: {
          'Authorization': 'Bearer' + "TODO", //TODO aggiungi tipo cookie
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          ...formData,
          vehicleId: parseInt(vehicleId, 10)
        }),
      });

      if (!response.ok) {
        throw new Error('Network response was not ok');
      }

      const result = await response.json();
      console.log(result);
      toast.success('Maintenance record created successfully!');
      setTimeout(() => navigate('/vehicles'), 2000);
    } catch (error) {
      console.error('Error:', error);
      toast.error('Error creating maintenance record');
    }
  };

  return (
      <div>
        <ToastContainer position="top-center" autoClose={2000} hideProgressBar pauseOnHover />

        <h1>Create Maintenance Record</h1>
        <form onSubmit={handleSubmit}>
          <div>
            <label htmlFor="date">Date:</label>
            <input
                type="datetime-local"
                id="date"
                name="date"
                value={formData.date}
                onChange={handleChange}
                required
            />
          </div>

          <div>
            <label htmlFor="pastDefects">Past Defects:</label>
            <textarea
                id="pastDefects"
                name="pastDefects"
                value={formData.pastDefects}
                onChange={handleChange}
                required
            />
          </div>

          <div>
            <label htmlFor="completedMaintenance">Completed Maintenance:</label>
            <textarea
                id="completedMaintenance"
                name="completedMaintenance"
                value={formData.completedMaintenance}
                onChange={handleChange}
                required
            />
          </div>

          <div>
            <label htmlFor="upcomingServiceNeeds">Upcoming Service Needs:</label>
            <input
                type="datetime-local" // 🛠️ fixed here
                id="upcomingServiceNeeds"
                name="upcomingServiceNeeds"
                value={formData.upcomingServiceNeeds}
                onChange={handleChange}
                required
            />
          </div>

          <button type="submit">Submit</button>
        </form>
      </div>
  );
};

export default MaintenanceForm;
