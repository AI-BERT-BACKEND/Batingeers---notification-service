# Swagger Local Testing Guide — Notification Service

All examples below use `localhost:8083` (default port). Adjust if your service runs on a different port.
Every endpoint requires a **Bearer JWT token** in the `Authorization` header.

---

## 1. Generate a JWT Token

Use [jwt.io](https://jwt.io) to generate a token with the following values:

**Algorithm:** `HS256`

**Header:**
```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

**Payload:**
```json
{
  "sub": "testuser",
  "userId": 1,
  "roles": ["ROLE_USER"],
  "iat": 1700000000,
  "exp": 9999999999
}
```

**Secret (256-bit):**
```
myDefaultSecretKeyForDevOnlyChangeInProduction1234567890
```

Copy the generated token and replace `<YOUR_JWT_TOKEN>` in all examples below.

> In Swagger UI, click **Authorize** at the top right, paste only the token value (without "Bearer "),
> and click **Authorize**.

---

## 2. Notification Inbox

### POST /api/v1/notifications — Create a notification

```bash
curl -X POST http://localhost:8083/api/v1/notifications \
  -H "Authorization: Bearer <YOUR_JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "type": "OVERLOAD_ALERT",
    "title": "Academic overload alert",
    "message": "You have 7 active tasks (5 urgent, 2 overdue). Consider redistributing your workload.",
    "severity": "HIGH"
  }'
```

**Payload with relatedEntityId:**
```json
{
  "userId": 1,
  "type": "TASK_REMINDER",
  "title": "Task reminder",
  "message": "The task 'Algebra Workshop' is due tomorrow.",
  "severity": "MEDIUM",
  "relatedEntityId": 42
}
```

**Payload — study session invite:**
```json
{
  "userId": 1,
  "type": "STUDY_SESSION_INVITE",
  "title": "Study session invitation",
  "message": "Carlos Lopez has invited you to a Calculus I study session on Friday at 4 PM.",
  "severity": "LOW"
}
```

**Payload — low performance alert:**
```json
{
  "userId": 1,
  "type": "LOW_PERFORMANCE_ALERT",
  "title": "Low academic performance alert",
  "message": "Your overall average is 2.8. Subjects at risk: Calculus I, Physics II.",
  "severity": "HIGH"
}
```

**Valid values for `type`:**
- `OVERLOAD_ALERT`
- `LOW_PERFORMANCE_ALERT`
- `STUDY_SUGGESTION`
- `TASK_REMINDER`
- `STUDY_SESSION_INVITE`
- `LEVEL_UP`

**Valid values for `severity`:**
- `HIGH`
- `MEDIUM`
- `LOW`
- `INFO`

---

### GET /api/v1/notifications/me — Get all my notifications

```bash
curl -X GET http://localhost:8083/api/v1/notifications/me \
  -H "Authorization: Bearer <YOUR_JWT_TOKEN>"
```

---

### GET /api/v1/notifications/me/unread — Get unread notifications only

```bash
curl -X GET http://localhost:8083/api/v1/notifications/me/unread \
  -H "Authorization: Bearer <YOUR_JWT_TOKEN>"
```

---

### GET /api/v1/notifications/me/count — Get unread count

```bash
curl -X GET http://localhost:8083/api/v1/notifications/me/count \
  -H "Authorization: Bearer <YOUR_JWT_TOKEN>"
```

**Expected response:**
```json
{
  "userId": 1,
  "count": 3
}
```

---

### PUT /api/v1/notifications/{id}/read — Mark one notification as read

Replace `{id}` with an actual notification ID returned from the GET endpoints (e.g., `1`).

```bash
curl -X PUT http://localhost:8083/api/v1/notifications/1/read \
  -H "Authorization: Bearer <YOUR_JWT_TOKEN>"
```

---

### PUT /api/v1/notifications/me/read-all — Mark all as read

```bash
curl -X PUT http://localhost:8083/api/v1/notifications/me/read-all \
  -H "Authorization: Bearer <YOUR_JWT_TOKEN>"
```

Returns **204 No Content** on success.

---

### GET /api/v1/notifications/me/alerts — Get alert notifications (last 7 days)

```bash
curl -X GET http://localhost:8083/api/v1/notifications/me/alerts \
  -H "Authorization: Bearer <YOUR_JWT_TOKEN>"
```

> This endpoint calls **task-service** and **academic-service** in real time.
> Without those services running, it returns any stored alert notifications from the last 7 days
> (including the seed data if the `local` profile is active).

---

### GET /api/v1/notifications/me/suggestions — Get today's study suggestions

```bash
curl -X GET http://localhost:8083/api/v1/notifications/me/suggestions \
  -H "Authorization: Bearer <YOUR_JWT_TOKEN>"
```

> This endpoint calls **planning-service**. Without it, it returns any suggestion notification
> already stored for today.

---

## 3. Stats — Alerts (R22)

### GET /api/v1/stats/alerts — Overload + low-grade alerts

```bash
curl -X GET http://localhost:8083/api/v1/stats/alerts \
  -H "Authorization: Bearer <YOUR_JWT_TOKEN>"
