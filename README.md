#  Praesidium Note

**Praesidium Note** is an Android application built to explore how a privacy-first, zero-knowledge note system can be designed and maintained under real-world platform constraints.

Rather than treating security as a bolt-on feature, the project approaches privacy, data ownership, and correctness as core architectural concerns. All sensitive data is handled locally, protected using hardware-backed cryptography where available, and kept isolated through explicit separation of responsibilities across layers. The codebase prioritizes clarity, long-term maintainability, and well-documented tradeoffs over feature completeness.

## Key Features

- **Zero-Knowledge Architecture:** Data is encrypted locally; the developer never sees your content.

- **Biometric Security:** Seamless integration with Android BiometricPrompt (Fingerprint/Face).

- **Hardware-Backed Encryption:** Keys are generated and stored inside the device's Secure Element (TEE/StrongBox).

- **Anti-Memory Scraping:** Defensive coding patterns (using `CharArray`) to wipe data from the Java Heap.

## Technical Architecture

Following the [Now in Android](https://github.com/android/nowinandroid "Now In Android") philosophy, this project utilizes **Clean Architecture** with a layered approach to separate concerns and maximize testability.

### Layer Breakdown

- **`:app`**: The entry point, containing `MainActivity` and global Hilt configuration.

- **`:presentation`**: Jetpack Compose UI, MVI/MVVM ViewModels, and UI state management.

- **`:domain`**: The "Brain." Contains pure Kotlin Business Logic, UseCases, and Repository interfaces. **No Android dependencies.**

- **`:data`**: Implementation of repositories, SQLCipher database (Room), and `AndroidKeyStore` interaction.


### Security Stack

- **SQLCipher:** AES-256 GCM encryption at the SQLite level.

- **KeyStore:** Utilizing the best available security provider for key generation.

- **Heap Hygiene:** Manual zeroing of `CharArray` to prevent sensitive data from lingering in the String Pool.

## What This Project Demonstrates

This project is intended as a practical demonstration of the following skills and design decisions:

- Designing a privacy-first Android application with explicit zero-knowledge constraints
- Applying Clean Architecture to keep business logic independent from the Android framework
- Practical use of Android Keystore, Secure Element, and SQLCipher for at-rest data protection
- Conscious handling of sensitive data in memory (avoiding long-lived Strings and heap retention)
- Integrating biometric authentication while respecting framework lifecycle limitations
- Making deliberate testing tradeoffs for platform-coupled APIs (e.g. Biometrics, Compose UI)
- Writing unit tests that validate decision logic and failure modes rather than UI mechanics
- Treating security, testability, and maintainability as first-class design inputs
- Documenting non-goals, constraints, and assumptions to support long-term ownership

## Development Setup

1. Clone the repository.

2. Ensure you have the latest **Android Studio (Otter or later)**.

3. The project uses **Hilt** for DI; if you see generated code errors, run `./gradlew assembleDebug`.

4. **Note on Emulators:** If the emulator does not support hardware-backed Keystore, the app uses a deterministic fallback key for development purposes.

## Testing Strategy

This project emphasizes meaningful unit testing over artificial coverage metrics.

- Domain & ViewModel logic is covered by JVM unit tests.

- Decision logic around authentication states (e.g. BiometricManager capability checks) is unit tested.

- UI composables are kept stateless where possible and validated via previews and manual verification rather than JVM unit tests.

> ### Note on Biometrics Testing
>
> Android’s BiometricPrompt is tightly coupled to the framework lifecycle and system UI.
> As such, full end-to-end biometric flows are intentionally not unit-tested on the JVM, as doing so would require heavy mocking of internal framework state and produce unreliable tests.
>
> In production scenarios, these flows are best validated via instrumentation tests or manual QA on real hardware.

## Non-Goals

This project intentionally does not aim to:
- Provide cross-device sync or cloud backups
- Abstract cryptographic primitives beyond their platform guarantees
- Simulate full biometric flows in JVM unit tests

## Sequence Diagram

````mermaid
sequenceDiagram
    autonumber
    participant UI as UI Layer (Compose)
    participant VM as ViewModel (State)
    participant UC as Use Case (Domain)
    participant REPO as Repository (Data)
    participant SEC as Security/Biometrics
    participant DB as Room Database

    Note over UI, DB: [PHASE 1: AUTHENTICATION GATE]

    UI->>VM: Check Auth Status
    VM->>UC: AuthenticateUser()
    UC->>REPO: validateSession()
    REPO->>SEC: promptBiometric()
    activate SEC
    Note right of SEC: System Biometric Prompt
    SEC-->>REPO: Success (SecretKey unlocked)
    deactivate SEC
    REPO-->>UC: AuthResult.Success
    UC-->>VM: State: Authenticated
    VM-->>UI: Navigate to NoteList

    Note over UI, DB: [PHASE 2: NOTE RETRIEVAL]

    UI->>VM: Load Notes
    activate VM
    VM->>UC: GetNotes()
    activate UC
    UC->>REPO: fetchEncryptedNotes()
    activate REPO
    REPO->>DB: queryAll()
    activate DB
    DB-->>REPO: List<NoteEntity> (Encrypted)
    deactivate DB

    loop Decryption per Note
        REPO->>SEC: decrypt(payload)
        SEC-->>REPO: Plaintext (CharArray)
    end

    REPO-->>UC: List<Note>
    deactivate REPO
    UC-->>VM: Result<List<Note>>
    deactivate UC
    VM-->>UI: Update LazyColumn State
    deactivate VM

    Note over UI, DB: [PHASE 3: SAVE NOTE (ZERO-KNOWLEDGE)]

    UI->>VM: saveNote(title, content)
    activate VM
    VM->>VM: Set isLoading = true

    Note right of VM: String converted to CharArray<br/>Secure memory handling

    VM->>UC: SaveNote(note)
    activate UC

    Note over UC: Business Rule Validation

    UC->>REPO: persistNote(note)
    activate REPO

    REPO->>SEC: encrypt(note.content)
    activate SEC
    Note right of SEC: AES-GCM Encryption
    SEC-->>REPO: EncryptedPayload (IV + Ciphertext)
    deactivate SEC

    REPO->>DB: insert(NoteEntity)
    activate DB
    DB-->>REPO: Success
    deactivate DB

    REPO-->>UC: Result.Success
    deactivate REPO
    UC-->>VM: Result.Success
    deactivate UC

    alt On Success
        VM->>VM: Set isSaved = true
        VM->>UI: Trigger Navigation Back
    else On Failure
        VM->>VM: Set error state
        VM->>UI: Show Snackbar
    end

    Note over VM: wipeLocalCharArrays()
    deactivate VM
````

## Screenshots

### Dark Mode

| Screen Name | Images |
| ----------- | ------ |
| **Vault Gate** | <img src="docs/images/vault_gate_dark.png" width="300" alt="Vault Gate Dark" /> |
| **Vault Dashboard (Empty)** | <img src="docs/images/vault_dashboard_empty_dark.png" width="300" alt="Vault Dashboard Dark" /> |
| **Vault Dashboard (with Notes)** | <img src="docs/images/vault_dashboard_list_dark_one.png" width="300" alt="Vault List Dark One" /> <img src="docs/images/vault_dashboard_list_dark_two.png" width="300" alt="Vault List Dark Two" /> |
| **Secure Editor** | <img src="docs/images/vault_editor_dark.png" width="300" alt="Vault Editor Dark" /> <img src="docs/images/vault_editor_saved_dark.png" width="300" alt="Vault Editor Save Dark" /> |
| **Vault Deletion** | <img src="docs/images/vault_dashboard_dark_purge_dialog.png" width="300" alt="Vault Note Purge Dark" /> <img src="docs/images/vault_dashboard_dark_purge_dialog_done.png" width="300" alt="Vault Note Purge Done Dark" /> |

### Light Mode
| Screen Name | Images |
| ----------- | ------ |
| **Vault Gate** | <img src="docs/images/vault_gate_light.png" width="300" alt="Vault Gate Light" />  |
| **Vault Dashboard (Empty)** | <img src="docs/images/vault_dashboard_empty_light.png" width="300" alt="Vault Dashboard Light" /> |
| **Vault Dashboard (with Notes)** | <img src="docs/images/vault_dashboard_list_light_one.png" width="300" alt="Vault List Light One" /> <img src="docs/images/vault_dashboard_list_light_two.png" width="300" alt="Vault List Light Two" /> |
| **Secure Editor** | <img src="docs/images/vault_editor_light.png" width="300" alt="Vault Editor Light" /> <img src="docs/images/vault_editor_saved_light.png" width="300" alt="Vault Editor Save Light" /> |
| **Vault Deletion** | <img src="docs/images/vault_dashboard_light_purge_dialog.png" width="300" alt="Vault Note Purge Light" /> <img src="docs/images/vault_dashboard_light_purge_dialog_done.png" width="300" alt="Vault Note Purge Done Light" /> |


## License

Praesidium Note is compliant with **Apache License 2.0**. See [LICENSE](https://github.com/miggymamba/PraesidiumNote/blob/main/LICENSE) for more information.
