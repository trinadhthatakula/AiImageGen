<p align="center">
  <img src="app/src/main/launcher-playstore.png" alt="Thor Logo" height="192dp">
</p>

# AI Image Gen: Hugging Face Android Inference

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=android&logoColor=white)
![Koin](https://img.shields.io/badge/Koin-FF5252?style=for-the-badge&logo=koin&logoColor=white)

A production-grade Android implementation of Hugging Face Inference APIs, built with **Clean Architecture** and **Modern Android Development (MAD)** practices.

This project demonstrates how to handle serverless GPU inference ("Cold Starts"), strictly typed state management, and secure API key handling in a professional Android environment.

---

## 🛠 The Tech Stack

* **Language:** 100% Kotlin
* **UI:** Jetpack Compose (Material3) + `AnimatedContent` for state transitions.
* **Async:** Coroutines + Flow.
* **Dependency Injection:** Koin (using the latest `module { singleOf(::Impl) }` DSL).
* **Networking:** Retrofit + OkHttp (Custom 30s timeouts for GPU latency).
* **Architecture:** MVVM + Clean Architecture (Presentation, Domain, Data).

---

## 🏛 Architecture

The app follows a strict separation of concerns:

```
graph TD
    UI[Presentation Layer<br/>(ViewModel + Compose)] -->|Observes State| Domain
    Domain[Domain Layer<br/>(UseCase + Repo Interface)] -->|Defines Rules| Data
    Data[Data Layer<br/>(Repo Impl + Retrofit)] -->|Implements| Network

```

1. **Presentation Layer:** Observes `StateFlow<ImageGenerationResult>`. Handles UI states (Loading, Success, Error).
2. **Domain Layer:** Pure Kotlin. Contains business rules, specifically the **Retry Policy** for handling HTTP 503 errors.
3. **Data Layer:** Handles API implementation, JSON parsing, and raw Bitmap decoding.

---

## 🔐 Setup & Secrets (Crucial)

This project uses a strict security practice to prevent committing API keys to version control. You **must** create a local secrets file to run the app.

### Step 1: Get a Hugging Face Token

1. Go to [Hugging Face Settings > Tokens](https://www.google.com/search?q=https://huggingface.co/settings/tokens).
2. Create a **Read** token.

### Step 2: Create `secrets.properties`

1. Navigate to the **root** of this project.
2. Create a file named `secrets.properties`.
3. Add your token inside:

```properties
HF_TOKEN=hf_your_actual_token_starts_with_hf_

```

> **Note:** The `secrets.properties` file is already added to `.gitignore`. **DO NOT** commit this file.

### Step 3: Sync Gradle

The `build.gradle.kts` file automatically reads this property and injects it into `BuildConfig.HF_TOKEN` during compilation.

---

## ⚡ Key Engineering Decisions

### 1. The "Cold Start" Strategy (Retry Policy)

Hugging Face's Free Tier (`hf-inference`) spins down models when inactive. The first request often fails with a **503 Service Unavailable**.

**The Solution:**
The `GenerateImageUseCase` implements a smart retry mechanism using `flow.retryWhen`:

* Intercepts 503 HTTP errors.
* Applies **exponential backoff** (wait 1s, then 2s, then 4s).
* Retries up to 3 times automatically before propagating an error.

### 2. Strict State Management

We avoid "null" checking hell by using a **Sealed Interface** for the result state.

```kotlin
sealed interface ImageGenerationResult {
    data object Loading : ImageGenerationResult
    data class Success(val image: Bitmap) : ImageGenerationResult
    data class Error(val message: String) : ImageGenerationResult
}

```

### 3. Dynamic API Routing

The networking layer is built to be provider-agnostic via the `HuggingFaceApi` interface. It supports dynamic path replacement to switch between:

* **hf-inference:** (Default) Free, Shared GPU, Slower.
* **fal-ai:** Paid, Dedicated, Instant (Requires paid account).

---

## 👤 Author

**Trinadh Thatakula**

* [GitHub](https://www.google.com/search?q=https://github.com/trinadhthatakula)
* [The Valhalla Suite](https://www.google.com/search?q=%23)
