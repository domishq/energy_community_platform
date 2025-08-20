FROM python:3.13.7-alpine
WORKDIR /app
COPY main.py .
RUN pip install nats-py redis
CMD ["python", "main.py"]
