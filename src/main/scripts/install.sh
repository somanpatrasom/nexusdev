#!/bin/bash

# ============================================================
#  NexusDev Installer v1.0
#  Sets up a complete NexusDev lab on any Ubuntu/Debian machine
#  Usage: bash install.sh
# ============================================================

set -e  # stop immediately if any command fails

# ── Colors ──────────────────────────────────────────────────
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
BOLD='\033[1m'
RESET='\033[0m'

# ── Helper functions ─────────────────────────────────────────
print_ok()   { echo -e "  ${GREEN}✔${RESET}  $1"; }
print_skip() { echo -e "  ${YELLOW}⊙${RESET}  $1 (already installed)"; }
print_info() { echo -e "  ${CYAN}→${RESET}  $1"; }
print_err()  { echo -e "  ${RED}✘${RESET}  $1"; }

print_step() {
    echo ""
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}"
    echo -e "  ${BOLD}$1${RESET}"
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}"
}

print_banner() {
    clear
    echo ""
    echo -e "${CYAN}╔══════════════════════════════════════════╗${RESET}"
    echo -e "${CYAN}║${RESET}                                          ${CYAN}║${RESET}"
    echo -e "${CYAN}║${RESET}     ${BOLD}⚡ Welcome to NexusDev${RESET}               ${CYAN}║${RESET}"
    echo -e "${CYAN}║${RESET}        Build your development lab        ${CYAN}║${RESET}"
    echo -e "${CYAN}║${RESET}                                          ${CYAN}║${RESET}"
    echo -e "${CYAN}╚══════════════════════════════════════════╝${RESET}"
    echo ""
}

# ── Check OS ─────────────────────────────────────────────────
check_os() {
    print_step "Checking system"

    if ! command -v apt &>/dev/null; then
        print_err "NexusDev requires Ubuntu or Debian."
        print_err "Support for other distributions coming soon."
        exit 1
    fi

    # lsb_release -d gives "Description: Ubuntu 24.04 LTS"
    # cut -f2 takes the part after the tab
    OS=$(lsb_release -d 2>/dev/null | cut -f2 || echo "Ubuntu/Debian")
    print_ok "OS: $OS"

    # Check if running as root — we need sudo access
    if [ "$EUID" -eq 0 ]; then
        print_err "Please do not run this installer as root."
        print_err "Run as a normal user — sudo will be used when needed."
        exit 1
    fi

    print_ok "User: $USER"
}

# ── Detect installed tools ────────────────────────────────────
detect_installed() {
    print_step "Detecting existing installations"

    # Set all flags to false first
    HAS_JAVA=false
    HAS_POSTGRES=false
    HAS_GIT=false
    HAS_MAVEN=false
    HAS_TAILSCALE=false
    HAS_DOCKER=false
    HAS_NODE=false
    HAS_PYTHON=false

    # Check each tool — set flag and print if found
    command -v java      &>/dev/null && HAS_JAVA=true      && print_skip "Java"
    command -v psql      &>/dev/null && HAS_POSTGRES=true  && print_skip "PostgreSQL"
    command -v git       &>/dev/null && HAS_GIT=true       && print_skip "Git"
    command -v mvn       &>/dev/null && HAS_MAVEN=true     && print_skip "Maven"
    command -v tailscale &>/dev/null && HAS_TAILSCALE=true && print_skip "Tailscale"
    command -v docker    &>/dev/null && HAS_DOCKER=true    && print_skip "Docker"
    command -v node      &>/dev/null && HAS_NODE=true      && print_skip "Node.js"
    command -v python3   &>/dev/null && HAS_PYTHON=true    && print_skip "Python 3"

    # Count how many are missing
    MISSING=0
    [ "$HAS_JAVA"     = false ] && MISSING=$((MISSING + 1))
    [ "$HAS_POSTGRES" = false ] && MISSING=$((MISSING + 1))
    [ "$HAS_GIT"      = false ] && MISSING=$((MISSING + 1))

    echo ""
    if [ "$MISSING" -eq 0 ]; then
        print_ok "All core tools already installed."
    else
        print_info "$MISSING core tool(s) not yet installed — will be set up based on your choices."
    fi
}

