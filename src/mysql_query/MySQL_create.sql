create database if not exists DigitalLibrary;
use DigitalLibrary;

create table if not exists LibUser (
IdUser int auto_increment primary key,
Username varchar(20) not null unique,
UserPass varchar(15) not null,
FirstLogin tinyint(1) not null default 1,
IsAdmin tinyint(1) not null default 0,
ChosenPath varchar(100) unique
);

create table if not exists Libraries (
IdLibrary int auto_increment primary key,
LibName varchar(20) not null unique
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
IdAuthor int,
AnnoPub int,
BookFile varchar(400) not null,
foreign key(IdAuthor) references Author(IdAuthor)
);

create table if not exists BookGenre (
IdBook int not null,
IdGenre int not null,
primary key(IdBook, IdGenre),
foreign key(IdBook) references Book(IdBook),
foreign key(IdGenre) references Genre(IdGenre)
);

create table if not exists BookLib (
IdBook int not null,
IdLibrary int not null,
primary key(IdBook, IdLibrary),
foreign key(IdBook) references Book(IdBook),
foreign key(IdLibrary) references Libraries(IdLibrary)
);

create table if not exists LibAccess(
IdUser int not null,
IdLibrary int not null,
foreign key(IdUser) references LibUser(IdUser),
foreign key(IdLibrary) references Libraries(IdLibrary)
);