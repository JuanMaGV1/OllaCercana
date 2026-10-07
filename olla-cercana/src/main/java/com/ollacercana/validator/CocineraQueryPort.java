package com.ollacercana.validator;

import java.util.UUID;

   
                                                                          
                                                                    
  
                                                                         
                                                                            
                                                                            
                                               
   
public interface CocineraQueryPort {

    boolean estaVerificada(UUID cocineraId);

    boolean estaPausada(UUID cocineraId);
}