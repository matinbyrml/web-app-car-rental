// src/components/AvailableVehicles.tsx
import React, { useState, useEffect, FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';

interface CarModel {
    id: number;
    brand: string;
    model: string;
    year: number;
    // …other CarModel fields
    price: number;
}

interface MaintenanceRecord {
    id: number;
    pastDefects: string;
    completedMaintenance: string;
    upcomingServiceNeeds: string; // ISO datetime
    date: string;                 // ISO datetime
    vehicleId: number;
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
    notes: any[];
    vehicleModelId: number;
    vehicleModel?: CarModel;
}

interface Page<T> {
    content: T[];
    totalPages: number;
    totalElements: number;
    number: number;  // current page (0-indexed)
    size: number;    // page size
}

const AvailableVehicles: React.FC = () => {
    const [startDate, setStartDate] = useState('');
    const [endDate, setEndDate]     = useState('');
    const [page, setPage]           = useState(0);
    const [size]                    = useState(10);
    const [pageData, setPageData]   = useState<Page<Vehicle> | null>(null);
    const [loading, setLoading]     = useState(false);
    const [error, setError]         = useState<string | null>(null);
    const [validationError, setValidationError] = useState<string | null>(null);

    const navigate = useNavigate();

    const fetchVehicles = async () => {
        if (!startDate || !endDate) return;
        setLoading(true);
        setError(null);

        try {
            const params = new URLSearchParams({
                startDate,
                endDate,
                page: page.toString(),
                size: size.toString(),
            });
            const res = await fetch(`/api/v1/vehicles/available?${params}`, {
                credentials: 'include'
            });
            if (!res.ok) throw new Error(`HTTP ${res.status}`);
            const data: Page<Vehicle> = await res.json();

            // fetch each vehicle's model
            const withModels = await Promise.all(
                data.content.map(async (veh) => {
                    try {
                        const mRes = await fetch(
                            `/api/v1/models/${veh.vehicleModelId}`, {
                                credentials: 'include'
                            }
                        );
                        if (!mRes.ok) return veh;
                        const model: CarModel = await mRes.json();
                        return { ...veh, vehicleModel: model };
                    } catch {
                        return veh;
                    }
                })
            );

            setPageData({ ...data, content: withModels });
        } catch (e: any) {
            setError(e.message || 'Failed to load vehicles');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        // re-fetch when page changes
        fetchVehicles();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [page]);

    const onSubmit = (e: FormEvent) => {
        e.preventDefault();
        setPage(0);
        setValidationError(null);
        if (startDate && endDate && endDate < startDate) {
            setValidationError('End date cannot be earlier than start date.');
            return;
        }
        fetchVehicles();
    };

    const handleViewDetails = (id: number) => {
        navigate(`/vehicles-spec?id=${id}`);
    };

    const getModelInfo = (v: Vehicle) =>
        v.vehicleModel
            ? `${v.vehicleModel.brand} ${v.vehicleModel.model} (${v.vehicleModel.year})`
            : 'Loading…';

    const getPrice = (v: Vehicle) =>
        v.vehicleModel?.price.toFixed(2) ?? 'N/A';

    // Calcola la data odierna in formato YYYY-MM-DD
    const today = new Date().toISOString().split('T')[0];

    return (
        <div className="vehicles-container">
            <header>
                <h1 className="vehicles-title">Available Vehicles</h1>
            </header>

            <form onSubmit={onSubmit} className="flex gap-2 mb-4">
                <label>
                    From:{' '}
                    <input
                        type="date"
                        min={today}
                        value={startDate}
                        onChange={e => setStartDate(e.target.value)}
                        required
                    />
                </label>
                <label>
                    To:{' '}
                    <input
                        type="date"
                        min={startDate || today}
                        value={endDate}
                        onChange={e => setEndDate(e.target.value)}
                        required
                    />
                </label>
                <button type="submit" className="create-vehicle-btn">
                    Search
                </button>
            </form>
            {validationError && (
                <div className="error-message">
                    <p>{validationError}</p>
                </div>
            )}

            {loading && <p className="loading-message">Loading…</p>}
            {error && (
                <div className="error-message">
                    <p>Error: {error}</p>
                    <button onClick={fetchVehicles}>Retry</button>
                </div>
            )}

            {pageData && (
                <>
                    <div className="vehicles-list">
                        {pageData.content.map(v => (
                            <div key={v.id} className="vehicle-card">
                                <div className="vehicle-header">
                                    <h3>{v.licensePlate}</h3>
                                </div>

                                <div className="vehicle-details">
                                    <p><strong>Model:</strong> {getModelInfo(v)}</p>
                                    <p><strong>VIN:</strong> {v.vin}</p>
                                    <p><strong>Mileage:</strong> {v.km.toLocaleString()} km</p>
                                    <p><strong>Price:</strong> ${getPrice(v)}</p>
                                    <div className="status-indicators">
                                        {v.pendingCleaning && <span className="status cleaning">Needs Cleaning</span>}
                                        {v.pendingRepair  && <span className="status repair">Needs Repair</span>}
                                        {v.maintenanceRecordHistory.length > 0 && (
                                            <span className="status maintenance">Maintenance History</span>
                                        )}
                                    </div>
                                </div>

                                <div className="vehicle-actions">
                                    <button
                                        onClick={() => handleViewDetails(v.id)}
                                        className="view-details-btn"
                                    >
                                        View Details
                                    </button>
                                </div>
                            </div>
                        ))}
                    </div>

                    {pageData.totalPages > 1 && (
                        <div className="pagination-controls">
                            <button
                                onClick={() => setPage(p => Math.max(0, p - 1))}
                                disabled={page === 0}
                                className="pagination-btn"
                            >
                                Previous
                            </button>
                            <span className="page-info">
                Page {page + 1} of {pageData.totalPages}
              </span>
                            <button
                                onClick={() => setPage(p => Math.min(pageData.totalPages - 1, p + 1))}
                                disabled={page + 1 >= pageData.totalPages}
                                className="pagination-btn"
                            >
                                Next
                            </button>
                        </div>
                    )}
                </>
            )}
        </div>
    );
};

export default AvailableVehicles;
