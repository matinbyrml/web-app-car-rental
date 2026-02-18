import React, { useEffect, useState } from 'react';
import { useLocation, Link } from 'react-router-dom';
import './VehicleMaintenance.css'; 
import keycloak from "../../../keycloak"

interface MaintenanceRecord {
  id: number;
  pastDefects: string;
  completedMaintenance: string;
  upcomingServiceNeeds: string; //  date-time
  date: string; // date-time
  vehicleId: number;
}

// Optional: if you want to fully type the page response
interface Page<T> {
  content: T[];
  // other page fields omitted for brevity...
}

const VehicleMaintenance: React.FC = () => {
  const [maintenanceRecords, setMaintenanceRecords] = useState<MaintenanceRecord[]>([]);
  const [error, setError] = useState<Error | null>(null);
  const location = useLocation();

  const params = new URLSearchParams(location.search);
  const vehicleId = params.get('id');

  useEffect(() => {
    const fetchMaintenanceRecords = async () => {
      if (!vehicleId) return;

      try {
          const response = await fetch(
              `/api/v1/vehicles/${vehicleId}/maintenances/`,
              {
                  credentials: 'include'
              }
          );

          if (!response.ok) {
          throw new Error('Network response was not ok');
        }

        // unwrap the Spring Data Page<MaintenanceRecord>
        const page = (await response.json()) as Page<MaintenanceRecord>;
        setMaintenanceRecords(page.content);
      } catch (error) {
        setError(error as Error);
      }
    };

    fetchMaintenanceRecords();
  }, [location, vehicleId]);

  const updateMaintenanceRecord = async (recordId: number, updatedRecord: MaintenanceRecord) => {
    if (!vehicleId) {
      alert('Vehicle ID is missing');
      return;
    }

      try {
          await keycloak.updateToken(30);
      } catch (error) {
          console.error('Failed to refresh token:', error);
      }

    try {
      const response = await fetch(
          `/api/v1/vehicles/${vehicleId}/maintenances/${recordId}`,
          {
              credentials: 'include',
            method: 'PUT',
            headers: { 
               'Authorization': 'Bearer' + "TODO", //TODO aggiungi tipo cookie
              'Content-Type': 'application/json' 
            },
            body: JSON.stringify({
              ...updatedRecord,
              date: new Date(updatedRecord.date).toISOString(),
              upcomingServiceNeeds: new Date(updatedRecord.upcomingServiceNeeds).toISOString(),
            }),
          }
      );

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || 'Failed to update');
      }

      const data: MaintenanceRecord = await response.json();
      setMaintenanceRecords(records =>
          records.map(r => (r.id === recordId ? data : r))
      );
      console.log('record updated');
    } catch (error) {
      console.error('Update error:', error);
      setError(error instanceof Error ? error : new Error('error'));
    }
  };

  const deleteMaintenanceRecord = async (recordId: number) => {
    if (!vehicleId) {
      alert('Vehicle ID is missing');
      return;
    }
    if (!window.confirm('delete?')) return;

     try {
          await keycloak.updateToken(30);
      } catch (error) {
          console.error('Failed to refresh token:', error);
      }

    try {
      const response = await fetch(
          `/api/v1/vehicles/${vehicleId}/maintenances/${recordId}`,
          {
            credentials: 'include',
            method: 'DELETE',
            headers: {
             'Authorization': 'Bearer' + "TODO", //TODO aggiungi tipo cookie
            }
          }
      );

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || 'Failed to delete');
      }

      setMaintenanceRecords(records =>
          records.filter(r => r.id !== recordId)
      );
      console.log('deleted successfully');
    } catch (error) {
      console.error('Delete error:', error);
      setError(error instanceof Error ? error : new Error('error'));
    }
  };

  return (
      <div>
        <header>
          <div className='vehicleMaintenance'>Vehicle Maintenance Records</div>
        </header>

        {error && (
            <div className='vehicleMaintenance'>
              <p>An error occurred while fetching the maintenance records: {error.message}</p>
            </div>
        )}

        {maintenanceRecords.length > 0 ? (
            <div className='vehicleMaintenance'>
              {maintenanceRecords.map(record => (
                  <div key={record.id} className="maintenance-record">
                    <h3>Maintenance Record #{record.id}</h3>
                    <p><strong>Past Defects:</strong> {record.pastDefects}</p>
                    <p><strong>Completed Maintenance:</strong> {record.completedMaintenance}</p>
                    <p><strong>Date:</strong> {new Date(record.date).toLocaleDateString()}</p>
                    <p><strong>Upcoming Service:</strong> {new Date(record.upcomingServiceNeeds).toLocaleDateString()}</p>
                    <div className="record-actions">
                      <button onClick={() => updateMaintenanceRecord(record.id, record)} className="btn-update">
                        Update
                      </button>
                      <button onClick={() => deleteMaintenanceRecord(record.id)} className="btn-delete">
                        Delete
                      </button>
                    </div>
                  </div>
              ))}
            </div>
        ) : (
            <div className='vehicleMaintenance no-records'>
              <p>No maintenance records found for this vehicle.</p>
            </div>
        )}

        {vehicleId && (
            <Link to={`/maintenance-form?id=${vehicleId}`} className="creation-button">
              <button>Create Maintenance Record</button>
            </Link>
        )}
      </div>
  );
};

export default VehicleMaintenance;
