# FIFO queue assignment
## Description
Create program in Java language that will process commands from FIFO queue using Producer –
Consumer pattern.

Supported commands are the following:
* Add  - adds a user into a database
* PrintAll – prints all users into standard output
* DeleteAll – deletes all users from database

User is defined as database table SUSERS with columns (USER_ID, USER_GUID, USER_NAME)

Demonstrate program on the following sequence (using main method or test):
* Add (1, &quot;a1&quot;, &quot;Robert&quot;)
* Add (2, &quot;a2&quot;, &quot;Martin&quot;)
* PrintAll
* DeleteAll
* PrintAll

Show your ability to unit test code on at least one class.

Goal of this exercise is to show Java language and JDK know-how, OOP principles, clean code
understanding, concurrent programming knowledge, unit testing experience.

Please do not use Spring framework in this exercise. Embedded database is sufficient.