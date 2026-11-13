import os 
from pathlib import Path

from dotenv import load_dotenv
from sqlalchemy.engine import URL

load_dotenv(Path(__file__).resolve().parent / ".env")

def database_url() -> URL:

    return URL.create(
        "postgresql+psycopg",
        username=os.getenv("DB_USER", "cti"),
        password=os.getenv("DB_PASSWORD", "cti123"),
        host=os.getenv("DB_HOST", "localhost"),
        port=int(os.getenv("DB_PORT", "5432")),
        database=os.getenv("DB_NAME", "ctiinsights"),
        query={"sslmode": os.getenv("DB_SSLMODE", "prefer")},
    )