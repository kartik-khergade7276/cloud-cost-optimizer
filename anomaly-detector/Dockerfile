FROM python:3.10-slim

# Set work directory
WORKDIR /app

# Install dependencies
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy source code
COPY . .

# Expose Flask port
ENV PORT=${PYTHON_HOST_PORT}
EXPOSE ${PYTHON_CONTAINER_PORT}

# Start Flask app
CMD ["python", "app.py"]
