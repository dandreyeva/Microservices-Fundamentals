package com.epam.microservice_song.componenttests.steps;

import com.epam.microservice_song.componenttests.CucumberSpringConfiguration;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.spring.CucumberContextConfiguration;
import org.junit.jupiter.api.Assertions;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
@CucumberContextConfiguration
public class StepDefinitions extends CucumberSpringConfiguration {
    private int idSong;

    @Given("User has idSong")
    public void idSong() {
        this.idSong = 1;
    }

    @Then("User gets 404 trying to find the song")
    public void getDeletedSong() {
        ResponseEntity response = testRestTemplate.getForEntity("/songs/" + idSong, String.class);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
