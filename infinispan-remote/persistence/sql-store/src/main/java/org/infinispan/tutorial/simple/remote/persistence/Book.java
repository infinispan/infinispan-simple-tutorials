package org.infinispan.tutorial.simple.remote.persistence;

import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;

import java.util.Objects;

public class Book {
   private String isbn;
   private String title;
   private String authorName;
   private String country;

   @ProtoFactory
   public Book(String isbn, String title, String authorName, String country) {
      this.isbn = isbn;
      this.title = title;
      this.authorName = authorName;
      this.country = country;
   }

   @ProtoField(1)
   public String getIsbn() {
      return isbn;
   }

   public void setIsbn(String isbn) {
      this.isbn = isbn;
   }

   @ProtoField(2)
   public String getTitle() {
      return title;
   }

   public void setTitle(String title) {
      this.title = title;
   }

   @ProtoField(value = 3, name = "name")
   public String getAuthorName() {
      return authorName;
   }

   public void setAuthorName(String authorName) {
      this.authorName = authorName;
   }

   @ProtoField(4)
   public String getCountry() {
      return country;
   }

   public void setCountry(String country) {
      this.country = country;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o)
         return true;
      if (o == null || getClass() != o.getClass())
         return false;
      Book book = (Book) o;
      return Objects.equals(isbn, book.isbn) && Objects.equals(title, book.title) && Objects
            .equals(authorName, book.authorName) && Objects.equals(country, book.country);
   }

   @Override
   public int hashCode() {
      return Objects.hash(isbn, title, authorName, country);
   }

   @Override
   public String toString() {
      return "Book{" + "isbn='" + isbn + '\'' + ", title='" + title + '\'' + ", authorName='" + authorName + '\''
            + ", country='" + country + '\'' + '}';
   }
}
