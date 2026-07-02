-- Voucher System Indexes
-- Improve query performance for promotions and user_vouchers

CREATE INDEX IF NOT EXISTS idx_promotions_active_dates ON promotions (is_active, tu_ngay, den_ngay);
CREATE INDEX IF NOT EXISTS idx_user_vouchers_user_status ON user_vouchers (user_id, status);
CREATE INDEX IF NOT EXISTS idx_user_vouchers_expired_at ON user_vouchers (expired_at);
CREATE INDEX IF NOT EXISTS idx_user_vouchers_promotion_id ON user_vouchers (promotion_id);
CREATE INDEX IF NOT EXISTS idx_promotions_code ON promotions (ma_code);
