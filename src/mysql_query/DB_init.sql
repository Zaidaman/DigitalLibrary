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

insert into libuser (Username, Userpass, IsAdmin) values
('admin', 12345, 1);

insert into libraries (LibName) values
('Test Library');

insert into book (Title, IdAuthor, AnnoPub, BookFile) values
('I promessi sposi', 1, 1840, 'pdf/I promessi sposi.pdf'),
('The hobbit', 2, 1937, 'pdf/The Hobbit.pdf'),
('The war of the worlds', 3, 1898, 'pdf/The war of the worlds.pdf');

insert into bookgenre (IdBook, IdGenre) values
('1','3'),
('2','1'),
('3','2');

insert into booklib (IdBook, IdLibrary) values
('1','1'),
('2','1'),
('3','1');

insert into libaccess (IdUser, IdLibrary) values
('1','1');