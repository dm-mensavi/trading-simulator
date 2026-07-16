# ── Stage 1: Build Angular app ─────────────────────────────
FROM node:20-alpine AS build
WORKDIR /app

# Install dependencies (layer-cached separately from source)
COPY package*.json ./
RUN npm install --legacy-peer-deps

COPY . .
RUN npm run build

# ── Stage 2: Serve with Nginx ──────────────────────────────
FROM nginx:alpine
# Angular 18+ (@angular/build:application) outputs to dist/<project-name>/browser
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
