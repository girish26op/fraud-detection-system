// Vercel Serverless Function: GET /api/fraud/audit-log

export default function handler(req, res) {
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET,OPTIONS');

  if (req.method === 'OPTIONS') {
    return res.status(200).end();
  }

  const logs = [
    `[${new Date().toISOString()}] [BLOCKED] TxId=TXN-88888 | Type=CARD | Account=ACC-102 | Amount=150000.00 | Location=Mumbai,INDIA | Reason=Card transaction [TXN-88888] failed VelocityRule: Amount 150000.0 exceeds limit of 100,000`,
    `[${new Date().toISOString()}] [BLOCKED] TxId=TXN-9228 | Type=CARD | Account=ACC-103 | Amount=4500.00 | Location=London,UK | Reason=Card transaction [TXN-9228] failed GeoJumpingRule: International transaction detected at London, UK`,
    `[${new Date().toISOString()}] [BLOCKED] TxId=TXN-4537 | Type=UPI | Account=ACC-105 | Amount=5000.00 | Location=Bangalore,INDIA | Reason=UPI transaction [TXN-4537] failed VpaRule: Invalid VPA address [bad_vpa_string]`
  ];

  return res.status(200).json(logs);
}
