# Data Ingestion Tool

A web-based tool for bidirectional data flow between ClickHouse and CSV files.

## Features

- Connect to ClickHouse using JWT authentication
- Upload and preview CSV files
- Select columns for ingestion
- Bidirectional data flow:
  - ClickHouse → CSV
  - CSV → ClickHouse
- Preview data before ingestion
- Progress tracking
- Modern web interface

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- ClickHouse server (for ClickHouse operations)
- Web browser (Chrome, Firefox, or Edge recommended)

## Installation

1. Clone the repository:
```bash
git clone <repository-url>
cd data-ingestion-tool
```

2. Build the project:
```bash
mvn clean install
```

## Running the Application

1. Start the application:
```bash
mvn spring-boot:run
```

2. Open your web browser and navigate to:
```
http://localhost:8080
```

## Usage

### Connecting to ClickHouse

1. Select "ClickHouse" as the source type
2. Enter your ClickHouse connection details:
   - Host
   - Port (default: 8123)
   - User
   - Database
   - JWT Token
3. Click "Connect" to establish the connection

### Working with CSV Files

1. Select "Flat File (CSV)" as the source type
2. Upload your CSV file
3. Specify the delimiter (default: comma)
4. Click "Preview" to see the data

### Data Ingestion

1. Configure the source (ClickHouse or CSV)
2. Configure the target (ClickHouse or CSV)
3. Select the columns you want to ingest
4. Click "Start Ingestion" to begin the process
5. Monitor the progress and wait for completion

## Configuration

The application can be configured through the following properties in `application.properties`:

```properties
# Server port
server.port=8080

# File upload settings
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# ClickHouse default settings
clickhouse.host=localhost
clickhouse.port=8123
```

## Development

### Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/zeotap/
│   │       ├── controller/
│   │       ├── model/
│   │       ├── service/
│   │       └── DataIngestionApplication.java
│   └── resources/
│       ├── static/
│       └── templates/
└── test/
```

### Adding New Features

1. Create new model classes in the `model` package
2. Add service methods in the appropriate service class
3. Create new controller endpoints
4. Update the frontend to support new features

## Testing

Run the test suite:
```bash
mvn test
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details. 