```

**Expected response when external services are unavailable (no availability configured):**
```json
{
  "overloadAlert": {
    "active": false,
    "title": "Weekly availability not configured",
    "message": "Set up your weekly availability to receive overload alerts.",
    "bannerVariant": null,
    "suggestedAction": null,
    "requiredHours": 0.0,
    "availableHours": 0.0,
    "overloadHours": 0.0
  },
  "lowGradeAlert": {
    "active": false,
    "threshold": 3.0,
    "subjectsAtRisk": [],
    "riskSubjects": []
  }
}
```

**Expected response when overload is active:**
```json
{
  "overloadAlert": {
    "active": true,
    "bannerVariant": "critical",
    "title": "Critical overload",
    "message": "You have 10 tasks (20.0 h estimated) vs 8.0 h available this week.",
    "suggestedAction": "Consider rescheduling some tasks or reducing your academic load this week.",
    "requiredHours": 20.0,
    "availableHours": 8.0,
    "overloadHours": 12.0
  },
  "lowGradeAlert": {
    "active": true,
    "bannerVariant": "warning",
    "title": "Low academic performance",
    "alertTitle": "Subjects at academic risk",
    "message": "Your current average is 2.8 (minimum threshold: 3.0). Subjects at risk: Calculus I.",
    "alertMessage": "You have 1 subject(s) at risk: Calculus I (2.4). Take action before the next exam.",
    "recommendation": "Review each subject at risk and consult with your academic advisor.",
    "currentAverage": 2.8,
    "threshold": 3.0,
    "subjectsAtRisk": ["Calculus I"],
    "riskSubjects": [
      {
        "subjectId": "1",
        "name": "Calculus I",
        "projectedGrade": 2.4,
        "riskLevel": "High",
        "recommendation": "Review missed assessments and seek tutoring support."
      }
    ],
    "generatedDate": "2026-05-18T10:00:00"
  }
}
```

---

## 4. Stats — Study Suggestion (R23)

### GET /api/v1/stats/suggestion — Highest-priority study suggestion

```bash
curl -X GET http://localhost:8083/api/v1/stats/suggestion \
  -H "Authorization: Bearer <YOUR_JWT_TOKEN>"
```

**Expected response when a pending task exists:**
```json
{
  "suggestedTask": "Algebra Workshop",
  "suggestionReason": "Priority 0.83: weight=1 (×0.6) + urgency 1/0.5 days (×0.4).",
  "subjectName": "Linear Algebra",
  "priority": 0.83,
  "dueDate": "2026-05-20"
}
```

Returns **204 No Content** when there are no pending tasks.

---

## 5. Kafka — Send events manually per topic

The notification service now listens on **5 independent topics**. Use the Kafka CLI to simulate events from each source microservice (requires the Kafka container to be running):

```bash
# Enter the Kafka container
docker exec -it kafka bash
```

---

### topic: `task.events` (task-service)

```bash
kafka-console-producer.sh --broker-list localhost:9092 --topic task.events
```

**TASK_REMINDER payload:**
```json
{"userId":1,"type":"TASK_REMINDER","title":"Task reminder","message":"The task 'Algebra Workshop' is due tomorrow.","severity":"MEDIUM","relatedEntityId":42}
```

**OVERLOAD_ALERT payload:**
```json
{"userId":1,"type":"OVERLOAD_ALERT","title":"Critical overload","message":"You have 10 tasks (20.0 h estimated) vs 8.0 h available this week.","severity":"HIGH","relatedEntityId":null}
```

---

### topic: `academic.events` (academic-service / stats-service)

```bash
kafka-console-producer.sh --broker-list localhost:9092 --topic academic.events
```

**LOW_PERFORMANCE_ALERT payload:**
```json
{"userId":1,"type":"LOW_PERFORMANCE_ALERT","title":"Low academic performance","message":"Your current average is 2.8 (minimum threshold: 3.0). Subjects at risk: Calculus I.","severity":"HIGH"}
```

---

### topic: `planning.events` (planning-service)

```bash
kafka-console-producer.sh --broker-list localhost:9092 --topic planning.events
```

**STUDY_SUGGESTION payload:**
```json
{"userId":1,"type":"STUDY_SUGGESTION","title":"Algebra Workshop","message":"Priority 0.83: weight=3 (×0.6) + urgency 1/0.5 days (×0.4).","severity":"INFO","relatedEntityId":42}
```

---

### topic: `social.events` (social-service)

```bash
kafka-console-producer.sh --broker-list localhost:9092 --topic social.events
```

**STUDY_SESSION_INVITE payload:**
```json
{"userId":1,"type":"STUDY_SESSION_INVITE","title":"Study session invitation","message":"Carlos Lopez has invited you to a Calculus I study session on Friday at 4 PM.","severity":"LOW","relatedEntityId":7}
```

---

### topic: `gamification.events` (gamification-service)

```bash
kafka-console-producer.sh --broker-list localhost:9092 --topic gamification.events
```

**LEVEL_UP payload:**
```json
{"userId":1,"newLevelNumber":5,"previousLevelNumber":4,"levelName":"Scholar"}
```

> The notification service generates the title and message automatically:
> - Title: `"Level Up! You've reached level 5: Scholar"`
> - Message: `"Congratulations! You advanced from level 4 to level 5. Keep it up!"`
> - Severity: `INFO`

---

## 6. Health Check

```bash
curl http://localhost:8083/actuator/health
```

**Expected:**
```json
{ "status": "UP" }
```

---

## 7. Swagger UI

Open in browser: [http://localhost:8083/swagger-ui.html](http://localhost:8083/swagger-ui.html)

OpenAPI JSON spec: [http://localhost:8083/v3/api-docs](http://localhost:8083/v3/api-docs)
