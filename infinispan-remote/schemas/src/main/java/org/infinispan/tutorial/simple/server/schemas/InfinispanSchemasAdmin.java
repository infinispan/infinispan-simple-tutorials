package org.infinispan.tutorial.simple.server.schemas;

import java.util.Optional;

import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.RemoteSchemasAdmin;
import org.infinispan.protostream.schema.Schema;
import org.infinispan.protostream.schema.Type;
import org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper;

/**
 * Demonstrates how to use the Schema Admin API in hotrod.
 * Schemas can also be manipulated via the CLI,
 * the Server Web Console or directly with the REST API.
 *
 * @since 16.0
 */
public class InfinispanSchemasAdmin {
   static RemoteCacheManager remoteCacheManager;
   static RemoteSchemasAdmin schemasAdmin;
   static final String SCHEMA_NAME = "simple_tuto_hello.proto";

   public static void main(String[] args) {
      connectToInfinispan();

      RemoteSchemasAdmin.SchemaOpResult result = createSchemaWithError();
      System.out.println("Schema op type: " + result.getType());
      System.out.println("Has error: " + result.hasError());
      System.out.println("Error: " + result.getError());

      result = updateSchemaWithCorrections();
      System.out.println("Schema op type: " + result.getType());
      System.out.println("Has error: " + result.hasError());
      System.out.println("Error: " + result.getError());

      System.out.println("Get schema named " + SCHEMA_NAME);
      Optional<Schema> schemaOpt = getOptionalSchema();
      if (schemaOpt.isPresent()) {
         System.out.println("Schema Content: " + schemaOpt.get().getContent());
      }

      result = removeSchema();
      System.out.println("Schema op type: " + result.getType());

      boolean exists = schemaExists();
      System.out.println("Schema still exists ? " + exists);

      disconnect();
   }

   static RemoteSchemasAdmin.SchemaOpResult createSchemaWithError() {
      return schemasAdmin.createOrUpdate(Schema.
            buildFromStringContent(SCHEMA_NAME, "What is love?"));
   }

   static RemoteSchemasAdmin.SchemaOpResult updateSchemaWithCorrections() {
      Schema schema = new Schema.Builder(SCHEMA_NAME)
            .packageName("hello")
            .addMessage("Greeting")
            .addField(Type.Scalar.STRING, "content", 1)
            .build();
     return schemasAdmin.createOrUpdate(schema);
   }

   static Optional<Schema> getOptionalSchema() {
      Optional<Schema> schemaOpt = schemasAdmin.get(SCHEMA_NAME);
      return schemaOpt;
   }

   static RemoteSchemasAdmin.SchemaOpResult removeSchema() {
      RemoteSchemasAdmin.SchemaOpResult result;
      result = schemasAdmin.remove(SCHEMA_NAME);
      return result;
   }

   static boolean schemaExists() {
      return schemasAdmin.exists(SCHEMA_NAME);
   }

   static void connectToInfinispan() {
      // Connect to the server
      remoteCacheManager = TutorialsConnectorHelper.connect();
      schemasAdmin = remoteCacheManager.administration().schemas();
   }

   static void disconnect() {
      // Stop the cache manager and release all resources
      TutorialsConnectorHelper.stop(remoteCacheManager);
   }
}
