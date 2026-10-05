package com.ollacercana.validator;

import com.ollacercana.domain.Cuenta;
import com.ollacercana.domain.MedioPago;
import com.ollacercana.domain.PerfilCocinera;
import com.ollacercana.domain.Rol;
import com.ollacercana.exception.ConflictoException;
import com.ollacercana.exception.ReglaDeNegocioException;
import com.ollacercana.repository.PerfilCocineraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PerfilCocineraValidator implements IPerfilCocineraValidator {

    private final PerfilCocineraRepository perfilCocineraRepository;

    @Override
    public void validarParaCrear(PerfilCocinera perfil, Cuenta cuenta) {
        validarConjuntoResidencial(perfil.getConjuntoResidencial());
        validarRol(cuenta);
        validarUnicidadPerfil(cuenta.getId());
        validarMediosDePago(perfil);
        validarTelefonosUnicos(perfil, null);
    }

    @Override
    public void validarParaActualizar(UUID perfilId, PerfilCocinera perfil) {
        validarConjuntoResidencial(perfil.getConjuntoResidencial());
        validarMediosDePago(perfil);
        validarTelefonosUnicos(perfil, perfilId);
    }

    private void validarConjuntoResidencial(String conjunto) {
        if (conjunto == null || conjunto.trim().isEmpty()) {
            throw new ConflictoException("El conjunto residencial debe estar seleccionado");
        }
    }

    private void validarRol(Cuenta cuenta) {
        if (cuenta.getRoles() == null || !cuenta.getRoles().contains(Rol.COCINERA)) {
            throw new ReglaDeNegocioException("La cuenta debe poseer el rol COCINERA para crear un perfil");
        }
    }

    private void validarUnicidadPerfil(Long cuentaId) {
        if (perfilCocineraRepository.findByCuentaId(cuentaId).isPresent()) {
            throw new ConflictoException("La cuenta ya cuenta con un perfil de cocinera registrado");
        }
    }

    private void validarMediosDePago(PerfilCocinera perfil) {
        if (perfil.getMediosPago() == null || perfil.getMediosPago().isEmpty()) {
            throw new ReglaDeNegocioException("Debe seleccionar al menos un medio de pago válido");
        }
        if (perfil.getMediosPago().contains(MedioPago.NEQUI) &&
                (perfil.getNumeroNequi() == null || perfil.getNumeroNequi().trim().isEmpty())) {
            throw new ReglaDeNegocioException("Debe especificar el número Nequi si seleccionó dicho medio de pago");
        }
        if (perfil.getMediosPago().contains(MedioPago.DAVIPLATA) &&
                (perfil.getNumeroDaviplata() == null || perfil.getNumeroDaviplata().trim().isEmpty())) {
            throw new ReglaDeNegocioException("Debe especificar el número Daviplata si seleccionó dicho medio de pago");
        }
    }

    private void validarTelefonosUnicos(PerfilCocinera perfil, UUID perfilActualId) {
        if (perfil.getNumeroNequi() != null && !perfil.getNumeroNequi().trim().isEmpty()) {
            boolean existe = (perfilActualId == null)
                    ? perfilCocineraRepository.existsByNumeroNequi(perfil.getNumeroNequi())
                    : perfilCocineraRepository.existsByNumeroNequiAndIdNot(perfil.getNumeroNequi(), perfilActualId);

            if (existe) {
                throw new ConflictoException("El número de Nequi ya se encuentra registrado por otra cocinera");
            }
        }

        if (perfil.getNumeroDaviplata() != null && !perfil.getNumeroDaviplata().trim().isEmpty()) {
            boolean existe = (perfilActualId == null)
                    ? perfilCocineraRepository.existsByNumeroDaviplata(perfil.getNumeroDaviplata())
                    : perfilCocineraRepository.existsByNumeroDaviplataAndIdNot(perfil.getNumeroDaviplata(), perfilActualId);

            if (existe) {
                throw new ConflictoException("El número de Daviplata ya se encuentra registrado por otra cocinera");
            }
        }
    }
}