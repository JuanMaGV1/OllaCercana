package com.ollacercana.service;

import com.ollacercana.domain.Cuenta;

public interface ICuentaService {

       
                                                                                        
      
                                                                      
                                                      
       
    Cuenta registrar(Cuenta cuenta);
    Cuenta autenticar(String identificador, String password);
}