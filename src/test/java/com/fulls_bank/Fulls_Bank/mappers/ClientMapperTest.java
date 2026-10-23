package com.fulls_bank.Fulls_Bank.mappers;

import com.fulls_bank.Fulls_Bank.dto.ClientRequestDTO;
import com.fulls_bank.Fulls_Bank.dto.ClientResponseDTO;
import com.fulls_bank.Fulls_Bank.entities.Client;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ClientMapperTest {

    private final ClientMapper mapper = new ClientMapperImpl();

    @Test
    void toResponse_shouldCopyAllFields() {
        ClientResponseDTO dto = mapper.toResponse(new Client(7L, "52998224725", "Ana", "Silva"));

        assertEquals(7L, dto.getId());
        assertEquals("52998224725", dto.getCpf());
        assertEquals("Ana", dto.getName());
        assertEquals("Silva", dto.getLastName());
    }

    @Test
    void toEntity_shouldStoreOnlyTheDigitsOfTheCpf() {
        Client client = mapper.toEntity(new ClientRequestDTO("529.982.247-25", "Ana", "Silva"));

        assertEquals("52998224725", client.getCpf());
    }

    @Test
    void toEntity_shouldCopyFieldsAndLeaveIdEmpty() {
        Client client = mapper.toEntity(new ClientRequestDTO("52998224725", "Ana", "Silva"));

        assertNull(client.getId());
        assertEquals("52998224725", client.getCpf());
        assertEquals("Ana", client.getName());
        assertEquals("Silva", client.getLastName());
    }
}
