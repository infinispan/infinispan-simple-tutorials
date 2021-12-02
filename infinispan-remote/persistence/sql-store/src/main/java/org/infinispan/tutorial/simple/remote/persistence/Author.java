package org.infinispan.tutorial.simple.remote.persistence;

import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;

import java.util.Objects;

public class Author {
   private int id;
   private String isbn;
   private String name;
   private String country;

   @ProtoFactory
   public Author(int id, String isbn, String name, String country) {
      this.id = id;
      this.isbn = isbn;
      this.name = name;
      this.country = country;
   }

   @ProtoField(value = 1, required = true)
   public int getId() {
      return id;
   }

   public void setId(int id) {
      this.id = id;
   }

   @ProtoField(2)
   public String getIsbn() {
      return isbn;
   }

   public void setIsbn(String isbn) {
      this.isbn = isbn;
   }

   @ProtoField(3)
   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   @ProtoField(4)
   public String getCountry() {
      return country;
   }

   public void setCountry(String country) {
      this.country = country;
   }

   @Override
   public String toString() {
      return "Author{" + "id=" + id + ", isbn='" + isbn + '\'' + ", name='" + name + '\'' + ", country='" + country
            + '\'' + '}';
   }

   @Override
   public boolean equals(Object o) {
      if (this == o)
         return true;
      if (o == null || getClass() != o.getClass())
         return false;
      Author author = (Author) o;
      return id == author.id && Objects.equals(isbn, author.isbn) && Objects.equals(name, author.name) && Objects
            .equals(country, author.country);
   }

   @Override
   public int hashCode() {
      return Objects.hash(id, isbn, name, country);
   }
}
