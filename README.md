# Purpose

For years now, I have monitored the weights of my pets and myself on a monthly basis. While this is easily tracked and plotted in Excel, I wanted to challenge myself to create a personal Java GUI, capable of growing in the future to incorporate veterinary needs.

# Requirements
## Software
- Java 1.8 (with language level set to _8 - Lambdas_)
- Maven 4.0.0
- PostgreSQL 13.1
## Pre-requisite Actions
- Before running this software, you must first start up PostgreSQL and create a _**weightwatcher**_ database in postgres.
- You must provide a file titled 'postgres' within the resources directory that contains the credentials for the user that created the _**weightwatcher**_ database. The first line of the file should be the username. The second line should be the password. This information is only used to make a Java database connection (JDBC).

# How to Use
1. Run Main.java.

TODO: Add details, examples, and screenshots

# Troubleshooting Advice
If you run into issues with the data within the database and need to access it directly, first ensure that postgres is running. If it is running, enter the following code from the command line:
```
/* Format */
psql DB_NAME USERNAME
/* Example */
psql weightwatcher postgres
```