# ── Stack selector ───────────────────────────────────────────
select_stack() {

    # ── Language ─────────────────────────────────────────────
    print_step "Step 1 of 3 — Choose your Language"
    echo ""
    echo -e "  ${BOLD}Which language will your team primarily use?${RESET}"
    echo ""
    echo -e "   1)  Java 21         ${CYAN}(backend, enterprise, Spring Boot)${RESET}"
    echo -e "   2)  Python 3.11     ${CYAN}(data, ML, scripting, Flask)${RESET}"
    echo -e "   3)  Node.js 20      ${CYAN}(web, APIs, JavaScript)${RESET}"
    echo -e "   4)  Java + Python   ${CYAN}(both)${RESET}"
    echo -e "   5)  All of the above"
    echo ""
    echo -e "  ${YELLOW}Tip: not sure? Pick 1 — Java is great for beginners.${RESET}"
    echo ""
    read -p "  Enter choice [1-5] (default: 1): " LANG_CHOICE
    LANG_CHOICE=${LANG_CHOICE:-1}   # if empty, default to 1

    INSTALL_JAVA=false
    INSTALL_PYTHON=false
    INSTALL_NODE=false

    case $LANG_CHOICE in
        1) INSTALL_JAVA=true ;;
        2) INSTALL_PYTHON=true ;;
        3) INSTALL_NODE=true ;;
        4) INSTALL_JAVA=true; INSTALL_PYTHON=true ;;
        5) INSTALL_JAVA=true; INSTALL_PYTHON=true; INSTALL_NODE=true ;;
        *) INSTALL_JAVA=true
           print_info "Invalid — defaulting to Java 21" ;;
    esac

    # ── Database ─────────────────────────────────────────────
    print_step "Step 2 of 3 — Choose your Database"
    echo ""
    echo -e "  ${BOLD}Which database will your project use?${RESET}"
    echo ""
    echo -e "   1)  PostgreSQL 16   ${CYAN}(recommended — powerful, reliable)${RESET}"
    echo -e "   2)  MySQL 8         ${CYAN}(widely used, simple)${RESET}"
    echo -e "   3)  Both"
    echo -e "   4)  Skip            ${CYAN}(I will set up my own)${RESET}"
    echo ""
    echo -e "  ${YELLOW}Tip: not sure? Pick 1 — PostgreSQL works with everything.${RESET}"
    echo ""
    read -p "  Enter choice [1-4] (default: 1): " DB_CHOICE
    DB_CHOICE=${DB_CHOICE:-1}

    INSTALL_POSTGRES=false
    INSTALL_MYSQL=false

    case $DB_CHOICE in
        1) INSTALL_POSTGRES=true ;;
        2) INSTALL_MYSQL=true ;;
        3) INSTALL_POSTGRES=true; INSTALL_MYSQL=true ;;
        4) ;;
        *) INSTALL_POSTGRES=true
           print_info "Invalid — defaulting to PostgreSQL 16" ;;
    esac

    # ── Tools ────────────────────────────────────────────────
    print_step "Step 3 of 3 — Choose your Tools"
    echo ""
    echo -e "  ${BOLD}Select additional tools (press Enter to accept default):${RESET}"
    echo ""

    INSTALL_GIT=false
    INSTALL_MAVEN=false
    INSTALL_TAILSCALE=false
    INSTALL_DOCKER=false

    # Git
    if [ "$HAS_GIT" = false ]; then
        read -p "  Install Git?       (version control)       [Y/n]: " r
        r=${r:-Y}
        [[ "$r" == "Y" || "$r" == "y" ]] && INSTALL_GIT=true
    fi

    # Maven — only relevant if Java chosen
    if [ "$INSTALL_JAVA" = true ] && [ "$HAS_MAVEN" = false ]; then
        read -p "  Install Maven?     (required for Java)     [Y/n]: " r
        r=${r:-Y}
        [[ "$r" == "Y" || "$r" == "y" ]] && INSTALL_MAVEN=true
    fi

    # Tailscale
    if [ "$HAS_TAILSCALE" = false ]; then
        read -p "  Install Tailscale? (access from anywhere)  [Y/n]: " r
        r=${r:-Y}
        [[ "$r" == "Y" || "$r" == "y" ]] && INSTALL_TAILSCALE=true
    fi

    # Docker
    if [ "$HAS_DOCKER" = false ]; then
        read -p "  Install Docker?    (lab isolation)         [y/N]: " r
        r=${r:-N}
        [[ "$r" == "Y" || "$r" == "y" ]] && INSTALL_DOCKER=true
    fi

    # ── Summary ──────────────────────────────────────────────
    echo ""
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}"
    echo -e "  ${BOLD}Your NexusDev Stack${RESET}"
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}"
    echo ""

    # Show what will be installed vs what already exists
    [ "$INSTALL_JAVA"     = true ] && [ "$HAS_JAVA"      = false ] && echo -e "  ${GREEN}+${RESET}  Java 21"
    [ "$HAS_JAVA"         = true ] && echo -e "  ${YELLOW}⊙${RESET}  Java (existing)"
    [ "$INSTALL_PYTHON"   = true ] && [ "$HAS_PYTHON"    = false ] && echo -e "  ${GREEN}+${RESET}  Python 3.11"
    [ "$HAS_PYTHON"       = true ] && echo -e "  ${YELLOW}⊙${RESET}  Python (existing)"
    [ "$INSTALL_NODE"     = true ] && [ "$HAS_NODE"      = false ] && echo -e "  ${GREEN}+${RESET}  Node.js 20"
    [ "$HAS_NODE"         = true ] && echo -e "  ${YELLOW}⊙${RESET}  Node.js (existing)"
    [ "$INSTALL_POSTGRES" = true ] && [ "$HAS_POSTGRES"  = false ] && echo -e "  ${GREEN}+${RESET}  PostgreSQL 16"
    [ "$HAS_POSTGRES"     = true ] && echo -e "  ${YELLOW}⊙${RESET}  PostgreSQL (existing)"
    [ "$INSTALL_MYSQL"    = true ] && echo -e "  ${GREEN}+${RESET}  MySQL 8"
    [ "$INSTALL_GIT"      = true ] && [ "$HAS_GIT"       = false ] && echo -e "  ${GREEN}+${RESET}  Git"
    [ "$HAS_GIT"          = true ] && echo -e "  ${YELLOW}⊙${RESET}  Git (existing)"
    [ "$INSTALL_MAVEN"    = true ] && [ "$HAS_MAVEN"     = false ] && echo -e "  ${GREEN}+${RESET}  Maven"
    [ "$HAS_MAVEN"        = true ] && echo -e "  ${YELLOW}⊙${RESET}  Maven (existing)"
    [ "$INSTALL_TAILSCALE" = true ] && [ "$HAS_TAILSCALE" = false ] && echo -e "  ${GREEN}+${RESET}  Tailscale"
    [ "$HAS_TAILSCALE"    = true ] && echo -e "  ${YELLOW}⊙${RESET}  Tailscale (existing)"
    [ "$INSTALL_DOCKER"   = true ] && [ "$HAS_DOCKER"    = false ] && echo -e "  ${GREEN}+${RESET}  Docker"
    [ "$HAS_DOCKER"       = true ] && echo -e "  ${YELLOW}⊙${RESET}  Docker (existing)"
    echo ""
    read -p "  Proceed with this stack? [Y/n]: " CONFIRM
    CONFIRM=${CONFIRM:-Y}
    if [[ "$CONFIRM" == "n" || "$CONFIRM" == "N" ]]; then
        echo ""
        print_info "Restarting stack selection..."
        select_stack   # restart if they say no
    fi
}

