# --- First database schema
 
# --- !Ups
 
CREATE TABLE books (
    id                        SERIAL PRIMARY KEY,
    name                      VARCHAR(255) NOT NULL,
    filename                  VARCHAR(255) NOT NULL
    );
     
# --- !Downs
 
DROP TABLE IF EXISTS books;

