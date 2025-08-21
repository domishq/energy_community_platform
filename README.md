# Energy Community Smartwatch Prototype

<img width="437" height="437" alt="image" src="https://github.com/user-attachments/assets/f9954c53-61cd-4640-9b3a-3f02b2b77aeb" /> <img width="235" height="489" alt="image" src="https://github.com/user-attachments/assets/6376ffaa-8da7-4e97-a866-0d9c777a48e1" />

This project explores the concept of **energy communities** through a minimal, energy-efficient smartwatch application. It demonstrates how complex energy infrastructure and data pipelines can be visualized and interacted with via a simple wearable interface.

---

## Project Overview

This prototype implements a complete **data flow from energy sources to a smartwatch**, allowing users to monitor net energy consumption in real time.  

Key features include:  
- **Caching:** Redis for fast access to the latest energy readings.  
- **Data aggregation and messaging:** NATS JetStream topics handle messages from multiple energy communities(even though mobile/smartwatch app currently is only subscribed to 'community1').  
- **Smartwatch communication:** The mobile app gets the data via SSE, and sends it to a smartwatch via the Data Layer API, ensuring low-latency updates.

---

## Personal Goals

- **Learn about energy communities:** From basic principles to full operational realization, understanding how energy data flows through distributed systems.  
- **Improve IoT data collection and processing skills:** Hands-on experience with energy sensors, messaging systems, and real-time aggregation pipelines.  
- **Master Docker:** Write and manage custom `Dockerfile`s and `docker-compose.yml` to simulate complex IoT infrastructures locally.

---

## Primary Project Goals

- **Fast data retrieval:** Ensure minimal delay from energy measurement to smartwatch display.  
- **Energy-efficient smartwatch app:** Minimize the smartwatch’s power consumption while maintaining responsive UI updates.
- **Infrastructure scalability** 
- **Elegant and minimal implementation:** Present complex infrastructure in a clear, intuitive, and maintainable way.

---

## Tech Stack

### Current Prototype
- **Messaging & Data Collection:** NATS JetStream with pub/sub topics for each energy community.  
- **Aggregator / Listener:** Python script subscribing to JetStream topics, updating:  
  - **Redis cache:** Stores latest values for fast retrieval.  
  - **NATS Key-Value store:** Persistent storage for community-specific metrics.  
- **Backend API:** Python + FastAPI exposing:
  - Current data via REST endpoints.  
  - Test POST endpoints to mock energy readings for easy development and testing.
- **Mobile App:** Flutter app gets the data by SSE and forwards it over Data Layer API to the Smartwatch App
- **Smartwatch App:** Kotlin app gets updates from smartphone via Data Layer API

### Production Vision
In a production environment, the architecture is designed for **scalability, reliability, and analytics-ready pipelines**:

1. **Smart meters / energy sensors** send readings to **Kafka topics**.  
2. **Kafka Streams** process the data:
   - Aggregate values across multiple sensors and communities.  
   - Cache frequently accessed metrics for fast consumption.  
   - Store historical or detailed data in **OLAP databases** for analytics.  
3. **Backend API** exposes the data:
   - **Server-Sent Events (SSE)** provide real-time updates to subscribed clients.  
   - REST endpoints for historical or aggregated data queries.
   - Better suitable technology for reliability and speed (for example Node.js based backend)  
4. **Smartwatch app** communicates via the **Wear OS Data Layer API**, receiving updates in near real time while keeping battery usage minimal.

---

## Current Prototype Data Flow

1. **Energy data publication:** Simulated or real energy readings are published to NATS JetStream topics per community.  
2. **Aggregator listener:** Python service subscribes to these topics:  
   - Updates **Redis** cache with the latest reading.  
   - Stores key data in **NATS KV** for persistence.  
3. **Backend API exposure:** FastAPI provides:  
   - **GET endpoints** for current readings.  
   - **POST endpoints** for mock data injection.  
4. **Smartwatch integration:** The WearOS app subscribes to backend updates via the **Data Layer API**, presenting them as a circular indicator for intuitive monitoring.  

This setup allows fast feedback loops for testing, debugging, and development while mimicking the eventual production data flow.

---

## Summary

This prototype demonstrates how to connect **energy communities**, backend processing, and smartwatch display in an **energy-efficient, real-time system**.  

While the prototype uses NATS JetStream and Python for simplicity, the architecture is fully compatible with **production-ready technologies** like Kafka, Kafka Streams, Node.js, and OLAP databases—designed to scale while maintaining performance and efficiency.  

By combining **IoT data collection, real-time aggregation, caching, and wearable UI**, this project provides a hands-on example of how modern energy infrastructure can be monitored and visualized in a user-friendly and sustainable manner.
