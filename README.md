# Apģērbu meklētājs

Mūsu RTU kursa projekta prototips.

**Ko lietotne dara:** tu augšupielādē apģērba bildi vai izvēlies krāsu, zīmolu un cenu, un lietotne parāda līdzīgus apģērbus no interneta veikaliem. Patīkamās preces var saglabāt izlasē.

**Komanda:** Vladlens Medvedevs, Darja Latišonoka, Kristīne Šaicāne, Violeta Žusele, Darja Lukjanova

## Kā palaist

1. Uzinstalē **Java 21** (piemēram, no adoptium.net).
2. Lejupielādē projektu:
```bash
   git clone https://github.com/vmedev/clothing-search-app.git
   cd clothing-search-app
```
3. Palaid:
   - macOS / Linux: `./mvnw spring-boot:run`
   - Windows: `mvnw.cmd spring-boot:run`
4. Atver pārlūkā **http://localhost:8080**

Vari arī atvērt projekta mapi VS Code vai IntelliJ IDEA un nospiest **Run** failā `ClothingSearchAppApplication.java`.

Meklēšanai pēc bildes vajag Google Gemini atslēgu. Pirms palaišanas ieraksti terminālī:
`export GEMINI_API_KEY=tava-atslēga` (macOS) vai `$env:GEMINI_API_KEY="tava-atslēga"` (Windows).

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
| Meklēt apģērbu | + | + |
| Skatīt preci un pāriet uz veikalu | + | + |
| Saglabāt meklēšanas vēsturi | — | + |
| Pievienot preces izlasei | — | + |

Ja viesis spiež «Pievienot izlasei», lietotne paskaidro, ka vispirms jāpiesakās.

## Kur kas atrodas

- `src/main/java/...` – programmas kods (Java)
- `src/main/resources/templates/` – lapas (HTML)
- `src/main/resources/static/` – dizains (CSS) un apģērbu bildes
- `src/main/resources/data.sql` – 50 apģērbu saraksts

## Svarīgi

Lietotnē nevar nopirkt apģērbu. Poga «Pāriet uz veikalu» atver preci veikala mājaslapā.