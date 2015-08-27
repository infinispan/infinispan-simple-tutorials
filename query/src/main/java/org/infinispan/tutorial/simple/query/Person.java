package org.infinispan.tutorial.simple.query;

import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;

@Indexed
public class Person {
   @Field(store = Store.YES, analyze = Analyze.NO)
   String name;

   @Field(store = Store.YES, analyze = Analyze.NO, indexNullAs = Field.DEFAULT_NULL_TOKEN)
   String surname;

   public Person(String name, String surname) {
      this.name = name;
      this.surname = surname;
   }

   @Override
   public String toString() {
      return "Person [name=" + name + ", surname=" + surname + "]";
   }
}