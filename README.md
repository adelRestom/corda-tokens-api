### Terminal 1
1. `cd my_token`
2. `./gradlew deployNodes`

### Terminal 2 (PartyA)
1. `cd my_token/build/nodes/PartyA`
2. `java -jar corda.jar`

### Terminal 3 (PartyB)
1. `cd my_token/build/nodes/PartyB`
2. `java -jar corda.jar`

### Terminal 4 (Notary)
1. `cd my_token/build/nodes/Notary`
2. `java -jar corda.jar`

### Terminal 5 (PartyA Webserver)
1. `cd my_token`
2. `./gradlew runPartyAServer`

### Terminal 6 (PartyB Webserver)
1. `cd my_token`
2. `./gradlew runPartyBServer`

### Open your web browser
1. PartyB balance should be zero: `http://localhost:50006/api/my_token/balance`

### Terminal 7
1. Issue 100 tokens to PartyB: `curl --request POST 'http://localhost:50005/api/my_token/issue?amount=100&partyName=O=PartyB,L=New%20York,C=US' --header "Content-Type=application/x-www-form-urlencoded"`

### Open your web browser
1. PartyB balance should be 100: `http://localhost:50006/api/my_token/balance`