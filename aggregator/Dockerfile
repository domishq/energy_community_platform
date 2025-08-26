FROM python:3.13.7-alpine
WORKDIR /app
COPY main.py .
RUN pip install nats-py redis watchfiles
CMD ["watchfiles", "python main.py", "."]
