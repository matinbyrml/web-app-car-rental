import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import './userInfo.css'
import keycloak from "../../../keycloak"

interface UserModel { 
  id: number;
  username: string;
  name: string;
  surname: string;
  ssn: string;
  email: string;
  password: string;
  phone: string;
  address: string;
  date: string;
  role: 'CUSTOMER' | 'FLEET_MANAGER' | 'STAFF' | 'MANAGER';
  createdDate: string;
}

const UserInfo: React.FC = () => {
  const [userDetails, setuserDetails] = useState<UserModel | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<Error | null>(null);
  const [isEditing, setIsEditing] = useState<boolean>(false);
  const location = useLocation();
  const navigate = useNavigate();

  useEffect(() => {
    const fetchuserDetails = async () => {
      const params = new URLSearchParams(location.search);
      const userId = params.get('id');

      if (userId) {
        try {
          const response = await fetch(`/api/v1/users/details/${userId}`, {
            credentials: 'include'
          });
          if (!response.ok) {
            throw new Error('Network response was not ok');
          }
          const result: UserModel = await response.json();
          setuserDetails(result);
        } catch (error) {
          setError(error as Error);
        } finally {
          setLoading(false);
        }
      }
    };

    fetchuserDetails();
  }, [location]);

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const { name, value, type } = e.target;
    const checked = (e.target as HTMLInputElement).checked;
    
    setuserDetails(prev => ({
      ...prev!,
      [name]: type === 'checkbox' ? checked : 
              (type === 'number' ? Number(value) : value)
    }));
  };

  const updateuserDetails = async () => {
    const params = new URLSearchParams(location.search);
    const userId = params.get('id');

    if (userId && userDetails) {
       try {
          await keycloak.updateToken(30);
      } catch (error) {
          console.error('Failed to refresh token:', error);
      }

      try {
        const response = await fetch(`/api/v1/users/${userId}`, {
          credentials: 'include',
          method: 'PUT',
          headers: {
            'Authorization': 'Bearer' + "TODO", //TODO aggiungi tipo cookie
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(userDetails),
        });

        if (!response.ok) {
          throw new Error('Failed to update user details');
        }

        const updatedDetails: UserModel = await response.json();
        setuserDetails(updatedDetails);
        setIsEditing(false);
        alert('Usr details updated successfully!');
      } catch (error) {
        setError(error as Error);
        alert('Error updating user details');
      }
    }
  };

  const deleteuser = async () => {
    const params = new URLSearchParams(location.search);
    const userId = params.get('id');

    if (userId) {
      if (!window.confirm('Are you sure you want to delete this user?')) return;
      
       try {
          await keycloak.updateToken(30);
      } catch (error) {
          console.error('Failed to refresh token:', error);
      }

      try {
        const response = await fetch(`/api/v1/users/${userId}`, {
          credentials: 'include',
          method: 'DELETE',
          headers: {
            'Authorization': 'Bearer' + "TODO", //TODO aggiungi tipo cookie
          }
        });

        if (!response.ok) {
          throw new Error('Failed to delete user');
        }

        alert('User deleted successfully!');
        navigate('/users');
      } catch (error) {
        setError(error as Error);
        alert('Error deleting user');
      }
    }
  };

  const toggleEdit = () => {
    setIsEditing(!isEditing);
  };

  if (loading) {
    return (
      <div className='userInfo'>
        <p>Loading user details...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className='userInfo'>
        <p>An error occurred while fetching the user details: {error.message}</p>
        <button onClick={() => navigate('/users')}>Back to users</button>
      </div>
    );
  }

  if (!userDetails) {
    return (
      <div className='userInfo'>
        <p>No user details found</p>
        <button onClick={() => navigate('/users')}>Back to users</button>
      </div>
    );
  }

  return (
    <div className="userInfo">
      <header>
        <h1>User Details</h1>
        <button onClick={() => navigate('/users')}>Back to users</button>
      </header>

      {isEditing ? (
        <div className="edit-form">
        <h2>Edit user: {userDetails.username}</h2>
      
        <div className="form-group">
          <label>Username:</label>
          <input
            type="text"
            name="username"
            value={userDetails.username}
            onChange={handleInputChange}
          />
        </div>
      
        <div className="form-group">
          <label>Name:</label>
          <input
            type="text"
            name="name"
            value={userDetails.name}
            onChange={handleInputChange}
          />
        </div>
      
        <div className="form-group">
          <label>Surname:</label>
          <input
            type="text"
            name="surname"
            value={userDetails.surname}
            onChange={handleInputChange}
          />
        </div>

        <div className="form-group">
          <label>SSN:</label>
          <input
            type="text"
            name="ssn"
            value={userDetails.ssn}
            onChange={handleInputChange}
          />
        </div>
      
        <div className="form-group">
          <label>Email:</label>
          <input
            type="email"
            name="email"
            value={userDetails.email}
            onChange={handleInputChange}
          />
        </div>
      
        <div className="form-group">
          <label>Password:</label>
          <input
            type="password"
            name="password"
            value={userDetails.password}
            onChange={handleInputChange}
          />
        </div>
      
        <div className="form-group">
          <label>Phone:</label>
          <input
            type="tel"
            name="phone"
            value={userDetails.phone}
            onChange={handleInputChange}
          />
        </div>
      
        <div className="form-group">
          <label>Address:</label>
          <input
            type="text"
            name="address"
            value={userDetails.address}
            onChange={handleInputChange}
          />
        </div>
      
        <div className="form-group">
          <label>Date:</label>
          <input
            type="date"
            name="date"
            value={new Date(userDetails.date).toLocaleDateString()}
            onChange={handleInputChange}
          />
        </div>
      
        <div className="form-group">
          <label>Role:</label>
          <select
            name="role"
            value={userDetails.role}
            onChange={handleInputChange}
          >
            <option value="CUSTOMER">CUSTOMER</option>
            <option value="FLEET_MANAGER">FLEET_MANAGER</option>
            <option value="STAFF">STAFF</option>
          </select>
        </div>
      
        <div className="form-group">
          <label>Created Date:</label>
          <input
            type="datetime-local"
            name="createdDate"
            value={new Date(userDetails.createdDate).toLocaleDateString()}
            onChange={handleInputChange}
          />
        </div>

        <div className="creation-button">
            <button onClick={updateuserDetails}>Save Changes</button>
            <button onClick={toggleEdit}>Cancel</button>
          </div>
      </div>      
      ) : (
        <div className="details-view">
        <h2>{userDetails.username}</h2>

        <div className="detail-item">
          <span className="detail-label">Name:</span>
          <span className="detail-value">{userDetails.name}</span>
        </div>

        <div className="detail-item">
          <span className="detail-label">Surname:</span>
          <span className="detail-value">{userDetails.surname}</span>
        </div>

        <div className="detail-item">
          <span className="detail-label">SSN:</span>
          <span className="detail-value">{userDetails.ssn}</span>
        </div>

        <div className="detail-item">
          <span className="detail-label">Email:</span>
          <span className="detail-value">{userDetails.email}</span>
        </div>

        <div className="detail-item">
          <span className="detail-label">Phone:</span>
          <span className="detail-value">{userDetails.phone}</span>
        </div>

        <div className="detail-item">
          <span className="detail-label">Address:</span>
          <span className="detail-value">{userDetails.address}</span>
        </div>

        <div className="detail-item">
          <span className="detail-label">Date of Birth:</span>
          <span className="detail-value">{new Date(userDetails.date).toLocaleDateString()}</span>
        </div>

        <div className="detail-item">
          <span className="detail-label">Role:</span>
          <span className="detail-value">{userDetails.role}</span>
        </div>

        <div className="detail-item">
          <span className="detail-label">Created Date:</span>
          <span className="detail-value">{new Date(userDetails.createdDate).toLocaleDateString()}</span>
        </div>

        <div className="creation-button">
          <button onClick={toggleEdit}>Edit User</button>
          <button onClick={deleteuser} className="delete-button">Delete User</button>
        </div>
      </div>

      )}
    </div>
  );
};

export default UserInfo;