package org.infinispan.tutorial.simple.remote.query;

import org.infinispan.api.annotations.indexing.Basic;
import org.infinispan.api.annotations.indexing.Indexed;
import org.infinispan.api.annotations.indexing.Keyword;
import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;

/**
 * This class is annotated with the infinispan Protostream support annotations.
 * With this method, you don't need to define a protobuf file and a marshaller for the object.
 */
@Indexed
public final class Person {

   @ProtoField(number = 1)
   @Keyword(projectable = true, sortable = true, normalizer = "lowercase", indexNullAs = "unnamed", norms = false)
   String firstName;

   @ProtoField(number = 2)
   @Keyword(projectable = true, sortable = true, normalizer = "lowercase", indexNullAs = "unnamed", norms = false)
   String lastName;

   @ProtoField(number = 3, defaultValue = "-1")
   @Basic
   int bornYear;

   @ProtoField(number = 4)
   @Keyword(projectable = true, sortable = true, normalizer = "lowercase", indexNullAs = "unnamed", norms = false)
   String bornIn;

   @ProtoFactory
   public Person(String firstName, String lastName, int bornYear, String bornIn) {
      this.firstName = firstName;
      this.lastName = lastName;
      this.bornYear = bornYear;
      this.bornIn = bornIn;
   }

   @Override
   public String toString() {
      return "Person{" +
            "firstName='" + firstName + '\'' +
            ", lastName='" + lastName + '\'' +
            ", bornYear='" + bornYear + '\'' +
            ", bornIn='" + bornIn + '\'' +
            '}';
   }
}
