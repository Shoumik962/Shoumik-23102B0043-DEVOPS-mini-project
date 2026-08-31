package com.foodwaste.tracker;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ApplicationTest {

    @Test
    void mainRunsWithoutError() {
        assertDoesNotThrow(() -> Application.main(new String[] {}));
    }
}
