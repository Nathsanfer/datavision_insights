package com.cti.backend_java.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.cti.backend_java.services.UploadService;
import com.cti.backend_java.dto.UploadDtos.EnvioValidadasRequest;
import org.springframework.http.HttpStatus;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController 
@RequestMapping ("/api/upload")
public class UploadController {
    private final UploadService service;

    public UploadController(UploadService service) {
        this.service = service;
    }

    @PostMapping("/validadas")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> enviarValidadas(@RequestBody EnvioValidadasRequest pedido) {
        return service.receberValidadas(pedido);
    }
    
}
