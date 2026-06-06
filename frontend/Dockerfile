# 构建阶段
FROM node:20-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm config set registry https://registry.npmmirror.com
RUN npm install
COPY . .
RUN npm run build

# 生产运行阶段
FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
# React router / Vue router fallback setup
RUN echo "server { listen 80; root /usr/share/nginx/html; index index.html; location / { try_files \$uri \$uri/ /index.html; } location /api/ { proxy_pass http://inventory-backend:8080/api/; } }" > /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
