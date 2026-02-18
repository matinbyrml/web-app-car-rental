import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {Role} from "../../auth/roles.ts";
import RoleGate from "../../auth/RoleGate.tsx";
import {useAuth} from "../../auth/AuthContext.tsx";


interface UserFormData  {
  username: string,
  name: string,
  surname: string,
  ssn: string,
  email: string,
  password: string,
  phone: string,
  address: string,
  dateOfBirth: string,
  role: 'CUSTOMER'| 'FLEET_MANAGER' |  'STAFF'
};

const RegisterForm: React.FC = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState<UserFormData>({
    username: '',
    name: '',
    surname: '',
    ssn: '',
    email: '',
    password: '',
    phone: '',
    address: '',
    dateOfBirth: '',
    role: 'CUSTOMER'
  });
  const { user } = useAuth();
  const csrf: string = user?.csrf ?? '';

const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
  const { name, value } = e.target;
  setFormData(prev => ({
    ...prev,
    [name as keyof UserFormData]: value,
  }));
};

const handleSubmit = async (e: React.FormEvent) => {
  e.preventDefault();

  const requiredFields: (keyof UserFormData)[] = [
    'username', 'name', 'surname', 'ssn', 'email', 
    'password', 'dateOfBirth', 'role'
  ];

  const missingFields = requiredFields.filter(field => !formData[field]);
  
  if (missingFields.length > 0) {
    alert(`Missing fields: ${missingFields.join(', ')}`);
    return;
  }

  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(formData.email)) {
    alert('Incorrect mail');
    return;
  }

  if (formData.phone.length > 9 && formData.phone.length <= 12) {
    alert('Incorrect phone number');
    return;
  }

  try {
    const dateTimestamp = formData.dateOfBirth ? new Date(formData.dateOfBirth).toLocaleDateString("sv-SE") : '';

    const userData = {
      ...formData,
      dateOfBirth: dateTimestamp
    };

    const response = await fetch('/api/v1/users/', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
          'X-XSRF-TOKEN': csrf
      },
      body: JSON.stringify(userData),
    });

    if (!response.ok) {
      let errorMessage = 'Error creating user.';
      try {
        const errorBody = await response.json();
        const statusMsg = errorBody.status ? `Status: ${errorBody.status}` : '';
        const detailMsg = errorBody.detail ? `Detail: ${errorBody.detail}` : '';
        errorMessage += `\n${statusMsg}\n${detailMsg}`;
      } catch {
        errorMessage += `\n${response.statusText}`;
      }
      throw new Error(errorMessage);
    }

    alert('User created successfully!');
    navigate('/login')
  } catch (error) {
    alert(error instanceof Error ? error.message : 'Error creating user');
  }
};


  return (
    <form onSubmit={handleSubmit}>
      <div className="form-group">
        <label htmlFor="username">Username:</label>
        <input
          type="text"
          id="username"
          name="username"
          value={formData.username}
          onChange={handleChange}
          required
          placeholder="Enter username"
        />
      </div>
      <div className="form-group">
        <label htmlFor="name">Name:</label>
        <input
          type="text"
          id="name"
          name="name"
          value={formData.name}
          onChange={handleChange}
          required
          placeholder="Enter name"
        />
      </div>
      <div className="form-group">
        <label htmlFor="surname">Surname:</label>
        <input
          type="text"
          id="surname"
          name="surname"
          value={formData.surname}
          onChange={handleChange}
          required
          placeholder="Enter surname"
        />
      </div>
      <div className="form-group">
        <label htmlFor="ssn">SSN:</label>
        <input
          type="text"
          id="ssn"
          name="ssn"
          value={formData.ssn}
          onChange={handleChange}
          required
          placeholder="Enter SSN"
        />
      </div>
      <div className="form-group">
        <label htmlFor="email">Email:</label>
        <input
          type="email"
          id="email"
          name="email"
          value={formData.email}
          onChange={handleChange}
          required
          placeholder="Enter email"
        />
      </div>
      <div className="form-group">
        <label htmlFor="password">Password:</label>
        <input
          type="password"
          id="password"
          name="password"
          value={formData.password}
          onChange={handleChange}
          required
          placeholder="Enter password"
        />
      </div>
      <div className="form-group">
        <label htmlFor="phone">Phone:</label>
        <input
          type="tel"
          id="phone"
          name="phone"
          value={formData.phone || ''}
          onChange={handleChange}
          placeholder="Enter phone number (optional)"
        />
      </div>
      <div className="form-group">
        <label htmlFor="address">Address:</label>
        <input
          type="text"
          id="address"
          name="address"
          value={formData.address || ''}
          onChange={handleChange}
          placeholder="Enter address (optional)"
        />
      </div>
      <div className="form-group">
        <label htmlFor="dateOfBirth">Date of Birth:</label>
        <input
          type="date"
          id="dateOfBirth"
          name="dateOfBirth"
          value={formData.dateOfBirth || ""} 
          onChange={handleChange}
        />
      </div>
      <div className="form-group">
        
        <RoleGate anyOf={['ROLE_MANAGER', 'ROLE_STAFF', 'ROLE_FLEET_MANAGER' as Role]}>
          <label htmlFor="role">Role:</label>

          <select
            id="role"
            name="role"
            value={formData.role}
            onChange={handleChange}
            required
          >
            <RoleGate anyOf={['ROLE_MANAGER', 'ROLE_FLEET_MANAGER' as Role]}>
              <option value="CUSTOMER">User</option>
              <option value="FLEET_MANAGER">Manager</option>
            </RoleGate>
            
            <RoleGate anyOf={['ROLE_STAFF' as Role]}>
              <option value="CUSTOMER">User</option>
              <option value="FLEET_MANAGER">Manager</option>
              <option value="STAFF">Staff</option>
            </RoleGate>
          </select>
        </RoleGate>
        
        <RoleGate anyOf={['ROLE_CUSTOMER' as Role]}>
          <input
            type="hidden"
            name="role"
            value="CUSTOMER"
            onChange={handleChange}
          />
          <div className="readonly-field">User (CUSTOMER)</div>
        </RoleGate>
      </div>
      <button type="submit" className="submit-btn">
        Submit
      </button>
    </form>
  );
};

export default RegisterForm;