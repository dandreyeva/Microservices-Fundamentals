Feature: Testing a REST API
  User should be able to submit GET and POST requests to song service

  Scenario: Find song which is not present
    Given User has idSong
    Then User gets 404 trying to find the song