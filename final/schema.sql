CREATE TABLE bill
(
    id INTEGER PRIMARY KEY NOT NULL,
    rental INTEGER NOT NULL,
    price DOUBLE PRECISION DEFAULT 0.0
);
CREATE TABLE borrower
(
    id INTEGER PRIMARY KEY NOT NULL,
    firstname VARCHAR(16) NOT NULL,
    lastname VARCHAR(16) NOT NULL,
    street VARCHAR(32) NOT NULL,
    pc VARCHAR(16),
    city VARCHAR(32) NOT NULL
);
CREATE TABLE rental
(
    id INTEGER PRIMARY KEY NOT NULL,
    begintime DATE NOT NULL,
    endtime DATE NOT NULL,
    borrower INTEGER,
    ended BOOLEAN DEFAULT false NOT NULL,
    insurance BOOLEAN DEFAULT false
);
CREATE TABLE specimen
(
    id INTEGER PRIMARY KEY NOT NULL,
    fuel INTEGER DEFAULT 0 NOT NULL,
    state INTEGER DEFAULT 0 NOT NULL,
    kilometers INTEGER DEFAULT 0 NOT NULL,
    registrationplate VARCHAR(16) NOT NULL,
    vehicle INTEGER NOT NULL,
    rental INTEGER
);
CREATE TABLE "user"
(
    id INTEGER PRIMARY KEY NOT NULL,
    email VARCHAR(32) NOT NULL,
    username VARCHAR(16) NOT NULL,
    password VARCHAR(64) NOT NULL,
    firstname VARCHAR(16) NOT NULL,
    lastname VARCHAR(16) NOT NULL,
    avatar VARCHAR(128) NOT NULL
);
CREATE TABLE vehicle
(
    id INTEGER PRIMARY KEY NOT NULL,
    type INTEGER NOT NULL,
    priceday INTEGER NOT NULL,
    brand VARCHAR(32) NOT NULL,
    model VARCHAR(32),
    cylinder INTEGER,
    luxury INTEGER DEFAULT 0
);
ALTER TABLE bill ADD FOREIGN KEY (rental) REFERENCES (id) ;
ALTER TABLE specimen ADD FOREIGN KEY (vehicle) REFERENCES vehicle (id);
ALTER TABLE specimen ADD FOREIGN KEY (rental) REFERENCES rental (id);
CREATE UNIQUE INDEX specimen_registrationplate_uindex ON specimen (registrationplate);
CREATE UNIQUE INDEX user2_email_uindex ON "user" (email);
CREATE UNIQUE INDEX user2_username_uindex ON "user" (username);

INSERT INTO public."user" (id, email, username, password, firstname, lastname, avatar) VALUES (1, 'fabien.beaujean@hotmail.fr', 'fabsgc', '61b115da227a57859490c53251490fffe66c6d67', 'Fabien', 'Beaujean', 'img/user.png');

INSERT INTO public.vehicle (id, type, priceday, brand, model, cylinder, luxury) VALUES (1, 1, 150, 'Peugeot', '206', 0, 0);
INSERT INTO public.vehicle (id, type, priceday, brand, model, cylinder, luxury) VALUES (2, 1, 250, 'Renault', '306', 0, 1);
INSERT INTO public.vehicle (id, type, priceday, brand, model, cylinder, luxury) VALUES (3, 1, 10, 'Fiat', '500', 0, 1);
INSERT INTO public.vehicle (id, type, priceday, brand, model, cylinder, luxury) VALUES (4, 2, 10, 'Triumph', '', 800, 0);
INSERT INTO public.vehicle (id, type, priceday, brand, model, cylinder, luxury) VALUES (6, 1, 10, 'Lexus', '500', 0, 1);
INSERT INTO public.vehicle (id, type, priceday, brand, model, cylinder, luxury) VALUES (7, 1, 32, 'FIAT', 'FIAT 500', 0, 0);
INSERT INTO public.vehicle (id, type, priceday, brand, model, cylinder, luxury) VALUES (8, 1, 1290, 'Lamborghini', 'Lamborghini Huracan', 0, 1);
INSERT INTO public.vehicle (id, type, priceday, brand, model, cylinder, luxury) VALUES (9, 2, 170, 'Honda', '', 500, 0);

