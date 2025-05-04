# User Onboarding Platform

A platform where users can create profiles, make posts, and interact with each other through comments, replies, and likes.

## Tech Stack

### Backend
- Java 17
- Spring Boot 3.2.3
- Spring Security with JWT authentication
- Spring Data JPA
- MySQL Database

### Frontend
- Next.js 14
- TypeScript
- Tailwind CSS
- shadcn/ui Component Library
- Axios for API requests
- React Hook Form with Zod for form validation

## Features

- User registration and authentication
- User profiles with customizable bio and profile picture
- Create, read, update, and delete posts
- Comment on posts
- Reply to comments
- Like posts and comments
- Responsive UI design

## Getting Started

### Prerequisites

- Java 17+
- Node.js 18+
- npm or yarn
- MySQL

### Backend Setup

1. Configure MySQL database:
   - Create a database named `user_onboarding_platform`
   - Update credentials in `backend/src/main/resources/application.properties` if needed

2. Run the backend:
   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```

   The backend will start on http://localhost:8080

3. Initialize database roles (only required for first run):
   ```sql
   INSERT INTO roles(name) VALUES('ROLE_USER');
   ```

### Frontend Setup

1. Install dependencies:
   ```bash
   cd frontend
   npm install
   # or
   yarn install
   ```

2. Run the development server:
   ```bash
   npm run dev
   # or
   yarn dev
   ```

   The frontend will start on http://localhost:3000

## API Endpoints

### Authentication
- `POST /api/auth/signup` - Register a new user
- `POST /api/auth/signin` - Login and get JWT token

### Users
- `GET /api/users/{id}` - Get user profile
- `GET /api/users/me` - Get current user profile
- `PUT /api/users/profile` - Update user profile

### Posts
- `GET /api/posts` - Get all posts
- `GET /api/posts/{id}` - Get post by ID
- `GET /api/posts/user/{userId}` - Get posts by user
- `POST /api/posts` - Create a new post
- `PUT /api/posts/{id}` - Update a post
- `DELETE /api/posts/{id}` - Delete a post
- `POST /api/posts/{id}/like` - Like/unlike a post

### Comments
- `GET /api/comments/post/{postId}` - Get comments for a post
- `GET /api/comments/{id}/replies` - Get replies for a comment
- `POST /api/comments/post/{postId}` - Add a comment to a post
- `POST /api/comments/{commentId}/reply` - Reply to a comment
- `PUT /api/comments/{id}` - Update a comment
- `DELETE /api/comments/{id}` - Delete a comment
- `POST /api/comments/{id}/like` - Like/unlike a comment 