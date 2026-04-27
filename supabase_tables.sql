-- SQL to create tables in Supabase

-- Users table
CREATE TABLE users (
    id TEXT PRIMARY KEY,
    username TEXT,
    full_name TEXT,
    email TEXT,
    phone_number TEXT,
    profile_image_url TEXT,
    seller_rating REAL DEFAULT 0,
    user_rating REAL DEFAULT 0,
    user_rating_count INTEGER DEFAULT 0,
    seller_rating_count INTEGER DEFAULT 0,
    overall_rating REAL DEFAULT 0,
    last_seen BIGINT,
    bio TEXT,
    last_edited BIGINT,
    profile_image_version BIGINT DEFAULT 0,
    address TEXT
);

-- Products table
CREATE TABLE products (
    id TEXT PRIMARY KEY,
    name TEXT,
    price REAL,
    created_at BIGINT,
    image_urls TEXT[],
    description TEXT,
    product_used TEXT,
    category TEXT,
    location TEXT,
    seller_id TEXT,
    buyer_id TEXT,
    listing_date TIMESTAMP WITH TIME ZONE,
    image_version BIGINT DEFAULT 0,
    status TEXT DEFAULT 'active',
    sold_to TEXT,
    sold_at TIMESTAMP WITH TIME ZONE,
    purchase_id TEXT,
    transaction_date BIGINT
);

-- Purchases table
CREATE TABLE purchases (
    id TEXT PRIMARY KEY,
    product_id TEXT,
    buyer_id TEXT,
    seller_id TEXT,
    price REAL,
    purchase_date TIMESTAMP WITH TIME ZONE,
    status TEXT DEFAULT 'completed',
    receiving_method TEXT,
    delivery_address TEXT
);

-- Enable Row Level Security if needed
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE products ENABLE ROW LEVEL SECURITY;
ALTER TABLE purchases ENABLE ROW LEVEL SECURITY;

-- Policies (adjust as needed)
CREATE POLICY "Users can view all users" ON users FOR SELECT USING (true);
CREATE POLICY "Users can update own profile" ON users FOR UPDATE USING (auth.uid() = id);

CREATE POLICY "Anyone can view products" ON products FOR SELECT USING (true);
CREATE POLICY "Sellers can insert own products" ON products FOR INSERT WITH CHECK (auth.uid() = seller_id);
CREATE POLICY "Sellers can update own products" ON products FOR UPDATE USING (auth.uid() = seller_id);

CREATE POLICY "Users can view own purchases" ON purchases FOR SELECT USING (auth.uid() = buyer_id OR auth.uid() = seller_id);
CREATE POLICY "Users can insert purchases" ON purchases FOR INSERT WITH CHECK (auth.uid() = buyer_id);