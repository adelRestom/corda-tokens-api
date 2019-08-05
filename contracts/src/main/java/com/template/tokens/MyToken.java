package com.template.tokens;

import com.r3.corda.lib.tokens.contracts.types.TokenType;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

public class MyToken implements TokenType {

    private final int fractionDigits = 6;
    private final String tokenIdentifier = "MY_TOKEN";

    @NotNull
    @Override
    public BigDecimal getDisplayTokenSize() {
        return BigDecimal.ONE.scaleByPowerOfTen(-fractionDigits);
    }

    @Override
    public int getFractionDigits() {
        return fractionDigits;
    }

    @NotNull
    @Override
    public Class<?> getTokenClass() {
        return this.getClass();
    }

    @NotNull
    @Override
    public String getTokenIdentifier() {
        return tokenIdentifier;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof MyToken;
    }
}
