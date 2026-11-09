package com.cti.backend_java.controllers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice 
public class ErroHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> tratarErroDto(MethodArgumentNotValidException e) {
        var erro = e.getBindingResult().getFieldErrors().get(0);
        return ResponseEntity.badRequest().body(Map.of(
            "mensagem", "Dados inválidos",
            "campo", erro.getField(),
            "erro", erro.getDefaultMessage()
        ));
    }
}
