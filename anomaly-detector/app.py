import os
from flask import Flask, request, jsonify
import pandas as pd
from sklearn.ensemble import IsolationForest

app = Flask(__name__)

@app.route('/detect', methods=['POST'])
def detect():
    try:
        # Get cost data from Java
        data = request.get_json()

        # Expecting a list of {"date": "YYYY-MM-DD", "amount": float}
        if not data or not isinstance(data, list):
            return jsonify({"error": "Invalid data format"}), 400

        df = pd.DataFrame(data)

        # Handle missing or invalid data
        if "amount" not in df.columns or df.empty:
            return jsonify({"error": "No 'amount' column found"}), 400

        # Anomaly detection using Isolation Forest
        model = IsolationForest(contamination=0.15, random_state=42)
        df['anomaly'] = model.fit_predict(df[['amount']])

        # Filter anomalies
        anomalies = df[df['anomaly'] == -1]

        # Convert to list of dicts
        return jsonify(anomalies.to_dict(orient="records"))

    except Exception as e:
        return jsonify({"error": str(e)}), 500


if __name__ == '__main__':
    # Read PORT from environment
    port = int(os.environ.get("PORT", "5000"))
    app.run(host="0.0.0.0", port=port)
