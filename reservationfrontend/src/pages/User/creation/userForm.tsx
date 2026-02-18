import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './userForm.css';
import keycloak from "../../../keycloak"

interface FormDataUser {
  username: string;
  name: string;
  surname: string;
  ssn: string;
  email: string;
  password: string;
  phone: string;
  address: string;
  dateOfBirth: string;      // YYYY-MM-DD
  role: 'CUSTOMER' | 'FLEET_MANAGER' | 'STAFF';
}

const UserForm: React.FC = () => {
  const navigate = useNavigate();

  const [formData, setFormData] = useState<FormDataUser>({
    username: '',
    name: '',
    surname: '',
    ssn: '',
    email: '',
    password: '',
    phone: '',
    address: '',
    dateOfBirth: '',
    role: 'CUSTOMER',
  });

  const handleChange = (
      e: React.ChangeEvent<
          HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement
      >
  ) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      // Inject createdDate as ISO string
      const payload = {
        ...formData,
        createdDate: new Date().toISOString(),
      };

      try {
          await keycloak.updateToken(30);
      } catch (error) {
          console.error('Failed to refresh token:', error);
      }

      const response = await fetch(
          '/api/v1/users/',
          {
            credentials: 'include',
            method: 'POST',
            headers: { 
              'Authorization': 'Bearer' + "TODO", //TODO aggiungi tipo cookie
              'Content-Type': 'application/json' 
            },
            body: JSON.stringify(payload),
          }
      );

      if (!response.ok) {
        const err = await response.text();
        throw new Error(err || 'Server error');
      }

      await response.json();
      navigate('/users');
    } catch (error) {
      console.error('Error creating user:', error);
      // you can show an alert or set an error message in state here
    }
  };

  return (
      <div className="user-form-container">
      <h1>Create User</h1>
        <form onSubmit={handleSubmit}>
          <label>
            Username
            <input
                name="username"
                value={formData.username}
                onChange={handleChange}
                required
            />
          </label>

          <label>
            Name
            <input
                name="name"
                value={formData.name}
                onChange={handleChange}
                required
            />
          </label>

          <label>
            Surname
            <input
                name="surname"
                value={formData.surname}
                onChange={handleChange}
                required
            />
          </label>

          <label>
            SSN
            <input
                name="ssn"
                value={formData.ssn}
                onChange={handleChange}
                required
            />
          </label>

          <label>
            Email
            <input
                type="email"
                name="email"
                value={formData.email}
                onChange={handleChange}
                required
            />
          </label>

          <label>
            Password
            <input
                type="password"
                name="password"
                value={formData.password}
                onChange={handleChange}
                required
            />
          </label>

          <label>
            Phone
            <input
                type="tel"
                name="phone"
                value={formData.phone}
                onChange={handleChange}
            />
          </label>

          <label>
            Address
            <input
                name="address"
                value={formData.address}
                onChange={handleChange}
            />
          </label>

          <label>
            Date of Birth
            <input
                type="date"
                name="dateOfBirth"
                value={formData.dateOfBirth}
                onChange={handleChange}
                required
            />
          </label>

          <label>
            Role
            <select
                name="role"
                value={formData.role}
                onChange={handleChange}
            >
              <option value="CUSTOMER">CUSTOMER</option>
              <option value="FLEET_MANAGER">FLEET MANAGER</option>
              <option value="STAFF">STAFF</option>
            </select>
          </label>

          <button type="submit">Create User</button>
        </form>
      </div>
  );
};

export default UserForm;
