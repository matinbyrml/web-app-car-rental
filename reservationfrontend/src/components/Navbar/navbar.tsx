// src/components/Navbar/Navbar.tsx
import React, { useRef } from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../../auth/AuthContext';
import {
  FaHome,
  FaCarSide,
  FaRegUser,
  FaSignOutAlt,
  FaSignInAlt,
  FaUsers,
  FaCalendarAlt,
  FaLayerGroup
} from 'react-icons/fa';
import './navbar.css';
import {useUnreadNotifications} from "../../hooks/useUnreadNotifications.ts";
import { LuCalendarSearch } from 'react-icons/lu';

const AUTH_BASE = ''; // gateway root (same origin as API Gateway)
const CSRF_PARAM_NAME = '_csrf'; // Spring Security default for form posts

const Navbar: React.FC = () => {
  const { user, isAuthenticated } = useAuth();
  const csrf = user?.csrf ?? '';
  const logoutFormRef = useRef<HTMLFormElement>(null);

  const handleLogin = () => {
    window.location.href = `${AUTH_BASE}/serverLogin`;
  };

  // Real POST form so browser follows 302 → Keycloak → http://localhost:4173/?logout
  const handleLogout = (e: React.MouseEvent) => {
    e.preventDefault();
    logoutFormRef.current?.submit();
  };
  const { count: unread } = useUnreadNotifications(30000);

  const roles = user?.roles ?? [];
  const isStaffish = roles.some(r =>
      ['ROLE_MANAGER', 'ROLE_STAFF', 'ROLE_FLEET_MANAGER'].includes(r)
  );
    const isCustomer = !isStaffish && roles.includes('ROLE_CUSTOMER');

  return (
      <nav className="navbar">
        <div className="navbar-container">
          <div className="logo">
            <FaCarSide className="logo-icon" />
            <span>Alberio Auto</span>
          </div>

          <ul className="nav-menu">
            <li className="nav-item">
              <NavLink to="/" end className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>
                <FaHome className="icon" />
                Home
              </NavLink>
            </li>

              {isAuthenticated && (
                  <li className="nav-item">
                      <NavLink to="/available" className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>
                          <FaCarSide className="icon" />
                          Available Vehicles
                      </NavLink>
                  </li>
              )
              }

            {/* Reservations – ONLY staff/fleet/manager */}
            {isStaffish && (
                <li className="nav-item">
                  <NavLink to="/reservations" className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>
                    <FaCalendarAlt className="icon" />
                    Reservations
                  </NavLink>
                </li>
            )}

            {/* My Reservations – ONLY customers */}
            {isCustomer && (
                <li className="nav-item">
                  <NavLink to="/my-reservations" className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>
                    <FaCalendarAlt className="icon" />
                    My Reservations
                  </NavLink>
                </li>
            )}

            <li className="nav-item">
              <NavLink to="/models" className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>
                <FaLayerGroup className="icon" />
                Car Models
              </NavLink>
            </li>

              { isAuthenticated && isCustomer && (
                  <li className="nav-item">
                      <NavLink to="/messages" className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>
                          <FaUsers className="icon" />
                          Messages
                          {isAuthenticated && unread > 0 && <span className="nav-badge">{unread}</span>}
                      </NavLink>
                  </li>
              )
              }

            {/* USERS - visible only if not just a customer */}
            {user?.roles && !(user.roles.length === 1 && user.roles.includes('ROLE_CUSTOMER')) && (
                <li className="nav-item">
                  <NavLink to="/users" className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>
                    <FaUsers className="icon" />
                    Users
                  </NavLink>
                </li>
            )}

            {user?.roles && !(user.roles.length === 1 && user.roles.includes('ROLE_CUSTOMER')) && (
                <li className="nav-item">
                  <NavLink to="/reservation-staff" className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>
                    <LuCalendarSearch className="icon" />
                    Reservation Admin
                  </NavLink>
                </li>
            )}

            {/* LOGIN / LOGOUT */}
            <li className="nav-item" style={{ cursor: 'pointer' }}>
              {isAuthenticated ? (
                  <>
                    {/* Hidden POST form so Spring handles OIDC logout and redirects */}
                    <form ref={logoutFormRef} method="POST" action="/logout" style={{ display: 'none' }}>
                      {csrf && <input type="hidden" name={CSRF_PARAM_NAME} value={csrf} />}
                    </form>

                    <a onClick={handleLogout} className="nav-link">
                      <FaSignOutAlt className="icon" /> Logout
                    </a>
                  </>
              ) : (
                  <span onClick={handleLogin} className="nav-link">
                <FaSignInAlt className="icon" /> Login
              </span>
              )}
            </li>

              {!isAuthenticated && (
                <li className="nav-item">
                  <NavLink to="/register" className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>
                    <FaSignInAlt className="icon" />
                      Register
                  </NavLink>
                </li>
              )}
            

            {/* USER INFO */}
            {isAuthenticated && (
                <li className="nav-user-info">
                  <FaRegUser className="icon" /> <span>{user?.name}</span>
                </li>
            )}
          </ul>
        </div>
      </nav>
  );
};

export default Navbar;
