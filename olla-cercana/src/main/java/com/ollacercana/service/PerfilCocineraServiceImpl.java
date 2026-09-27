package com.ollacercana.service;

import com.ollacercana.domain.CodigoOTP;
import com.ollacercana.domain.Cuenta;
import com.ollacercana.domain.PerfilCocinera;
import com.ollacercana.dto.request.PerfilCocineraRequestDTO;
import com.ollacercana.dto.response.PerfilCocineraResponseDTO;
import com.ollacercana.exception.ConflictoException;
import com.ollacercana.exception.ResourceNotFoundException;
import com.ollacercana.mapper.PerfilCocineraMapper;
import com.ollacercana.repository.CodigoOTPRepository;
import com.ollacercana.repository.CuentaRepository;
import com.ollacercana.repository.PerfilCocineraRepository;
import com.ollacercana.validator.IPerfilCocineraValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PerfilCocineraServiceImpl implements IPerfilCocineraService {

    private final PerfilCocineraRepository perfilRepository;
    private final CuentaRepository cuentaRepository;
    private final CodigoOTPRepository codigoOTPRepository;
    private final PerfilCocineraMapper perfilMapper;
    private final IPerfilCocineraValidator validator;

    @Override
    @Transactional
    public PerfilCocineraResponseDTO crearPerfil(PerfilCocineraRequestDTO request) {
        Cuenta cuenta = cuentaRepository.findById(request.getCuentaId())
                .orElseThrow(() -> new ConflictoException("La cuenta asociada con ID " + request.getCuentaId() + " no existe"));

        validator.validarParaCrear(request, cuenta);

        PerfilCocinera perfil = perfilMapper.toDomain(request);
        perfil.setCuenta(cuenta);

        PerfilCocinera guardado = perfilRepository.save(perfil);
        log.info("Perfil de cocinera creado con ID: {}", guardado.getId());

        // Generar código OTP de 6 dígitos para verificación telefónica
        String codigoGenerado = String.format("%06d", new Random().nextInt(999999));
        CodigoOTP otp = CodigoOTP.builder()
                .perfilId(guardado.getId())
                .codigo(codigoGenerado)
                .fechaExpiracion(LocalDateTime.now().plusMinutes(10))
                .usado(false)
                .build();
        codigoOTPRepository.save(otp);

        log.info("=================================================");
        log.info(" CÓDIGO OTP GENERADO PARA PERFIL {}: {}", guardado.getId(), codigoGenerado);
        log.info("=================================================");

        return perfilMapper.toResponseDTO(guardado);
    }

    @Override
    @Transactional
    public PerfilCocineraResponseDTO actualizarPerfil(UUID id, PerfilCocineraRequestDTO request) {
        PerfilCocinera perfilExistente = perfilRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PerfilCocinera", id));

        validator.validarParaActualizar(id, request);

        perfilExistente.setPresentacion(request.getPresentacion());
        perfilExistente.setConjuntoResidencial(request.getConjuntoResidencial());
        perfilExistente.setEspecialidades(request.getEspecialidades());
        perfilExistente.setMediosPago(request.getMediosPago());
        perfilExistente.setNumeroNequi(request.getNumeroNequi());
        perfilExistente.setNumeroDaviplata(request.getNumeroDaviplata());

        PerfilCocinera actualizado = perfilRepository.save(perfilExistente);
        log.info("Perfil de cocinera actualizado con ID: {}", actualizado.getId());

        return perfilMapper.toResponseDTO(actualizado);
    }

    @Override
    @Transactional
    public boolean verificarTelefono(UUID perfilId, String otp) {
        PerfilCocinera perfil = perfilRepository.findById(perfilId)
                .orElseThrow(() -> new ResourceNotFoundException("PerfilCocinera", perfilId));

        CodigoOTP codigoOTP = codigoOTPRepository.findTopByPerfilIdAndUsadoFalseOrderByFechaExpiracionDesc(perfilId)
                .orElseThrow(() -> new ConflictoException("No hay ningún código OTP activo para este perfil. Debe solicitar uno nuevo."));

        if (!codigoOTP.esValido(otp)) {
            throw new ConflictoException("El código OTP ingresado es inválido o ya ha expirado");
        }

        codigoOTP.setUsado(true);
        codigoOTPRepository.save(codigoOTP);

        perfil.setVerificada(true);
        if (perfil.getCuenta() != null && perfil.getCuenta().getCredenciales() != null) {
            perfil.getCuenta().getCredenciales().setCelularVerificado(true);
        }
        perfilRepository.save(perfil);

        log.info("Teléfono verificado correctamente para el perfil {}", perfilId);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public PerfilCocineraResponseDTO obtenerPorCuentaId(Long cuentaId) {
        PerfilCocinera perfil = perfilRepository.findByCuentaId(cuentaId)
                .orElseThrow(() -> new ConflictoException("No existe perfil asociado a la cuenta ID " + cuentaId));
        return perfilMapper.toResponseDTO(perfil);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PerfilCocineraResponseDTO> listarDestacadas() {
        return perfilRepository.findByEsDestacadaTrue().stream()
                .map(perfilMapper::toResponseDTO)
                .toList();
    }
}