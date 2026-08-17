# Petals Market

A compact MERN e-commerce application with buyer/seller accounts, virtual balances, product CRUD, optional image upload, carts, checkout, stock control, and persisted orders.

## Run locally

1. Start MongoDB (the included compose file is a fallback if an existing Docker MongoDB container is not already available): `docker compose up -d`.
2. Copy `server/.env.example` to `server/.env`; set a strong `JWT_SECRET`. Update `MONGO_URI` if your existing container uses a different host/port.
3. Install packages: `npm install`, `npm install --prefix server`, `npm install --prefix client`.
4. Seed sample data: `npm run seed`.
5. Run both applications: `npm run dev`.

The app opens at `http://localhost:5173`; API is at port 5000.

## Demo accounts

- Buyer: `buyer@petals.test` / `Petals123!`
- Seller: `seller@petals.test` / `Petals123!`

Each new account receives $1,000. Values are stored as integer cents. Checkout executes in a MongoDB transaction, deducting buyer balance, crediting sellers, decrementing stock, recording an order, and clearing the cart atomically.

## Deployment

Deploy `client` to GitHub Pages (set its API URL for production) and deploy `server` to a Node host such as Render/Railway with MongoDB Atlas or a managed Mongo database. GitHub Pages cannot host Express or MongoDB.