# ── Install selected components ───────────────────────────────
install_components() {
    print_step "Installing components"

    # Check if anything actually needs installing
    NEEDS_INSTALL=false
    [ "$INSTALL_JAVA"      = true ] && [ "$HAS_JAVA"      = false ] && NEEDS_INSTALL=true
    [ "$INSTALL_PYTHON"    = true ] && [ "$HAS_PYTHON"    = false ] && NEEDS_INSTALL=true
    [ "$INSTALL_NODE"      = true ] && [ "$HAS_NODE"      = false ] && NEEDS_INSTALL=true
    [ "$INSTALL_POSTGRES"  = true ] && [ "$HAS_POSTGRES"  = false ] && NEEDS_INSTALL=true
    [ "$INSTALL_MYSQL"     = true ]                                  && NEEDS_INSTALL=true
    [ "$INSTALL_GIT"       = true ] && [ "$HAS_GIT"       = false ] && NEEDS_INSTALL=true
    [ "$INSTALL_MAVEN"     = true ] && [ "$HAS_MAVEN"     = false ] && NEEDS_INSTALL=true
    [ "$INSTALL_TAILSCALE" = true ] && [ "$HAS_TAILSCALE" = false ] && NEEDS_INSTALL=true
    [ "$INSTALL_DOCKER"    = true ] && [ "$HAS_DOCKER"    = false ] && NEEDS_INSTALL=true

    if [ "$NEEDS_INSTALL" = false ]; then
        print_ok "Nothing to install — all selected tools already present."
        return
    fi

    print_info "Updating package list..."
    sudo apt-get update -qq

    # ── Java ────────────────────────────────────────────────
    if [ "$INSTALL_JAVA" = true ] && [ "$HAS_JAVA" = false ]; then
        print_info "Installing Java 21..."
        sudo apt-get install -y -qq openjdk-21-jdk
        print_ok "Java 21 installed"
    fi

    # ── Python ──────────────────────────────────────────────
    if [ "$INSTALL_PYTHON" = true ] && [ "$HAS_PYTHON" = false ]; then
        print_info "Installing Python 3..."
        sudo apt-get install -y -qq python3 python3-pip python3-venv
        print_ok "Python 3 installed"
    fi

    # ── Node.js ─────────────────────────────────────────────
    if [ "$INSTALL_NODE" = true ] && [ "$HAS_NODE" = false ]; then
        print_info "Installing Node.js 20..."
        curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash - &>/dev/null
        sudo apt-get install -y -qq nodejs
        print_ok "Node.js 20 installed"
    fi

    # ── PostgreSQL ──────────────────────────────────────────
    if [ "$INSTALL_POSTGRES" = true ] && [ "$HAS_POSTGRES" = false ]; then
        print_info "Installing PostgreSQL 16..."
        sudo apt-get install -y -qq postgresql postgresql-contrib
        sudo systemctl start postgresql
        sudo systemctl enable postgresql
        print_ok "PostgreSQL 16 installed and started"
    fi

    # ── MySQL ───────────────────────────────────────────────
    if [ "$INSTALL_MYSQL" = true ]; then
        print_info "Installing MySQL 8..."
        sudo apt-get install -y -qq mysql-server
        sudo systemctl start mysql
        sudo systemctl enable mysql
        print_ok "MySQL 8 installed and started"
    fi

    # ── Git ─────────────────────────────────────────────────
    if [ "$INSTALL_GIT" = true ] && [ "$HAS_GIT" = false ]; then
        print_info "Installing Git..."
        sudo apt-get install -y -qq git
        print_ok "Git installed"
    fi

    # ── Maven ───────────────────────────────────────────────
    if [ "$INSTALL_MAVEN" = true ] && [ "$HAS_MAVEN" = false ]; then
        print_info "Installing Maven..."
        sudo apt-get install -y -qq maven
        print_ok "Maven installed"
    fi

    # ── Tailscale ───────────────────────────────────────────
    if [ "$INSTALL_TAILSCALE" = true ] && [ "$HAS_TAILSCALE" = false ]; then
        print_info "Installing Tailscale..."
        curl -fsSL https://tailscale.com/install.sh | sh &>/dev/null
        print_ok "Tailscale installed"
        print_info "Run 'sudo tailscale up' after setup to connect."
    fi

    # ── Docker ──────────────────────────────────────────────
    if [ "$INSTALL_DOCKER" = true ] && [ "$HAS_DOCKER" = false ]; then
        print_info "Installing Docker..."
        curl -fsSL https://get.docker.com | sh &>/dev/null
        sudo usermod -aG docker $USER
        print_ok "Docker installed"
        print_info "Log out and back in for Docker permissions to take effect."
    fi
}

