package org.infinispan.tutorial.simple.server.schemas;


import static org.infinispan.client.hotrod.RemoteSchemasAdmin.SchemaOpResultType.CREATED;
import static org.infinispan.client.hotrod.RemoteSchemasAdmin.SchemaOpResultType.UPDATED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.infinispan.client.hotrod.RemoteSchemasAdmin;
import org.junit.Test;

public class InfinispanSchemasAdminTest {

   @Test
   public void testSchemasCRUD() {
      InfinispanSchemasAdmin.connectToInfinispan();
      assertNotNull(InfinispanSchemasAdmin.schemasAdmin);

      InfinispanSchemasAdmin.removeSchema();
      assertFalse(InfinispanSchemasAdmin.schemaExists());

      RemoteSchemasAdmin.SchemaOpResult schemaWithError = InfinispanSchemasAdmin.createSchemaWithError();
      assertEquals(CREATED, schemaWithError.getType());
      assertTrue(schemaWithError.hasError());
      assertTrue(schemaWithError.getError().contains("Syntax error"));

      RemoteSchemasAdmin.SchemaOpResult schemaCorrect = InfinispanSchemasAdmin.updateSchemaWithCorrections();
      assertEquals(UPDATED, schemaCorrect.getType());
      assertFalse(schemaCorrect.hasError());

      assertTrue(InfinispanSchemasAdmin.schemaExists());
      assertTrue(InfinispanSchemasAdmin.getOptionalSchema().isPresent());

      InfinispanSchemasAdmin.disconnect();
   }
}