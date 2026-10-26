# Campus Lost & Found Matching System (Java, OOP project)

A complete Java backend: console app + REST web server + website, built to demonstrate Object-Oriented Programming.
No Maven, no Spring, no external libraries. Only the JDK.

- Report **lost** and **found** items, search them, and get **automatic match suggestions** with a score out of 100.
- Data is saved in `data/lost.csv` and `data/found.csv`.
- Two ways to use it: the **console menu** or the **website** (served by the Java backend).
- **43 automatic tests** prove that every feature works (`TestRunner`).

---

## 1. Setup in VS Code (first time)

1. Install **JDK 17 or 21** (adoptium.net, Temurin). Check in a terminal: `java -version` and `javac -version`.
2. Install **VS Code** and add the extension **"Extension Pack for Java"** (Extensions tab, search for it).
3. **File -> Open Folder** and choose this project folder (the one that contains `src`, `web`, `README.md`).
4. Wait until VS Code finishes "Importing Java projects" (bottom right).
5. Open the **Run and Debug** panel (Ctrl+Shift+D). Pick one of three ready-made configurations and press the green play button:

| Configuration | What it does |
|---|---|
| **1. Console App** | Menu in the terminal (`com.findr.Main`) |
| **2. Web Server** | Starts the backend. Then open **http://localhost:8080** |
| **3. Run Tests** | Runs all 43 tests and prints PASS / FAIL |

No VS Code? Double-click `run-console.bat`, `run-server.bat` or `run-tests.bat` (Windows), or run `./run.sh console|server|tests`.

## 2. Project structure

```
FindrBackend/
  src/com/findr/
    Main.java                  console menu
    ServerMain.java            starts the web backend
    model/                     THE OBJECTS
      Item.java                abstract parent class
      LostItem.java            child of Item
      FoundItem.java           child of Item
      MatchResult.java         immutable result (implements Comparable)
      Category.java            enum
      ItemStatus.java          enum
      JsonSerializable.java    interface
    matching/                  THE ALGORITHM (swappable)
      Matchable.java           interface
      WeightedMatcher.java     default scoring
      StrictMatcher.java       child of WeightedMatcher
    repository/                STORAGE
      Repository.java          generic interface
      InMemoryRepository.java  ArrayList + HashMap
      FileStorage.java         CSV file handling
    service/                   BUSINESS LOGIC
      ItemService.java         the brain of the program
      SearchCriteria.java      search filters
      ServiceFactory.java      builds and wires everything
      SampleData.java          first-run sample items
    exception/                 FindrException + 3 children
    util/                      Json.java, Validator.java
    web/ApiServer.java         REST API + static website
    test/TestRunner.java       43 automatic tests
  web/index.html               the website (talks to the API)
  data/                        CSV files (created on first run)
  docs/                        class diagram + viva guide
```

## 3. Architecture (layers)

```
Website / Console   ->   ItemService   ->   Repository (memory)  ->  FileStorage (CSV)
 (ApiServer, Main)       (rules, matching)   (ArrayList, HashMap)
                              |
                         Matchable (Weighted / Strict)
```

- **Model** = what things *are* (Item, LostItem, FoundItem).
- **Repository** = where things are *stored*.
- **Service** = what the system *does* (validate, match, search).
- **Main / ApiServer** = how the user *talks* to it. They contain no business rules.

Class diagram: `docs/class_diagram.png`.

## 4. The matching rule (score out of 100)

| Check | Points |
|---|---|
| Same category | 30 |
| Same location | 20 |
| Found within 2 days of the loss (3 to 7 days: 10) | 20 |
| Shared description words (stop words removed) | up to 30 |

Pairs scoring **60 or more** are shown as possible matches. **Strict mode** returns 0 unless category and location both match.
Sample data results: wallet 95, calculator 94, iPhone 85.

## 5. OOP concepts: where to find each one

| Concept | File | What to point at |
|---|---|---|
| Class and object | every file | `new LostItem(...)` in `SampleData.java` |
| Abstraction | `Item.java` | `abstract class` with 5 abstract methods |
| Encapsulation | `Item.java`, `InMemoryRepository.java` | private fields, getters, `findAll()` returns a copy |
| Inheritance | `LostItem`, `FoundItem`, `StrictMatcher`, exceptions | `extends`, `super(...)` |
| Polymorphism (overriding) | `displayDetails()`, `getType()`, `matcher.calculateScore()` | one call, different behaviour |
| Polymorphism (overloading) | `ItemService.add()`, `search()`, `findMatches()`; `Validator.text()`; constructors | same name, different parameters |
| Interfaces | `Matchable`, `Repository`, `JsonSerializable`, `Comparable` | `implements` |
| Constructor chaining | `Item`, `LostItem`, `FoundItem` | `this(...)` and `super(...)` |
| Composition (has-a) | `ItemService`, `MatchResult`, `Item` | fields that are other objects |
| Enums | `Category`, `ItemStatus` | constants with a constructor and a method |
| Generics | `Repository<T extends Item>`, `List<? extends Item>` | type-safe and reusable |
| Custom exceptions | `exception/` | `FindrException` -> 3 children |
| Collections | `InMemoryRepository`, `WeightedMatcher` | ArrayList, HashMap, HashSet, `Collections.sort` |
| Immutability | `MatchResult` | all fields `final` |
| `equals` / `hashCode` / `toString` | `Item.java` | items are equal when IDs are equal |
| static and final | `ItemService.MIN_SCORE`, `WeightedMatcher` constants | shared constants |
| instanceof and casting | `ItemService.delete()`, `Main.matchForOne()` | check, then downcast |
| File handling | `FileStorage.java` | `Files`, `BufferedWriter`, try-with-resources |
| String handling | `WeightedMatcher.keywords()`, `Json.quote()` | `split`, `toLowerCase`, `StringBuilder` |
| Design patterns | Strategy (`Matchable`), Repository, Factory (`ServiceFactory`), Dependency Injection (`ItemService` constructor) | explained in `docs/OOP_VIVA_GUIDE.md` |

Not used (be honest if asked): inner/anonymous classes beyond one lambda in the tests, multithreading code written by us, and a database. The project uses CSV files on purpose, to match the File Handling topic.

## 6. Website API (the backend)

| Method | URL | Purpose |
|---|---|---|
| GET | `/api/state` | all items and matches |
| GET | `/api/items?type=&category=&location=&keyword=&date=` | search |
| POST | `/api/items` | report an item `{type,name,cat,desc,loc,date,p1,p2}` |
| PUT | `/api/items/{id}/status` | `{"status":"CLAIMED"}` |
| DELETE | `/api/items/{id}` | delete |
| GET | `/api/matches?lostId=L1` | matches for one lost item |

Errors return JSON like `{"error":"..."}` with status 400 (bad input), 404 (not found) or 500.
If you open `web/index.html` without the server running, the page switches to an offline demo mode.

## 7. Reset the data

Delete the `data/` folder. The next run recreates it with the 8 sample items.
