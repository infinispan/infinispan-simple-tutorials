package org.infinispan.tutorial.simple.remote.marshalling;

import org.infinispan.protostream.schema.Schema;
import org.infinispan.protostream.schema.Type;

public final class MagazineSchemaCreator {
    static public Schema magazineSchema() {
        return new Schema.Builder("magazine.proto")
                .packageName("magazine_sample")
                .addMessage("Magazine")
                .addField(Type.Scalar.STRING, "name", 1)
                .addField(Type.Scalar.INT32, "publicationYear", 2)
                .addField(Type.Scalar.INT32, "publicationMonth", 3)
                .addRepeatedField(Type.Scalar.STRING, "stories", 4)
                .build();
    }
}
