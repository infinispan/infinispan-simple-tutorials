package org.infinispan.tutorial.simple.hibernate.cache.local.model;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
@Cacheable
public class Person {

   @Id
   @GeneratedValue
   private Long id;
   private String name;

   public Person(String name) {
      this.name = name;
   }

   public Person() {
   }

   public Long getId() {
      return id;
   }

   public void setId(Long id) {
      this.id = id;
   }

   public String getName() {
      return name;
   }

   public void setName(String firstName) {
      this.name = firstName;
   }

   @Override
   public String toString() {
      return "Person{" +
            "id=" + id +
            ", name='" + name + '\'' +
            '}';
   }

}
