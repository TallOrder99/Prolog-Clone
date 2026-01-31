***

# LogicCompiler: A Mini-Prolog Implementation on the WAM

## Overview
**LogicCompiler** is a fully functional compiler and runtime engine for a subset of the Prolog programming language. It is built from scratch in **Java** and implements the **Warren Abstract Machine (WAM)** architecture.

This project demonstrates the entire compilation pipeline for a logic programming language, transforming high-level declarative logic into low-level stack-based machine instructions that support unification and backtracking.

## Features
*   **Declarative Syntax:** Supports standard Prolog syntax for Facts, Rules, and Queries.
*   **Inference Engine:** Implements "SlD Resolution" via the WAM.
*   **Backtracking:** Automatically finds multiple solutions to a query using Choice Points.
*   **Recursion:** Supports recursive rules (e.g., graph pathfinding, ancestor logic).
*   **Interactive REPL:** A command-line interface for defining knowledge bases and executing queries dynamically.
*   **WAM Assembly:** Can verify the compiler output by inspecting generated WAM instructions.

---

## Architecture Pipeline

The project follows a strict 6-phase compiler design:

1.  **Lexical Analysis & Parsing (ANTLR4):**
    *   Converts raw source text into a Parse Tree.
    *   Grammar defined in `LogicMini.g4`.
2.  **AST Generation:**
    *   Traverses the Parse Tree to build a custom Abstract Syntax Tree (AST).
    *   Classes: `Program`, `Clause`, `Structure`, `Variable`, `Atom`.
3.  **Optimization:**
    *   **Fact Reordering:** Groups clauses by predicate signature (e.g., `parent/2`) and sorts Facts before Rules to improve search efficiency.
4.  **Compilation:**
    *   Translates the AST into a linear list of **WAM Instructions**.
    *   Handles variable register allocation, stack frame management (`ALLOCATE`/`DEALLOCATE`), and indexing logic (`TRY`/`RETRY`/`TRUST`).
5.  **Virtual Machine (The WAM):**
    *   A register-based virtual machine that executes the compiled bytecode.
    *   Manages the **Heap** (data), **Stack** (environments), **Registers** (arguments), and **Trail** (undo log).
6.  **Interaction:**
    *   An interactive shell that accepts dynamic input and formats results.

---

## Technical Implementation Details

### The Warren Abstract Machine (WAM)
This project implements a simplified version of the WAM, the standard architecture for logic programming.

#### 1. Memory Layout
*   **Heap:** Stores complex terms (Structures) and global variables. Uses a "tagged pointer" system (`WamCell`) where every cell has a Tag (`REF`, `STR`, `CON`) and a Value/Pointer.
*   **Registers (`A1`...`An`):** Pass arguments between the caller and the callee.
*   **Stack:** Stores **Environments** (local variables for rules) and **Choice Points** (snapshots of the machine state for backtracking).
*   **Trail:** An "undo log." When a variable is bound, its address is pushed here. Upon backtracking, the trail is unwound to reset variables to `null` (unbound).

#### 2. Instruction Set (Opcodes)
The compiler generates the following assembly instructions:

| Category | Opcode | Description |
| :--- | :--- | :--- |
| **Put** | `PUT_CONSTANT`, `PUT_VARIABLE`, `PUT_VALUE` | Prepares arguments in registers before calling a predicate. |
| **Get** | `GET_CONSTANT`, `GET_VARIABLE` | Unifies incoming arguments with the Head of a clause. |
| **Control** | `CALL`, `PROCEED` | Jumps to a predicate; Returns to the caller. |
| **Stack** | `ALLOCATE`, `DEALLOCATE` | Creates/Destroys stack frames for rules with local variables. |
| **Indexing** | `TRY_ME_ELSE`, `RETRY_ME_ELSE`, `TRUST_ME` | Manages backtracking branches (Choice Points). |

#### 3. Unification
The core engine uses a unification algorithm that:
1.  **Dereferences** pointer chains to find actual values.
2.  **Binds** unbound variables to values (or other variables).
3.  **Fails** if atoms or structures do not match, triggering the backtracking mechanism.

---

## Getting Started

### Prerequisites
*   **Java JDK 17** or higher.
*   **Maven** (for dependency management).

### Installation & Build
1.  Clone the repository.
2.  Build the project using Maven:
    ```bash
    mvn clean package
    ```
3.  This generates the executable JAR in the `target/` directory.

### Running the Compiler
Run the `Main` class from your IDE or via command line:
```bash
java -cp target/classes:target/dependency/* Main
```
*(Note: Ensure ANTLR dependencies are on the classpath).*

---

## Usage Guide

When you run the application, it enters an interactive mode.

### Step 1: Define Knowledge Base
Enter facts and rules. End with `done.` on a new line.

```prolog
KB> father(anakin, luke).
KB> father(anakin, leia).
KB> parent(X, Y) :- father(X, Y).
KB> sibling(X, Y) :- parent(Z, X), parent(Z, Y).
KB> done.
```

### Step 2: Query
Ask questions. The system allows you to request multiple answers.

```prolog
?- sibling(luke, leia).
true.

?- sibling(luke, X).
X = leia
   Next solution? (y/n) > y
X = luke  (Note: Luke is his own sibling in this logic!)
   Next solution? (y/n) > n
```

---

## Supported Syntax

**Facts:**
```prolog
cat(tom).
likes(alice, pizza).
```

**Rules:**
```prolog
% "X is an animal if X is a cat"
animal(X) :- cat(X).

% "X is a grandparent of Z if X is parent of Y AND Y is parent of Z"
grandparent(X, Z) :- parent(X, Y), parent(Y, Z).
```

**Variables:** Must start with an Uppercase letter (`X`, `Person`, `Food`).
**Atoms:** Must start with a Lowercase letter (`tom`, `cat`, `pizza`).

---

## Limitations vs. Standard Prolog
This is a "Mini" implementation designed for educational purposes. It differs from ISO Prolog in the following ways:

1.  **No Math:** Operators like `is`, `+`, `-`, `*` are not supported.
2.  **No Lists:** Syntax like `[H|T]` is not supported.
3.  **No Cut (`!`):** There is no operator to prune the search tree manually.
4.  **No Negation:** Operators like `\=` or `not()` are not implemented.
5.  **Simplified Memory:** The Heap grows indefinitely (no Garbage Collection).

---

## Project Structure

```
src/main/java
├── ast/             # Abstract Syntax Tree classes (Program, Clause, Term)
├── compiler/        # Compiler logic (AST -> WAM Instructions)
├── generated/       # ANTLR4 generated Lexer and Parser
├── logic/           # (Optional) High-level unification logic
├── optimizer/       # Clause reordering logic
├── wam/             # Virtual Machine (Memory, Opcodes, Execution Loop)
└── Main.java        # Entry point and REPL
src/main/antlr4
└── LogicMini.g4     # Grammar definition
```

## Credits
Built as a comprehensive compiler construction project involving:
*   Language Design (Grammar)
*   Compiler Backend (Code Generation)
*   Runtime Environment (Virtual Machine)
