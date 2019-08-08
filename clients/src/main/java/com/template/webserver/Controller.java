package com.template.webserver;

import com.r3.corda.lib.tokens.contracts.internal.schemas.PersistentFungibleToken;
import com.r3.corda.lib.tokens.contracts.states.FungibleToken;
import com.template.flows.ExampleFlowWithFixedToken;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.Builder;
import net.corda.core.node.services.vault.CriteriaExpression;
import net.corda.core.node.services.vault.FieldInfo;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.node.services.vault.QueryCriteria.VaultCustomQueryCriteria;
import net.corda.core.node.services.vault.QueryCriteria.VaultQueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static net.corda.core.node.services.vault.QueryCriteriaUtils.getField;

/**
 * Define your API endpoints here.
 */
@RestController
@RequestMapping("/api") // The paths for HTTP requests are relative to this base path.
public class Controller {
    private final CordaRPCOps proxy;
    private final static Logger logger = LoggerFactory.getLogger(Controller.class);
    private final Party ourIdentity;

    // Used when running local Spring Boot webserver
    public Controller(NodeRPCConnection rpc) {
        this.proxy = rpc.proxy;
        this.ourIdentity = proxy.nodeInfo().getLegalIdentities().get(0);
    }

    // Used when running node's embedded webserver in the cloud
    public Controller(CordaRPCOps rpc) {
        this.proxy = rpc;
        this.ourIdentity = proxy.nodeInfo().getLegalIdentities().get(0);
    }

    /**
     * Returns the node's name.
     */
    @GetMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    public HashMap<String, String> whoami() {
        HashMap<String, String> myIdMap = new HashMap<>();
        myIdMap.put("me", ourIdentity.getName().toString());
        return myIdMap;
    }

    /**
     * Initiates a flow to issue MY_TOKEN tokens.
     */
    @PostMapping(value = "/my_token/issue", produces = MediaType.TEXT_PLAIN_VALUE, consumes = MediaType.ALL_VALUE,
                 headers = "Content-Type=application/x-www-form-urlencoded")
    public ResponseEntity<String> issueMyToken(HttpServletRequest request){
        // TO-DO: Change to double instead of Long (Amount only accepts Long; needs research)
        Long amount = Long.parseLong(request.getParameter("amount"));
        if (amount <= 0 ) {
            return ResponseEntity.badRequest().body("Query parameter 'amount' must be non-negative.\n");
        }

        String partyName = request.getParameter("partyName");
        if (partyName == null) {
            return ResponseEntity.badRequest().body("Query parameter 'partyName' must not be null.\n");
        }

        CordaX500Name partyX500Name = CordaX500Name.parse(partyName);
        Party otherParty = Optional.ofNullable(proxy.wellKnownPartyFromX500Name(partyX500Name))
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Party named %s cannot be found.\n", partyName)));

        try {
            SignedTransaction signedTx  = proxy.startTrackedFlowDynamic(
                    ExampleFlowWithFixedToken.class,amount, otherParty).getReturnValue().get();
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(String.format("Transaction id %s committed to ledger.\n", signedTx.getId()));
        }
        catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    /**
     * Displays current node's MY_TOKEN balance.
     */
    @GetMapping(value = "/my_token/balance", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getMyTokenBalance() throws NoSuchFieldException {
        QueryCriteria generalCriteria = new VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);
        // Custom query criteria
        FieldInfo tokenIdentifier = getField("tokenIdentifier", PersistentFungibleToken.class);
        FieldInfo holder = getField("holder", PersistentFungibleToken.class);

        CriteriaExpression tokenIdentifierIndex = Builder.equal(tokenIdentifier, "MY_TOKEN");
        CriteriaExpression holderIndex = Builder.equal(holder, ourIdentity);

        QueryCriteria customCriteria1 = new VaultCustomQueryCriteria(tokenIdentifierIndex);
        QueryCriteria customCriteria2 = new VaultCustomQueryCriteria(holderIndex);

        QueryCriteria criteria = generalCriteria.and(customCriteria1).and(customCriteria2);

        List<StateAndRef<FungibleToken>> myTokens = proxy.vaultQueryByCriteria(criteria, FungibleToken.class)
                .getStates();
        double myBalance = myTokens.stream().map(it -> it.getState().getData().getAmount().getQuantity())
                .mapToDouble(Long::doubleValue).sum();
        return ResponseEntity.status(HttpStatus.OK)
                .body(String.format("Your current MY_TOKEN balance is: %f.\n", myBalance));
    }
}