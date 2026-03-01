# ⚠️ Error Response Examples

Canonical examples of API error payloads returned by `GlobalExceptionHandler`.

## 🧾 Response Shape

All error responses use this base structure:

```json
{
  "timestamp": "2026-03-01T14:30:45",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/books"
}
```

Validation failures include `validationErrors`:

```json
{
  "timestamp": "2026-03-01T14:30:45",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/books",
  "validationErrors": {
    "title": "Title is required",
    "libraryId": "Library ID is required"
  }
}
```

## 🔍 404 Not Found

Returned for missing resources (`ResourceNotFoundException`).

### Example: Book not found

**Request**

```http
GET /api/books/999 HTTP/1.1
Host: localhost:8080
```

**Response**

```json
{
  "timestamp": "2026-03-01T14:30:45",
  "status": 404,
  "error": "Not Found",
  "message": "Book with ID 999 not found",
  "path": "/api/books/999"
}
```

### Example: Library not found on delete

**Request**

```http
DELETE /api/libraries/999 HTTP/1.1
Host: localhost:8080
```

**Response**

```json
{
  "timestamp": "2026-03-01T14:31:22",
  "status": 404,
  "error": "Not Found",
  "message": "Library with ID 999 not found",
  "path": "/api/libraries/999"
}
```

## 🚫 400 Bad Request

Returned for validation failures and business-rule violations.

### Validation failure (`MethodArgumentNotValidException`)

**Request**

```http
POST /api/books HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "title": "Clean Code"
}
```

**Response**

```json
{
  "timestamp": "2026-03-01T14:35:20",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/books",
  "validationErrors": {
    "author": "Author is required",
    "isbn": "ISBN is required",
    "libraryId": "Library ID is required"
  }
}
```

### Business-rule failure (`BadRequestException`)

**Request**

```http
POST /api/books HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "isbn": "9780132350884",
  "title": "Clean Code",
  "author": "Robert C. Martin",
  "genre": "Software Engineering",
  "publicationDate": "2010-01-01",
  "acquisitionDate": "2009-12-01",
  "pages": 464,
  "libraryId": 1
}
```

**Response**

```json
{
  "timestamp": "2026-03-01T14:38:40",
  "status": 400,
  "error": "Bad Request",
  "message": "Acquisition date cannot be before publication date",
  "path": "/api/books"
}
```

### Invalid pagination values (`IllegalArgumentException`)

**Request**

```http
GET /api/books?page=-1&size=10 HTTP/1.1
Host: localhost:8080
```

**Response**

```json
{
  "timestamp": "2026-03-01T14:39:25",
  "status": 400,
  "error": "Bad Request",
  "message": "Page index must not be less than zero",
  "path": "/api/books"
}
```

## 💥 500 Internal Server Error

Fallback for unhandled exceptions.

```json
{
  "timestamp": "2026-03-01T14:45:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred: ...",
  "path": "/api/..."
}
```

## 🧪 Quick Test Commands

### 404 test

```bash
curl -X GET http://localhost:8080/api/books/999
```

### 400 validation test

```bash
curl -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d '{"title":"Only Title"}'
```

### 400 business-rule test

```bash
curl -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d '{"isbn":"9780132350884","title":"Clean Code","author":"Robert C. Martin","publicationDate":"2010-01-01","acquisitionDate":"2009-12-01","pages":464,"libraryId":1}'
```

## 📝 Notes

- Exact `message` text for framework-generated exceptions can vary by Spring version.
- Keep this file aligned with `src/main/java/com/library/api/exception/GlobalExceptionHandler.java`.
