# Apģērbu meklētājs

Mūsu RTU kursa projekta prototips.

**Ko lietotne dara:** tu augšupielādē apģērba bildi vai izvēlies kategoriju, krāsu, zīmolu un cenu, un lietotne parāda līdzīgus apģērbus no interneta veikaliem. Bildē redzamo apģērbu atpazīst mākslīgais intelekts (Google Gemini). Patīkamās preces var saglabāt izlasē, un visi meklējumi tiek saglabāti vēsturē.

**Komanda:** Vladlens Medvedevs, Darja Latišonoka, Kristīne Šaicāne, Violeta Žusele, Darja Lukjanova

## Kā palaist

1. Uzinstalē **Java 21** (piemēram, no adoptium.net).
2. Lejupielādē projektu:
   ```bash
   git clone https://github.com/vmedev/clothing-search-app.git
   cd clothing-search-app
   ```
3. Meklēšanai pēc bildes vajag Google Gemini atslēgu (bez maksas: [aistudio.google.com](https://aistudio.google.com) → Get API key). Projekta mapē nokopē failu `.env.example` ar nosaukumu `.env` un ieraksti tajā savu atslēgu:
   ```
   GEMINI_API_KEY=tava-atslēga
   ```
   Bez atslēgas lietotne arī strādā, tikai meklējot pēc bildes, kategoriju un krāsu būs jāizvēlas pašam.
4. Palaid:
    - macOS / Linux: `./mvnw spring-boot:run`
    - Windows: `mvnw.cmd spring-boot:run`
5. Atver pārlūkā **http://localhost:8080**

Vari arī atvērt projekta mapi IntelliJ IDEA un nospiest **Run** failā `ClothingSearchAppApplication.java`.

Pirmajā palaišanā datubāze izveidojas pati un tajā ielādējas 50 apģērbi.

## Kā ieiet

Atverot lietotni, parādās pieteikšanās forma. Ir trīs iespējas:

- **Ieiet ar gatavu kontu:**

  | Lietotājvārds | Parole |
    |---|---|
  | `demo` | `demo1234` |
  | `admin` | `admin1234` |

- **Reģistrēties:** izdomā lietotājvārdu, e-pastu un paroli (vismaz 8 simboli un 1 cipars).
- **Ieiet kā viesis:** bez konta.

## Ko var viesis un ko lietotājs ar kontu

| | Viesis | Ar kontu |
|---|---|---|
| Meklēt apģērbu pēc bildes un pēc filtriem | + | + |
| Skatīt preci un pāriet uz veikalu | + | + |
| Saglabāt, labot un dzēst meklēšanas vēsturi | — | + |
| Pievienot preces izlasei | — | + |

Ja viesis spiež «Pievienot izlasei» vai atver vēsturi, lietotne paskaidro, ka vispirms jāpiesakās.

## Lapas

**Strādā pilnībā (dati tiek nolasīti, apstrādāti un saglabāti datubāzē):**

| Lapa | Adrese | Kas notiek |
|---|---|---|
| Meklēt pēc bildes | `/upload` | Bildi var izvēlēties, ievilkt vai ielīmēt ar Ctrl+V. MI nosaka kategoriju un krāsu, lietotne parāda līdzīgākās preces un saglabā meklējumu vēsturē. |
| Meklēt pēc filtriem | `/search` | Kategorija, krāsa, zīmols, cena no/līdz. |
| Meklēšanas vēsture | `/history` | Saviem meklējumiem: atkārtot, labot, dzēst. |
| Preces kartīte | `/product/{id}` | Cena, zīmols, krāsa, veikals, pievienošana izlasei. |
| Izlase | `/favorites` | Saglabātās preces, noņemšana. |
| Pieteikšanās un reģistrācija | `/login`, `/register` | Konti tiek glabāti datubāzē, paroles šifrētas. |

**Tikai izskats ar iekodētiem datiem:**

- Paziņojumi par cenu izmaiņām: `/notifications`
- Lietotāja profils: `/profile`
- Administratora panelis: `/admin`

## Atšķirības no dokumentācijas

- Dokumentācijā lietotne meklē līdzīgas preces dažādās mājaslapās. Prototipā bildi atpazīst Gemini MI (kategorija, krāsa, stils), un līdzīgās preces tiek meklētas mūsu katalogā ar 50 reālām precēm no 9 veikaliem (Zalando, Cropp, House, H&M, Zara, Reserved, About You, Pull&Bear, Sinsay). Katrai precei ir saite uz īsto preces lapu veikalā.
- Ja MI neatbild, lietotājs kategoriju un krāsu izvēlas pats, lai meklēšana tik un tā strādātu.
- Paziņojumi, profila labošana, saglabātie filtri un administratora funkcijas prototipā ir tikai vizuāli.

## Kur kas atrodas

- `src/main/java/...` – programmas kods (Java, Spring Boot)
- `src/main/resources/templates/` – lapas (HTML, Thymeleaf)
- `src/main/resources/static/` – dizains (CSS) un apģērbu bildes
- `src/main/resources/data.sql` – 50 apģērbu saraksts
- `uploads/` – lietotāju augšupielādētās bildes (izveidojas automātiski)

## Svarīgi

Lietotnē nevar nopirkt apģērbu. Poga «Pāriet uz veikalu» atver preci veikala mājaslapā.