INSERT INTO public.specimen (id, fuel, state, kilometers, registrationplate, vehicle, rental) VALUES (9, 75, 1, 220055, 'AB456745', 2, null);
INSERT INTO public.specimen (id, fuel, state, kilometers, registrationplate, vehicle, rental) VALUES (8, 100, 1, 22000, 'AB456791', 1, null);
INSERT INTO public.specimen (id, fuel, state, kilometers, registrationplate, vehicle, rental) VALUES (11, 100, 1, 4689, 'AB456792', 7, null);
INSERT INTO public.specimen (id, fuel, state, kilometers, registrationplate, vehicle, rental) VALUES (12, 100, 1, 568, 'AB456793', 7, null);
INSERT INTO public.specimen (id, fuel, state, kilometers, registrationplate, vehicle, rental) VALUES (13, 100, 1, 3450, 'AB456794', 7, null);
INSERT INTO public.specimen (id, fuel, state, kilometers, registrationplate, vehicle, rental) VALUES (14, 100, 1, 6896, 'AB456795', 7, null);
INSERT INTO public.specimen (id, fuel, state, kilometers, registrationplate, vehicle, rental) VALUES (15, 100, 1, 1234, 'AB456796', 7, null);
INSERT INTO public.specimen (id, fuel, state, kilometers, registrationplate, vehicle, rental) VALUES (16, 100, 1, 3646, 'AB456797', 7, null);
INSERT INTO public.specimen (id, fuel, state, kilometers, registrationplate, vehicle, rental) VALUES (17, 100, 1, 9627, 'AB456798', 7, null);
INSERT INTO public.specimen (id, fuel, state, kilometers, registrationplate, vehicle, rental) VALUES (18, 100, 1, 8654, 'AB456799', 7, null);
INSERT INTO public.specimen (id, fuel, state, kilometers, registrationplate, vehicle, rental) VALUES (19, 100, 1, 5384, 'AB456800', 7, null);
INSERT INTO public.specimen (id, fuel, state, kilometers, registrationplate, vehicle, rental) VALUES (22, 100, 1, 5673, 'AB456803', 8, null);
INSERT INTO public.specimen (id, fuel, state, kilometers, registrationplate, vehicle, rental) VALUES (23, 100, 1, 6891, 'AB456804', 8, null);
INSERT INTO public.specimen (id, fuel, state, kilometers, registrationplate, vehicle, rental) VALUES (24, 100, 1, 6837, 'AB456805', 8, null);
INSERT INTO public.specimen (id, fuel, state, kilometers, registrationplate, vehicle, rental) VALUES (25, 100, 1, 4963, 'AB456806', 8, null);
INSERT INTO public.specimen (id, fuel, state, kilometers, registrationplate, vehicle, rental) VALUES (26, 100, 1, 7390, 'AB456807', 8, null);
INSERT INTO public.specimen (id, fuel, state, kilometers, registrationplate, vehicle, rental) VALUES (27, 100, 1, 8907, 'AB456808', 8, null);
INSERT INTO public.specimen (id, fuel, state, kilometers, registrationplate, vehicle, rental) VALUES (28, 100, 1, 4628, 'AB456809', 8, null);
INSERT INTO public.specimen (id, fuel, state, kilometers, registrationplate, vehicle, rental) VALUES (29, 100, 1, 5830, 'AB456810', 8, null);
INSERT INTO public.specimen (id, fuel, state, kilometers, registrationplate, vehicle, rental) VALUES (34, 100, 1, 5732, 'AB456815', 9, null);
INSERT INTO public.specimen (id, fuel, state, kilometers, registrationplate, vehicle, rental) VALUES (35, 100, 1, 7984, 'AB456816', 9, null);
INSERT INTO public.specimen (id, fuel, state, kilometers, registrationplate, vehicle, rental) VALUES (36, 100, 1, 9632, 'AB456817', 9, null);
INSERT INTO public.specimen (id, fuel, state, kilometers, registrationplate, vehicle, rental) VALUES (37, 100, 1, 4738, 'AB456818', 9, null);
INSERT INTO public.specimen (id, fuel, state, kilometers, registrationplate, vehicle, rental) VALUES (38, 100, 1, 6387, 'AB456819', 9, null);
INSERT INTO public.specimen (id, fuel, state, kilometers, registrationplate, vehicle, rental) VALUES (39, 100, 1, 7893, 'AB456820', 9, null);
INSERT INTO public.specimen (id, fuel, state, kilometers, registrationplate, vehicle, rental) VALUES (40, 100, 1, 7906, 'AB456821', 9, null);
INSERT INTO public.specimen (id, fuel, state, kilometers, registrationplate, vehicle, rental) VALUES (41, 100, 1, 8796, 'AB456822', 9, null);
INSERT INTO public.specimen (id, fuel, state, kilometers, registrationplate, vehicle, rental) VALUES (42, 100, 1, 6302, 'AB456823', 9, null);
INSERT INTO public.specimen (id, fuel, state, kilometers, registrationplate, vehicle, rental) VALUES (43, 100, 1, 9732, 'AB456824', 9, null);
INSERT INTO public.specimen (id, fuel, state, kilometers, registrationplate, vehicle, rental) VALUES (47, 0, 1, 9472, 'AB456829', 6, null);
INSERT INTO public.specimen (id, fuel, state, kilometers, registrationplate, vehicle, rental) VALUES (48, 100, 1, 4381, 'AB456830', 6, null);
INSERT INTO public.specimen (id, fuel, state, kilometers, registrationplate, vehicle, rental) VALUES (49, 100, 1, 1649, 'AB456831', 6, null);
INSERT INTO public.specimen (id, fuel, state, kilometers, registrationplate, vehicle, rental) VALUES (50, 100, 1, 9472, 'AB456832', 6, null);
INSERT INTO public.specimen (id, fuel, state, kilometers, registrationplate, vehicle, rental) VALUES (51, 100, 1, 1749, 'AB456833', 6, null);
INSERT INTO public.specimen (id, fuel, state, kilometers, registrationplate, vehicle, rental) VALUES (52, 100, 1, 7983, 'AB456834', 6, null);
INSERT INTO public.specimen (id, fuel, state, kilometers, registrationplate, vehicle, rental) VALUES (53, 0, 1, 3568, 'AB456835', 6, null);
INSERT INTO public.specimen (id, fuel, state, kilometers, registrationplate, vehicle, rental) VALUES (54, 100, 1, 45689, 'AB456836', 6, null);
INSERT INTO public.specimen (id, fuel, state, kilometers, registrationplate, vehicle, rental) VALUES (55, 100, 1, 4367, 'AB456837', 6, null);
INSERT INTO public.specimen (id, fuel, state, kilometers, registrationplate, vehicle, rental) VALUES (56, 100, 1, 5637, 'AB456838', 6, null);
INSERT INTO public.specimen (id, fuel, state, kilometers, registrationplate, vehicle, rental) VALUES (2, 75, 1, 4, 'AB456790', 1, null);
INSERT INTO public.specimen (id, fuel, state, kilometers, registrationplate, vehicle, rental) VALUES (10, 50, 0, 9004, 'AB456750', 3, null);
INSERT INTO public.specimen (id, fuel, state, kilometers, registrationplate, vehicle, rental) VALUES (1, 100, 1, 7, 'AB456789', 1, null);

