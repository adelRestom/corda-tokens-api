package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;
import com.r3.corda.lib.tokens.contracts.states.FungibleToken;
import com.r3.corda.lib.tokens.contracts.types.IssuedTokenType;
import com.r3.corda.lib.tokens.contracts.types.TokenType;
import com.r3.corda.lib.tokens.contracts.utilities.TransactionUtilitiesKt;
import com.r3.corda.lib.tokens.workflows.flows.rpc.IssueTokens;
import net.corda.core.contracts.Amount;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.StartableByRPC;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.utilities.ProgressTracker;

@StartableByRPC
public class ExampleFlowWithFixedToken extends FlowLogic<SignedTransaction> {
    private final ProgressTracker progressTracker = new ProgressTracker();

    private final Long quantity;
    private final Party recipient;

    public ExampleFlowWithFixedToken(Long quantity, Party recipient) {
        this.quantity = quantity;
        this.recipient = recipient;
    }

    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }

    @Override
    @Suspendable
    public SignedTransaction call() throws FlowException {
        // Get the fixed token type
        TokenType token = new TokenType("MY_TOKEN", 6);

        // Assign the issuer who will be issuing the tokens
        IssuedTokenType issuedTokenType = new IssuedTokenType(getOurIdentity(), token);

        // Specify how much quantity to issue to holder
        Amount<IssuedTokenType> issueQuantity = new Amount(quantity, issuedTokenType);

        // Create fungible quantity specifying the new owner
        FungibleToken fungibleToken  = new FungibleToken(issueQuantity, recipient, TransactionUtilitiesKt.getAttachmentIdForGenericParam(token));

        // Use built in flow for issuing tokens on ledger
        return subFlow(new IssueTokens(ImmutableList.of(fungibleToken)));
    }
}
