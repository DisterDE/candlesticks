# Candlestick Aggregator API Documentation

This service handles real-time aggregation of financial instrument price data into candlestick charts, enabling
financial analysis and visualization. The candlestick aggregation is built to process high-frequency updates for
thousands of instruments.

## Prerequisites

Ensure Docker is installed on your system to run the service.

## Installation Instructions

**Run the Docker Compose setup:**

   ```bash
   docker-compose up
   ```

The service will then be accessible at [http://localhost:9000](http://localhost:9000).

## Interacting with the Service

You can interact with the service using tools like Postman, cURL, or Swagger-UI. The Swagger-UI is available
at [http://localhost:9000/swagger](http://localhost:9000/swagger).

### Endpoints

- `GET /candlesticks?isin=<ISIN>` - Retrieves the aggregated candlesticks for the specified ISIN.

#### Error Responses

- `400 Bad Request` - Missing or invalid `isin` query parameter.
- `404 Not Found` - No handler available for the specified `isin`.
- `500 Internal Server Error` - General server-side issue.

### WebSocket Streams

- **Quotes WebSocket (`/quotes`)**: Streams real-time price updates for instruments.
- **Instruments WebSocket (`/instruments`)**: Streams add/delete events for instruments.

## Authorization

This version does not implement authorization as the primary focus is on real-time aggregation.

---

## Design Decisions

### Data Structures

1. **Mutable List for Candlestick State**:

- **Reason**: Simplicity and performance for the limited size of `maxCandles` (default: 30).
- **Rejection of Circular Buffer**: Overhead of managing indices and reduced clarity for development.
- **Rejection of Concurrent Collections**: Not needed as data is accessed within a coroutine context.

2. **Immutable Copies for External Access**:

- Candlesticks are copied before returning them to ensure no external mutation affects internal state.

3. **Channels for Sequential Event Processing**:

- Handles updates in a non-blocking way while ensuring sequential processing of incoming data.

---

### System Architecture

1. **Real-Time WebSocket Data Ingestion**:

- Two WebSocket endpoints are handled:
    - `/quotes` for price updates.
    - `/instruments` for instrument lifecycle events (add/delete).

2. **Handler Manager**:

- Manages candlestick handlers for different ISINs.
- Ensures efficient access and lifecycle management of handlers.

3. **Candlestick Handler**:

- Maintains state for an instrument.
- Processes high-frequency updates efficiently.

4. **Scaling and Resilience**:

- Currently, the service operates as a single instance. Proposals for horizontal scaling are listed below.

---

## Proposals for Scaling and Future Enhancements

1. **Scaling to 50,000+ Instruments**:

- **Sharding by ISIN**: Partition handlers across multiple nodes.
- **Distributed Cache**: Use Redis or Memcached for state sharing between nodes.

2. **Failover Support**:

- **Handler State Persistence**: Periodically store handler states in a database for recovery.
- **Leader Election**: Use distributed algorithms (e.g., Raft) to handle failover in clustered environments.

3. **Performance Enhancements**:

- **Batch Updates**: Process multiple updates together for high-frequency instruments.
- **Event Deduplication**: Filter redundant updates at the WebSocket level.

4. **Monitoring and Metrics**:

- **Prometheus Integration**: Track the number of instruments, update rates, and processing times.
- **Alerting**: Set up alerts for system bottlenecks or failures.

---

## Development Decisions

1. **Dispatcher Choices**:

- Default dispatcher is used for computational tasks (candlestick aggregation).
- IO dispatcher handles WebSocket connections efficiently.

2. **Simplified State Management**:

- All state is localized within individual handlers to reduce shared resource contention.

3. **Error Handling**:

- Comprehensive exception handling for WebSocket reconnection, invalid data, and lifecycle management.

---