# Rate Limiter as a Service

This project is a standalone microservice that lets any backend application enforce API rate limits without building the logic themselves. Register a client, set a capacity and refill rate, and the service handles the rest — using the token bucket algorithm with Redis for fast token state and PostgreSQL for persistent storage. The project is also fully dockerized and deployed on AWS EC2.

## Tech Stack

![Java](https://img.shields.io/badge/Java_17-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![AWS](https://img.shields.io/badge/AWS_EC2-FF9900?style=for-the-badge&logo=amazon-aws&logoColor=white)
![JMeter](https://img.shields.io/badge/Apache_JMeter-D22128?style=for-the-badge&logo=apache-jmeter&logoColor=white)
