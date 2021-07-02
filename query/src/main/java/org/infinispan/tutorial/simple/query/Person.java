package org.infinispan.tutorial.simple.query;


import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.KeywordField;

@Indexed
public class Person {
   @KeywordField
   String name;

   @KeywordField
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