

**GroovyForth** is a minimal Forth interpreter implemented in Groovy. It serves as an educational and experimental platform for understanding Forth's unique execution model, stack-based architecture, and extensibility. The interpreter supports core Forth words, custom macro definitions, and eventually aims to serve as a lightweight virtual runtime for embedded or JVM-based applications.

---

## 🚀 Project Goals

- Build a minimalist yet extensible Forth interpreter in Groovy
- Emulate the behavior of traditional Forth systems (e.g., Jones Forth)
- Maintain runtime clarity by isolating execution logic in a dedicated context
- Enable experimentation with Forth-inspired bytecode and embedded VMs
- Facilitate scripting and REPL-based workflows on the JVM

---

## 🛠️ Architecture Overview

The system centers around a few key classes:

### `RuntimeContext`

This class encapsulates the state of the interpreter during execution:
- `Stack<Object> stack`: Operand stack
- `List<String> inputTokens`: Current execution stream
- `Map<String, WordEntry> dictionary`: Word definitions
- `Map<Integer, Integer> memory`: Simulated memory store
- `int here`: Memory pointer for allocation
- `int tokenIndex`: Instruction pointer for token stream

RuntimeContext supports isolation of execution state, allowing for recursive interpretation, macro expansion, and control flow management.

### `WordEntry`

Represents a word in the Forth dictionary.
- Fields:
  - `String name`
  - `Closure action`
  - `List<String> compiledTokens` (optional)
  - `boolean immediate`
  - `int address` (reserved for memory-based usage)

### `Interpreter`

The main control loop:
- Reads input as a stream of tokens
- Dispatches each token using `RuntimeContext.dictionary`
- Handles compilation (`:` ... `;`) and control flow words (`IF`, `ELSE`, `THEN`, `DO`, `LOOP`, etc.)
- Supports custom word definitions, literals, and basic arithmetic

---

## 📦 Features Implemented

- [x] Stack manipulation: `DUP`, `DROP`, `SWAP`, `OVER`, `2DUP`, `2DROP`, etc.
- [x] Arithmetic: `+`, `-`, `*`, `/`, `/MOD`, `1+`, `2-`, etc.
- [x] Comparison: `0=`, `0<`, `0>`
- [x] Memory: `HERE`, `ALLOT`, `!`, `@`
- [x] Control flow: `IF`, `ELSE`, `THEN`, `DO`, `LOOP`, `BEGIN`, `UNTIL`, etc.
- [x] Definition: `: name ... ;`, `IMMEDIATE`, `CREATE`
- [ ] REPL interface (planned)
- [ ] File-based script loading
- [ ] Bytecode generation (planned)
- [ ] JIT-style optimization (experimental)

---

## 📐 Design Philosophy

- **Runtime Clarity**: All state changes happen via `RuntimeContext`, avoiding global side effects
- **Composable Words**: Each Forth word is a `Closure`, enabling easy composition and redefinition
- **Minimal Bootstrapping**: The system starts with just enough primitives to self-host new words
- **JVM-Centric**: Fully Groovy/Java compatible with no native dependencies

---

## 🧪 Example Usage

```forth
: SQUARE DUP * ;
5 SQUARE .   \ prints 25

: FIB
    DUP 2 < IF DROP 1 EXIT THEN
    DUP 1- RECURSE
    SWAP 2- RECURSE
    + ;

10 FIB .
````

---

## 🧰 Development & Build

### Prerequisites

* Java 17+ or 21+
* Groovy 4+
* Gradle (if using the provided `build.gradle`)

### Run Interpreter

```bash
./gradlew run
```

Or manually:

```bash
groovy src/main/groovy/biz/digitalindustry/groovyforth/Main.groovy
```

---

## 🗂️ Project Structure

```
groovyforth/
├── src/
│   └── main/
│       └── groovy/
│           └── biz/
│               └── digitalindustry/
│                   └── groovyforth/
│                       ├── RuntimeContext.groovy
│                       ├── WordEntry.groovy
│                       ├── Interpreter.groovy
│                       └── Main.groovy
├── test/
│   └── ... (planned)
├── build.gradle
└── README.md
```

---

## 🔭 Roadmap

* [ ] Add REPL interface with command history and introspection
* [ ] Support `INCLUDE` and file-based scripts
* [ ] Implement bytecode format and executor
* [ ] WASM backend experiments (long-term)
* [ ] Native Forth vocabulary bootstrapping (e.g., `DOES>`, `IMMEDIATE`)
* [ ] Test suite using `GroovyTestCase` or Spock

---

## 🤝 Contributing

Contributions are welcome! Open an issue or pull request with improvements, bug fixes, or new features. Ideas for novel Forth words or experimental directions are especially encouraged.

---

## 📜 License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---

## ✨ Credits

Inspired by:

* [JonesForth](https://github.com/nornagon/jonesforth)
* Chuck Moore's original Forth philosophy
* The Groovy community

Developed with ❤️ by [Geoffrey P.](#)

