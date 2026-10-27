
from flask import Flask, render_template
from pathlib import Path

app = Flask(__name__)

PROJECT_DIR = Path(__file__).resolve().parent.parent
OUTPUT_FILE = PROJECT_DIR / "output" / "part-r-00000"


@app.route("/")
def index():
    products = []

    if OUTPUT_FILE.exists():
        with OUTPUT_FILE.open("r", encoding="utf-8") as file:
            for line in file:
                parts = line.strip().split("\t", 1)

                if len(parts) != 2:
                    continue

                product_name, analysis = parts

                try:
                    fields = analysis.split(",")
                    total = int(fields[0].split(":")[-1].strip())
                    reviews = int(fields[1].split(":")[-1].strip())
                    average = float(fields[2].split(":")[-1].strip())
                except (ValueError, IndexError):
                    continue

                products.append({
                    "name": product_name,
                    "total": total,
                    "reviews": reviews,
                    "average": average
                })

    products.sort(key=lambda item: item["average"], reverse=True)

    return render_template("index.html", products=products)


if __name__ == "__main__":
    app.run(debug=True)
