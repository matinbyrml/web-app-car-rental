import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import { AuthProvider } from './auth/AuthContext';
import ProtectedRoute from './auth/ProtectedRoute';
import Navbar from './components/Navbar/navbar.tsx';
import Home from './pages/Home/home.tsx';
import Models from './pages/Models/models.tsx';
import Vehicles from './pages/Vehicles/vehicles.tsx';
import ModelForm from './pages/Models/creation/modelsForm.tsx';
import VehicleForm from './pages/Vehicles/creation/vehiclesForm.tsx';
import VehicleMaintenance from './pages/Vehicles/maintenance/maitenance.tsx';
import MaintenanceForm from './pages/Vehicles/maintenance/maitenanceForm/maitenanceForm.tsx';
import ModelSpec from "./pages/Models/modelSpec/modelSpec.tsx";
import VehicleSpec from "./pages/Vehicles/vehiclesSpec/vehiclesSpec.tsx";
import UserManagment from './pages/User/UserManagment.tsx';
import UserForm from './pages/User/creation/userForm.tsx';
import UserInfo from './pages/User/userInfo/userInfo.tsx';
import Reservations from './pages/Reservations/reservations.tsx';
import ReservationSpec from './pages/Reservations/reservationSpec/reservationSpec.tsx';
import AvailableVehicles from "./pages/Vehicles/available/AvailableVehicles.tsx";
import PaypalReturn from "./pages/PaypalReturn.tsx";
import Messages from './pages/Messages/Messages.tsx';
import MyReservations from "./pages/Reservations/reservationSpec/myReservations.tsx";
import ReservationForm from './pages/Reservations/creation/reservationForm.tsx';
import StaffManagment from './pages/StaffManagment/staffmanagment.tsx';
import ReservationManagmentUpdate from './pages/StaffManagment/ReservationManagmentUpdate/ReservationManagmentUpdate.tsx';
import ReservationManagmentReturn from './pages/StaffManagment/ReservationManagmentReturn/ReservationManagmentReturn.tsx';
import RegisterForm from './pages/Login/loginform.tsx';

function App() {
  return (
      <AuthProvider>
        <Router>
          <Navbar />
          <Routes>
            {/* public */}
            <Route path="/" element={<Home />} />
            <Route path="model-spec" element={<ModelSpec />} />
            <Route path="/models" element={<Models />} />
            <Route path="/vehicles" element={<Vehicles />} />
            <Route path="/vehicles-spec" element={<VehicleSpec />} />
            <Route path="/paypal-return" element={<PaypalReturn />} />
            <Route path="/my-reservations" element={<ProtectedRoute anyOf={['ROLE_CUSTOMER']}><MyReservations /></ProtectedRoute>}/>
            <Route path="/available" element={<AvailableVehicles />} />
            <Route path="/messages" element={<Messages />} />
            <Route path="/user-info" element={<UserInfo />} />
            <Route path="/reservation-form" element={<ReservationForm/>} />
            <Route path="/register" element={<RegisterForm/>} />
            {/* protected: must be logged in */}
            <Route
                path="/reservations"
                element={
                  <ProtectedRoute>
                    <Reservations />
                  </ProtectedRoute>
                }
            />
            <Route
                path="/reservation-spec/:id"
                element={
                  <ProtectedRoute>
                    <ReservationSpec />
                  </ProtectedRoute>
                }
            />

            {/* RBAC: manager/staff/fleet-manager only */}
            <Route
                path="/create-model"
                element={
                  <ProtectedRoute anyOf={['ROLE_MANAGER','ROLE_STAFF','ROLE_FLEET_MANAGER']}>
                    <ModelForm />
                  </ProtectedRoute>
                }
            />
            <Route
                path="/create-vehicle"
                element={
                  <ProtectedRoute anyOf={['ROLE_MANAGER','ROLE_STAFF','ROLE_FLEET_MANAGER']}>
                    <VehicleForm />
                  </ProtectedRoute>
                }
            />
            <Route
                path="/vehicle-maintenance"
                element={
                  <ProtectedRoute anyOf={['ROLE_MANAGER','ROLE_STAFF','ROLE_FLEET_MANAGER']}>
                    <VehicleMaintenance />
                  </ProtectedRoute>
                }
            />
            <Route
                path="/maintenance-form"
                element={
                  <ProtectedRoute anyOf={['ROLE_MANAGER','ROLE_STAFF','ROLE_FLEET_MANAGER']}>
                    <MaintenanceForm />
                  </ProtectedRoute>
                }
            />

            {/* user management (example): manager-only */}
            <Route
                path="/users"
                element={
                  <ProtectedRoute anyOf={['ROLE_MANAGER']}>
                    <UserManagment />
                  </ProtectedRoute>
                }
            />
            <Route
                path="/create-user"
                element={
                  <ProtectedRoute anyOf={['ROLE_MANAGER']}>
                    <UserForm />
                  </ProtectedRoute>
                }
            />
            <Route
                path="/user-info"
                element={
                  <ProtectedRoute>
                    <UserInfo />
                  </ProtectedRoute>
                }
            />
            <Route
                path="/reservation-staff"
                element={
                  <ProtectedRoute anyOf={['ROLE_MANAGER','ROLE_STAFF','ROLE_FLEET_MANAGER']}>
                    <StaffManagment />
                  </ProtectedRoute>
                }
            />
            <Route
                path="/reservation-update/:id"
                element={
                  <ProtectedRoute anyOf={['ROLE_CUSTOMER','ROLE_MANAGER','ROLE_STAFF','ROLE_FLEET_MANAGER']}>
                    <ReservationManagmentUpdate />
                  </ProtectedRoute>
                }
            />
            <Route
                path="/reservation-return/:id"
                element={
                  <ProtectedRoute anyOf={['ROLE_MANAGER','ROLE_STAFF','ROLE_FLEET_MANAGER']}>
                    <ReservationManagmentReturn />
                  </ProtectedRoute>
                }
            />

          </Routes>
        </Router>
      </AuthProvider>
  );
}

export default App;
