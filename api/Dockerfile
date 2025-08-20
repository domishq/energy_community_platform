FROM python:3.13-slim
WORKDIR /app

# Copy only requirements first for faster rebuilds
COPY main.py . 

# Install dependencies
RUN pip install --no-cache-dir fastapi uvicorn[standard] redis nats-py

# Use uvicorn with --reload for auto-reload
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000", "--reload"]
