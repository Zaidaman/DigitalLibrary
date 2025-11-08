create database if not exists DigitalLibrary;
use DigitalLibrary;

create table if not exists LibUser (
IdUser int auto_increment primary key,
Username varchar(20) not null unique,
UserPass varchar(15) not null
);

create table if not exists `Library` (
IdLibrary int auto_increment primary key,
LibName varchar(20) not null unique,
IdUser int not null default 1,
foreign key(IdUser) references LibUser(IdUser)
);

create table if not exists Genre(
IdGenre int auto_increment primary key,
GenreName varchar(20) not null unique
);

create table if not exists Author(
IdAuthor int auto_increment primary key,
AuthorName varchar(20) not null,
MidName varchar(20),
Surname varchar(20) not null
);

create table if not exists Book (
IdBook int auto_increment primary key,
Title varchar(50) not null,
IdAuthor int not null default 1,
AnnoPub int,
BookFile mediumblob not null,
IdLibrary int not null default 1,
foreign key(IdAuthor) references Author(IdAuthor),
foreign key(IdLibrary) references `Library`(IdLibrary)
);

create table if not exists BookGenre (
IdBook int not null,
IdGenre int not null,
primary key(IdBook, IdGenre),
foreign key(IdBook) references Book(IdBook),
foreign key(IdGenre) references Genre(IdGenre)
);