import express from 'express';
import cors from 'cors';
import helmet from 'helmet';
import morgan from 'morgan';
import dotenv from 'dotenv';
import authRoutes from './routes/auth.js';
import crmRoutes from './routes/crm.js';
import inspectionRoutes from './routes/inspections.js';
import contractRoutes from './routes/contracts.js';

dotenv.config();

const app = express();
const port = process.env.PORT || 4000;

app.use(helmet());
app.use(cors());
app.use(morgan('dev'));
app.use(express.json());

// Routes
app.use('/auth', authRoutes);
app.use('/crm', crmRoutes);
app.use('/inspections', inspectionRoutes);
app.use('/contracts', contractRoutes);

app.get('/health', (req, res) => {
  res.json({ status: 'ok', timestamp: new Date().toISOString() });
});

app.listen(port, () => {
  console.log(`Inspectavision API running on port ${port}`);
});
