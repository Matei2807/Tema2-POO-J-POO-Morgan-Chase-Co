@Author:
- `Hutu Matei-Alexandru`
- `321CA`

## Proiect Etapa 2 - J. POO Morgan Chase & Co.

# Implementarea bancii #

## Clasele implementate ##
1. `Bank` - clasa care reprezinta banca in sine, cu toate operatiile pe care le
   poate efectua
2. `User` - clasa care reprezinta un utilizator al bancii
3. `Account` - clasa care reprezinta un cont bancar
    + Tipurile diferite de conturi ce pot fi create, care extind clasa Account
      (cont curent, cont economii)
4. `Transaction` - clasa care reprezinta o tranzactie
    + Tipurile diferite de tranzactii ce pot fi salvate, care extind clasa
      Transaction
5. `Card` - clasa care reprezinta un card bancar
6. `Commerciant` - clasa care reprezinta un comerciant
7. `ExchangeRate` - clasa care reprezinta un curs de schimb valutar

## Implementare ##

- Implementarea incepe din Main unde se creeaza o noua instanta a clasei Bank,
  care va efectua toate operatiile cerute
- Se citesc utilizatorii si cursurile de schimb valutar din fisierul de input
  (se salveaza si fiecare schimb valutar pe invers pentru a se putea face
  conversia in ambele sensuri)
- Se ruleaza operatiile cerute in functie de comanda citita
- In afara de functiile cerute in cardul bancii, am mai adaugat functii pentru
  gasirea unui utilizator sau cont in functie de IBAN/email/numar de card. Pentru
  o utilizare mai facila a acestor functii in clasa bank, am folosit si `Null
Object Pattern`(1) pentru a returna un utilizator/cont inexistent in loc de a
  returna null(si a verifica apoi daca obiectul returnat este null)
- De asemenea, a mai fost nevoie de implementarea unei functii pentru a gasi
  cursul valutar intre oricare 2 valute, folosind un algoritm recursiv
- Pentru a putea salva de fiecare data tranzactiile efectuate, am folosit un
  `Factory Pattern`(2) pentru a crea obiecte diferite care extind clasa Transaction,
  in functie de tipul tranzactiei efectuate
- Fiecare tranzactie este salvata intr-un vector de tranzactii in user-ul care
  a efectuat-o pentru o mai buna gestionare a acestora
- De asemenea, in utilizator sa salveaza si conturile sale, iar in fiecare
  cont, cardurile atasate, cat si toti comerciantii care au efectuat tranzactii
- cu acel cont


- `Added Strategy pattern for the cashback system`(3)
- `Added Singleton pattern for the bank`(4)
