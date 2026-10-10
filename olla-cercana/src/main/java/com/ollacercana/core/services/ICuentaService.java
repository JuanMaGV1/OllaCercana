package com.ollacercana.core.services;

import com.ollacercana.core.models.Cuenta;

public interface ICuentaService {

       
                                                                                        
      
                                                                      
                                                      
       
    Cuenta registrar(Cuenta cuenta);
    Cuenta autenticar(String identificador, String password);
    void cambiarAvisos(Long cuentaId, boolean activos, Long solicitanteId);
}