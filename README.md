# Multiple Datasource Spring Boot

This project is an example of implementing two datasources (Oracle and Postgres) in Spring Boot using Hibernate/JPA and integrating libraries including Lombok and for Json parsing.

Usually, API calls are made to receive JSON objects, the variables are parsed, and then converted into specific POJO classes that are then written to a Postgres database. The implementation of two datasources was done because the Oracle database contains certain data to add in the writing of the Model to the Postgres database. In the case of this project, it was not used, however, I left the configuration of the dual datasource and the repository (ControllerRepository) with a custom query for querying the Oracle database.
