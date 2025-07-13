# ♕ BYU CS 240 Chess

This project demonstrates mastery of proper software design, client/server architecture, networking using HTTP and WebSocket, database persistence, unit testing, serialization, and security.

## 10k Architecture Overview

The application implements a multiplayer chess server and a command line chess client.

[![Sequence Diagram](10k-architecture.png)](https://sequencediagram.org/index.html#initialData=C4S2BsFMAIGEAtIGckCh0AcCGAnUBjEbAO2DnBElIEZVs8RCSzYKrgAmO3AorU6AGVIOAG4jUAEyzAsAIyxIYAERnzFkdKgrFIuaKlaUa0ALQA+ISPE4AXNABWAexDFoAcywBbTcLEizS1VZBSVbbVc9HGgnADNYiN19QzZSDkCrfztHFzdPH1Q-Gwzg9TDEqJj4iuSjdmoMopF7LywAaxgvJ3FC6wCLaFLQyHCdSriEseSm6NMBurT7AFcMaWAYOSdcSRTjTka+7NaO6C6emZK1YdHI-Qma6N6ss3nU4Gpl1ZkNrZwdhfeByy9hwyBA7mIT2KAyGGhuSWi9wuc0sAI49nyMG6ElQQA)

[![Sequence Diagram](Minjoong-Diagram-for-CS240.png)](https://sequencediagram.org/index.html#initialData=IYYwLg9gTgBAwgGwJYFMB2YBQAHYUxIhK4YwDKKUAbpTngUSWDABLBoAmCtu+hx7ZhWqEUdPo0EwAIsDDAAgiBAoAzqswc5wAEbBVKGBx2ZM6MFACeq3ETQBzGAAYAdAE5M9qBACu2AMQALADMABwATG4gMP7I9gAWYDoIPoYASij2SKoWckgQaJiIqKQAtAB85JQ0UABcMADaAAoA8mQAKgC6MAD0PgZQADpoAN4ARP2UaMAAtihjtWMwYwA0y7jqAO7QHAtLq8soM8BICHvLAL6YwjUwFazsXJT145NQ03PnB2MbqttQu0WyzWYyOJzOQLGVx0UBQwAA1jAAOreBwwABm0GOYBQHBgACkyC0AHIwaAwGbZVRINE0zFQbH5QpsTjcWCle43Z4wACSCG49mACAUUHsPjmGAAogAPFTYAgFa7VSh3e7Fcz1QJOJzDcZzdTAezzepjSVQbx1GB6PGwgCOqRySyu6A4mBZT3ZlRuonqsKyOUoAAoMv6cVAMva1GAAJRKkQqO6VWTyJQqdT1I1gACqA0Dbw+KFjycUyjUqkTRh0tQAYjS8TnKMWrZYYPnZmIYXDEdIAEIY8G4mCbeKnQzous0xwNqDFzDF1Nl1Ve6o+mTaBfqGVyhWFb0JjnL6jc+elzeylDyplx24H+DIDUwACs2t1y31qkNxuWZot9WLegMGB2n0RF0QHPFAw3ak0X-fRCydMxOEwTsERgachyQMB4lbAYCxgIVYWADgWxQaVsjADQTzTctbz3FB6mnWc6KXKoj0tBQEEI4jgPhdAtwvHdrxVW91QwTUnGCV8xnfT8Fm-c1oHqNs5nwzi4WImB5F4tAEJdOd11PGjORXFR6jQHx+WvUQKyostahAQicWnQNGO0IsDOoitjFqBQOHrAZZxQ7s+zA0c8WHUcYAcuECDRVz5H0lNDJYui-w8st+MvRVmNo5VLVss9tyvLl2TVe8xKfF9RjfMtZJNH9FLXeQAMMYDVFA8CYBcgK3N0pCcqTdL02iuQUAUHwsMgib4mLdyks8+5vN8vFxqwwLCOC-swqHEduCixzJxgVaZu0RKSwW4z43oprzoy88st3EzDFyti0vmu6iuyvKWNEsB6mfHVquk2qjTk00FPy7QWqAkCtu4CDjtmvrXRy+53TZeoQ3IygMlUCysHR4TLpqF4Jlw9slhNYFlmAab2ggbTKeWS4hNKypfvqcIqtecnPhgKnvlprD6cZ-nmahRDXU8bw-H8aB2CNGJqzgSVpDgBQYAAGQgLJCl+isSvqZo2i6XoDHUJldWUlA1l+f4OCuEqK0JuoRmt239D+HZoQ25FUUcelsUHQkSTJWBKQt2k0EDvJFRdlK8vqPkBSFEUxQlMBMsEp2RPKv6YC1QG9RBr9wd-K0iJgO0HTAZG3UeNkDae+oEB1mlA213WIxr2MBpuqCMxQbNc2tubbvULyqxgWtODQnr5GbHCpnbZDfd7OHBwivbx04Q74uAM6oITq63vH1Qs+Kp7j5J-vDIvr62J+vP-u5mqDVB+qIdP6G2o67bIMModWCBhox1yCjAYkEAl6wExD4JCBUjKHioKucyllUZIO5FmaY01oBIAAF64nvo9R+ucSj5y1AARikjJD+8ly5wKFvEXBBDdgwGdPAoaiDWLINMnPRsp1V5dhgE0T29sKTZGxCAeIuIrL7mJpg7BWFmGEPutnb6pCHyUOoSXMGDVLQMJwVAfBuI659wQfZRyY1ppTTWm5Q+yVFpVmWkdaa60hHr1CvDHakURqxUcIjU6CDr6rgQUQ2Rz15GQ3eoVASl8SFlTIS-Iub8Py0LLo1YBrVYaeMHDYk68hQFsMluEliLt6idxpLjfG9dWREwwZaHmy8+YCxBIwkW6Amb7EduohJD4uZFzJk0+YYt9itLpgzDpIzLgwGKdLXwAQvAoHQDEOIiRFnLM7r4LA+tIlG2kJKTWkp2iSm6D0c21ICjDDaRM4hN5KhlPwuM7SphwEAFkqSHXJMcBAgdBzXOefHF6N9k6ZFTqKcU5gwk516RVQu2j36lz0fUa0VcUCRkdEUvSNSPRNxPjAVu9gtkdx1ls7uUZe5X3uOYmAmZjqBn+egMeR9HH1BnitVx2hF4MsKII1C69KAWm8XtLih0AkJSCUCkJnCoWUt2bfaiMr4ns2fpVZJwMEW6K-jIPsoBPI5IRhygpddeWIjFcAGAaAIDMFgRw6JXDUoWosggEpQKFGMOURwRVN4YXkKcFQoGNDEVaoMUooxLDjUSuJquM1LrBp2vqBwFA3AcR0u5UyhxlRvIyCTUPQwMaTXapgAK8kW9DCJuTaKw1B9I1IKlXar11lXVRLPl6upd5EmqvhakoN5d17ltzRvA1tijWYqQi6tGDduQjCuICuV07WZPw7f03U7CpZeHmf4WEeJ-DYBpIiTW5EYAAHF2waB2fUo2R6jmnMFHMK5Tz0ALonbUy03KXm+3eZHRwXyhS-LxG+2dF7eT8lBcKcFGdW1s3bZo1+6ru2avLii6uUZjWAtrbw5AOQT36g7uRbDagyU5ApVdGynDB5gFTQ+tA6aFqZqnmylxw7zU6BbG+gt-KIZCsMCKtEMaa3cLrS21RcSeERKA6E4TD9vXKo7QDLtdU6EZJ1clfVXUzWFNXe+oRZqLVWoxL4W1Z9gm8NQc69B3C3WGOMZ6yTty20cwLn6+TaSkWtkUUwsNJjR1S34w6mNZiyP4rw6ewMNHFwsq1hASuQoEDHvbLOdjfZi2wFLaioih18Ozl883eVH1YlScbXKiTn07NQYc3JgNOjP69uU3qzqgZMu9W86YbLeKD05DJOiOLcwmKyvuZOy07WwD4dUFUhABMBvXyncsW9ag5INBcIt7o3SlXQYqsuoGs3VDzZgCMGb7YeTSDkiCCKTlebGjGN8ZIoB4TTgLHsb4s3iTtkWFCGAy3ZnrtlhwAA7G4JwKAnAxElMEOAKsABs8BLHdcMEUPOuKb7Gw6De9s97hY3N1E99sK27kPBffUblMAADUNL2zPZUiHYkWnUKvN-ViYORJSRfI+WiLHcxsWNybUnEDgowPp0hbZp9PrxJqsDQhxqSG0U11Q5NqNvDfEoHw4GOAlj8OEZjLG3L6YaVD0o+j7SYWJ4RYYzpljjz9ePsS0WzjqWeP+KrfYi66HrrFfy7cwr4npWC+hTJh8FW9twYU+k-KtXFyqbyUjZrBadOWutQZ10rXRNmSdeOoDWD3WeZsyVoXvvYVOcqxq6rjUQ0eesxGzhxnrr+cpXGs+9QFdK9m7NR34W6P1BVzFQwaAUCbBhwl8BHHBWpd8Rl+LgSK+St4a7h6qeLPNqgpBxdfvYNi6LyHmANIBjWvq41kdmmW9G7l9dWbh3Z8PI76NNXahqmAbny8fbcxDtyRx-ZlVG2A8n6O-UffX2AiWCTa3L3rEEgAkGAP-vyBAL3viBADSDDjENdiAPCHDmQgjtyM0FmKbD0LNmjvEO0mgLqNgAgMAP-lAHABAK3FAGsJ-i-p6Hjh6ATlRsTqTo-tIEwYQcQZQGQRQQSIztToiLTj8vTniJTmHOIl+hiKgAgK6LfobMBinHzhClKN7j0rnr6qLlVoppaJLuirXFHmhgJrwgAFYwFoBK7QE0hX46HEaiakbxo64UbWJpoH40Rt7Tx1iMb5IHxW7JZcZpYcCVpMbOGV6nwL7KEkZc5a7nxhHSZrb5z+4pJB6ubry6ph71bqbGrgIx56Y2oJ4T5H7J5oJ9Z35oTuYeqL4aJ57+oB6r6aFKSlGZ7l52rBEeG9bhG14Dz2GN4HbSCG4uGVisruG75eED5JY267TcbqSj49bj5NGT4u5e7Z7mayHT5qKrblYr4aHB5-ih4Tzh5DEaazLgL4a6Zx5wK5GzH5GOqFHhFyo9hETq7lHC6ObqGF61HMFd7ZHx6NFGZzH1BDGa4OpSIoCIEb5dbsEkFcHkgETqQthaSW7gJkDYB6bQlESwkIiPpLGJxHRqSok8R8TRGv4dpaiSQF7wZr71A2DIk4kaRwk6RR59wOoGDMAgA+DmjmDQKaRQJIgsA8jHI9A9iawKBwAADSMAqgrcWAAWdhfgWgOIXR0xBSQREWWY2Asphg-x3hYxkUMpeQaI-xiegmoRixRRyxCxbuOesRSSzmPaSmrYqpo0g6XU+xxq5mDy86MhWJ86PulpMA7+P+MsCyUAxBKyIBayQZLYiAcIsAwA2AhBhAscaAMy56xRDQ+yhyxypyxgT6-W+Od4UZ6uawlqPBocvwMi+hgJ3AeAoWmu1KIAlZUAHECA1ZEqrh0gOaOIqksW+8awZqawjwfep04CeiZIygrJW6vh-afinZRgMxPxlxKxImHuxRC5UmhJy+LxZJbxTYdZUZh0+qdcBpvCVAMBKMRRbpM6suc6NBS+623MmmQAA)


## Modules

The application has three modules.

- **Client**: The command line program used to play a game of chess over the network.
- **Server**: The command line program that listens for network requests from the client and manages users and games.
- **Shared**: Code that is used by both the client and the server. This includes the rules of chess and tracking the state of a game.

## Starter Code

As you create your chess application you will move through specific phases of development. This starts with implementing the moves of chess and finishes with sending game moves over the network between your client and server. You will start each phase by copying course provided [starter-code](starter-code/) for that phase into the source code of the project. Do not copy a phases' starter code before you are ready to begin work on that phase.

## IntelliJ Support

Open the project directory in IntelliJ in order to develop, run, and debug your code using an IDE.

## Maven Support

You can use the following commands to build, test, package, and run your code.

| Command                    | Description                                     |
| -------------------------- | ----------------------------------------------- |
| `mvn compile`              | Builds the code                                 |
| `mvn package`              | Run the tests and build an Uber jar file        |
| `mvn package -DskipTests`  | Build an Uber jar file                          |
| `mvn install`              | Installs the packages into the local repository |
| `mvn test`                 | Run all the tests                               |
| `mvn -pl shared test`      | Run all the shared tests                        |
| `mvn -pl client exec:java` | Build and run the client `Main`                 |
| `mvn -pl server exec:java` | Build and run the server `Main`                 |

These commands are configured by the `pom.xml` (Project Object Model) files. There is a POM file in the root of the project, and one in each of the modules. The root POM defines any global dependencies and references the module POM files.

## Running the program using Java

Once you have compiled your project into an uber jar, you can execute it with the following command.

```sh
java -jar client/target/client-jar-with-dependencies.jar

♕ 240 Chess Client: chess.ChessPiece@7852e922
```
