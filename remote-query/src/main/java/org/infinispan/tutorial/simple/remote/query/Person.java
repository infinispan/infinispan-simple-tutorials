package org.infinispan.tutorial.simple.remote.query;

import org.infinispan.protostream.annotations.ProtoField;

/**
 * This class is annotated with the infinispan Protostream support annotations.
 * With this method, you don't need to define a protobuf file and a marshaller for the object.
 */
public final class Person {

   @ProtoField(number = 1, defaultValue = "")
   String firstName;

   @ProtoField(number = 2, defaultValue = "")
   String lastName;

   @ProtoField(number = 3, defaultValue = "0")
   int bornYear;

   @ProtoField(number = 4, defaultValue = "")
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
