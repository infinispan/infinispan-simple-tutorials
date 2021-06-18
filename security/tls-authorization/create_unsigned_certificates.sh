# Create a basic keystore with PKCS12
# Use rhdg-server as the alias and 'secret' as the password
keytool -genkeypair -alias infinispan-server -keystore server-keystore.pfx -keyalg RSA -dname "CN=Server,OU=Infinispan,O=JBoss,L=Red Hat" -storepass secret -validity 365 -keysize 4096
# Export the self-signed certificate
keytool -exportcert -alias infinispan-server -file server-cert.cer -keystore server-keystore.pfx -storepass secret
# Import the certificate to a client trust store
# Use 'trustSecret' as the password for the client trust store
keytool -import -v -trustcacerts -alias infinispan-server -file server-cert.cer -keystore server-truststore.pfx -storepass trustSecret -noprompt

# Generate the first client certificate
keytool -genkeypair -alias client1 -keystore client1-keystore.pfx -storepass Client1secret -dname "CN=Client1,OU=Infinispan,O=JBoss,L=Red Hat" -validity 365 -keyalg RSA -keysize 2048
# Export the self-signed certificate
keytool -exportcert -alias client1 -file client1.cer -keystore client1-keystore.pfx -storepass Client1secret
# Import the signed certificate to a trust store
keytool -importcert -alias client1 -keystore client-truststore.pfx -file client1.cer -storepass ClientSecret -noprompt

# Generate the second client certificate
keytool -genkeypair -alias client2 -keystore client2-keystore.pfx -storepass Client2secret -dname "CN=Client2,OU=Infinispan,O=JBoss,L=Red Hat" -validity 365 -keyalg RSA -keysize 2048
# Export the self-signed certificate
keytool -exportcert -alias client2 -file client2.cer -keystore client2-keystore.pfx -storepass Client2secret
# Import the signed certificate to a trust store
keytool -importcert -alias client2 -keystore client-truststore.pfx -file client2.cer -storepass ClientSecret -noprompt
