package org.infinispan.tutorial.simple.remote.query;

import org.infinispan.protostream.annotations.ProtoDoc;
import org.infinispan.protostream.annotations.ProtoField;

/**
 * This class is annotated with the infinispan Protostream support annotations.
 * With this method, you don't need to define a protobuf file and a marshaller for the object.
 * See this tutorial for additional explanations:
 *
 * https://blog.infinispan.org/2018/06/making-java-objects-queryable-by.html
 */
@ProtoDoc("@Indexed")
public final class Person {

   @ProtoDoc("@Field")
   @ProtoField(number = 1, required = true)
   String firstName;

   @ProtoDoc("@Field")
   @ProtoField(number = 2, required = true)
   String lastName;

   @ProtoDoc("@Field")
   @ProtoField(number = 3, required = true)
   int bornYear;

   @ProtoDoc("@Field")
   @ProtoField(number = 4, required = true)
   String bornIn;

   public Person() {

   }

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
