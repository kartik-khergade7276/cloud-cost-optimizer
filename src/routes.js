const express = require("express");
const router = express.Router();
const pool = require("./db");

// Get last 30 days of cost data
router.get("/costs", async (req, res) => {
  try {
    const result = await pool.query("SELECT * FROM aws_cost ORDER BY date DESC LIMIT 30");
    res.json(result.rows);
  } catch (err) {
    console.error(err.message);
    res.status(500).send("Server Error");
  }
});

// Store anomaly detection result
router.post("/anomaly", async (req, res) => {
  const { date, anomaly_detected } = req.body;
  try {
    await pool.query("INSERT INTO anomalies(date, anomaly_detected) VALUES($1,$2)", [date, anomaly_detected]);
    res.json({ status: "success" });
  } catch (err) {
    console.error(err.message);
    res.status(500).send("Server Error");
  }
});

module.exports = router;
