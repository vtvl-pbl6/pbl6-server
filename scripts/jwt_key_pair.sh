# Description: Generate a RSA key pair for JWT token signing and verification
# Usage: sh scripts/jwt_key_pair.sh

secret_dir="./src/main/resources/secret"
access_token_private_key="$secret_dir/access_token_private_key"
access_token_public_key="$secret_dir/access_token_public_key"
refresh_token_private_key="$secret_dir/refresh_token_private_key"
refresh_token_public_key="$secret_dir/refresh_token_public_key"

rm -rf $secret_dir
mkdir $secret_dir

# Access token
openssl genrsa -out "$access_token_private_key.pem" 2048
openssl pkcs8 -topk8 -inform PEM -outform DER -in "$access_token_private_key.pem" -out "$access_token_private_key.der" -nocrypt # Convert private key to PKCS#8 format
openssl rsa -in "$access_token_private_key.pem" -pubout -outform DER -out "$access_token_public_key.der"
openssl rsa -in "$access_token_private_key.pem" -pubout > "$access_token_public_key.pem"

# Refresh token
openssl genrsa -out "$refresh_token_private_key.pem" 2048
openssl pkcs8 -topk8 -inform PEM -outform DER -in "$refresh_token_private_key.pem" -out "$refresh_token_private_key.der" -nocrypt # Convert private key to PKCS#8 format
openssl rsa -in "$refresh_token_private_key.pem" -pubout -outform DER -out "$refresh_token_public_key.der"
openssl rsa -in "$refresh_token_private_key.pem" -pubout > "$refresh_token_public_key.pem"
