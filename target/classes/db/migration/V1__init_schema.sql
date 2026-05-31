-- Extensions
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ─────────────────────────────────────
-- ENUMS
-- ─────────────────────────────────────
CREATE TYPE user_role          AS ENUM ('STUDENT', 'CLIENT', 'ADMIN', 'MODERATOR');
CREATE TYPE job_status         AS ENUM ('OPEN', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED');
CREATE TYPE application_status AS ENUM ('PENDING', 'SHORTLISTED', 'HIRED', 'REJECTED');
CREATE TYPE contract_status    AS ENUM ('ACTIVE', 'SUBMITTED', 'REVISION', 'COMPLETED', 'CANCELLED');
CREATE TYPE payment_status     AS ENUM ('PENDING', 'HELD', 'RELEASED', 'FAILED', 'REFUNDED', 'DISPUTED');
CREATE TYPE dispute_status     AS ENUM ('OPEN', 'UNDER_REVIEW', 'RESOLVED', 'CLOSED');
CREATE TYPE dispute_opened_by  AS ENUM ('STUDENT', 'CLIENT');

-- ─────────────────────────────────────
-- USERS
-- ─────────────────────────────────────
CREATE TABLE users (
                       id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       name        VARCHAR(100)  NOT NULL,
                       email       VARCHAR(150)  NOT NULL UNIQUE,
                       password    VARCHAR(255)  NOT NULL,
                       role        user_role     NOT NULL,
                       verified    BOOLEAN       NOT NULL DEFAULT FALSE,
                       active      BOOLEAN       NOT NULL DEFAULT TRUE,
                       created_at  TIMESTAMP     NOT NULL DEFAULT NOW(),
                       updated_at  TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- ─────────────────────────────────────
-- REFRESH TOKENS
-- ─────────────────────────────────────
CREATE TABLE refresh_tokens (
                                id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                user_id     UUID      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                token       TEXT      NOT NULL UNIQUE,
                                expires_at  TIMESTAMP NOT NULL,
                                revoked     BOOLEAN   NOT NULL DEFAULT FALSE,
                                created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ─────────────────────────────────────
-- STUDENT PROFILES
-- ─────────────────────────────────────
CREATE TABLE student_profiles (
                                  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                  user_id         UUID          NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
                                  college         VARCHAR(200),
                                  city            VARCHAR(100),
                                  bio             TEXT,
                                  skills          TEXT[],
                                  portfolio_links TEXT[],
                                  github_url      VARCHAR(255),
                                  linkedin_url    VARCHAR(255),
                                  hourly_rate     NUMERIC(10,2),
                                  profile_image   VARCHAR(500),
                                  created_at      TIMESTAMP     NOT NULL DEFAULT NOW(),
                                  updated_at      TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- ─────────────────────────────────────
-- CLIENT PROFILES
-- ─────────────────────────────────────
CREATE TABLE client_profiles (
                                 id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                 user_id         UUID          NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
                                 business_name   VARCHAR(200),
                                 category        VARCHAR(100),
                                 city            VARCHAR(100),
                                 contact_number  VARCHAR(20),
                                 website         VARCHAR(255),
                                 profile_image   VARCHAR(500),
                                 created_at      TIMESTAMP     NOT NULL DEFAULT NOW(),
                                 updated_at      TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- ─────────────────────────────────────
-- JOBS
-- ─────────────────────────────────────
CREATE TABLE jobs (
                      id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                      client_id       UUID          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                      title           VARCHAR(200)  NOT NULL,
                      description     TEXT          NOT NULL,
                      category        VARCHAR(100),
                      skills_required TEXT[],
                      budget          NUMERIC(10,2) NOT NULL,
                      deadline        DATE,
                      is_remote       BOOLEAN       NOT NULL DEFAULT TRUE,
                      status          job_status    NOT NULL DEFAULT 'OPEN',
                      attachment_url  VARCHAR(500),
                      created_at      TIMESTAMP     NOT NULL DEFAULT NOW(),
                      updated_at      TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- ─────────────────────────────────────
-- JOB APPLICATIONS
-- ─────────────────────────────────────
CREATE TABLE job_applications (
                                  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                  job_id          UUID               NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
                                  student_id      UUID               NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                  proposal        TEXT               NOT NULL,
                                  price_quote     NUMERIC(10,2)      NOT NULL,
                                  delivery_days   INT                NOT NULL,
                                  status          application_status NOT NULL DEFAULT 'PENDING',
                                  created_at      TIMESTAMP          NOT NULL DEFAULT NOW(),
                                  updated_at      TIMESTAMP          NOT NULL DEFAULT NOW(),
                                  UNIQUE(job_id, student_id)
);

-- ─────────────────────────────────────
-- CONTRACTS
-- ─────────────────────────────────────
CREATE TABLE contracts (
                           id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                           job_id          UUID            NOT NULL REFERENCES jobs(id),
                           client_id       UUID            NOT NULL REFERENCES users(id),
                           student_id      UUID            NOT NULL REFERENCES users(id),
                           amount          NUMERIC(10,2)   NOT NULL,
                           status          contract_status NOT NULL DEFAULT 'ACTIVE',
                           submission_url  VARCHAR(500),
                           started_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
                           completed_at    TIMESTAMP,
                           created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
                           updated_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

-- ─────────────────────────────────────
-- PAYMENTS
-- ─────────────────────────────────────
CREATE TABLE payments (
                          id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          contract_id         UUID           NOT NULL REFERENCES contracts(id),
                          client_id           UUID           NOT NULL REFERENCES users(id),
                          student_id          UUID           NOT NULL REFERENCES users(id),
                          amount              NUMERIC(10,2)  NOT NULL,
                          commission          NUMERIC(10,2)  NOT NULL,
                          student_payout      NUMERIC(10,2)  NOT NULL,
                          razorpay_fee        NUMERIC(10,2)  NOT NULL DEFAULT 0,
                          net_received        NUMERIC(10,2)  NOT NULL DEFAULT 0,
                          payout_sent         BOOLEAN        NOT NULL DEFAULT FALSE,
                          payout_sent_at      TIMESTAMP,
                          payout_note         VARCHAR(500),
                          gateway_order_id    VARCHAR(255)   UNIQUE,
                          gateway_payment_id  VARCHAR(255)   UNIQUE,
                          gateway_signature   VARCHAR(500),
                          status              payment_status NOT NULL DEFAULT 'PENDING',
                          created_at          TIMESTAMP      NOT NULL DEFAULT NOW(),
                          updated_at          TIMESTAMP      NOT NULL DEFAULT NOW()
);

-- ─────────────────────────────────────
-- REVIEWS
-- ─────────────────────────────────────
CREATE TABLE reviews (
                         id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                         contract_id       UUID     NOT NULL REFERENCES contracts(id),
                         reviewer_id       UUID     NOT NULL REFERENCES users(id),
                         reviewed_user_id  UUID     NOT NULL REFERENCES users(id),
                         rating            SMALLINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
                         comment           TEXT,
                         created_at        TIMESTAMP NOT NULL DEFAULT NOW(),
                         UNIQUE(contract_id, reviewer_id)
);

-- ─────────────────────────────────────
-- DISPUTES
-- ─────────────────────────────────────
CREATE TABLE disputes (
                          id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          contract_id     UUID              NOT NULL REFERENCES contracts(id),
                          opened_by_id    UUID              NOT NULL REFERENCES users(id),
                          opened_by_role  dispute_opened_by NOT NULL,
                          reason          TEXT              NOT NULL,
                          proof_urls      TEXT[],
                          status          dispute_status    NOT NULL DEFAULT 'OPEN',
                          resolution      TEXT,
                          resolved_by_id  UUID              REFERENCES users(id),
                          resolved_at     TIMESTAMP,
                          created_at      TIMESTAMP         NOT NULL DEFAULT NOW(),
                          updated_at      TIMESTAMP         NOT NULL DEFAULT NOW()
);

-- ─────────────────────────────────────
-- INDEXES — core
-- ─────────────────────────────────────
CREATE INDEX idx_jobs_client_id       ON jobs(client_id);
CREATE INDEX idx_jobs_status          ON jobs(status);
CREATE INDEX idx_applications_job_id  ON job_applications(job_id);
CREATE INDEX idx_applications_student ON job_applications(student_id);
CREATE INDEX idx_contracts_client     ON contracts(client_id);
CREATE INDEX idx_contracts_student    ON contracts(student_id);
CREATE INDEX idx_payments_contract    ON payments(contract_id);
CREATE INDEX idx_payments_status      ON payments(status);
CREATE INDEX idx_disputes_contract    ON disputes(contract_id);
CREATE INDEX idx_refresh_tokens_user  ON refresh_tokens(user_id);

-- ─────────────────────────────────────
-- INDEXES — optimized
-- ─────────────────────────────────────
CREATE INDEX idx_jobs_category_status    ON jobs(category, status);
CREATE INDEX idx_payments_student_status ON payments(student_id, status);
CREATE INDEX idx_payments_client_status  ON payments(client_id, status);
CREATE INDEX idx_disputes_status_created ON disputes(status, created_at DESC);
CREATE INDEX idx_refresh_tokens_token    ON refresh_tokens(token);
CREATE INDEX idx_applications_status     ON job_applications(status);