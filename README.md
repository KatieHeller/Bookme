# Bookme Application

## Description

The Bookme Application allows users to book different meeting rooms in Onelity's offices in both Cologne and
Thessaloniki. Users are able to create bookings in any existing meeting rooms which are available during the specific
time and date requested. The application uses Spring Security to authenticate and authorize all of its users. Each
user either takes the role of an employee or an admin. Only an admin is able to create, update, or delete meeting rooms,
and a booking can only be updated or deleted by the user who created it or an admin. Otherwise, anyone is able to view
meeting rooms and view as well as create bookings.

## Installation/Running Program

1. Install Java 17 (LTS) and Postman.
2. In your IDE, create a new project from version control with the URL https://github.com/KatieHeller/Bookme
3. In this new project, add a new application configuration named 'Bookme Application' and set the main class to be
BookMeApplication. Set the SDK to use java 17. Give the configuration the following environment variables:
DB_URL=jdbc:postgresql://192.168.1.195:5432/Bookme-db?user=postgres&password=docker;DB_USERNAME=postgres;DB_PASSWORD=docker
4. Then, run Bookme Application.

## Using Application

To use the application, open Postman and create a new HTTP tab. Enter the URL to be http://192.168.1.195:8080/.
Before accessing any endpoints, you will need to enter your authentication information in the 'Authorization' tab.
Set the type to 'Basic Auth' and enter your username and password.

The different endpoints available are /meeting-rooms and /bookings.

As an employee, you can make only GET requests from the /meeting-rooms endpoint. From the /bookings end point, employees
can always make GET and POST requests. Employees may only make DELETE and PUT requests for ids corresponding to bookings
created by themselves.

As an admin, you can access any GET, POST, DELETE, AND PUT requests on both the /meeting-rooms and /bookings endpoints.

When users want to POST or PUT either meeting rooms or bookings into the database, the application expects the input to
be in JSON format.
The format of a meeting room is as follows:
{
"name": "",
"location": "",
"capacity":
}
The names of meeting rooms must all be unique and the location must be either "Cologne" or "Thessaloniki".

The format of a booking is:
{
"room": "",
"title": "",
"description": "",
"startDate": "2000-01-01",
"endDate": "2000-01-01",
"startTime": "00:00:00",
"endTime": "00:00:00",
"participants": ,
"repeat_pattern": ""
}
The room must correspond to the name of a room which exists in the database. The repeat pattern is optional, and if
used, must equal either "every day" or "every same day of the week".

## Testing Application

To test the application, simply go to the terminal and run the command 'mvn test'. The output will show how the numbers
of tests run, tests failed, errors, and skipped tests.

## Assumptions

Some assumptions were made in the making of the Bookme Application. These include:

- a meeting cannot span multiple days
- the start time of a meeting must be before the end time
- if the location of a meeting room changes, all bookings associated with that room will move with it
- when updating meeting rooms, capacity cannot be below the value of any associated bookings' numbers of participants
- if the repeat option of a booking is null, then its start and end dates must be equivalent
- when a meeting room is deleted, all associated bookings are deleted as well