INSERT INTO public.borrower (id, firstname, lastname, street, pc, city) VALUES (2, 'Luc', 'Lorentz', 'rue de Rambouillet', '78001', 'Rambouillet');
INSERT INTO public.borrower (id, firstname, lastname, street, pc, city) VALUES (1, 'Fabien', 'Beaujean', '1 allée des mûriers', '91370', 'Verrières-le-Buisson');
INSERT INTO public.borrower (id, firstname, lastname, street, pc, city) VALUES (5, 'Martin', 'Prieur', '1 allée des prûniers', '78009', 'Jouy en Josas');
INSERT INTO public.borrower (id, firstname, lastname, street, pc, city) VALUES (6, 'Abel', 'Derderrian', '28 Sandon Road', 'ST16 3ES', 'Stafford');
INSERT INTO public.borrower (id, firstname, lastname, street, pc, city) VALUES (8, 'Lahlou', 'Belabbas', '2 rue du temple de la foi', '75009', 'Paris');
INSERT INTO public.borrower (id, firstname, lastname, street, pc, city) VALUES (9, 'Elsa', 'Cto', 'rue de Dourdan', '91300', 'Dourdan');
INSERT INTO public.borrower (id, firstname, lastname, street, pc, city) VALUES (11, 'Adbelkrim', 'Lahlou', 'rue de quelque part', '75005', 'Paris');
INSERT INTO public.borrower (id, firstname, lastname, street, pc, city) VALUES (12, 'Efrei', 'Esigetel', '30-32 Avenue de la République', '94800', 'Villejuif');
INSERT INTO public.borrower (id, firstname, lastname, street, pc, city) VALUES (7, 'Fatym', 'Lagrou', '27 place de la Nation', '75006', 'Paris');
INSERT INTO public.borrower (id, firstname, lastname, street, pc, city) VALUES (10, 'Valentin', 'Boitel-Denyzet', 'rue du parc', '69001', 'Lyon');

INSERT INTO public.rental (id, begintime, endtime, borrower, ended, insurance) VALUES (4, '2016-05-08', '2016-06-21', 1, true, false);
INSERT INTO public.rental (id, begintime, endtime, borrower, ended, insurance) VALUES (7, '2016-05-18', '2016-05-27', 2, true, false);
INSERT INTO public.rental (id, begintime, endtime, borrower, ended, insurance) VALUES (6, '2016-05-08', '2016-05-20', 2, true, false);

INSERT INTO public.bill (id, rental, price) VALUES (11, 4, 360);
INSERT INTO public.bill (id, rental, price) VALUES (12, 7, 237);
INSERT INTO public.bill (id, rental, price) VALUES (13, 6, 175);