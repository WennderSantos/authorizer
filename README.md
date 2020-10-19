# Authorizer
A Clojure program that simulates the authorization flow of a financial transaction.

## Table content
* [How it works](#how-does-it-works)
* [Built with](#built-with)
* [Getting started](#getting-started)
* [Code and design decisions](#code-and-design-decisions)
* [Business decisions](#business-decisions)

## How does it works?
By reading a json data from a file/stdin, the program will execute creating an account an apply validations to transactions.

## Built With
* [Clojure](https://clojure.org/)
* [Leiningen](https://leiningen.org/)
* [Midje](https://github.com/marick/Midje)

## Getting Started

After clone the project, the instructions bellow will get you a copy of the project up and running on your local machine for development and testing purposes.

## Prerequisites

What things you need to install to run this project

* [Clojure](https://clojure.org/guides/getting_started)
* [Leiningen](https://leiningen.org/)

## Restore dependencies
```
lein deps
```

## Run the application

You need to inform the input file path.

```
lein run < PATH_TO_INPUT_FILE
```

There is one input file as a sample on the root of the project, you can use it like this:
```
lein run < operations
```

After execute the above command, you should see the following output:
```json
{"account":{"active-card":true,"available-limit":100},"violations":[]}
{"account":{"active-card":true,"available-limit":80},"violations":[]}
{"account":{"active-card":true,"available-limit":80},"violations":["insufficient-limit"]}
```

## Running the tests

To run all the tests
```
lein midje
```

# Code and design decisions
All the logic that validates a transaction, create an account and apply a transaction is in the ./src/authorizer/logic.clj file.

First thing I did was to write some tests to the `is-account?` and the `is-transaction?` methods to get familiar with clojure sintaxe. After that, all the other things start to flow as I was thinking on how to model the validations.

I decided to divide the code of the transaction validation in the following steps:
1.  The Boolean check where I can get the answer of "is this right?".
2.  Apply a violation if something is not with.

Data that need to be parsed, like when the user inputs data and when the program outputs data, is being done at the "border" of the whole "architecture" of this app. This lets the functions on the `logic` capable to accept calls from other kinds of front-ends.

For simplicity sake, there are some magic numbers in functions dealing with validation of high-frequency-small-interval and doubled-transaction. These numbers are the minutes interval (2) and the maximum transactions allowed in the interval. A better approach here would be get these values from a config file.


# Business decisions
I assumed some business definitions by myself. In a team, these definitions should be decided together with team members.

When validating the high-frequency-small-interval rule I am checking if the state already contains 3 transactions on a two minute interval compared with the current transaction being checked. If the state contains two transactions on two minute interval, the third one will be applied.

When validating the doubled-transaction rule I am checking if the state already contains 2 similar transactions on a two minute interval compared with the current transaction being checked. If state contains one transaction similiar to the one being checked on two minute interval, the second one will be applied.
