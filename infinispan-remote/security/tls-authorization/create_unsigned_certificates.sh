#!/bin/bash

if [ -f ${JAVA_HOME}/bin/keytool ] ; then
  # Maven uses JAVA_HOME to compile the project. If keytool bin is there, use it instead
  KEYTOOL_CMD="${JAVA_HOME}/bin/keytool"
else
  # Otherwise, uses the keytool defined in path.
  KEYTOOL_CMD="keytool"
fi

# Create a basic keystore with PKCS12
# Use rhdg-server as the alias and 'secret' as the password
${KEYTOOL_CMD} -genkeypair -alias infinispan-server -keystore server-keystore.pfx -keyalg RSA -dname "CN=Server,OU=Infinispan,O=JBoss,L=Red Hat" -storepass secret -validity 365 -keysize 4096
# Export the self-signed certificate
${KEYTOOL_CMD} -exportcert -alias infinispan-server -file server-cert.cer -keystore server-keystore.pfx -storepass secret
# Import the certificate to a client trust store
# Use 'trustSecret' as the password for the client trust store
${KEYTOOL_CMD} -import -v -trustcacerts -alias infinispan-server -file server-cert.cer -keystore server-truststore.pfx -storepass trustSecret -noprompt

# Generate the first client certificate
${KEYTOOL_CMD} -genkeypair -alias client1 -keystore client1-keystore.pfx -storepass Client1secret -dname "CN=Client1,OU=Infinispan,O=JBoss,L=Red Hat" -validity 365 -keyalg RSA -keysize 2048
# Export the self-signed certificate
${KEYTOOL_CMD} -exportcert -alias client1 -file client1.cer -keystore client1-keystore.pfx -storepass Client1secret
# Import the signed certificate to a trust store
${KEYTOOL_CMD} -importcert -alias client1 -keystore client-truststore.pfx -file client1.cer -storepass ClientSecret -noprompt

# Generate the second client certificate
${KEYTOOL_CMD} -genkeypair -alias client2 -keystore client2-keystore.pfx -storepass Client2secret -dname "CN=Client2,OU=Infinispan,O=JBoss,L=Red Hat" -validity 365 -keyalg RSA -keysize 2048
# Export the self-signed certificate
${KEYTOOL_CMD} -exportcert -alias client2 -file client2.cer -keystore client2-keystore.pfx -storepass Client2secret
# Import the signed certificate to a trust store
${KEYTOOL_CMD} -importcert -alias client2 -keystore client-truststore.pfx -file client2.cer -storepass ClientSecret -noprompt
