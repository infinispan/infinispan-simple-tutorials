DROP TABLE IF EXISTS books;
DROP TABLE IF EXISTS authors;

CREATE TABLE authors (
                         id     INTEGER     NOT NULL IDENTITY PRIMARY KEY,
                         isbn   VARCHAR(30) NOT NULL,
                         name VARCHAR(30) NOT NULL,
                         country VARCHAR(30) NOT NULL
);

CREATE INDEX authors_isbn ON authors (isbn);

CREATE TABLE books (
  id     INTEGER     NOT NULL IDENTITY PRIMARY KEY,
  isbn   VARCHAR(30) NOT NULL,
  title  VARCHAR(50) NOT NULL,
  author_id INTEGER     NOT NULL
);

CREATE INDEX books_isbn ON books (isbn);

alter table if exists books
    add constraint author_id_constraint
        foreign key (author_id)
            references AUTHORS;
