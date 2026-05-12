package org.infinispan.tutorial.simple.query;

import org.infinispan.api.annotations.indexing.Basic;
import org.infinispan.api.annotations.indexing.Indexed;

// tag::entity[]
@Indexed
public class Person {
   @Basic
   String name;

   @Basic
   String surname;

   public Person(String name, String surname) {
      this.name = name;
      this.surname = surname;
   }
// end::entity[]

   @Override
   public String toString() {
      return "Person [name=" + name + ", surname=" + surname + "]";
   }
}
