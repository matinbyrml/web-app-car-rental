import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import './UserManagement.css'

interface UserModel { 
  id: number;
  username: string;
  name: string;
  surname: number;
  ssn: string;
  email: string;
  password: string;
  phone: string;
  address: string;
  date: string;
  role: 'CUSTOMER' | 'FLEET_MANAGER' | 'STAFF';
  createdDate: string;
}

const UserManagment: React.FC = () => {
  const [data, setData] = useState<UserModel[] | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<Error | null>(null);
  const [currentPage, setCurrentPage] = useState<number>(1);
  const itemsPerPage = 20;
  const navigate = useNavigate();
  useEffect(() => {
    const fetchData = async () => {
      try {
        const response = await fetch('/api/v1/users/', {
          credentials: 'include'
        });
        if (response.status === 403) {
          throw new Error('You do not have permission to view this page.');
        }

        if (!response.ok) {
          throw new Error('Network response was not ok');
        }
        const result = await response.json();
        if (Array.isArray(result.content)) {
          setData(result.content); // <- store only the list inside content
        } else {
          throw new Error('API did not return a list of users.');
        }
      } catch (error) {
        setError(error as Error);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  const indexOfLastItem = currentPage * itemsPerPage;
  const indexOfFirstItem = indexOfLastItem - itemsPerPage;
  const currentItems = data ? data.slice(indexOfFirstItem, indexOfLastItem) : [];

  const totalPages = data ? Math.ceil(data.length / itemsPerPage) : 0;

  const handlePageChange = (pageNumber: number) => {
    setCurrentPage(pageNumber);
  };

  const handleViewDetails = (userId: number) => {
    navigate(`/user-info?id=${userId}/`);
  };

  return (
    <div>
      <header>
        <div className='userManagment'>Welcome to the User Page</div>
      </header>

      {loading && (
        <div className='userManagment'>
          <p>Please wait while we fetch the data.</p>
        </div>
      )}

      {error && (
        <div className='userManagment'>
          <p>An error occurred while fetching the data: {error.message}</p>
          <button onClick={() => window.location.reload()}>Retry</button>
        </div>
      )}

      {data && (
        <div className='userManagment'>
          <p>Here are the results fetched from the API:</p>
          <div>
          {currentItems.map((item) => (
            <div key={item.id} className="user-card">
              <h3>{item.username}</h3>
              <p><strong>Name:</strong> {item.name}</p>
              <p><strong>Surname:</strong> {item.surname}</p>
              <p><strong>SSN:</strong> {item.ssn}</p>
              <p><strong>Email:</strong> {item.email}</p>
              <p><strong>Password:</strong> {item.password}</p>
              <p><strong>Phone:</strong> {item.phone}</p>
              <p><strong>Address:</strong> {item.address}</p>
              <p><strong>Date:</strong> {new Date(item.date).toLocaleDateString()}</p>
              <p><strong>Role:</strong> {item.role}</p>
              <p><strong>Created Date:</strong> {new Date(item.createdDate).toLocaleDateString()}</p>
              <button onClick={() => handleViewDetails(item.id)}>
                View Details
              </button>
            </div>
          ))}

          </div>
          <div className='pagination'>
            <button
              onClick={() => handlePageChange(currentPage - 1)}
              disabled={currentPage === 1}
            >
              Previous
            </button>
            <span>
              Page {currentPage} of {totalPages}
            </span>
            <button
              onClick={() => handlePageChange(currentPage + 1)}
              disabled={currentPage === totalPages}
            >
              Next
            </button>
          </div>
        </div>
      )}

      <Link to="/create-user" className="creation-button">
        <button>Create user</button>
      </Link>
    </div>
  );
};

export default UserManagment;
