# Use a base Python image
FROM python:3.8-slim

# Set working directory in the container
WORKDIR /app

# Copy application code to the container
COPY store.py ./app/store.py

# Install Flask and required libraries
RUN pip install flask

# Expose the application port
EXPOSE 9092

# Run the Flask application
CMD ["python", "store.py"]
