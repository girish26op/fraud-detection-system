// Vercel Serverless Function: POST /api/fraud/evaluate
// Implements Java 21 Fraud Detection Engine Rules in cloud serverless runtime

let accountCache = {
  "ACC-101": { count: 3, total: 32000 },
  "ACC-102": { count: 1, total: 150000 },
  "ACC-103": { count: 2, total: 9000 },
  "ACC-104": { count: 4, total: 12800 },
  "ACC-105": { count: 1, total: 5000 },
  "ACC-106": { count: 1, total: 99999 }
};

let auditLogs = [
  `[${new Date().toISOString()}] [BLOCKED] TxId=TXN-INIT-01 | Type=CARD | Account=ACC-102 | Amount=150000.00 | Location=Mumbai,INDIA | Reason=VelocityRule: Amount exceeds limit of 100,000`,
  `[${new Date().toISOString()}] [BLOCKED] TxId=TXN-INIT-02 | Type=CARD | Account=ACC-103 | Amount=4500.00 | Location=London,UK | Reason=GeoJumpingRule: Cross-border transaction detected`
];

export default function handler(req, res) {
  // Set CORS headers
  res.setHeader('Access-Control-Allow-Credentials', 'true');
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET,OPTIONS,PATCH,DELETE,POST,PUT');
  res.setHeader(
    'Access-Control-Allow-Headers',
    'X-CSRF-Token, X-Requested-With, Accept, Accept-Version, Content-Length, Content-MD5, Content-Type, Date, X-Api-Version'
  );

  if (req.method === 'OPTIONS') {
    return res.status(200).end();
  }

  if (req.method !== 'POST') {
    return res.status(405).json({ error: 'Method not allowed' });
  }

  const payload = req.body || {};
  const { txId, accountId, amount, city, country, type, isInternational, vpaId } = payload;

  const numAmount = parseFloat(amount) || 0;
  let isBlocked = false;
  let reason = "Transaction passed all Java 21 fraud detection rules";

  // Rule 1: Velocity Rule (Threshold > 100,000)
  if (numAmount > 100000) {
    isBlocked = true;
    reason = `${type} transaction [${txId}] failed VelocityRule: Amount ₹${numAmount.toLocaleString()} exceeds limit of ₹100,000`;
  }
  // Rule 2: Geo-Jumping Rule (International / Cross-Border for Domestic Account)
  else if (type === 'CARD' && (isInternational || (country && country.toUpperCase() !== 'INDIA'))) {
    isBlocked = true;
    reason = `Card transaction [${txId}] failed GeoJumpingRule: Cross-border transaction detected at ${city}, ${country}`;
  }
  // Rule 3: VPA Rule (UPI Syntax check)
  else if (type === 'UPI' && (!vpaId || !vpaId.includes('@'))) {
    isBlocked = true;
    reason = `UPI transaction [${txId}] failed VpaRule: Invalid VPA address [${vpaId}] missing @ provider`;
  }

  const timestamp = new Date().toISOString();

  if (isBlocked) {
    auditLogs.push(`[${timestamp}] [BLOCKED] TxId=${txId} | Type=${type} | Account=${accountId} | Amount=${numAmount.toFixed(2)} | Location=${city},${country} | Reason=${reason}`);
    return res.status(200).json({
      txId: txId || "UNKNOWN",
      status: "BLOCKED",
      message: reason,
      timestamp: timestamp
    });
  }

  // Update in-memory stream cache
  const acc = accountId || "ACC-101";
  if (!accountCache[acc]) {
    accountCache[acc] = { count: 0, total: 0 };
  }
  accountCache[acc].count += 1;
  accountCache[acc].total += numAmount;

  return res.status(200).json({
    txId: txId || "UNKNOWN",
    status: "APPROVED",
    message: reason,
    timestamp: timestamp
  });
}
