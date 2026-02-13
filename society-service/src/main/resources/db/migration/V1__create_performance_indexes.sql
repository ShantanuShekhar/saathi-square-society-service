-- Performance Optimization: Database Indexes
-- This script adds indexes to improve query performance and prevent N+1 query issues

-- ============================================
-- ANNOUNCEMENT TABLE INDEXES
-- ============================================

-- Index for filtering active announcements by society
CREATE INDEX IF NOT EXISTS idx_announcement_society_status ON announcement(society_id, status, is_deleted);
-- Index for scheduled announcements lookup
CREATE INDEX IF NOT EXISTS idx_announcement_scheduled ON announcement(status, scheduled_at) WHERE status = 'SCHEDULED';
-- Index for expired announcements lookup
CREATE INDEX IF NOT EXISTS idx_announcement_expires ON announcement(status, expires_at) WHERE status = 'ACTIVE';
-- Index for pinned announcements
CREATE INDEX IF NOT EXISTS idx_announcement_pinned ON announcement(society_id, is_pinned, status) WHERE is_pinned = true;
-- Index for unpinning job
CREATE INDEX IF NOT EXISTS idx_announcement_pinned_until ON announcement(is_pinned, pinned_until) WHERE is_pinned = true AND pinned_until IS NOT NULL;
-- Index for created_by (user lookup)
CREATE INDEX IF NOT EXISTS idx_announcement_created_by ON announcement(created_by);
-- Index for priority sorting
CREATE INDEX IF NOT EXISTS idx_announcement_priority ON announcement(priority);
-- Index for created_at sorting
CREATE INDEX IF NOT EXISTS idx_announcement_created_at ON announcement(created_at DESC);

-- ============================================
-- MARKETPLACE_POST TABLE INDEXES
-- ============================================

-- Index for filtering posts by society
CREATE INDEX IF NOT EXISTS idx_marketplace_post_society ON marketplace_post(society_id, status, is_deleted);
-- Index for post type filtering
CREATE INDEX IF NOT EXISTS idx_marketplace_post_type ON marketplace_post(society_id, post_type, status);
-- Index for created_by (user lookup)
CREATE INDEX IF NOT EXISTS idx_marketplace_post_created_by ON marketplace_post(created_by);
-- Index for status filtering
CREATE INDEX IF NOT EXISTS idx_marketplace_post_status ON marketplace_post(status);
-- Index for created_at sorting
CREATE INDEX IF NOT EXISTS idx_marketplace_post_created_at ON marketplace_post(created_at DESC);
-- Index for search (title, description)
-- Note: For full-text search, consider adding FULLTEXT index in MySQL
CREATE INDEX IF NOT EXISTS idx_marketplace_post_title ON marketplace_post(title);
CREATE INDEX IF NOT EXISTS idx_marketplace_post_description ON marketplace_post(description(255));

-- ============================================
-- MARKETPLACE_IMAGE TABLE INDEXES
-- ============================================

-- Index for loading images by post (prevents N+1)
CREATE INDEX IF NOT EXISTS idx_marketplace_image_post ON marketplace_image(post_id, display_order);

-- ============================================
-- MARKETPLACE_COMMENT TABLE INDEXES
-- ============================================

-- Index for loading comments by post (prevents N+1)
CREATE INDEX IF NOT EXISTS idx_marketplace_comment_post ON marketplace_comment(post_id, is_deleted, created_at);
-- Index for created_by
CREATE INDEX IF NOT EXISTS idx_marketplace_comment_created_by ON marketplace_comment(created_by);

-- ============================================
-- FLAT TABLE INDEXES
-- ============================================

-- Index for society and occupancy status
CREATE INDEX IF NOT EXISTS idx_flat_society_status ON flat(society_id, occupancy_status);
-- Index for floor relationship
CREATE INDEX IF NOT EXISTS idx_flat_floor ON flat(floor_id);
-- Index for soft delete filtering
CREATE INDEX IF NOT EXISTS idx_flat_is_deleted ON flat(is_deleted);
-- Index for flat number search
CREATE INDEX IF NOT EXISTS idx_flat_flat_number ON flat(flat_number);

-- ============================================
-- FLOOR TABLE INDEXES
-- ============================================

-- Index for tower relationship
CREATE INDEX IF NOT EXISTS idx_floor_tower ON floor(tower_id);
-- Index for floor number filtering
CREATE INDEX IF NOT EXISTS idx_floor_number ON floor(floor_number);

-- ============================================
-- TOWER TABLE INDEXES
-- ============================================

-- Index for society relationship
CREATE INDEX IF NOT EXISTS idx_tower_society ON tower(society_id);

-- ============================================
-- USER_FLAT_MAPPING TABLE INDEXES
-- ============================================

-- Index for finding active mappings by flat
CREATE INDEX IF NOT EXISTS idx_user_flat_mapping_flat_active ON user_flat_mapping(flat_id, is_active) WHERE is_active = true;
-- Index for finding mappings by user
CREATE INDEX IF NOT EXISTS idx_user_flat_mapping_user ON user_flat_mapping(user_id);
-- Index for active mappings by user
CREATE INDEX IF NOT EXISTS idx_user_flat_mapping_user_active ON user_flat_mapping(user_id, is_active) WHERE is_active = true;

-- ============================================
-- SOCIETY_USER_MAPPING TABLE INDEXES
-- ============================================

-- Index for user lookups
CREATE INDEX IF NOT EXISTS idx_society_user_mapping_user ON society_user_mapping(user_id);
-- Index for society lookups
CREATE INDEX IF NOT EXISTS idx_society_user_mapping_society ON society_user_mapping(society_id);
-- Composite index for user-society queries
CREATE INDEX IF NOT EXISTS idx_society_user_mapping_user_society ON society_user_mapping(user_id, society_id);

-- ============================================
-- SOCIETY TABLE INDEXES
-- ============================================

-- Index for created_by lookups
CREATE INDEX IF NOT EXISTS idx_society_created_by ON society(created_by);
-- Index for created_at sorting
CREATE INDEX IF NOT EXISTS idx_society_created_at ON society(created_at DESC);

-- ============================================
-- FLAT_PAYMENT TABLE INDEXES
-- ============================================

-- Index for flat relationship
CREATE INDEX IF NOT EXISTS idx_flat_payment_flat ON flat_payment(flat_id);
-- Index for payment date filtering
CREATE INDEX IF NOT EXISTS idx_flat_payment_date ON flat_payment(payment_date DESC);
-- Index for payment status
CREATE INDEX IF NOT EXISTS idx_flat_payment_status ON flat_payment(payment_status);

-- ============================================
-- PAYMENT_PLAN TABLE INDEXES
-- ============================================

-- Index for society relationship
CREATE INDEX IF NOT EXISTS idx_payment_plan_society ON payment_plan(society_id);
-- Index for active plans
CREATE INDEX IF NOT EXISTS idx_payment_plan_active ON payment_plan(is_active) WHERE is_active = true;

-- ============================================
-- FLAT_PLAN_MAPPING TABLE INDEXES
-- ============================================

-- Index for flat relationship
CREATE INDEX IF NOT EXISTS idx_flat_plan_mapping_flat ON flat_plan_mapping(flat_id);
-- Index for payment plan relationship
CREATE INDEX IF NOT EXISTS idx_flat_plan_mapping_plan ON flat_plan_mapping(payment_plan_id);
-- Composite index for active mappings
CREATE INDEX IF NOT EXISTS idx_flat_plan_mapping_active ON flat_plan_mapping(flat_id, is_active) WHERE is_active = true;

