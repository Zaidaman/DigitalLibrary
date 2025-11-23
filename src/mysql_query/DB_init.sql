use digitallibrary;

insert into author (AuthorName, MidName, Surname) values
('Alessandro','','Manzoni'),
('John','Ronald Reuel','Tolkien'),
('Herbert','George','Wells');

insert into genre (GenreName) values
('Fantasy'),
('Science Fiction'),
('Drama'),
('Noir'),
('Horror');

insert into libuser (Username, Userpass) values
('admin', 12345);

insert into libraries (LibName, IdUser) values
('Test Library', 1);

insert into book (Title, IdAuthor, AnnoPub, BookFile) values
('I promessi sposi', 1, 1840, 'C:/ProgramData/MySQL/MySQL Server 9.4/Uploads/I promessi sposi.pdf'),
('The hobbit', 2, 1937, 'C:/ProgramData/MySQL/MySQL Server 9.4/Uploads/The Hobbit.pdf'),
('The war of the worlds', 3, 1898, 'C:/ProgramData/MySQL/MySQL Server 9.4/Uploads/The war of the worlds.pdf');

insert into bookgenre (IdBook, IdGenre) values
('1','3'),
('2','1'),
('3','2');

insert into booklib (IdBook, IdLibrary) values
('1','1'),
('2','1'),
('3','1');