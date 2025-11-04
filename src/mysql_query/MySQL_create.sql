create database if not exists DigitalLibrary;
use DigitalLibrary;

create table if not exists `Library` (
IdLibrary int auto_increment primary key,
Name varchar(20) not null unique
);

create table if not exists Book (
IdBook int auto_increment primary key,
Title varchar(50) not null,
Author varchar(30) not null,
Genre varchar(15) not null,
Subgenre varchar(15),
Description varchar(100),
FileDir varchar(100) not null,
IdLibrary int not null default 1,
foreign key(IdLibrary) references `Library`(IdLibrary)
);