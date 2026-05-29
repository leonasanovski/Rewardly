# Rewardly

## Created by:
- Leon Asanovski
- Petar Avramovikj

Rewardly is a family task-and-reward app designed for children aged 11–15 and their parents. Parents create tasks for their children, children complete and submit them, and parents review the submissions to award tokens. Children can then spend those tokens in a reward market to claim prizes set by their parent.

## Roles

**Parent** — creates and manages tasks, reviews child submissions (with a star rating and comment), adjusts token balances manually, manages the reward catalogue, and monitors each child's wallet.

**Child** — views assigned tasks, starts and submits them with a written note, a self-rating, and optional file attachments. Completed tasks are reviewed by the parent who awards tokens based on performance. Children spend tokens in the reward market and track their purchases in their wallet.

## Flow

Parent creates a task → Child starts and submits it → Parent reviews and awards tokens → Child spends tokens on a reward → Parent marks the reward as redeemed.

## Proof of Concept

This is a proof-of-concept, fully functional application backed by an **H2 in-memory database** that is pre-seeded with test data on every startup (one parent family with two children, tasks in every status, rewards, and purchased rewards). You can clone the repository and run it locally to explore all features — no database setup required.

**Test accounts**

| Role | Login | Password |
|------|-------|----------|
| Parent | marija@test.com | test123 |
| Child | filip | test123 |
| Child | sara | test123 |

## Running locally

```bash
# Backend (port 8090)
cd backend && run-the-application-locally

# Frontend (port 4200)
cd frontend && npm install && npm start
```

Open `http://localhost:4200/login or http://localhost:4200/register`.

## Stack

| Layer | Technology                                                         |
|-------|--------------------------------------------------------------------|
| Backend | Spring Boot framework, coded in Kotlin language                    |
| Security | Spring Security 6 and JWT                              |
| Persistence | Spring Data JPA, Hibernate, H2 in-memory                           |
| Frontend | Angular                                                            |
| Styling | Bootstrap 5, combined with custom SCSS design system for consistency |

