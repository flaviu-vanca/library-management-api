Feature: Library Management API - Libraries Endpoints
  As an API consumer
  I want to manage libraries through the REST API
  So that I can create, read, update, and delete library records

  Background:
    * url baseUrl
    * header Content-Type = 'application/json'
    * header Accept = 'application/json'

  Scenario: Create a new library successfully
    Given path '/api/libraries'
    And request
      """
      {
        "name": "Karate Test Library",
        "address": "123 Karate Street, Test City",
        "phone": "+1-555-0123",
        "email": "karate@testlibrary.org",
        "establishedDate": "1990-05-20"
      }
      """
    When method POST
    Then status 201
    And match response.id == '#number'
    And match response.name == 'Karate Test Library'
    And match response.address == '123 Karate Street, Test City'
    And match response.phone == '+1-555-0123'
    And match response.email == 'karate@testlibrary.org'
    And match response.totalBooks == 0
    * def createdLibraryId = response.id

  Scenario: Get library by ID returns correct library
    # First create a library
    Given path '/api/libraries'
    And request
      """
      {
        "name": "Get By ID Library",
        "address": "456 Test Avenue"
      }
      """
    When method POST
    Then status 201
    * def libraryId = response.id

    # Then get it by ID
    Given path '/api/libraries', libraryId
    When method GET
    Then status 200
    And match response.id == libraryId
    And match response.name == 'Get By ID Library'

    # Cleanup
    Given path '/api/libraries', libraryId
    When method DELETE
    Then status 204

  Scenario: Get library that does not exist returns 404
    Given path '/api/libraries/99999'
    When method GET
    Then status 404
    And match response.status == 404
    And match response.error == 'Not Found'
    And match response.message contains 'Library'

  Scenario: Create library with blank name returns 400
    Given path '/api/libraries'
    And request
      """
      {
        "name": "",
        "address": "Some Address"
      }
      """
    When method POST
    Then status 400
    And match response.status == 400
    And match response.validationErrors.name == '#present'

  Scenario: Get all libraries with pagination
    Given path '/api/libraries'
    And param page = 0
    And param size = 5
    When method GET
    Then status 200
    And match response.pageNumber == 0
    And match response.pageSize == 5
    And match response.content == '#array'

  Scenario: Update library successfully
    # First create a library
    Given path '/api/libraries'
    And request
      """
      {
        "name": "Library To Update"
      }
      """
    When method POST
    Then status 201
    * def libraryId = response.id

    # Update it
    Given path '/api/libraries', libraryId
    And request
      """
      {
        "name": "Updated Library Name",
        "address": "New Address"
      }
      """
    When method PUT
    Then status 200
    And match response.name == 'Updated Library Name'
    And match response.address == 'New Address'

    # Cleanup
    Given path '/api/libraries', libraryId
    When method DELETE
    Then status 204

  Scenario: Delete library successfully
    # First create a library
    Given path '/api/libraries'
    And request
      """
      {
        "name": "Library To Delete"
      }
      """
    When method POST
    Then status 201
    * def libraryId = response.id

    # Delete it
    Given path '/api/libraries', libraryId
    When method DELETE
    Then status 204

    # Verify it's gone
    Given path '/api/libraries', libraryId
    When method GET
    Then status 404

  Scenario: Create library with invalid email returns 400
    Given path '/api/libraries'
    And request
      """
      {
        "name": "Invalid Email Library",
        "email": "not-a-valid-email"
      }
      """
    When method POST
    Then status 400
    And match response.validationErrors.email == '#present'

  Scenario: Get books for a library
    # First create a library
    Given path '/api/libraries'
    And request
      """
      {
        "name": "Library With Books Query"
      }
      """
    When method POST
    Then status 201
    * def libraryId = response.id

    # Get books (should be empty initially)
    Given path '/api/libraries', libraryId, 'books'
    When method GET
    Then status 200
    And match response.content == '#array'
    And match response.totalElements == 0

    # Cleanup
    Given path '/api/libraries', libraryId
    When method DELETE
    Then status 204
