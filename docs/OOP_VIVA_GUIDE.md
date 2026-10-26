# Viva guide: how to explain this project

## 60-second introduction (say this first)
"My project is a Campus Lost and Found Matching System in Java. Students report lost items, security reports found items,
and the system scores every lost/found pair out of 100 using category, location, date and description. The design is layered:
model classes, repositories, a service layer, and two front ends, a console menu and a REST web server. I used abstraction,
inheritance, polymorphism, interfaces, generics, enums and custom exceptions, and I wrote 43 automatic tests."

## 5-minute live demo script
1. **Run Tests** (VS Code config 3): all 43 PASS. "Every feature is tested."
2. **Console App**: option 7 shows 3 matches (95, 94, 85).
3. Option 1: report a lost "Red Umbrella", category Personal, Main Gate, 2026-10-19. It instantly shows a 100 match with F3.
4. Option 10: switch to **Strict** mode, run option 7 again. Say: "I changed the algorithm at run time without changing any other code. That is polymorphism through the Matchable interface."
5. Type a wrong date. Say: "InvalidInputException is a custom exception, caught in one place in Main."
6. **Web Server** (config 2): open http://localhost:8080 and show the same data in the website. Footer says "Connected to Java backend".

## The 10 questions you will most likely get

**1. Where is abstraction?**
`Item` is abstract. You cannot write `new Item()`. It declares abstract methods (`getType`, `getPartyName`, `getPartyDetail`, `getContactInfo`, `toCsv`) that LostItem and FoundItem must implement.

**2. Where is encapsulation?**
All fields of `Item` are private. Only getters are public, and the only setter is `setStatus`. `MatchResult` is immutable (all `final`). `InMemoryRepository.findAll()` returns a copy so outside code cannot damage the list.

**3. Where is inheritance?**
`LostItem extends Item`, `FoundItem extends Item`, `StrictMatcher extends WeightedMatcher`, and three exceptions extend `FindrException`. The children call `super(...)` and reuse the parent code.

**4. Where is polymorphism?**
Runtime: `for (Item i : items) i.displayDetails();` prints [LOST] or [FOUND] depending on the real object. Also `matcher.calculateScore(...)` runs Weighted or Strict depending on which object is plugged in.
Compile time (overloading): `ItemService.add(LostItem)` vs `add(FoundItem)`, `search(criteria)` vs `search(keyword)`, `findMatches(lost)` vs `findMatches()`.

**5. Abstract class vs interface? Where did you use each?**
Abstract class (`Item`): shared fields and shared code, one parent only. Interface (`Matchable`, `Repository`): a pure contract, a class can implement many.

**6. What is `Repository<T extends Item>`?**
A generic interface. `T` is a placeholder type that must be an Item or its child. The same code stores LostItem and FoundItem safely, and the compiler blocks mistakes.

**7. Why a service layer?**
`ItemService` holds all rules. Both `Main` (console) and `ApiServer` (web) call it, so rules are written once. This is the single responsibility idea.

**8. What is dependency injection here?**
`ItemService` receives its repositories, storage and matcher through its constructor instead of creating them. `ServiceFactory` does the wiring. That makes the parts swappable and easy to test.

**9. How do you handle errors?**
Custom checked exceptions under `FindrException`. Validation throws `InvalidInputException` (HTTP 400), a missing ID throws `ItemNotFoundException` (404), file problems throw `StorageException` (500).

**10. How is data saved?**
`FileStorage` writes one line per item to `data/lost.csv` and `data/found.csv` using `BufferedWriter` in a try-with-resources block, and reads them back at start-up. Corrupted lines are skipped.

## Extra points that impress examiners
- Strategy pattern: `Matchable` lets you add a new matching algorithm without editing `ItemService` (open/closed principle).
- Enum `Category` makes spelling mistakes impossible.
- `equals`/`hashCode` are based on the ID, so two objects with the same ID are the same item.
- `MatchResult implements Comparable`, so `Collections.sort(results)` puts the best match first.
- Tests use a temporary folder, so they never touch your real data.

## Possible questions about what is NOT there
- *"Why no database?"* The syllabus topic is File Handling, so CSV files are used. A database would replace only `FileStorage` (the rest stays the same, because of the Repository interface).
- *"Why no Spring?"* To keep the code fully explainable. The JDK's built-in HTTP server is enough for a REST API.
