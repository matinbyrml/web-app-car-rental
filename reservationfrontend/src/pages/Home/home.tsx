import { FaCarSide, FaMapMarkedAlt, FaTools } from 'react-icons/fa';
import './home.css';
import {Link} from "react-router-dom";

const Home = () => {
    return (
        <div className="home-hero">
            <div className="hero-content">
                <h1>Your Journey Starts Here</h1>
                <p>Premium cars, transparent pricing, 24/7 support.</p>
                <Link to="/vehicles">
                    <button className="btn-primary">Browse Vehicles</button>
                </Link>
            </div>
            <div className="hero-image">
                <FaCarSide size={200} />
            </div>
            <div className="features">
                <div className="feature-card">
                    <FaMapMarkedAlt size={40} />
                    <h3>Wide Coverage</h3>
                    <p>Rent anywhere, anytime across Europe.</p>
                </div>
                <div className="feature-card">
                    <FaCarSide size={40} />
                    <h3>New Fleet</h3>
                    <p>Latest models, fully sanitized.</p>
                </div>
                <div className="feature-card">
                    <FaTools size={40} />
                    <h3>24/7 Maintenance</h3>
                    <p>Roadside assistance whenever you need it.</p>
                </div>
            </div>
        </div>
    );
};

export default Home;
