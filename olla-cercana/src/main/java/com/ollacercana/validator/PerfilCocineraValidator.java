package com.ollacercana.validator;

import com.ollacercana.exception.ConflictoException;
import com.ollacercana.exception.ReglaDeNegocioException;
import com.ollacercana.model.domain.Cuenta;
import com.ollacercana.model.domain.MedioPago;
import com.ollacercana.model.domain.Rol;
import com.ollacercana.model.dto.request.PerfilCocineraRequestDTO;
import com.ollacercana.repository.PerfilCocineraRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PerfilCocineraValidator implements IPerfilCocineraValidator {

    private final PerfilCocineraRepository perfilCocineraRepository;

    @Override
    public void validarParaCrear(PerfilCocineraRequestDTO request, Cuenta cuenta) {
        validarConjuntoResidencial(request.getConjuntoResidencial());
        validarRol(cuenta);
        validarUnicidadPerfil(cuenta.getId());
        validarMediosDePago(request);
        validarTelefonosUnicos(request, null);
    }

    @Override
    public void validarParaActualizar(UUID perfilId, PerfilCocineraRequestDTO request) {
        validarConjuntoResidencial(request.getConjuntoResidencial());
        validarMediosDePago(request);
        validarTelefonosUnicos(request, perfilId);
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

    private void validarMediosDePago(PerfilCocineraRequestDTO request) {
        if (request.getMediosPago() == null || request.getMediosPago().isEmpty()) {
            throw new ReglaDeNegocioException("Debe seleccionar al menos un medio de pago válido");
        }

        if (request.getMediosPago().contains(MedioPago.NEQUI) &&
                (request.getNumeroNequi() == null || request.getNumeroNequi().trim().isEmpty())) {
            throw new ReglaDeNegocioException("Debe especificar el número Nequi si seleccionó dicho medio de pago");
        }

        if (request.getMediosPago().contains(MedioPago.DAVIPLATA) &&
                (request.getNumeroDaviplata() == null || request.getNumeroDaviplata().trim().isEmpty())) {
            throw new ReglaDeNegocioException("Debe especificar el número Daviplata si seleccionó dicho medio de pago");
        }
    }

    private void validarTelefonosUnicos(PerfilCocineraRequestDTO request, UUID perfilActualId) {
        if (request.getNumeroNequi() != null && !request.getNumeroNequi().trim().isEmpty()) {
            perfilCocineraRepository.findAll().stream()
                    .filter(p -> perfilActualId == null || !p.getId().equals(perfilActualId))
                    .filter(p -> request.getNumeroNequi().equals(p.getNumeroNequi()))
                    .findAny()
                    .ifPresent(p -> {
                        throw new ConflictoException("El número de Nequi ya se encuentra registrado por otra cocinera");
                    });
        }

        if (request.getNumeroDaviplata() != null && !request.getNumeroDaviplata().trim().isEmpty()) {
            perfilCocineraRepository.findAll().stream()
                    .filter(p -> perfilActualId == null || !p.getId().equals(perfilActualId))
                    .filter(p -> request.getNumeroDaviplata().equals(p.getNumeroDaviplata()))
                    .findAny()
                    .ifPresent(p -> {
                        throw new ConflictoException("El número de Daviplata ya se encuentra registrado por otra cocinera");
                    });
        }
    }
}