# ── Configure database ────────────────────────────────────────
configure_database() {
    print_step "Configuring database"

    # Make sure PostgreSQL is running
    if ! sudo systemctl is-active --quiet postgresql; then
        print_info "Starting PostgreSQL..."
        sudo systemctl start postgresql
    fi

    # Generate a secure random password for nexus_admin
    # openssl rand -base64 16 gives random bytes as base64
    # tr removes non-alphanumeric chars, head limits to 16 chars
    DB_PASS=$(openssl rand -base64 16 | tr -dc 'a-zA-Z0-9' | head -c 16)

    print_info "Creating database user..."

    # Run as postgres user — create nexus_admin if not exists
    sudo -u postgres psql -q << EOF
DO \$\$
BEGIN
    IF NOT EXISTS (
        SELECT FROM pg_roles WHERE rolname = 'nexus_admin'
    ) THEN
        CREATE USER nexus_admin WITH PASSWORD '$DB_PASS';
        RAISE NOTICE 'User nexus_admin created.';
    ELSE
        ALTER USER nexus_admin WITH PASSWORD '$DB_PASS';
        RAISE NOTICE 'User nexus_admin password updated.';
    END IF;
END
\$\$;
EOF

    print_info "Creating database..."

    # Create nexusdev database if it doesn't exist
    sudo -u postgres psql -q << EOF
SELECT 'CREATE DATABASE nexusdev OWNER nexus_admin'
WHERE NOT EXISTS (
    SELECT FROM pg_database WHERE datname = 'nexusdev'
)\gexec
EOF

    # Grant all privileges
    sudo -u postgres psql -q -d nexusdev << EOF
GRANT ALL PRIVILEGES ON DATABASE nexusdev TO nexus_admin;
GRANT ALL ON SCHEMA public TO nexus_admin;
EOF

    print_info "Creating tables..."

    # Create all 6 tables — IF NOT EXISTS means safe to re-run
    PGPASSWORD=$DB_PASS psql -U nexus_admin -d nexusdev -h localhost -q << EOF
CREATE TABLE IF NOT EXISTS users (
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    username    VARCHAR(50)  UNIQUE NOT NULL,
    role        VARCHAR(20)  DEFAULT 'member',
    invite_code VARCHAR(50)  UNIQUE,
    joined_at   TIMESTAMP    DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS sessions (
    id               SERIAL PRIMARY KEY,
    user_id          INTEGER REFERENCES users(id),
    topic            VARCHAR(200) NOT NULL,
    duration_minutes INTEGER NOT NULL,
    notes            TEXT,
    created_at       TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS devices (
    id          SERIAL PRIMARY KEY,
    user_id     INTEGER REFERENCES users(id),
    name        VARCHAR(100),
    ip_address  VARCHAR(45),
    mac_address VARCHAR(17),
    first_seen  TIMESTAMP DEFAULT NOW(),
    last_seen   TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS file_transfers (
    id             SERIAL PRIMARY KEY,
    filename       VARCHAR(255),
    size_bytes     BIGINT,
    from_device_id INTEGER REFERENCES devices(id),
    to_device_id   INTEGER REFERENCES devices(id),
    direction      VARCHAR(10),
    status         VARCHAR(20),
    transferred_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS lab_templates (
    id               SERIAL PRIMARY KEY,
    name             VARCHAR(100) NOT NULL,
    topic            VARCHAR(200) NOT NULL,
    duration_minutes INTEGER NOT NULL,
    notes_scaffold   TEXT,
    device_config    JSONB,
    created_by       INTEGER REFERENCES users(id),
    share_code       VARCHAR(50) UNIQUE NOT NULL,
    created_at       TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS lab_instances (
    id           SERIAL PRIMARY KEY,
    template_id  INTEGER REFERENCES lab_templates(id),
    user_id      INTEGER REFERENCES users(id),
    status       VARCHAR(20) DEFAULT 'active',
    notes        TEXT,
    started_at   TIMESTAMP DEFAULT NOW(),
    completed_at TIMESTAMP
);
EOF

    # Save password for use in later steps
    NEXUSDEV_DB_PASS=$DB_PASS

    print_ok "Database configured"
    print_ok "Tables created"
}

# ── Set up directories ────────────────────────────────────────
setup_directories() {
    print_step "Setting up NexusDev directories"

    sudo mkdir -p /opt/nexusdev
    sudo mkdir -p /opt/nexusdev/lab/shared
    sudo mkdir -p /opt/nexusdev/lab/admin
    sudo mkdir -p /opt/nexusdev/lab/members
    sudo mkdir -p /opt/nexusdev/logs
    sudo mkdir -p /opt/nexusdev/config

    sudo chown -R $USER:$USER /opt/nexusdev

    print_ok "Directories created at /opt/nexusdev/"
    print_info "Shared workspace → /opt/nexusdev/lab/shared/"
    print_info "Member folders  → /opt/nexusdev/lab/members/"
}

# ── Copy JAR ─────────────────────────────────────────────────
copy_jar() {
    print_step "Installing NexusDev"

    # Look for the JAR — check common locations
    JAR_SRC=""
    [ -f "./target/nexusdev-1.0-SNAPSHOT.jar" ] && JAR_SRC="./target/nexusdev-1.0-SNAPSHOT.jar"
    [ -f "./nexusdev.jar"                      ] && JAR_SRC="./nexusdev.jar"
    [ -f "/opt/nexusdev/nexusdev.jar"          ] && JAR_SRC="/opt/nexusdev/nexusdev.jar"

    if [ -n "$JAR_SRC" ]; then
        cp "$JAR_SRC" /opt/nexusdev/nexusdev.jar
        print_ok "NexusDev installed from: $JAR_SRC"
    else
        print_err "NexusDev JAR not found."
        print_info "Please run this installer from your project directory"
        print_info "or place nexusdev.jar in the current folder."
        exit 1
    fi
}

# ── Write config ──────────────────────────────────────────────
write_config() {
    print_step "Writing configuration"

    cat > /opt/nexusdev/config/application.properties <<EOF
# PostgreSQL connection
spring.datasource.url=jdbc:postgresql://localhost:5432/nexusdev
spring.datasource.username=nexus_admin
spring.datasource.password=${NEXUSDEV_DB_PASS}
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA settings
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=false

# Server
server.port=8080
spring.application.name=NexusDev
EOF

    print_ok "Config written → /opt/nexusdev/config/application.properties"
}

# ── Create systemd service ────────────────────────────────────
create_service() {
    print_step "Creating NexusDev system service"

    # systemd is not available in WSL by default
    # detect WSL and skip service creation if needed
    if grep -qi microsoft /proc/version 2>/dev/null; then
        print_info "WSL detected — skipping systemd service."
        print_info "To start NexusDev manually:"
        print_info "java -jar /opt/nexusdev/nexusdev.jar \\"
        print_info "  --spring.config.location=file:/opt/nexusdev/config/application.properties"
        WSL_MODE=true
        return
    fi

    WSL_MODE=false

    sudo tee /etc/systemd/system/nexusdev.service > /dev/null <<EOF
[Unit]
Description=NexusDev Lab Server
After=network.target postgresql.service

[Service]
Type=simple
User=$USER
WorkingDirectory=/opt/nexusdev
ExecStart=/usr/bin/java -jar /opt/nexusdev/nexusdev.jar \
  --spring.config.location=file:/opt/nexusdev/config/application.properties
Restart=on-failure
RestartSec=10
StandardOutput=append:/opt/nexusdev/logs/nexusdev.log
StandardError=append:/opt/nexusdev/logs/nexusdev.log

[Install]
WantedBy=multi-user.target
EOF

    sudo systemctl daemon-reload
    sudo systemctl enable nexusdev
    sudo systemctl start nexusdev

    print_ok "Service created and started"
    print_info "Auto-starts on boot"
    print_info "Logs → /opt/nexusdev/logs/nexusdev.log"
}

# ── Create admin account ──────────────────────────────────────
create_admin() {
    print_step "Creating your admin account"
    echo ""

    read -p "  Your name:  " ADMIN_NAME
    read -p "  Username:   " ADMIN_USERNAME

    # Generate admin invite code
    ADMIN_CODE="NEXUS-ADMIN-$(openssl rand -hex 2 | tr 'a-f' 'A-F')"

    PGPASSWORD=$NEXUSDEV_DB_PASS psql -U nexus_admin -d nexusdev -h localhost -q <<EOF
INSERT INTO users (name, username, role, invite_code)
VALUES ('$ADMIN_NAME', '$ADMIN_USERNAME', 'admin', '$ADMIN_CODE')
ON CONFLICT (username) DO UPDATE SET role = 'admin';
EOF

    print_ok "Admin account created: $ADMIN_USERNAME"

    # Create a member invite code for sharing
    MEMBER_CODE="NEXUS-$(echo $ADMIN_USERNAME | tr 'a-z' 'A-Z')-$(openssl rand -hex 2 | tr 'a-f' 'A-F')"

    PGPASSWORD=$NEXUSDEV_DB_PASS psql -U nexus_admin -d nexusdev -h localhost -q <<EOF
INSERT INTO lab_templates (name, topic, duration_minutes, notes_scaffold, share_code, created_by)
SELECT
    'Default Lab',
    'Getting Started with NexusDev',
    60,
    E'## Goal\n\n## What I built\n\n## What I learned\n\n## Blockers',
    '$MEMBER_CODE',
    id
FROM users WHERE username = '$ADMIN_USERNAME'
ON CONFLICT (share_code) DO NOTHING;
EOF

    LAB_JOIN_CODE=$MEMBER_CODE
    print_ok "Default lab template created"
}

# ── Get machine IP ────────────────────────────────────────────
get_ip() {
    if command -v tailscale &>/dev/null; then
        MACHINE_IP=$(tailscale ip -4 2>/dev/null || hostname -I | awk '{print $1}')
    else
        MACHINE_IP=$(hostname -I | awk '{print $1}')
    fi
}

# ── Final summary ─────────────────────────────────────────────
print_summary() {
    echo ""
    echo -e "${GREEN}╔══════════════════════════════════════════╗${RESET}"
    echo -e "${GREEN}║                                          ║${RESET}"
    echo -e "${GREEN}║   ⚡ NexusDev Lab is Ready!              ║${RESET}"
    echo -e "${GREEN}║                                          ║${RESET}"
    echo -e "${GREEN}╠══════════════════════════════════════════╣${RESET}"
    echo -e "${GREEN}║                                          ║${RESET}"
    echo -e "${GREEN}║  Dashboard                               ║${RESET}"
    echo -e "${GREEN}║  ${CYAN}http://${MACHINE_IP}:8080${RESET}"
    echo -e "${GREEN}║                                          ║${RESET}"
    echo -e "${GREEN}║  SSH access                              ║${RESET}"
    echo -e "${GREEN}║  ${CYAN}ssh ${USER}@${MACHINE_IP}${RESET}"
    echo -e "${GREEN}║                                          ║${RESET}"
    echo -e "${GREEN}║  Share this with your team               ║${RESET}"
    echo -e "${GREEN}║  ${YELLOW}${LAB_JOIN_CODE}${RESET}"
    echo -e "${GREEN}║                                          ║${RESET}"
    echo -e "${GREEN}╚══════════════════════════════════════════╝${RESET}"
    echo ""

    if [ "$WSL_MODE" = true ]; then
        echo -e "  ${YELLOW}WSL detected — start NexusDev manually:${RESET}"
        echo -e "  ${CYAN}java -jar /opt/nexusdev/nexusdev.jar \\${RESET}"
        echo -e "  ${CYAN}  --spring.config.location=file:/opt/nexusdev/config/application.properties${RESET}"
        echo ""
    else
        echo -e "  ${CYAN}Service commands:${RESET}"
        echo -e "  sudo systemctl status nexusdev    → check status"
        echo -e "  sudo systemctl restart nexusdev   → restart"
        echo -e "  tail -f /opt/nexusdev/logs/nexusdev.log → live logs"
        echo ""
    fi
}

# ── Main ─────────────────────────────────────────────────────
print_banner
echo -e "  This installer will:"
echo -e "  ${CYAN}→${RESET}  Detect what's already on your machine"
echo -e "  ${CYAN}→${RESET}  Let you choose your stack (Lego-style)"
echo -e "  ${CYAN}→${RESET}  Install only what's missing"
echo -e "  ${CYAN}→${RESET}  Set up NexusDev and start it automatically"
echo -e "  ${CYAN}→${RESET}  Give you a join code to share with your team"
echo ""
read -p "  Press Enter to begin..." _
check_os
detect_installed
select_stack
install_components
configure_database
setup_directories
copy_jar
write_config
create_service
create_admin
get_ip
print_summary