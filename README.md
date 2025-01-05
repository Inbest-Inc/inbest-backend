# Inbest Backend

Welcome to the backend application of *Inbest*, a web-based social platform for investors. This application serves as the core system, powering features like portfolio management, user authentication, and real-time stock tracking.

---

## About Inbest
*Inbest* is designed to create a collaborative environment where investors of all experience levels can share insights, manage their portfolios, and learn from each other. The backend system ensures the platform runs smoothly by handling user data, managing portfolios, and integrating real-time financial information.

---

## Key Features
- Manage investment portfolios with ease.
- Track real-time stock performance.
- Connect with a community of investors through secure and scalable systems.
- Provide seamless integration with third-party financial data sources.

---

## Getting Started
1. Clone the repository:
  ```bash
   git clone https://github.com/Inbest-Inc/backend.git
  ```

3. Navigate to the project directory:
  ```bash
   cd backend
  ```

5. Install dependencies:
  ```bash
   mvn clean install
  ```

7. Configure your database by updating the `.env` file. For example:
  ```bash
   POSTGRES_HOST=127.0.0.1
  POSTGRES_PORT=5432
  POSTGRES_DB=inbestDB
  POSTGRES_USER=inbest
  POSTGRES_PASSWORD=inbest
  ```

9. Start the application:
  ```bash
   mvn spring-boot:run
  ```

11. Access the application API documentation through Swagger UI:
  ```bash
   http://localhost:8080/swagger-ui.html
  ```
---

## Support
If you encounter any issues or have questions, feel free to reach out to the team or open an issue in the repository.

---

## License
This project is licensed under the CTIS License. See the `LICENSE` file for details

