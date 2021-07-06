# First create a server trust store using create-signed_certificates.sh
# Make a copy of the server trust store
cp server_truststore.p12 server_truststoreAuth.p12

# Import the client certificate into the server trust store
# This is necessary if you use client certificate authentication
keytool -importcert -alias client1 -keystore server_truststoreAuth.p12 -file client1.cer -storepass ServerTrustsecret -noprompt
keytool -importcert -alias client2 -keystore server_truststoreAuth.p12 -file client2.cer -storepass ServerTrustsecret -noprompt
