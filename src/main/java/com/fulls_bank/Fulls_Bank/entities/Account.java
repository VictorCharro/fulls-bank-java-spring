package com.fulls_bank.Fulls_Bank.entities;

import com.fulls_bank.Fulls_Bank.enums.AccountType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@AllArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(unique = true)
    private int accountNumber;

    @NotNull
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    public AccountType type;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;
}
