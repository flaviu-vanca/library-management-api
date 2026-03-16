Feature: Library Management API - Books Endpoints
  As an API consumer
  I want to manage books through the REST API
  So that I can create, read, update, and delete book records

  Background:
    * url baseUrl
    * header Content-Type = 'application/json'
    * header Accept = 'application/json'
    # Helper function to generate unique 13-digit ISBN
    * def generateIsbn = function(){ return '978' + Math.floor(Math.random() * 9000000000 + 1000000000) }

  Scenario: Create a book in an existing library successfully
    # First create a library
    Given path '/api/libraries'
    And request
      """
      {
        "name": "Book Test Library"
      }
      """
    When method POST
    Then status 201
    * def libraryId = response.id
    # Generate unique ISBN using timestamp
    * def uniqueIsbn = generateIsbn()

    # Create a book in that library
    Given path '/api/books'
    And request
      """
      {
        "isbn": "#(uniqueIsbn)",
        "title": "1984 Karate Test",
        "author": "George Orwell",
        "genre": "Dystopian",
        "publicationDate": "1949-06-08",
        "acquisitionDate": "2020-01-15",
        "pages": 328,
        "libraryId": #(libraryId)
      }
      """
    When method POST
    Then status 201
    And match response.id == '#number'
    And match response.isbn == uniqueIsbn
    And match response.title == '1984 Karate Test'
    And match response.author == 'George Orwell'
    And match response.libraryId == libraryId
    And match response.libraryName == 'Book Test Library'
    * def bookId = response.id

    # Cleanup
    Given path '/api/books', bookId
    When method DELETE
    Then status 204

    Given path '/api/libraries', libraryId
    When method DELETE
    Then status 204

  Scenario: Get book by ID returns correct book
    # Setup: Create library and book with unique ISBN
    Given path '/api/libraries'
    And request { "name": "Get Book Library" }
    When method POST
    Then status 201
    * def libraryId = response.id
    * def uniqueIsbn = generateIsbn()

    Given path '/api/books'
    And request
      """
      {
        "isbn": "#(uniqueIsbn)",
        "title": "Don Quixote Test",
        "author": "Miguel de Cervantes",
        "libraryId": #(libraryId)
      }
      """
    When method POST
    Then status 201
    * def bookId = response.id

    # Test: Get book by ID
    Given path '/api/books', bookId
    When method GET
    Then status 200
    And match response.id == bookId
    And match response.isbn == uniqueIsbn
    And match response.title == 'Don Quixote Test'

    # Cleanup
    Given path '/api/books', bookId
    When method DELETE
    Given path '/api/libraries', libraryId
    When method DELETE

  Scenario: Get book that does not exist returns 404
    Given path '/api/books/99999'
    When method GET
    Then status 404
    And match response.status == 404
    And match response.message contains 'Book'

  Scenario: Create book with duplicate ISBN returns 400
    # Setup: Create library and first book with unique ISBN
    Given path '/api/libraries'
    And request { "name": "Duplicate ISBN Library" }
    When method POST
    Then status 201
    * def libraryId = response.id
    # Generate unique ISBN for this test
    * def uniqueIsbn = generateIsbn()

    Given path '/api/books'
    And request
      """
      {
        "isbn": "#(uniqueIsbn)",
        "title": "First Unique Book",
        "author": "Test Author",
        "libraryId": #(libraryId)
      }
      """
    When method POST
    Then status 201
    * def firstBookId = response.id

    # Test: Try to create another book with same ISBN
    Given path '/api/books'
    And request
      """
      {
        "isbn": "#(uniqueIsbn)",
        "title": "Different Title",
        "author": "Different Author",
        "libraryId": #(libraryId)
      }
      """
    When method POST
    Then status 400
    And match response.message contains 'ISBN'
    And match response.message contains 'already exists'

    # Cleanup
    Given path '/api/books', firstBookId
    When method DELETE
    Given path '/api/libraries', libraryId
    When method DELETE

  Scenario: Filter books by genre
    # First get books filtered by genre
    Given path '/api/books'
    And param genre = 'Fiction'
    When method GET
    Then status 200
    And match response.content == '#array'
    And match response.pageNumber == 0

  Scenario: Delete a book successfully
    # Setup: Create library and book with unique ISBN
    Given path '/api/libraries'
    And request { "name": "Delete Book Library" }
    When method POST
    Then status 201
    * def libraryId = response.id
    * def uniqueIsbn = generateIsbn()

    Given path '/api/books'
    And request
      """
      {
        "isbn": "#(uniqueIsbn)",
        "title": "War and Peace Test",
        "author": "Leo Tolstoy",
        "libraryId": #(libraryId)
      }
      """
    When method POST
    Then status 201
    * def bookId = response.id

    # Test: Delete the book
    Given path '/api/books', bookId
    When method DELETE
    Then status 204

    # Verify it's deleted
    Given path '/api/books', bookId
    When method GET
    Then status 404

    # Cleanup
    Given path '/api/libraries', libraryId
    When method DELETE

  Scenario: Search books by author
    Given path '/api/books'
    And param author = 'Tolkien'
    When method GET
    Then status 200
    And match response.content == '#array'

  Scenario: Get books by publication date range
    Given path '/api/books/by-publication-date'
    And param startDate = '1900-01-01'
    And param endDate = '2025-12-31'
    When method GET
    Then status 200
    And match response.content == '#array'

  Scenario: Get books by acquisition date range
    Given path '/api/books/by-acquisition-date'
    And param startDate = '2000-01-01'
    And param endDate = '2025-12-31'
    When method GET
    Then status 200
    And match response.content == '#array'

  Scenario: Create book with invalid library ID returns 404
    * def uniqueIsbn = generateIsbn()
    Given path '/api/books'
    And request
      """
      {
        "isbn": "#(uniqueIsbn)",
        "title": "Orphan Book",
        "author": "Unknown Author",
        "libraryId": 99999
      }
      """
    When method POST
    Then status 404
    And match response.message contains 'Library'

  Scenario: Update book successfully
    # Setup with unique ISBN
    Given path '/api/libraries'
    And request { "name": "Update Book Library" }
    When method POST
    Then status 201
    * def libraryId = response.id
    * def uniqueIsbn = generateIsbn()

    Given path '/api/books'
    And request
      """
      {
        "isbn": "#(uniqueIsbn)",
        "title": "Original Title",
        "author": "Original Author",
        "libraryId": #(libraryId)
      }
      """
    When method POST
    Then status 201
    * def bookId = response.id

    # Test: Update the book
    Given path '/api/books', bookId
    And request
      """
      {
        "isbn": "#(uniqueIsbn)",
        "title": "Updated Title",
        "author": "Updated Author",
        "genre": "Updated Genre",
        "pages": 500,
        "libraryId": #(libraryId)
      }
      """
    When method PUT
    Then status 200
    And match response.title == 'Updated Title'
    And match response.author == 'Updated Author'
    And match response.genre == 'Updated Genre'
    And match response.pages == 500

    # Cleanup
    Given path '/api/books', bookId
    When method DELETE
    Given path '/api/libraries', libraryId
    When method DELETE

  Scenario: Get all books with pagination and sorting
    Given path '/api/books'
    And param page = 0
    And param size = 5
    And param sort = 'title,asc'
    When method GET
    Then status 200
    And match response.pageNumber == 0
    And match response.pageSize == 5
    And match response.content == '#array'
