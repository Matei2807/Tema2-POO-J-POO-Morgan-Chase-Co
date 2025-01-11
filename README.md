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
      (cont curent, cont economii, cont de business)
4. `Transaction` - clasa care reprezinta o tranzactie
    + Tipurile diferite de tranzactii ce pot fi salvate, care extind clasa
      Transaction
5. `Card` - clasa care reprezinta un card bancar
6. `Commerciant` - clasa care reprezinta un comerciant
7. `ExchangeRate` - clasa care reprezinta un curs de schimb valutar
8. `SplitPayment` - clasa care reprezinta o plata impartita
9. `Date` - clasa care reprezinta o data
10. `TransactionInfoForCashback` - clasa care reprezinta informatiile necesare
    pentru a calcula cashback-ul
11. `CashbackStrategy` - interfata care defineste metoda de calcul a cashback-ului
    + Tipurile diferite de strategii de cashback, care implementeaza interfata
      (SpendingTreshold, NrOfTransactions)

## Implementare ##

- Implementarea incepe din Main unde se foloseste de instanta clasei Bank pentru
  a efectua operatiile cerute. Clasa Bank este implementata ca `Singleton 
pattern`(1) pentru a asigura ca exista o singura instanta a acesteia
- Se citesc utilizatorii, cursurile de schimb valutar din fisierul de input si
comerciantii (se salveaza si fiecare schimb valutar pe invers pentru a se putea
face conversia in ambele sensuri)
- Se ruleaza operatiile cerute in functie de comanda citita
- In afara de functiile cerute in cardul bancii, am mai adaugat functii pentru
gasirea unui utilizator sau cont in functie de IBAN/email/numar de card. Pentru
o utilizare mai facila a acestor functii in clasa bank, am folosit si `Null
Object Pattern`(2) pentru a returna un utilizator/cont inexistent in loc de a
returna null(si a verifica apoi daca obiectul returnat este null)
- De asemenea, a mai fost nevoie de implementarea unei functii pentru a gasi
cursul valutar intre oricare 2 valute, folosind un algoritm recursiv
- Pentru a putea salva de fiecare data tranzactiile efectuate, am folosit un
`Factory Pattern`(3) pentru a crea obiecte diferite care extind clasa Transaction,
in functie de tipul tranzactiei efectuate
- Fiecare tranzactie este salvata intr-un vector de tranzactii in user-ul care
a efectuat-o pentru o mai buna gestionare a acestora
- De asemenea, in utilizator sa salveaza si conturile sale, iar in fiecare
cont, cardurile atasate, cat si toti comerciantii care au efectuat tranzactii
cu acel cont
- Pentru noile implementari necesare etapei 2, a trebuit sa se faca cateva
modificari in clase. In Bank va trebui sa retinem si toti comerciantii, cu
tranzactiile efectuate cu acestia, cat si o coada de plati impartite(ce
urmeaza a fi efectuate sau refuzate)
- In clasa User mai trebuie retinut cat si 'voucher-urile' pe care le-a primit
sau folosit. In clasa Account trebuie retinut si comerciantii cu care s-a
efectuat o tranzactie, cat si informatiile necesare pentru a calcula cashback-ul
- Pentru implementarea cashback-ului, am folosit `Strategy Pattern`(4) prin
implementarea unei interfete CashbackStrategy, cu 2 tipuri de strategii 
implementate: SpendingTreshold si NrOfTransactions. Cele 2 strategii sunt
folosite in functie de tranzactiile efectuate si tipul contului
- Pentru calcularea comisionului, am folosit o functie creata in clasa Bank care
calculeaza comisionul in functie de tipul contului si suma tranzactionata
- In cazul platiilor impartite, se foloseste o coada ce retine instante de
clasa SplitPayment, care contine informatiile necesare pentru a efectua plata.
Ele sunt apoi efectuate in ordinea in care au fost adaugate in coada(si
acceptate)
- Pentru noile tipuri de business reports, am implementat o noua clasa
BusinessAccountTransaction, care retine toate informatiile necesare ale
unei tranzactii efectuate cu un cont de business. Aceste informatii sunt
salvate intr-o lista in clasa BusinessAccount(ce extinde clasa Account)

## Pattern-uri folosite ##
1. `Singleton Pattern` - folosit pentru a asigura ca exista o singura instanta a
   clasei Bank
2. `Null Object Pattern` - folosit pentru a returna un utilizator/cont inexistent
   in loc de a returna null(utilizare mai facila)
3. `Factory Pattern` - folosit pentru a crea obiecte diferite care extind clasa
   Transaction, in functie de tipul tranzactiei efectuate
4. `Strategy Pattern` - folosit pentru a calcula cashback-ul in functie de
   tranzactiile efectuate si tipul contului
