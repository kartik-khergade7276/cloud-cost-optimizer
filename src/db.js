const { Pool } = require("pg");

const pool = new Pool({
  user: "postgres",
  host: "localhost",   // or "postgres" if running in Docker
  database: "cloudcost",
  password: "Kheru@7276",
  port: 5435,
});

module.exports = pool;
