# -------------------------------------------------------------
# 🏗️ Build stage
# Uses Node.js (Alpine) to install deps and build the static site
# -------------------------------------------------------------
FROM node:20-alpine AS build

# Workdir inside the container
WORKDIR /app

# Copy only package files first to leverage layer caching
COPY package*.json ./

# Install exact, locked dependencies (from package-lock.json)
RUN npm ci

# Copy the rest of the front-end source
COPY . .

# Provide the API base path to Vite at build time (used by your app code)
# Example: fetch(`${import.meta.env.VITE_API_URL}/...`)
ENV VITE_API_URL=/api

# Build production-optimized static assets into /app/dist
RUN npm run build


# -------------------------------------------------------------
# 🚀 Run stage
# Serves the built assets with Nginx
# -------------------------------------------------------------
FROM nginx:alpine

# Replace default server config (handles SPA routing, /api proxy, etc.)
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy compiled static files from the build stage to Nginx web root
COPY --from=build /app/dist /usr/share/nginx/html

# Expose HTTP port
EXPOSE 80
