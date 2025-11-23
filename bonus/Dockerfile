FROM python:3.15-rc-alpine

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY app.py .

ENV PORT=8080
ENV HOST=0.0.0.0

EXPOSE 8080

CMD ["python", "app.py"]

