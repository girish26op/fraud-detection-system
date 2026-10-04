// Vercel Serverless Function: GET /api/fraud/history/:accountId

export default function handler(req, res) {
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET,OPTIONS');

  if (req.method === 'OPTIONS') {
    return res.status(200).end();
  }

  const { accountId } = req.query;
  const acc = accountId || 'ACC-101';

  // Sample dynamic stream stats
  const defaults = {
    'ACC-101': { count: 3, avg: 15666.67 },
    'ACC-102': { count: 1, avg: 150000.00 },
    'ACC-103': { count: 2, avg: 4500.00 },
    'ACC-104': { count: 4, avg: 3200.00 },
    'ACC-105': { count: 1, avg: 5000.00 },
    'ACC-106': { count: 1, avg: 99999.00 }
  };

  const stat = defaults[acc] || { count: 1, avg: 15000.00 };

  return res.status(200).json({
    accountId: acc,
    transactionCount: stat.count,
    averageAmount: stat.avg
  });
}
