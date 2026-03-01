# 🧷 Lombok Setup

Use this guide if your IDE shows Lombok-related errors even though Maven builds succeed.

## 🎯 Scope

This file is only for Lombok/IDE setup and troubleshooting.
For project setup and API usage, see `README.md`.

## 💡 IntelliJ IDEA

### 1. Install Lombok plugin

1. Open `File` -> `Settings` -> `Plugins`
2. Search for `Lombok`
3. Install and restart IntelliJ

### 2. Enable annotation processing

1. Open `File` -> `Settings` -> `Build, Execution, Deployment` -> `Compiler` -> `Annotation Processors`
2. Enable `Annotation processing`
3. Keep `Obtain processors from project classpath` selected

### 3. Rebuild

```bash
mvn clean compile
```

## 🧰 VS Code

1. Install `Extension Pack for Java`
2. Install a Lombok support extension (for example `Lombok Annotations Support for VS Code`)
3. Reload the window
4. Run:

```bash
mvn clean compile
```

## 🖥️ Eclipse

1. Download `lombok.jar` from https://projectlombok.org/download
2. Run:

```bash
java -jar lombok.jar
```

3. Select your Eclipse installation and complete install
4. Restart Eclipse, then run Maven update/clean build

## ✅ Verification

### Maven compile must pass

```bash
mvn clean compile
```

### Class contains generated accessors

```bash
javap -p target/classes/com/library/api/model/Book.class
```

You should see generated methods such as `getTitle`, `setTitle`, etc.

## 🛠️ Common Problems

### "Cannot find symbol" for Lombok-generated methods

- Plugin missing in IDE
- Annotation processing disabled
- IDE cache out of sync

Fix sequence:
1. Enable plugin + annotation processing
2. Run `mvn clean compile`
3. Invalidate IDE caches and restart

### IDE errors but Maven build succeeds

This is usually an IDE indexing problem, not a compiler problem.
Re-import the Maven project and rebuild indexes.

### `log` not found for `@Slf4j`

Lombok was not processed for that class in the IDE session.
Rebuild after enabling annotation processing.

## 📦 POM Check

Project includes Lombok in:
- Dependency list
- `maven-compiler-plugin` annotation processors

If needed, confirm in `pom.xml`:

- `org.projectlombok:lombok`
- `maven.compiler.source` and `maven.compiler.target` set to `25`

## ✔️ Quick Checklist

- Lombok plugin installed in your IDE
- Annotation processing enabled
- `mvn clean compile` succeeds
- IDE recognizes Lombok-generated members
