```mermaid
graph TD
    user -->|HTTPS 요청| user-management-web
    user -->|HTTPS 요청| partner-management-web
		user-management-web -->|API 요청| role-service
    user-management-web -->|API 요청| auth-service
    auth-service -->|제어| user_db
    role-service -->|제어| role_db

    partner-management-web -->|API 요청| auth-service
    partner-management-web -->|API 요청| carbon-emission-service
    carbon-emission-service -->|제어| carbon_emission_db

```
