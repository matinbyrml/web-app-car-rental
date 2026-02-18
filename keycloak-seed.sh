#!/usr/bin/env sh
set -euo pipefail

# Config
KC_URL="http://localhost:9090"     # service name + internal port (because this script runs inside Docker)
REALM="alberioauto"
ADMIN_USER="admin"
ADMIN_PASS="password"


# Get admin token
TOKEN=$(curl -s -X POST "${KC_URL}/realms/master/protocol/openid-connect/token" \
  -d "username=${ADMIN_USER}" \
  -d "password=${ADMIN_PASS}" \
  -d "grant_type=password" \
  -d "client_id=admin-cli" \
  | jq -r '.access_token')

if [ -z "$TOKEN" ] || [ "$TOKEN" = "null" ]; then
  echo "❌ Failed to get admin token"
  exit 1
fi

echo "🔑 Got admin token"

# Example: create a couple of users automatically
create_user() {
  local username=$1
  local password=$2
  local role=$3

  echo "👤 Creating user $username with role $role ..."

  # Create user
  USER_ID=$(curl -s -o /dev/null -w "%{http_code}" -X POST \
    "${KC_URL}/admin/realms/${REALM}/users" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
      \"username\": \"${username}\",
      \"enabled\": true,
      \"credentials\": [{\"type\":\"password\",\"value\":\"${password}\",\"temporary\": false}]
    }")

  if [ "$USER_ID" -ge 200 ] && [ "$USER_ID" -lt 300 ]; then
    echo "✅ User $username created"
  else
    echo "⚠️  User $username may already exist or creation failed ($USER_ID)"
  fi

  # Assign role
  USER_UUID=$(curl -s "${KC_URL}/admin/realms/${REALM}/users?username=${username}" \
    -H "Authorization: Bearer $TOKEN" | jq -r '.[0].id')

  ROLE=$(curl -s "${KC_URL}/admin/realms/${REALM}/roles/${role}" \
    -H "Authorization: Bearer $TOKEN")

  curl -s -X POST \
    "${KC_URL}/admin/realms/${REALM}/users/${USER_UUID}/role-mappings/realm" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d "[${ROLE}]" > /dev/null

  echo "✅ Assigned role $role to user $username"
}

# Create example users
create_user "customer1" "password" "CUSTOMER"
create_user "staff1" "password" "STAFF"
create_user "manager1" "password" "MANAGER"

echo "🎉 Keycloak seeding finished"
