package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.r3.corda.lib.tokens.workflows.flows.rpc.IssueTokens;
import com.template.tokens.MyToken;
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

    private final Long amount;
    private final Party recipient;

    public ExampleFlowWithFixedToken(Long amount, Party recipient) {
        this.amount = amount;
        this.recipient = recipient;
    }

    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }

    @Override
    @Suspendable
    public SignedTransaction call() throws FlowException {
        MyToken myToken = new MyToken();
        return (SignedTransaction) subFlow(new IssueTokens(new Amount<MyToken>(amount, myToken),
                getOurIdentity(), recipient));
    }
}
