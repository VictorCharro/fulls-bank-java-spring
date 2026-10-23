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
        ClientResponseDTO dto = mapper.toResponse(new Client(7L, "12345678901", "Ana", "Silva"));

        assertEquals(7L, dto.getId());
        assertEquals("12345678901", dto.getCpf());
        assertEquals("Ana", dto.getName());
        assertEquals("Silva", dto.getLastName());
    }

    @Test
    void toEntity_shouldCopyFieldsAndLeaveIdEmpty() {
        Client client = mapper.toEntity(new ClientRequestDTO("12345678901", "Ana", "Silva"));

        assertNull(client.getId());
        assertEquals("12345678901", client.getCpf());
        assertEquals("Ana", client.getName());
        assertEquals("Silva", client.getLastName());
    }
}
