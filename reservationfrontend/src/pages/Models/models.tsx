import './models.css';
import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  FaCarSide,
  FaCalendarAlt,
  FaRoad,
  FaDoorOpen,
  FaUsers,
  FaEuroSign,
} from 'react-icons/fa';
import RoleGate from "../../auth/RoleGate.tsx";
import {Role} from "../../auth/roles.ts";

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

const Models: React.FC = () => {
  const [data, setData] = useState<CarModel[] | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 20;
  const navigate = useNavigate();

  useEffect(() => {
      fetch('/api/v1/models/', {
          credentials: 'include'
      })
        .then(res => {
          if (!res.ok) throw new Error('Network response was not ok');
          return res.json();
        })
        .then(result => {
          if (Array.isArray(result.content)) setData(result.content);
          else throw new Error('Unexpected API format');
        })
        .catch(err => setError(err))
        .finally(() => setLoading(false));
  }, []);

  const totalPages = data ? Math.ceil(data.length / itemsPerPage) : 0;
  const start = (currentPage - 1) * itemsPerPage;
  const currentItems = data?.slice(start, start + itemsPerPage) || [];

  if (loading)
    return (
        <div className="models-page">
          <div className="models-header">Loading models…</div>
        </div>
    );

  if (error)
    return (
        <div className="models-page">
          <div className="models-header error">
            <p>{error.message}</p>
            <button onClick={() => window.location.reload()}>Retry</button>
          </div>
        </div>
    );

  return (
      <div className="models-page">
        <h1 className="models-header">Our Car Models</h1>

        <div className="models-grid">
          {currentItems.map(m => (
              <div key={m.id} className="model-card">
                <h3>
                  <FaCarSide className="card-icon" />
                  {m.brand} {m.model}
                </h3>

                <ul className="model-specs">
                  <li>
                    <FaCalendarAlt /> {m.year}
                  </li>
                  <li>
                    <FaRoad /> {m.segment}
                  </li>
                  <li>
                    <FaDoorOpen /> {m.doors} doors
                  </li>
                  <li>
                    <FaUsers /> {m.seats} seats
                  </li>
                  <li>
                    <FaEuroSign /> €{m.price}/day
                  </li>
                </ul>

                <button
                    className="btn-view"
                    onClick={() => navigate(`/model-spec?id=${m.id}`)}
                >
                  View Details
                </button>
              </div>
          ))}
        </div>

        <div className="pagination-controls">
          <button
              className="pagination-btn"
              onClick={() => setCurrentPage(p => p - 1)}
              disabled={currentPage === 1}
          >
            Previous
          </button>
          <span className="page-info">
          Page {currentPage} of {totalPages}
        </span>
          <button
              className="pagination-btn"
              onClick={() => setCurrentPage(p => p + 1)}
              disabled={currentPage === totalPages}
          >
            Next
          </button>
        </div>

          <RoleGate anyOf={['ROLE_MANAGER', 'ROLE_STAFF', 'ROLE_FLEET_MANAGER' as Role]}>
              <Link to="/create-model" className="creation-button">
                  Create Car Model
              </Link>
          </RoleGate>
      </div>
  );
};

export default Models;
