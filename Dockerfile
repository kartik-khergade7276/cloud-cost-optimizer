FROM node:18-alpine
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
EXPOSE ${NODE_PORT}
CMD ["node", "src/app.js"]
