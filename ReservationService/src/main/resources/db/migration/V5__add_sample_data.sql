INSERT INTO car_model (
    id, air_conditioning, doors, luggage, motor_displacement, price,
    seats, year, brand, category, drivetrain, engine_type,
    infotainment_system, model, safety_features, segment, transmission_type
) VALUES
      (1, TRUE, 4, 450, 1998, 10.00, 5, 2022, 'Toyota', 'Sedan', 'FWD', 'PETROL',
       'RADIO', 'Camry', 'ABS, Airbags, Lane Assist', 'MIDSIZE', 'AUTOMATIC'),

      (2, TRUE, 2, 180, 1499, 5.00, 4, 2023, 'Mazda', 'Coupe', 'RWD', 'PETROL',
       'RADIO', 'MX-5 Miata', 'ABS, Airbags, Traction Control', 'COMPACT', 'MANUAL'),

      (3, TRUE, 5, 520, 2993, 15.00, 5, 2021, 'BMW', 'SUV', 'AWD', 'DIESEL',
       'BLUETOOTH', 'X5', 'ABS, Airbags, Blind Spot Monitor', 'SUV', 'AUTOMATIC'),

      (4, TRUE, 5, 390, 1598, 20.00, 5, 2022, 'Hyundai', 'Hatchback', 'FWD', 'HYBRID',
       'USB', 'Ioniq', 'ABS, Airbags, Lane Keep Assist', 'COMPACT', 'AUTOMATIC'),

      (5, TRUE, 4, 480, 1999, 35.00, 5, 2023, 'Tesla', 'Sedan', 'AWD', 'ELECTRIC',
       'BLUETOOTH', 'Model 3', 'Autopilot, Airbags, ABS', 'MIDSIZE', 'AUTOMATIC');


INSERT INTO vehicle (
    id, km, pending_cleaning, pending_repair, vehicle_model_id,
    availability, license_plate, vin
) VALUES
      (1, 15200, FALSE, FALSE, 1, 'AVAILABLE', 'ABC-1234', '1HGCM82633A004352'),

      (2, 48700, TRUE, FALSE, 2, 'RENTED', 'XYZ-5678', 'JM1NB353X40214567'),

      (3, 32100, FALSE, TRUE, 3, 'MAINTENANCE', 'BMW-8901', '5UXFA13585LY12345'),

      (4, 8700, FALSE, FALSE, 4, 'AVAILABLE', 'HYU-2468', 'KMHDH41E07U123456'),

      (5, 5400, TRUE, FALSE, 5, 'RENTED', 'TES-3579', '5YJ3E1EA7LF123456');

-- V7__seed_user_notifications.sql
INSERT INTO user_notifications (user_id, type, status, title, body, created_by, created_at, is_deleted)
VALUES
    (1, 'SYSTEM', 'UNREAD', 'Welcome to the system!', 'Your account has been successfully created.', 'system', NOW(), false),
    (1, 'RESERVATION', 'UNREAD', 'Reservation Confirmed', 'Your reservation #101 has been confirmed.', 'reservation-service', NOW(), false),
    (1, 'PAYMENT', 'READ', 'Payment Received', 'We have received your payment of 120 EUR.', 'payment-service', NOW() - INTERVAL '1 day', false),
    (2, 'VEHICLE', 'UNREAD', 'Vehicle Ready', 'Your car is ready for pickup at the station.', 'fleet-manager', NOW(), false),
    (2, 'RESERVATION', 'ARCHIVED', 'Reservation Cancelled', 'Your reservation #99 has been cancelled.', 'reservation-service', NOW() - INTERVAL '5 days', false),
    (3, 'SYSTEM', 'UNREAD', 'System Update', 'We will perform maintenance on Sunday at 2 AM.', 'system', NOW(), false),
    (3, 'PAYMENT', 'UNREAD', 'Refund Issued', 'A refund of 50 EUR has been processed.', 'payment-service', NOW(), false);
