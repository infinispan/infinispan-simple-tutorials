#!/bin/bash

if [ -f ${JAVA_HOME}/bin/keytool ] ; then
  # Maven uses JAVA_HOME to compile the project. If keytool bin is there, use it instead
  KEYTOOL_CMD="${JAVA_HOME}/bin/keytool"
else
  # Otherwise, uses the keytool defined in path.
  KEYTOOL_CMD="keytool"
fi

# Generate a CA certificate so that clients can trust server certificates
${KEYTOOL_CMD} -genkeypair -validity 365 -keyalg RSA -keysize 2048 -storetype PKCS12 -alias ca -dname "CN=CA,OU=Infinispan,O=JBoss,L=Red Hat" -storepass CAsecret -keystore ca.p12 -ext bc:c
# Extract the CA certificate to a file that you can import into other stores
${KEYTOOL_CMD} -exportcert -alias ca -keystore ca.p12 -storepass CAsecret -file ca.cer


# Generate the server certificate
${KEYTOOL_CMD} -genkeypair -validity 365 -keyalg RSA -keysize 2048 -storetype PKCS12 -alias infinispan-server -dname "CN=Server,OU=Infinispan,O=JBoss,L=Red Hat" -storepass Serversecret -keystore server_keystore.p12
# Create a Certificate Signing Request (CSR) for the server certificate
${KEYTOOL_CMD} -certreq -alias infinispan-server -keystore server_keystore.p12 -storepass Serversecret -file server.csr
# Sign the server CSR with the CA
${KEYTOOL_CMD} -gencert -keystore ca.p12 -alias ca -infile server.csr -outfile server.cer -storepass CAsecret
# Import the CA certificate into the server keystore
# This is required because the keystore needs the full certificate chain
${KEYTOOL_CMD} -importcert -alias ca -keystore server_keystore.p12 -file ca.cer -storepass Serversecret -noprompt
# Import the signed certificate back into the server keystore
${KEYTOOL_CMD} -importcert -alias infinispan-server -keystore server_keystore.p12 -file server.cer -storepass Serversecret -noprompt

# Create a server trust store that contains the CA
${KEYTOOL_CMD} -importcert -alias ca -keystore server_truststore.p12 -storepass ServerTrustsecret -file ca.cer -noprompt

# Generate the first client certificate
${KEYTOOL_CMD} -genkeypair -validity 365 -keyalg RSA -keysize 2048 -alias client1 -dname "CN=Client1,OU=Infinispan,O=JBoss,L=Red Hat" -storepass Client1secret -keystore client1_keystore.p12
# Create a Certificate Signing Request (CSR) for the client certificate
${KEYTOOL_CMD} -certreq -alias client1 -keystore client1_keystore.p12 -storepass Client1secret -file client1.csr
# Sign the client CSR with the CA
${KEYTOOL_CMD} -gencert -keystore ca.p12 -alias ca -infile client1.csr -outfile client1.cer -storepass CAsecret
# Import the CA certificate into the client keystore
# This is required because the keystore needs the full certificate chain
${KEYTOOL_CMD} -importcert -alias ca -keystore client1_keystore.p12 -file ca.cer -storepass Client1secret -noprompt
# Import the signed certificate back into the client keystore
${KEYTOOL_CMD} -importcert -alias client1 -keystore client1_keystore.p12 -file client1.cer -storepass Client1secret -noprompt


# Generate the second client certificate
${KEYTOOL_CMD} -genkeypair -validity 365 -keyalg RSA -keysize 2048 -alias client2 -dname "CN=Client2,OU=Infinispan,O=JBoss,L=Red Hat" -storepass Client2secret -keystore client2_keystore.p12
# Create a Certificate Signing Request (CSR) for the client certificate
${KEYTOOL_CMD} -certreq -alias client2 -keystore client2_keystore.p12 -storepass Client2secret -file client2.csr
# Sign the client CSR with the CA
${KEYTOOL_CMD} -gencert -keystore ca.p12 -alias ca -infile client2.csr -outfile client2.cer -storepass CAsecret
# Import the CA certificate into the client keystore
# This is required because the keystore needs the full certificate chain 
${KEYTOOL_CMD} -importcert -alias ca -keystore client2_keystore.p12 -file ca.cer -storepass Client2secret -noprompt
# Import the signed certificate back into the client keystore
${KEYTOOL_CMD} -importcert -alias client2 -keystore client2_keystore.p12 -file client2.cer -storepass Client2secret -noprompt
